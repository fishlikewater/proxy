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
package com.github.fishlikewater.kit;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * <p>
 * kryo 序列化工具类
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年02月16日 12:02
 **/
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class KryoUtil {

    private static final ThreadLocal<Kryo> KRYO_LOCAL = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.setReferences(true);
        kryo.setRegistrationRequired(false);
        return kryo;
    });

    public static void remove() {
        KRYO_LOCAL.remove();
    }

    /**
     * 获取kryo对象
     *
     * @return com.esotericsoftware.kryo.Kryo
     * @author fishlikewater@126.com
     * @since 2023/2/16 16:02
     */
    public static Kryo getInstance() {
        return KRYO_LOCAL.get();
    }

    /**
     * 序列化对象 包含类信息
     *
     * @param obj 序列化对象
     * @return byte[]
     * @author fishlikewater@126.com
     * @since 2023/2/16 16:01
     */
    public static <T> byte[] writeToByteArray(T obj) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Output output = new Output(byteArrayOutputStream);

        Kryo kryo = getInstance();
        kryo.writeClassAndObject(output, obj);
        output.flush();

        return byteArrayOutputStream.toByteArray();
    }

    /**
     * 对象序列化为字符串
     *
     * @param obj 序列化对象
     * @return java.lang.String
     * @author fishlikewater@126.com
     * @since 2023/2/16 16:05
     */
    public static <T> String writeToString(T obj) {
        return new String(Base64.getEncoder().encode(writeToByteArray(obj)), StandardCharsets.UTF_8);
    }

    /**
     * 将字节数组反序列化为原对象
     *
     * @param byteArray writeToByteArray 方法序列化后的字节数组
     * @return 原对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T readFromByteArray(byte[] byteArray) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
        Input input = new Input(byteArrayInputStream);

        Kryo kryo = getInstance();
        return (T) kryo.readClassAndObject(input);
    }

    /**
     * 将 String 反序列化为原对象
     * 利用了 Base64 编码
     *
     * @param str writeToString 方法序列化后的字符串
     * @param <T> 原对象的类型
     * @return 原对象
     */
    public static <T> T readFromString(String str) {
        return readFromByteArray(Base64.getDecoder().decode(str.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * 将对象序列化为字节数组 不包含类型的信息
     *
     * @param obj 任意对象
     * @param <T> 对象的类型
     * @return 序列化后的字节数组
     */
    public static <T> byte[] writeObjectToByteArray(T obj) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Output output = new Output(byteArrayOutputStream);

        Kryo kryo = getInstance();
        kryo.writeObject(output, obj);
        output.flush();

        return byteArrayOutputStream.toByteArray();
    }

    /**
     * 将对象序列化为 String
     * 利用了 Base64 编码
     *
     * @param obj 任意对象
     * @param <T> 对象的类型
     * @return 序列化后的字符串
     */
    public static <T> String writeObjectToString(T obj) {
        return new String(Base64.getEncoder().encode(writeObjectToByteArray(obj)), StandardCharsets.UTF_8);
    }

    /**
     * 将字节数组反序列化为原对象
     *
     * @param byteArray writeToByteArray 方法序列化后的字节数组
     * @param clazz     原对象的 Class
     * @param <T>       原对象的类型
     * @return 原对象
     */
    public static <T> T readObjectFromByteArray(byte[] byteArray, Class<T> clazz) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
        Input input = new Input(byteArrayInputStream);

        Kryo kryo = getInstance();
        return kryo.readObject(input, clazz);
    }

    /**
     * 将 String 反序列化为原对象
     * 利用了 Base64 编码
     *
     * @param str   writeToString 方法序列化后的字符串
     * @param clazz 原对象的 Class
     * @param <T>   原对象的类型
     * @return 原对象
     */
    public static <T> T readObjectFromString(String str, Class<T> clazz) {
        return readObjectFromByteArray(Base64.getDecoder().decode(str.getBytes(StandardCharsets.UTF_8)), clazz);
    }
}
