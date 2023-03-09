package com.github.fishlikewater.codec;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;

/**
 * <p>
 *  专为http消息穿透设计
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年02月07日 10:52
 **/
@Data
@Accessors(chain = true)
@EqualsAndHashCode
public class HttpProtocol implements Serializable{

    /**
     *
     * 每次传输消息的id
     **/
    private long id;

    /**
     *
     * 消息类型
     **/
    private HttpProtocol.CmdEnum cmd;


    /**
     *
     * 目标服务名
     */
    private String dstServer;

    /**
     *
     * 注册服务名[多服务以,分割]
     */
    private String registerName;

    /**
     *
     * url
     */
    private String url;

    /**
     *
     * 请求头
     */
    private Map<String, String> heads;

    /**
     *
     * 请求方法
     */
    private String method;


    /**
     *
     * http版本
     */
    private String version;


    /**
     *
     * 响应码
     */
    private int code;


    /**
     *
     * 消息类容
     **/
    private byte[] bytes;


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
        CLOSE(5);
        @Getter
        private final int code;

        CmdEnum(int code){
            this.code = code;
        }

        public static HttpProtocol.CmdEnum getInstance(int code){
            for (HttpProtocol.CmdEnum value : HttpProtocol.CmdEnum.values()) {
                if (value.code == code){
                    return value;
                }
            }
            return null;
        }
    }


}
