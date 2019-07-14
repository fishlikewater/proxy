package com.github.fishlikewater.proxy.kit.idwork;

/**
 * @author <p><a>fishlikewater@126.com</a></p>
 * @date 2019年07月01日 12:11
 * @since
 **/
public interface IdWorker {

    int MIN_HANDLER_ID = IdWorkerHandler.MIN_WORKER_INDEX;

    int MAX_HANDLER_ID = IdWorkerHandler.MAX_WORKER_INDEX;

    long nextId();
}
