package com.zimbra.cs.service.servlets.fileupload;

import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.util.Zimbra;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TimerTask;

final class MapReaperTask extends TimerTask {

  MapReaperTask() {}

  @Override
  public void run() {
    try {
      ArrayList<Upload> reaped = new ArrayList<>();
      int sizeBefore;
      int sizeAfter;
      synchronized (FileUploadServlet.mPending) {
        sizeBefore = FileUploadServlet.mPending.size();
        long cutoffTime = System.currentTimeMillis() - FileUploadServlet.UPLOAD_TIMEOUT_MSEC;
        for (Iterator<Upload> it = FileUploadServlet.mPending.values().iterator(); it.hasNext(); ) {
          Upload up = it.next();
          if (!up.accessedAfter(cutoffTime)) {
            FileUploadServlet.mLog.debug("Purging cached upload: %s", up);
            it.remove();
            reaped.add(up);
            up.markDeleted();
            assert (FileUploadServlet.mPending.get(up.uuid) == null);
          }
        }
        sizeAfter = FileUploadServlet.mPending.size();
      }

      int removed = sizeBefore - sizeAfter;
      if (removed > 0) {
        FileUploadServlet.mLog.info(
            "Removed %d expired file uploads; %d pending file uploads", removed, sizeAfter);
      } else if (sizeAfter > 0) {
        FileUploadServlet.mLog.info("%d pending file uploads", sizeAfter);
      }

      for (Upload up : reaped) {
        up.purge();
      }
    } catch (Throwable e) { // don't let exceptions kill the timer
      if (e instanceof OutOfMemoryError) {
        Zimbra.halt("Caught out of memory error", e);
      }
      ZimbraLog.system.warn("Caught exception in FileUploadServlet timer", e);
    }
  }
}
