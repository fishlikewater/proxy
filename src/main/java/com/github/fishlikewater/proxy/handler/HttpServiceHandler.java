package com.github.fishlikewater.proxy.handler;

import com.github.fishlikewater.proxy.kit.PassWordCheck;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLException;

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
    //创建一会用于连接web服务器的	Bootstrap
    private Bootstrap b = new Bootstrap();

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
            HttpRequest req = (HttpRequest) msg;
            if (PassWordCheck.basicLogin(req)) { //检测密码
                HttpMethod method = req.method();	//获取请求方式，http的有get post ...， https的是 CONNECT
                String headerHost = req.headers().get("Host");	//获取请求头中的Host字段
                String host = "";
                int port = 80;									//端口默认80
                String[] split = headerHost.split(":");			//可能有请求是 host:port的情况，
                host = split[0];
                if (split.length > 1) {
                    port = Integer.valueOf(split[1]);
                }
                Promise<Channel> promise = createPromise(host, port);	//根据host和port创建连接到服务器的连接

				/*
				根据是http还是http的不同，为promise添加不同的监听器
				*/
                if (method.equals(HttpMethod.CONNECT)) {
                    //如果是https的连接
                    log.info("https");
                    promise.addListener(new FutureListener<Channel>() {
                        @Override
                        public void operationComplete(Future<Channel> channelFuture) throws Exception {
                            //首先向浏览器发送一个200的响应，证明已经连接成功了，可以发送数据了
                            FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, new HttpResponseStatus(200, "OK"));
                            //向浏览器发送同意连接的响应，并在发送完成后移除httpcode和httpservice两个handler
                            ctx.writeAndFlush(resp).addListener(new ChannelFutureListener() {
                                @Override
                                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                                    log.info("https响应200完成");
                                    ChannelPipeline p = ctx.pipeline();
                                    p.remove("httpcode");
                                    p.remove("httpservice");
                                }
                            });
                            ChannelPipeline p = ctx.pipeline();
                            //将客户端channel添加到转换数据的channel
                            p.addLast(new NoneHandler(channelFuture.getNow()));
                        }
                    });
                } else {
                    //如果是http连接，首先将接受的请求转换成原始字节数据
                    EmbeddedChannel em = new EmbeddedChannel(new HttpRequestEncoder());
                    em.writeOutbound(req);
                    final Object o = em.readOutbound();
                    em.close();
                    promise.addListener(new FutureListener<Channel>() {
                        @Override
                        public void operationComplete(Future<Channel> channelFuture) throws Exception {
                            //移除	httpcode	httpservice 并添加	NoneHandler，并向服务器发送请求的byte数据
                            ChannelPipeline p = ctx.pipeline();
                            p.remove("httpcode");
                            p.remove("httpservice");
                            //添加handler
                            p.addLast(new NoneHandler(channelFuture.getNow()));
                            channelFuture.get().writeAndFlush(o);
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
           // ReferenceCountUtil.release(msg);
        }
    }


    //根据host和端口，创建一个连接web的连接
    private Promise<Channel> createPromise(String host, int port) throws SSLException {
        final Promise<Channel> promise = ctx.executor().newPromise();
        b.group(ctx.channel().eventLoop())
                .channel(NioSocketChannel.class)
                .remoteAddress(host, port)
                .handler(new ClientServiceInitializer(ctx, host, port))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .connect()
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        if (channelFuture.isSuccess()) {
                            log.info("连接目标服务器成功");
                            promise.setSuccess(channelFuture.channel());
                        } else {
                            log.warn("连接目标服务器失败");
                            ctx.close();
                            channelFuture.cancel(true);
                        }
                    }
                });
        return promise;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(cause.getMessage());
    }
}