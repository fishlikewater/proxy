package com.github.fishlikewater.proxy;

import com.alibaba.fastjson.JSON;
import com.github.fishlikewater.proxy.kit.Request;
import com.github.fishlikewater.proxy.kit.Response;
import io.netty.handler.codec.http.*;
import org.junit.Test;

import java.net.Socket;

/**
 * @author zhangx
 * @version V1.0
 * @mail fishlikewater@126.com
 * @ClassName TestClient
 * @Description
 * @Date 2019年03月06日 9:52
 * @since
 **/
public class TestClient {

    public static void main(String[] args){
        System.out.println(isConnected());
    }


    @Test
    public void test1(){
        FullHttpRequest req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
        HttpHeaders headers = req.headers();
        headers.set("type", "111");
        Request request = new Request();
        request.setMethod(HttpMethod.GET.name());
        //request.setHeader(headers);
        request.setUrl("1111");
        String jsonString = JSON.toJSONString(request);
        System.out.println(jsonString);
        Request request1 = JSON.parseObject(jsonString, Request.class);
        System.out.println(request1.getHeader());
    }

    @Test
    public void test2() {
        Response response = new Response();
        response.setCode(222);
        response.setBody("你好".getBytes());
        String s = JSON.toJSONString(response);
        System.out.println(s);
        Response response1 = JSON.parseObject(s, Response.class);
        System.out.println(new String(response1.getBody()));


    }
    @SuppressWarnings("resource")
    public static boolean isConnected(){
        Socket socket;
        try{
            socket=new Socket("127.0.0.1", 11080);
            socket.sendUrgentData(0xFF);
            return true;
        }catch(Exception e){
            return false;
        }

    }
}

