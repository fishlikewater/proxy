package com.github.fishlikewater.proxy;

import javafx.application.ConditionalFeature;
import javafx.application.Platform;

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
        Platform.isSupported(ConditionalFeature.INPUT_METHOD);
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

