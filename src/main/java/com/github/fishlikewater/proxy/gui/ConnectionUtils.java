package com.github.fishlikewater.proxy.gui;

import com.github.fishlikewater.proxy.boot.TcpProxyClient;
import com.github.fishlikewater.proxy.conf.ProxyConfig;
import com.github.fishlikewater.proxy.conf.ProxyType;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;

/**
 * @author <p><a>fishlikewater@126.com</a></p>
 * @since 2019年08月31日 9:52
 **/
public class ConnectionUtils {

    private static TcpProxyClient nettyProxyClient;

    private static Properties prop;

    public static Boolean isGui = false;

    @Setter
    @Getter
    private static boolean isRetry = true;

    @Setter
    private static Label state;
    @Setter
    private static Button connection;;
    static {
        try {
            prop = new Properties();
            prop.load(new InputStreamReader(Objects.requireNonNull(ConnectionUtils.class
                    .getClassLoader().getResourceAsStream("application-client.properties")), StandardCharsets.UTF_8));
            isGui = Boolean.valueOf(prop.getProperty("proxy.gui"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void connection(String remote, int remotePort, String local, int localPort, String path){
        if(nettyProxyClient != null){
            stop();
            return;
        }
        connection.setDisable(true);
        connection.setStyle("-fx-background-color:gray;");
        ProxyConfig proxyConfig = new ProxyConfig();
        proxyConfig.setType(new ProxyType[]{ProxyType.proxy_client});
        proxyConfig.setAddress(remote);
        proxyConfig.setPort(remotePort);
        proxyConfig.setLocalAddress(local);
        proxyConfig.setLocalPort(localPort);
        proxyConfig.setProxyPath(path);
        proxyConfig.setToken(prop.getProperty("proxy.token"));
        proxyConfig.setIsOpenCheckMemoryLeak(Boolean.parseBoolean(prop.getProperty("proxy.is-open-check-memory-leak")));
        proxyConfig.setLogging(Boolean.parseBoolean(prop.getProperty("proxy.logging")));
        proxyConfig.setTimeout(Long.parseLong(prop.getProperty("proxy.timeout")));
        nettyProxyClient = new TcpProxyClient(proxyConfig, ProxyType.proxy_client);
        nettyProxyClient.run();
        state.setText("连接成功");
        isRetry = true;
        connection.setDisable(false);
        connection.setStyle("-fx-background-color:#1fad4e");
        connection.setText("关闭");
    }

    public static void setStateText(String text){
        if(isGui){
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    state.setText(text);
                }
            });
        }
    }

    public static void setConnState(boolean isUse){
        if(isGui){
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    if(isUse){
                        connection.setStyle("-fx-background-color:#1fad4e");
                    }else {
                        connection.setStyle("-fx-background-color:red");
                    }
                }
            });
        }
    }


    static void stop(){
        isRetry = false;
        if(nettyProxyClient != null){
            nettyProxyClient.stop();
        }

    }
    public  static void reset(){
        if(isGui){
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    if(state != null){
                        state.setText("连接已断开");
                    }
                    nettyProxyClient = null;
                    connection.setText("开始");
                    connection.setDisable(false);
                    connection.setStyle("-fx-background-color:#0095ff");
                }
            });
        }
    }
}
