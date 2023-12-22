package com.github.fishlikewater.server.kit;

/**
 * <p>
 * ip池
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年03月17日 10:53
 **/
public interface IpPool {

    /**
     * 获取一个未使用的ip
     *
     * @return java.lang.Integer
     * @author fishlikewater@126.com
     * @since 2023/3/17 10:59
     */
    Integer getIp();


    /**
     * 回收地址
     *
     * @param ip 地址
     * @author fishlikewater@126.com
     * @since 2023/3/17 11:01
     */
    void retrieve(int ip);


    /**
     * 删除ip
     *
     * @param ip 地址
     * @author fishlikewater@126.com
     * @since 2023/3/17 11:01
     */
    void remove(int ip);

}
