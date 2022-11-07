package com.github.fishlikewater.proxyp2p.kit;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @author: fishlikewater@126.com
 * @since: 2022年11月07日 15:20
 **/
@Data
@Accessors(chain = true)
public class MessageData implements Serializable {

    private CmdEnum cmdEnum;

    private String id;

    private int state;

    private ByteBuf byteBuf;

    private Dst dst;

    private String registerName;

    @Data
    public static class Dst implements Serializable{

        private String dstAddress;

        private int dstPort;
    }


    public enum  CmdEnum implements Serializable{
        MAKE_HOLE_INIT(0),//打洞前 发送双方地址
        MAKE_HOLE(1),//打洞
        VALID(2),
        REQUEST(3),
        RESPONSE(4),
        HEALTH(5),
        CLOSE(6),
        CONNECTION(7),
        ACK(8);

        @Getter
        private final int code;

        CmdEnum(int code){
            this.code = code;
        }
    }

}
