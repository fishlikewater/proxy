package com.github.fishlikewater.proxy.gui;/**
 * @author <p><a>fishlikewater@126.com</a></p>
 * @date 2019年08月30日 23:47
 * @since
 **/

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.PopupWindow;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Proxy implements Initializable {

    @Setter
    private Application application;

    @FXML
    private TextField remote;

    @FXML
    private TextField remotePort;

    @FXML
    private TextField local;

    @FXML
    private TextField localPort;

    @FXML
    private Button connection;

    @FXML
    private Label state;

    @FXML
    private TextField path;


    @FXML
    public void vaildPort(KeyEvent keyEvent) {
        Pattern pattern = Pattern.compile("^[1-9](\\d?)+$");
        String port = remotePort.getText();
        Matcher matcher = pattern.matcher(port);
        if(!matcher.find()){
            int length = port.length();
            if(length>0){
                remotePort.deleteText(new IndexRange(length-1, length));
            }
        }
    }

    @FXML
    public void vaildLocalPort(KeyEvent keyEvent) {
        Pattern pattern = Pattern.compile("^[1-9](\\d?)+$");
        String port = localPort.getText();
        Matcher matcher = pattern.matcher(port);
        if(!matcher.find()){
            int length = port.length();
            if(length>0){
                localPort.deleteText(new IndexRange(length-1, length));
            }
        }
    }

    @FXML
    public void connection(MouseEvent mouseEvent) {
        if(vaild()){
            state.setTextFill(Color.valueOf("#628cda"));
            ConnectionUtils.setState(state);
            ConnectionUtils.setConnection(connection);
            ConnectionUtils.connection(remote.getText(), Integer.valueOf(remotePort.getText()), local.getText(), Integer.valueOf(localPort.getText()), path.getText());
        }else {
            state.setTextFill(Color.valueOf("red"));
        }
    }

    private boolean vaild(){
        boolean flag =true;
        if(StringUtils.isEmpty(remote.getText())){
            state.setText("远程地址不能为空");
            flag = false;
            return flag;
        }
        if(StringUtils.isEmpty(remotePort.getText())){
            state.setText("远程端口不能为空");
            flag = false;
            return flag;
        }
        if(StringUtils.isEmpty(local.getText())){
            state.setText("本地地址不能为空");
            flag = false;
            return flag;
        }
        if(StringUtils.isEmpty(localPort.getText())){
            state.setText("本地端口不能为空");
            flag = false;
            return flag;
        }
        if(StringUtils.isEmpty(path.getText())){
            state.setText("注册路劲不能为空");
            flag = false;
            path.setFocusTraversable(true);
            return flag;
        }
        return flag;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        remote.setText("www.fishlikewater.com");
        remotePort.setText("11080");
        local.setText("127.0.0.1");
        localPort.setText("9000");
        Tooltip tooltip = new Tooltip("注册路劲");
        tooltip.setAnchorLocation(PopupWindow.AnchorLocation.CONTENT_BOTTOM_LEFT);
        path.setTooltip(tooltip);
        connection.setDefaultButton(true);
       /* remote.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

            }
        });*/
    }
}
