package com.github.fishlikewater.cutpackets;

import com.github.fishlikewater.cutpackets.boot.CutPacketsBoot;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * <p>
 *
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年07月15日 5:57
 **/

@SpringBootApplication
public class CutPacketsApplication implements CommandLineRunner, DisposableBean {

    private CutPacketsBoot cutPacketsBoot;

    public static void main(String[] args) {
        SpringApplication.run(CutPacketsApplication.class, args);
    }

    @Override
    public void destroy() throws Exception {
        if (cutPacketsBoot != null){
            cutPacketsBoot.stop();
        }
    }

    @Override
    public void run(String... args) throws Exception {
        cutPacketsBoot = new CutPacketsBoot();
        cutPacketsBoot.start();
    }
}
