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
    private Bootstrap b = new Bootstrap();
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
            HttpRequest req = (HttpRequest) msg;
            boolean isContinue = !isAuth || PassWordCheck.basicLogin(req);
            if (isContinue) { //检测密码
                HttpMethod method = req.method();    //获取请求方式，http的有get post ...， https的是 CONNECT
                String headerHost = req.headers().get("Host");    //获取请求头中的Host字段
                String host = "";
                int port = 80;                                    //端口默认80
                String[] split = headerHost.split(":");            //可能有请求是 host:port的情况，
                host = split[0];
                if (split.length > 1) {
                    port = Integer.valueOf(split[1]);
                }else {
                    if (req.uri().indexOf("https") == 0) {
                        port = 443;
                    }
                }
                Promise<Channel> promise = createPromise(host, port);    //根据host和port创建连接到服务器的连接

				/*
				根据是http还是http的不同，为promise添加不同的监听器
				*/
                if (method.equals(HttpMethod.CONNECT)) {

                    //如果是https的连接
                    promise.addListener(new FutureListener<Channel>() {
                        @Override
                        public void operationComplete(Future<Channel> channelFuture) throws Exception {
                            //首先向浏览器发送一个200的响应，证明已经连接成功了，可以发送数据了
                            FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, new HttpResponseStatus(200, "OK"));
                            //向浏览器发送同意连接的响应，并在发送完成后移除httpcode和httpservice两个handler
                            channelHandlerContext.writeAndFlush(resp).addListener(new ChannelFutureListener() {
                                @Override
                                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                                    if(channelFuture.isSuccess()){
                                        ChannelPipeline p = channelHandlerContext.pipeline();
                                        if(p.names().size() != 0){
                                            p.remove("httpcode");
                                            p.remove("httpservice");
                                        }
                                    }else {
                                        channelHandlerContext.close();
                                    }
                                }
                            });
                            ChannelPipeline p = channelHandlerContext.pipeline();
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
                            ChannelPipeline p = channelHandlerContext.pipeline();
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
            //log.info(msg.toString());
            //log.warn("只支持http/https请求代理");
            // ReferenceCountUtil.release(msg);
        }
    }


    //根据host和端口，创建一个连接web的连接
    private Promise<Channel> createPromise(String host, int port) {
        final Promise<Channel> promise = ctx.executor().newPromise();
        if (EpollKit.epollIsAvailable()) {
            b.channel(EpollSocketChannel.class);
        } else {
            b.channel(NioSocketChannel.class);
        }
        b.group(ctx.channel().eventLoop())
                .remoteAddress(host, port)
                .handler(new ClientServiceInitializer(ctx))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .connect()
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        if (channelFuture.isSuccess()) {
                            log.info("连接目标服务器成功:{}:{}", host, port);
                            promise.setSuccess(channelFuture.channel());
                        } else {
                            log.warn("连接目标服务器失败:{}:{}", host, port);
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
