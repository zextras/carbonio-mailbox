// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store.file;

import java.io.File;
import java.io.IOException;
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
    private Map<Short, String> volumeBlobsProgress = new LinkedHashMap<Short, String>();
    private Map<Short, String> blobDigestsProgress = new LinkedHashMap<Short, String>();
    
    private final static BlobDeduper SINGLETON = new BlobDeduper();

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
        return new Pair<Integer,Long>(0, Long.valueOf(0));
    }

    private Pair<Integer, Long> deDupe(List<BlobReference> blobs) throws ServiceException {
        int linksCreated = 0;
        long sizeSaved = 0;
        long srcInodeNum = 0;
        String srcPath = null;
        // check if there is any processed blob
        for (BlobReference blob : blobs) {
            if (blob.isProcessed()) {
                String path = FileBlobStore.getBlobPath(blob.getMailboxId(),
                        blob.getItemId(), blob.getRevision(),
                        blob.getVolumeId());
                try {
                    IO.FileInfo fileInfo = IO.fileInfo(path);
                    if (fileInfo != null) {
                        srcInodeNum = fileInfo.getInodeNum();
                        srcPath = path;
                        break;
                    }
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        if (srcInodeNum == 0) {
            // check the path with maximum links
            // organize the paths based on inode
            MultiMap inodeMap = new MultiValueMap();
            for (BlobReference blob : blobs) {
                String path = FileBlobStore.getBlobPath(blob.getMailboxId(),
                        blob.getItemId(), blob.getRevision(),
                        blob.getVolumeId());
                try {
                    IO.FileInfo fileInfo = IO.fileInfo(path);
                    if (fileInfo != null) {
                        inodeMap.put(fileInfo.getInodeNum(), path);
                        blob.setFileInfo(fileInfo);
                    }
                } catch (IOException e) {
                    // ignore
                }
            }
            // find inode which has maximum paths
            int maxPaths = 0;
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
        }
        if (srcInodeNum == 0) {
            return new Pair<Integer,Long>(0, Long.valueOf(0));
        }
        // First create a hard link for the source path, so that the file
        // doesn't get deleted in the middle.
        String holdPath = srcPath + "_HOLD";
        File holdFile = new File(holdPath);
        try {
            IO.link(srcPath, holdPath);
            // Now link the other paths to source path
            for (BlobReference blob : blobs) {
                if (blob.isProcessed()) {
                    continue;
                }
                String path = FileBlobStore.getBlobPath(blob.getMailboxId(),
                        blob.getItemId(), blob.getRevision(),
                        blob.getVolumeId());
                try {
                    if (blob.getFileInfo() == null) {
                        blob.setFileInfo(IO.fileInfo(path));
                    }
                } catch (IOException e) {
                    // ignore
                }
                if (blob.getFileInfo() == null) {
                    continue;
                }
                if (srcInodeNum == blob.getFileInfo().getInodeNum()) {
                    markBlobAsProcessed(blob);
                    continue;
                }
                // create the links for paths in two steps.
                // first create a temp link and then rename it to actual path
                // this guarantees that the file is always available.
                String tempPath = path + "_TEMP";
                File tempFile = new File(tempPath);
                try {
                    IO.link(holdPath, tempPath);
                    File destFile = new File(path);
                    tempFile.renameTo(destFile);
                    markBlobAsProcessed(blob);
                    linksCreated++;
                    sizeSaved += blob.getFileInfo().getSize();
                } catch (IOException e) {
                    ZimbraLog.misc.warn("Ignoring the error while deduping " + path, e);
                } finally {
                    if (tempFile.exists()) {
                        tempFile.delete();
                    }
                }
            }
        } catch (IOException e) {
            ZimbraLog.misc.warn("Ignoring the error while creating a link for " + srcPath, e);
        } finally { // delete the hold file
            if (holdFile.exists()) {
                holdFile.delete();
            }
        }
        return new Pair<Integer,Long>(linksCreated, sizeSaved);
    }
        
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
        return new Pair<Integer,Long>(totalLinksCreated, totalSizeSaved);
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
                List<Integer> groupList = new ArrayList<Integer>();
                groupList.addAll(groupIds);
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
