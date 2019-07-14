package com.github.fishlikewater.proxy.kit.idwork;

/**
 * @author <p><a>fishlikewater@126.com</a></p>
 * @date 2019年07月01日 12:11
 * @since
 **/
public class IdWorkerFactory {

    public static IdWorker create(int ... indexes) {
        return new StardardIdWorker(indexes);
    }
}
