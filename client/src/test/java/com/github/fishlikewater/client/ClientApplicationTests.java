/*
 * Copyright © 2024 zhangxiang (fishlikewater@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.fishlikewater.client;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

class ClientApplicationTests {

    @Test
    void contextLoads() {
        // 创建 ProcessBuilder 对象，设置要执行的命令
        String scriptPath = "E:\\IdeaProjects2\\proxy\\client\\bin\\virtualAdapter.bat";
        String variableValue = "192.168.12.110";
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("cmd.exe", "/c", scriptPath);
        processBuilder.environment().put("localAddress", variableValue);
        try {
            // 启动外部进程并执行命令
            Process process = processBuilder.start();
            // 读取输出结果
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // 等待进程执行完成
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                // 获取环境变量，检查变量是否已经设置
                Map<String, String> env = processBuilder.environment();
                String varValue = env.get("localAddress");
                System.out.println("localAddress: " + varValue);
            } else {
                System.out.println("Failed to set variable");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }


    }

}
