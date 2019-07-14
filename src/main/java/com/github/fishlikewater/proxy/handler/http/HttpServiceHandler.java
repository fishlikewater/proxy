package com.github.fishlikewater.proxy.handler.http;

import com.github.fishlikewater.proxy.kit.EpollKit;
import com.github.fishlikewater.proxy.kit.PassWordCheck;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @author zhangx
 * @version V1.0
 * @mail fishlikewater@126.com
 * @ClassName HttpServiceHandler
 * @Description
 * @date 2019年02月26日 21:50
 **/
@Slf4j
public class HttpServiceHandler extends SimpleChannelInboundHandler<HttpObject> {

    //保留全局ctx
    private ChannelHandlerContext ctx;
    private Bootstrap clientstrap;
    private boolean isAuth;

    public HttpServiceHandler(boolean isAuth){
        this.isAuth = isAuth;
    }

    //channelActive方法中将ctx保留为全局变量
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        //log.info("连接活动");
        this.ctx = ctx;
    }

    //Complete方法中刷新数据
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, HttpObject msg) throws Exception {
        if (msg instanceof HttpRequest) {
            //转成 HttpRequest
            FullHttpRequest req = (FullHttpRequest) msg;
            boolean isContinue = !isAuth || PassWordCheck.basicLogin(req);
            if (isContinue) { //检测密码
                HttpMethod method = req.method();    //获取请求方式，http的有get post ...， https的是 CONNECT
                String headerHost = req.headers().get("Host");    //获取请求头中的Host字段
                log.info(headerHost);
                String host = "";
                int port = 80;                                    //端口默认80
                if(headerHost == null){
                    log.warn("not host this request {}", req);
                    channelHandlerContext.channel().close();
                    return;
                }
                String[] split = headerHost.split(":");            //可能有请求是 host:port的情况，
                if(split.length == 0){
                    log.warn("not host this request {}", headerHost);
                    channelHandlerContext.channel().close();
                    return;
                }
                host = split[0];
                if (split.length > 1) {
                    port = Integer.valueOf(split[1]);
                }else {
                    if (req.uri().indexOf("https") == 0) {
                        port = 443;
                    }
                }
                Promise<Channel> promise = createPromise(host, port);    //根据host和port创建连接到服务器的连接
                if (method.equals(HttpMethod.CONNECT)) {
                    //如果是https的连接
                    promise.addListener(new FutureListener<Channel>() {
                        @Override
                        public void operationComplete(Future<Channel> webchannelFuture) throws Exception {

                            if (webchannelFuture.isSuccess()) {
                                log.info("连接https web 成功，开始发送数据");
                                //首先向浏览器发送一个200的响应，证明已经连接成功了，可以发送数据了
                                FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, new HttpResponseStatus(200, "OK"));
                                //向浏览器发送同意连接的响应，并在发送完成后移除httpcode和httpservice两个handler
                                log.info("向浏览器发送200响应");
                                channelHandlerContext.channel().writeAndFlush(resp).addListener(new ChannelFutureListener() {
                                    @Override
                                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                                        if(channelFuture.isSuccess()){
                                            log.info("向浏览器发送200响应, 成功");
                                            ChannelPipeline p = channelHandlerContext.pipeline();
                                            p.remove("httpcode");
                                            p.remove("aggregator");
                                            p.remove("httpservice");
                                            //添加handler
                                            ChannelPipeline pipeline = webchannelFuture.get().pipeline();
                                            pipeline.addLast(new NoneHandler(channelHandlerContext.channel()));
                                        }else {
                                            log.info("向浏览器发送200响应, 失败");
                                            channelHandlerContext.close();
                                        }
                                    }
                                });
                            } else {
                                log.info("连接https web 失败");
                            }
                        }
                    });
                }else {
                    //如果是http连接，首先将接受的请求转换成原始字节数据
                    log.info("处理http 请求");
                    EmbeddedChannel em = new EmbeddedChannel(new HttpRequestEncoder());
                    em.writeOutbound(req);
                    final Object o = em.readOutbound();
                    em.close();
                    promise.addListener(new FutureListener<Channel>() {
                        @Override
                        public void operationComplete(Future<Channel> channelFuture) throws Exception {
                            //移除	httpcode	httpservice 并添加	NoneHandler，并向服务器发送请求的byte数据
                            if(channelFuture.isSuccess()){
                                log.info("连接http web 成功，开始发送数据");
                                ChannelPipeline p = channelHandlerContext.pipeline();
                                p.remove("httpcode");
                                p.remove("httpservice");
                                //添加handler
                                ChannelPipeline pipeline = channelFuture.get().pipeline();
                                pipeline.addLast(new NoneHandler(channelHandlerContext.channel()));
                                channelFuture.get().writeAndFlush(o);
                            }else {
                                log.info("连接http web 失败");
                            }
                        }
                    });
                }
            } else {
                FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.PROXY_AUTHENTICATION_REQUIRED);
                resp.headers().add("Proxy-Authenticate", "Basic realm=\"Text\"");
                resp.headers().setInt("Content-Length", resp.content().readableBytes());
                ctx.writeAndFlush(resp);
            }
        } else {
            //log.info(msg.toString());
            //log.warn("只支持http/https请求代理");
            // ReferenceCountUtil.release(msg);
        }
    }


    //根据host和端口，创建一个连接web的连接
    private Promise<Channel> createPromise(String host, int port) {
        Bootstrap bootstrap = bootstrapConfig();
        final Promise<Channel> promise = ctx.executor().newPromise();
        bootstrap.remoteAddress(host, port);
        bootstrap.connect()
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        if (channelFuture.isSuccess()) {
                            log.info("connection success address {}, port {}", host, port);
                            promise.setSuccess(channelFuture.channel());
                        } else {
                            log.warn("connection fail address {}, port {}", host, port);
                            channelFuture.cancel(true);
                        }
                    }
                });
        return promise;
    }

    private Bootstrap bootstrapConfig(){
        if (clientstrap == null) clientstrap = new Bootstrap();
        else return this.clientstrap;
        clientstrap.option(ChannelOption.SO_REUSEADDR, true);
        clientstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        if (EpollKit.epollIsAvailable()) {//linux系统下使用epoll
            clientstrap.channel(EpollSocketChannel.class);
        } else {
            clientstrap.channel(NioSocketChannel.class);
        }
        clientstrap.group(ctx.channel().eventLoop());
        clientstrap.handler(new ClientServiceInitializer());
        return clientstrap;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException) {
            // 远程主机强迫关闭了一个现有的连接的异常
            ctx.channel().close();
        } else {
            super.exceptionCaught(ctx, cause);
        }
    }
}
