// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;

import com.zimbra.common.localconfig.DebugConfig;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.Pair;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.db.DbMailItem;
import com.zimbra.cs.db.DbMailbox;
import com.zimbra.cs.db.DbPool;
import com.zimbra.cs.db.DbPool.DbConnection;
import com.zimbra.cs.db.DbVolumeBlobs;
import com.zimbra.cs.mailbox.MailServiceException;
import com.zimbra.cs.store.MailboxBlob.MailboxBlobInfo;
import com.zimbra.cs.util.SpoolingCache;
import com.zimbra.cs.volume.Volume;
import com.zimbra.cs.volume.Volume.VolumeMetadata;
import com.zimbra.cs.volume.VolumeManager;
import com.zimbra.znative.IO;

public class BlobDeduper {
    
    private boolean inProgress = false;
    private boolean stopProcessing = false;
    private int totalLinksCreated = 0;
    private long totalSizeSaved = 0;
    private Map<Short, String> volumeBlobsProgress = new LinkedHashMap<>();
    private Map<Short, String> blobDigestsProgress = new LinkedHashMap<>();
    
    private static final BlobDeduper SINGLETON = new BlobDeduper();

    private BlobDeduper() {
    }
    
    public static BlobDeduper getInstance() {
        return SINGLETON;
    }

    private Pair<Integer, Long> processDigest(String digest, Volume volume) throws ServiceException {
        // get the blobs
        DbConnection conn = null;
        List<BlobReference> blobs;
        try {
            conn = DbPool.getConnection();
            blobs = DbVolumeBlobs.getBlobReferences(conn, digest, volume);
        } finally {
            DbPool.quietClose(conn);
        }
        // dedupe the paths
        if (blobs.size() > 1) {
            ZimbraLog.misc.debug("Deduping " + blobs.size() + " files for digest " + digest + " volume " + volume.getId());
            return deDupe(blobs);
        } else if (blobs.size() == 1) { 
            // mark the blob as processed if there is only one blob for given digest.
            markBlobAsProcessed(blobs.get(0));
        }
        return new Pair<>(0, 0L);
    }

    private Pair<Integer, Long> deDupe(List<BlobReference> blobs) throws ServiceException {
        String[] paths = buildPaths(blobs);
        SourceBlob src = findProcessedSource(blobs, paths);
        if (src == null) {
            src = findMaxLinkedSource(blobs, paths);
        }
        if (src == null) {
            return new Pair<>(0, 0L);
        }
        return hardlinkOthers(blobs, paths, src);
    }

    private String[] buildPaths(List<BlobReference> blobs) throws ServiceException {
        String[] paths = new String[blobs.size()];
        for (int i = 0; i < blobs.size(); i++) {
            BlobReference b = blobs.get(i);
            paths[i] = FileBlobStore.getBlobPath(
                    b.getMailboxId(), b.getItemId(), b.getRevision(), b.getVolumeId());
        }
        return paths;
    }

    private SourceBlob findProcessedSource(List<BlobReference> blobs, String[] paths) {
        for (int i = 0; i < blobs.size(); i++) {
            BlobReference blob = blobs.get(i);
            if (blob.isProcessed()) {
                IO.FileInfo fi = tryFileInfo(paths[i]);
                if (fi != null) {
                    blob.setFileInfo(fi);
                    return new SourceBlob(fi.getInodeNum(), paths[i]);
                }
            }
        }
        return null;
    }

    private SourceBlob findMaxLinkedSource(List<BlobReference> blobs, String[] paths) {
        MultiMap inodeMap = new MultiValueMap();
        for (int i = 0; i < blobs.size(); i++) {
            IO.FileInfo fi = tryFileInfo(paths[i]);
            if (fi != null) {
                inodeMap.put(fi.getInodeNum(), paths[i]);
                blobs.get(i).setFileInfo(fi);
            }
        }
        int maxPaths = 0;
        long srcInodeNum = 0;
        String srcPath = null;
        @SuppressWarnings("unchecked")
        Iterator<Map.Entry<Long, Collection<String>>> iter = inodeMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Long, Collection<String>> entry = iter.next();
            if (entry.getValue().size() > maxPaths) {
                maxPaths = entry.getValue().size();
                srcInodeNum = entry.getKey();
                srcPath = entry.getValue().iterator().next();
            }
        }
        return srcInodeNum == 0 ? null : new SourceBlob(srcInodeNum, srcPath);
    }

    private static IO.FileInfo tryFileInfo(String path) {
        try {
            return IO.fileInfo(path);
        } catch (IOException e) {
            return null;
        }
    }

    private Pair<Integer, Long> hardlinkOthers(
            List<BlobReference> blobs, String[] paths, SourceBlob src) throws ServiceException {
        // [linksCreated, sizeSaved] — single allocation per deDupe() call,
        // avoids per-blob Pair allocation in the hot link loop.
        long[] acc = {0, 0};
        String holdPath = src.path() + "_HOLD";
        File holdFile = new File(holdPath);
        try {
            IO.link(src.path(), holdPath);
            for (int i = 0; i < blobs.size(); i++) {
                tryLinkOne(blobs.get(i), paths[i], holdPath, src.inodeNum(), acc);
            }
        } catch (IOException e) {
            ZimbraLog.misc.warn("Ignoring the error while creating a link for " + src.path(), e);
        } finally {
            deleteQuietly(holdFile);
        }
        return new Pair<>((int) acc[0], acc[1]);
    }

    private static void deleteQuietly(File file) {
        try {
            Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            ZimbraLog.misc.warn("Failed to delete " + file, e);
        }
    }

    private void tryLinkOne(
            BlobReference blob, String path, String holdPath, long srcInodeNum, long[] acc)
            throws ServiceException {
        if (blob.isProcessed()) {
            return;
        }
        if (blob.getFileInfo() == null) {
            IO.FileInfo fi = tryFileInfo(path);
            if (fi != null) {
                blob.setFileInfo(fi);
            }
        }
        if (blob.getFileInfo() == null) {
            return;
        }
        if (srcInodeNum == blob.getFileInfo().getInodeNum()) {
            markBlobAsProcessed(blob);
            return;
        }
        hardlinkOne(blob, path, holdPath, acc);
    }

    // Create the link in two steps: first a temp link, then rename to the actual path.
    // This guarantees the file is always available.
    private void hardlinkOne(BlobReference blob, String path, String holdPath, long[] acc)
            throws ServiceException {
        String tempPath = path + "_TEMP";
        File tempFile = new File(tempPath);
        try {
            IO.link(holdPath, tempPath);
            // Prefer the raw File#renameTo syscall over Files.move: on this hot path
            // it avoids ~1.5us of FileSystemProvider dispatch per blob (+73% in our
            // bench). Semantics match rename(2) on Linux: atomic, replaces target.
            if (!tempFile.renameTo(new File(path))) {
                throw new IOException("rename(" + tempPath + " -> " + path + ") failed");
            }
            markBlobAsProcessed(blob);
            acc[0]++;
            acc[1] += blob.getFileInfo().getSize();
        } catch (IOException e) {
            ZimbraLog.misc.warn("Ignoring the error while deduping " + path, e);
        } finally {
            deleteQuietly(tempFile);
        }
    }

    private record SourceBlob(long inodeNum, String path) {}
        
    private void markBlobAsProcessed(BlobReference blob) throws ServiceException {
        DbConnection conn = null;
        try {
            conn = DbPool.getConnection();
            DbVolumeBlobs.updateProcessed(conn, blob.getId(), true);
            conn.commit();
        } finally {
            DbPool.quietClose(conn);
        }
    }
    
    public synchronized void stopProcessing() {
        if (inProgress) {
            ZimbraLog.misc.info("Setting stopProcessing flag.");
            stopProcessing = true;
        }
    }
    
    private synchronized boolean isStopProcessing() {
        return stopProcessing;
    }
    
    public synchronized boolean isRunning() {
        return inProgress;
    }
    
    private synchronized void resetProgress() {
        inProgress = false;
        stopProcessing = false;
    }
    
    private synchronized void incrementCountAndSize(int count, long size) {
        totalLinksCreated += count;
        totalSizeSaved += size;
    }

    public synchronized Pair<Integer, Long> getCountAndSize() {
        return new Pair<>(totalLinksCreated, totalSizeSaved);
    }
    
    public synchronized Map<Short, String> getVolumeBlobsProgress() {
        return volumeBlobsProgress;
    }
    
    public synchronized void setVolumeBlobsProgress(short volumeId, String str) {
        volumeBlobsProgress.put(volumeId, str);
    }

    public synchronized Map<Short, String> getBlobDigestsProgress() {
        return blobDigestsProgress;
    }
    
    public synchronized void setBlobDigestsProgress(short volumeId, String str) {
        blobDigestsProgress.put(volumeId, str);
    }

    public void resetVolumeBlobs(List<Short> volumeIds) throws ServiceException {
        synchronized (this) {
            if (inProgress) {
                throw MailServiceException.TRY_AGAIN("Dedupe is in progress. Stop the dedupe and then run reset again.");
            }
            inProgress = true;
        }
        DbConnection conn = null;
        try {
            conn = DbPool.getConnection();
            if (volumeIds.isEmpty()) {
                // truncate the volume_blobs
                DbVolumeBlobs.deleteAllBlobRef(conn);
                // update the volume metadata
                for (Volume vol : VolumeManager.getInstance().getAllVolumes()) {
                    switch (vol.getType()) {
                        case Volume.TYPE_MESSAGE:
                        case Volume.TYPE_MESSAGE_SECONDARY:
                            // reset volume metadata.
                            updateMetadata(vol.getId(), new VolumeMetadata(0, 0, 0));
                            break;
                    }
                }
            } else {
                for (short volumeId : volumeIds) {
                    Volume vol = VolumeManager.getInstance().getVolume(volumeId);
                    // remove the entries from volume_blobs and reset volume metadata.
                    DbVolumeBlobs.deleteBlobRef(conn, vol);
                    // reset volume metadata.
                    updateMetadata(vol.getId(), new VolumeMetadata(0, 0, 0));
                }
            }
            conn.commit();
        } finally {
            DbPool.quietClose(conn);
            resetProgress();
        } 
    }
    
    private Volume updateMetadata(short volumeId, VolumeMetadata metadata) throws ServiceException {
        VolumeManager mgr = VolumeManager.getInstance();
        Volume.Builder builder = Volume.builder(mgr.getVolume(volumeId));
        builder.setMetadata(metadata);
        return mgr.update(builder.build());
    }

    public void process(List<Short> volumeIds) throws ServiceException, IOException {
        synchronized (this) {
            if (inProgress) {
                throw MailServiceException.TRY_AGAIN("Dedupe is already in progress. Only one request can be run at a time.");
            }
            inProgress = true;
            totalLinksCreated = 0;
            totalSizeSaved = 0;
            volumeBlobsProgress.clear();
            blobDigestsProgress.clear();
        }
        Thread thread = new BlobDeduperThread(volumeIds);
        thread.setName("BlobDeduper");
        thread.start();
    }
    
    private class BlobDeduperThread extends Thread {
        List<Short> volumeIds;

        public BlobDeduperThread(List<Short> volumeIds) {
            this.volumeIds = volumeIds;
        }
        
        private void populateVolumeBlobs(short volumeId, int groupId, int lastSyncDate, int currentSyncDate) throws ServiceException {
            DbConnection conn = null;
            Iterable<MailboxBlobInfo> allBlobs = null;
            try {      
                conn = DbPool.getConnection();
                allBlobs = DbMailItem.getAllBlobs(conn, groupId, volumeId, lastSyncDate, currentSyncDate);
                for (MailboxBlobInfo info : allBlobs) {
                    try {
                        DbVolumeBlobs.addBlobReference(conn, info);
                    } catch (MailServiceException se) {
                        // ignore if the row already exists.
                        if (!MailServiceException.ALREADY_EXISTS.equals(se.getCode())) {
                            throw se;
                        }
                    }
                }
                conn.commit();
            } finally {
                DbPool.quietClose(conn);
            }
        }
        
        private List<Integer> getSortedGroupIds() throws ServiceException {
            DbConnection conn = null;
            try {
                conn = DbPool.getConnection();
                Set<Integer> groupIds = DbMailbox.getMboxGroupIds(conn);
                List<Integer> groupList = new ArrayList<>(groupIds);
                Collections.sort(groupList);
                return groupList;
            } finally {
                DbPool.quietClose(conn);
            }
        }
        
        private void populateVolumeBlobs(Volume vol) throws ServiceException {
            VolumeMetadata metadata = vol.getMetadata();
            boolean resumed = false;
            if (metadata.getCurrentSyncDate() == 0) {
                // this is not a resume. update the current sync date.
                metadata.setCurrentSyncDate((int) (System.currentTimeMillis() / 1000));
            } else { // this is resumed request.
                resumed = true;
            }
            if (DebugConfig.disableMailboxGroups) {
                populateVolumeBlobs(vol.getId(), -1, metadata.getLastSyncDate(), metadata.getCurrentSyncDate());
                setVolumeBlobsProgress(vol.getId(), "1/1");
            } else {
                List<Integer> groupIds = getSortedGroupIds();
                for (int i = 0; i < groupIds.size(); i++) {
                    if (resumed && groupIds.get(i) <= metadata.getGroupId()) {
                        continue;
                    }
                    populateVolumeBlobs(vol.getId(), groupIds.get(i), metadata.getLastSyncDate(), metadata.getCurrentSyncDate());
                    metadata.setGroupId(groupIds.get(i));
                    vol = updateMetadata(vol.getId(), metadata);
                    setVolumeBlobsProgress(vol.getId(), i+1 + "/" + groupIds.size());
                    if (isStopProcessing()) {
                        ZimbraLog.misc.info("Recieved the stop signal. Stopping the deduplication process.");
                        throw ServiceException.INTERRUPTED("received stop signal");
                    }
                }
            }
            // reset group-id and update currentSync and lastSync.
            metadata.setLastSyncDate(metadata.getCurrentSyncDate());
            metadata.setCurrentSyncDate(0);
            metadata.setGroupId(0);
            vol = updateMetadata(vol.getId(), metadata);
            // if this is a resumed sync, run another sync to catch up to current date.
            if (resumed) {
                populateVolumeBlobs(vol);
            }
        }
        
        public void run() {   
            for (short volumeId : volumeIds) {
                try {
                    ZimbraLog.misc.info("Running deduper for volume %d", volumeId);
                    Volume vol = VolumeManager.getInstance().getVolume(volumeId);
                    // populate the volume_blox table first;
                    populateVolumeBlobs(vol);
                    SpoolingCache<String> digests;
                    DbConnection conn = null;
                    try {
                        conn = DbPool.getConnection();
                        digests = DbVolumeBlobs.getUniqueDigests(conn, vol);
                    } finally {
                        DbPool.quietClose(conn);
                    }
                    int count = 0;
                    setBlobDigestsProgress(volumeId, count + "/" + digests.size());
                    for (String digest : digests) {
                        Pair<Integer, Long> pair = processDigest(digest, vol);
                        incrementCountAndSize(pair.getFirst(), pair.getSecond());
                        count++;
                        setBlobDigestsProgress(volumeId, count + "/" + digests.size());
                        if (isStopProcessing()) {
                            ZimbraLog.misc.info("Recieved the stop signal. Stopping the deduplication process.");
                            break;
                        }
                    }
                } catch (Throwable t) {
                    ZimbraLog.misc.error("error while performing deduplication", t);
                } finally {
                    resetProgress();
                }
            }
            ZimbraLog.misc.info("Deduping done. Total of " + totalLinksCreated
                    + " links created and saved approximately " + totalSizeSaved + " bytes.");
        }
    }
}
