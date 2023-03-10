### netty实现的代理服务器

###### 实现主要功能有:

* socks5 代理服务器  

* 内网穿透 服务端及客户端

> _socks5 代理服务器:_

* 启动server 即可, 配置文件如下:
```properties
#启动服务类型
proxy.type[0]=socks
#监听地址端口
proxy.port=9010
proxy.address=0.0.0.0

# 连接服务器验证token
proxy.token=131420

#是否开启netty 日志
proxy.logging=false

# socks5代理 是否开启验证模式
proxy.auth=true

# 心跳 超时检测 间隔
proxy.timeout=60
#系统日志
logging.config=classpath:logback.xml

#socks 代理时是否开启固定本地端口
proxy.use-local-ports=false
proxy.local-ports[0]=50110
proxy.local-ports[1]=50111
```
* socks5 若开启验证模式,可在account.json 文件中添加账户:
```json
{
  "test": "123456"
}
```

>_内网穿透 通过中间服务器中转,实现远程访问内网tcp资源:_
    
1 启动服务端(server)
```properties
#启动服务类型
proxy.type[0]=proxy_server
#监听地址端口
proxy.port=9010
proxy.address=0.0.0.0
# 连接服务器验证token
proxy.token=131420
#是否开启netty 日志
proxy.logging=false
# socks5代理 是否开启验证模式
proxy.auth=true
# 心跳 超时检测 间隔
proxy.timeout=60
#系统日志
logging.config=classpath:logback.xml
```

2 资源客户端注册(client)
```properties
#服务端地址端口 与服务端保持一致
proxy.port=9010
proxy.address=127.0.0.1
#注册客户端标记(调用客户端使用这一标记来连接)
proxy.proxy-path=zx2
#连接服务器验证token 与服务端保持一致
proxy.token=131420
#是否开启netty 日志
proxy.logging=false
# 心跳 超时检测 间隔
proxy.timeout=45
#系统日志
logging.config=classpath:logback.xml
```

3 调用客户端连接(call-client)
```properties
#远程服务器
proxy.port=9010
proxy.address=127.0.0.1
#远程服务器验证密钥
proxy.token=131420
#匹配目标机
proxy.proxy-path=zx2

#本地socks服务
proxy.socks-address=0.0.0.0
proxy.socks-port=9010
#为true时才需要配置下面用户密码
proxy.auth=false
proxy.username=zhangx
proxy.password=123456
#是否开启netty 日志
proxy.logging=false
# 心跳 超时检测 间隔
proxy.timeout=30
#系统日志
logging.config=classpath:logback.xml

proxy.mapping=true
proxy.proxy-mappings[0].domain=www.fishlikewater.com
proxy.proxy-mappings[0].ip=192.168.5.221
```
***
> _call-client 会默认开启一个socks5 代理，通过proxy.socks-port 指定端口,可以通过socks代理软件拦截本地流量转发到该端口。_