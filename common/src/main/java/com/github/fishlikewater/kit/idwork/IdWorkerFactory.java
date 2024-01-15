package com.github.fishlikewater.kit.idwork;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author <p><a>fishlikewater@126.com</a></p>
 * @date 2019年07月01日 12:11
 * @since 1.0.0
 **/
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IdWorkerFactory {

    public static IdWorker create(int... indexes) {
        return new StardardIdWorker(indexes);
    }
}
