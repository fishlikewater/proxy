package com.github.fishlikewater.codec;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 *  数据传输协议
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2022年11月18日 17:07
 **/
@Data
@Accessors(chain = true)
@EqualsAndHashCode
public class MessageProtocol {

    /**
     *
     * 每次传输消息的id
     **/
    private long id;

    /**
     *
     * 消息类型
     **/
    private CmdEnum cmd;

    /**
     *
     * 消息类容的协议
     **/
    private ProtocolEnum protocol;

    /**
     *
     * 需要连接的目标机器
     **/
    private Dst dst;

    /**
     *
     * 状态码  判断目标机器连接是否成功
     **/
    private byte state = 0;

    /**
     *
     * 消息类容 不做处理 直接传输
     **/
    private byte[] bytes;



    @Data
    public static class Dst implements Serializable{

        private String dstAddress;

        private int dstPort;
    }


    @Getter
    public enum  CmdEnum implements Serializable {

        //验证
        AUTH(0),
        //客户端注册
        REGISTER(1),
        //请求
        REQUEST(2),
        //响应
        RESPONSE(3),
        //心跳
        HEALTH(4),
        //关闭
        CLOSE(5),
        //请求与目标地址建立连接
        CONNECTION(6),
        //连接建立是否成功应答
        ACK(7),
        //建立数据通道
        DATA_CHANNEL(8),
        //建立数据通道回应
        DATA_CHANNEL_ACK(9);
        private final int code;

        CmdEnum(int code){
            this.code = code;
        }

        public static CmdEnum getInstance(int code){
            for (CmdEnum value : CmdEnum.values()) {
                if (value.code == code){
                    return value;
                }
            }
            return null;
        }
    }


    @Getter
    public enum  ProtocolEnum implements Serializable {
        //http消息  内网穿透
        HTTP(1),
        //类vpn模式代理
        SOCKS(2);
        private final int code;

        ProtocolEnum(int code){
            this.code = code;
        }

        public static ProtocolEnum getInstance(int code){
            for (ProtocolEnum value : ProtocolEnum.values()) {
                if (value.code == code){
                    return value;
                }
            }
            return null;
        }
    }
}
