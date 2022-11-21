package com.github.fishlikewater.codec;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 *  数据传输协议
 *
 * </p>
 *
 * @author: fishlikewater@126.com
 * @since: 2022年11月18日 17:07
 **/
@Data
@Accessors(chain = true)
public class MessageProtocol {

    private long id; //每次传输消息的id

    private CmdEnum cmd; //消息类型

    private ProtocolEnum protocol; //消息类容的协议

    private Dst dst; //需要连接的目标机器

    private byte state = 0; //状态码  判断目标机器连接是否成功

    private byte[] bytes; //消息类容 不做处理 直接传输



    @Data
    public static class Dst implements Serializable{

        private String dstAddress;

        private int dstPort;
    }


    public enum  CmdEnum implements Serializable {
        AUTH(0),//验证
        REGISTER(1), //客户端注册
        REQUEST(2), //请求
        RESPONSE(3),//响应
        HEALTH(4), //心跳
        CLOSE(5), //关闭
        CONNECTION(6),//请求与目标地址建立连接
        ACK(7), //连接建立是否成功应答
        DATA_CHANNEL(8);//建立数据通道
        @Getter
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


    public enum  ProtocolEnum implements Serializable {
        HTTP(1), //http消息  内网穿透
        SOCKS(2); //类vpn模式代理
        @Getter
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
