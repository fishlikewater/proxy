package com.github.fishlikewater.server.handle.socks;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.watch.SimpleWatcher;
import cn.hutool.core.io.watch.WatchMonitor;
import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.Map;

/**
 *
 * @since: 2022年08月20日 22:51
 * @author: fishlikewater@126.com
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class FileListenerConfig implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        final File file = FileUtil.file("account.json");
        WatchMonitor.createAll(file, new SimpleWatcher(){
            @Override
            public void onModify(WatchEvent<?> event, Path currentPath) {
                log.warn("账户文件修改");
                try {
                    final Map<String, String> map = JSON.parseObject(new FileInputStream(file),  Map.class);
                    Socks5Contans.setAccountMap(map);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
