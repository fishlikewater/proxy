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


>_内网穿透 通过中间服务器中转,实现远程访问内网tcp资源:_

1 启动服务端(server)
```properties
#启动服务类型
proxy.type[0]=proxy_server
#监听地址端口
proxy.port=9008
proxy.address=0.0.0.0
proxy.boot-model=vpn
# 连接服务器验证token
proxy.token=131420
#是否开启netty 日志
proxy.logging=false
# 心跳 超时检测 间隔
proxy.timeout=60
#系统日志
logging.config=classpath:logback.xml
```

2 资源客户端注册(client)
```properties
proxy.port=9008
proxy.address=127.0.0.1
#注册资源名
proxy.proxy-name=test
#启动模式
proxy.boot-model=vpn
proxy.token=131420
proxy.logging=true
proxy.timeout=45
logging.config=classpath:logback.xml
#固定虚拟ip 如不设置则会服务器分配
proxy.fixed-ip=192.168.12.1
#是否开启socks5服务(纯作为资源调用可设置为false, 开启时 启用socks5代理软件将流量转发到改端口 就可以通过虚拟ip调用服务器中注册的所有服务)
proxy.open-socks5=false
socks.address=0.0.0.0
socks.port=9010
socks.auth=false
socks.username=test
socks.password=123456
socks.check-connect=false
```