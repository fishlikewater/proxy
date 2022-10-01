package com.github.fishlikewater.proxy.gui;
/**
 * @author <p><a>fishlikewater@126.com</a></p>
 * @date 2019年08月31日 9:16
 * @since
 **/

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

import java.io.InputStream;

public class ProxyGui extends Application {

    private Stage stage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage)  throws Exception {
        stage = primaryStage;
        //primaryStage.setIconified(true);//是否最小化到任务栏
        primaryStage.setTitle("内网穿透客户端");
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                //此处当stage关闭时，同时结束程序，避免stage关闭后，程序界面关闭了，但后台线程却依然运行的问题
                System.exit(0);
            }
        });
        getMainController("Proxy.fxml");
        //primaryStage.setMaximized(true);//是否最大化显示
        primaryStage.setAlwaysOnTop(true);
        //primaryStage.sizeToScene();
        primaryStage.setResizable(false);//禁止缩放
        primaryStage.show();

    }

    private void getMainController(String fxml){
        try {
            Proxy proxy = (Proxy) replaceSceneContent("Proxy.fxml");
            proxy.setApplication(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private Initializable replaceSceneContent(String fxml) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        InputStream in = ProxyGui.class.getResourceAsStream(fxml);
        loader.setBuilderFactory(new JavaFXBuilderFactory());
        //loader.setLocation(ProxyGui.class.getResource(fxml));
        Parent root;
        try {
            root = loader.load(in);
        } finally {
            in.close();
        }
        JMetro jMetro = new JMetro(root, Style.LIGHT);
        Scene scene = new Scene(root, 650, 500);
        stage.setScene(scene);
        return (Initializable) loader.getController();
    }

    @Override
    public void stop() throws Exception {
        ConnectionUtils.stop();
    }
}
