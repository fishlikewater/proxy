package com.github.fishlikewater.kit;


import com.github.fishlikewater.kit.idwork.IdWorker;
import com.github.fishlikewater.kit.idwork.IdWorkerFactory;

/**
 * @author <p><a>fishlikewater@126.com</a></p>
 * @since 2019年07月01日 13:23
 **/
public class IdUtil {

    private static final IdWorker idWorker = IdWorkerFactory.create(1);


    public static String next() {

        return String.valueOf(idWorker.nextId());
    }

    public static long id() {

        return idWorker.nextId();
    }

}
