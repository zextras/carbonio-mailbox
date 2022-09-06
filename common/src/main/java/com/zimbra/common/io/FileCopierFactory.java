// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.io;

import com.zimbra.common.io.FileCopierOptions.IOType;
import com.zimbra.common.io.FileCopierOptions.Method;

public class FileCopierFactory {

  public static FileCopier createCopier(FileCopierOptions opts) {
    return createCopier(
        opts.getMethod(),
        opts.getIOType(),
        opts.getOIOCopyBufferSize(),
        opts.getAsyncQueueCapacity(),
        opts.getNumParallelWorkers(),
        opts.getNumPipes(),
        opts.getNumReadersPerPipe(),
        opts.getNumWritersPerPipe(),
        opts.getPipeBufferSize());
  }

  public static FileCopier createCopier(
      Method method,
      IOType ioType,
      int oioCopyBufSize,
      int queueSize,
      int parallelWorkers,
      int numPipes,
      int readConcurrency,
      int writeConcurrency,
      int pipeBufSize) {
    FileCopier copier;
    switch (method) {
      case PARALLEL:
        copier =
            new AsyncFileCopier(
                ioType.equals(IOType.NIO), oioCopyBufSize, queueSize, parallelWorkers);
        break;
      case PIPE:
        copier =
            new AsyncPipedFileCopier(
                ioType.equals(IOType.NIO),
                oioCopyBufSize,
                queueSize,
                numPipes,
                readConcurrency,
                writeConcurrency,
                pipeBufSize);
        break;
      case SERIAL:
        copier = new SerialFileCopier(ioType.equals(IOType.NIO), oioCopyBufSize);
        break;
      default:
        throw new IllegalArgumentException("Invalid method " + method);
    }
    return copier;
  }
}
