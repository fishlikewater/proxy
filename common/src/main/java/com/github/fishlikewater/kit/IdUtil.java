package com.github.fishlikewater.kit;


import com.github.fishlikewater.kit.idwork.IdWorker;
import com.github.fishlikewater.kit.idwork.IdWorkerFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author <p><a>fishlikewater@126.com</a></p>
 * @since 2019年07月01日 13:23
 **/
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IdUtil {

    private static final IdWorker idWorker = IdWorkerFactory.create(1);


    public static String next() {

        return String.valueOf(idWorker.nextId());
    }

    public static long id() {

        return idWorker.nextId();
    }

}
