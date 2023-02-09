package com.github.fishlikewater.server.handle.http;

import com.github.fishlikewater.server.handle.NoneHandler;
import com.github.fishlikewater.server.kit.BootStrapFactroy;
import com.github.fishlikewater.server.kit.PassWordCheck;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * @author zhangx
 * @version V1.0
 **/
@Slf4j
public class HttpProxyHandler extends SimpleChannelInboundHandler<HttpObject> {

    private final boolean isAuth;

    public HttpProxyHandler(boolean isAuth){
        this.isAuth = isAuth;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        if (msg instanceof HttpRequest) {
            //转成 HttpRequest
            FullHttpRequest req = (FullHttpRequest) msg;
            boolean isContinue = !isAuth || PassWordCheck.basicLogin(req);
            if (isContinue) {
                //获取请求方式，http的有get post ...， https的是 CONNECT
                HttpMethod method = req.method();
                //获取请求头中的Host字段
                String headerHost = req.headers().get("Host");
                String host;
                //端口默认80
                int port = 80;
                if(headerHost == null){
                    log.warn("not host this request {}", req);
                    ctx.close();
                    return;
                }
                //可能有请求是 host:port的情况，
                String[] split = headerHost.split(":");
                host = split[0];
                if (split.length > 1) {
                    port = Integer.parseInt(split[1]);
                }
                if (method.equals(HttpMethod.CONNECT)) {
                    handlerHttps(host, port, ctx);
                }else {
                    handlerHttp(host, port, req, ctx);
                }
            } else {
                    FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.PROXY_AUTHENTICATION_REQUIRED);
                    resp.headers().add("Proxy-Authenticate", "Basic realm=\"Text\"");
                    resp.headers().setInt("Content-Length", resp.content().readableBytes());
                    ctx.writeAndFlush(resp);
                }
            } else {
                log.warn("只支持http/https请求代理");
            }
        }

        /**
         * 处理http
         */
        private void handlerHttp(String host, int port, FullHttpRequest req, ChannelHandlerContext ctx){
            //根据host和port创建连接到服务器的连接
            Promise<Channel> promise = createPromise(new InetSocketAddress(host, port), ctx);
            //如果是http连接，首先将接受的请求转换成原始字节数据
            log.debug("处理http 请求");
            ReferenceCountUtil.retain(req);
            promise.addListener((FutureListener<Channel>) channelFuture -> {
                //移除	httpcode	httpservice 并添加	NoneHandler，并向服务器发送请求的byte数据
                if(channelFuture.isSuccess()){
                    log.debug("连接http web 成功，开始发送数据");
                    ChannelPipeline p = ctx.pipeline();
                    p.remove("httpcode");
                    p.remove("httpProxy");
                    p.addLast(new NoneHandler(channelFuture.get()));
                    //添加handler
                    ChannelPipeline pipeline = channelFuture.get().pipeline();
                    pipeline.addLast(new NoneHandler(ctx.channel()));
                    pipeline.addLast(new HttpRequestEncoder());
                    channelFuture.get().writeAndFlush(req).addListener(t-> {});
                }else {
                    log.debug("连接http web 失败");
                }
            });
        }


        /**
         * 处理https
         */
        private void handlerHttps(String host, int port, ChannelHandlerContext ctx){
            //根据host和port创建连接到服务器的连接
            Promise<Channel> promise = createPromise(new InetSocketAddress(host, port), ctx);
            //如果是https的连接
            promise.addListener((FutureListener<Channel>) webchannelFuture -> {

                if (webchannelFuture.isSuccess()) {
                    log.debug("连接https web 成功，开始发送数据");
                    //首先向浏览器发送一个200的响应，证明已经连接成功了，可以发送数据了
                    FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, new HttpResponseStatus(200, "OK"));
                    //向浏览器发送同意连接的响应，并在发送完成后移除httpcode和httpservice两个handler
                    log.debug("向浏览器发送200响应");
                    ctx.channel().writeAndFlush(resp).addListener((ChannelFutureListener) channelFuture -> {
                        if(channelFuture.isSuccess()){
                            log.debug("向浏览器发送200响应, 成功");
                            ChannelPipeline p = ctx.pipeline();
                            p.remove("httpcode");
                            p.remove("aggregator");
                            p.remove("httpservice");
                            p.addLast(new NoneHandler(webchannelFuture.get()));
                            //添加handler
                            ChannelPipeline pipeline = webchannelFuture.get().pipeline();
                            pipeline.addLast(new NoneHandler(ctx.channel()));
                        }else {
                            log.debug("向浏览器发送200响应, 失败");
                            ctx.close();
                        }
                    });
                } else {
                    log.debug("连接https web 失败");
                }
            });
        }


        /**
         *  根据host和端口，创建一个连接web的连接
         * @author fishlikewater@126.com
         * @param address 地址
         * @param ctx 通道
         * @since 2023/2/8 10:25
         */
        private Promise<Channel> createPromise(InetSocketAddress address, ChannelHandlerContext ctx) {
            Bootstrap bootstrap = BootStrapFactroy.bootstrapConfig(ctx);
            final Promise<Channel> promise = ctx.executor().newPromise();
            bootstrap.remoteAddress(address);
            bootstrap.connect()
                    .addListener((ChannelFutureListener) channelFuture -> {
                        if (channelFuture.isSuccess()) {
                            log.debug("connection success address {}, port {}", address.getHostString(), address.getPort());
                            promise.setSuccess(channelFuture.channel());
                        } else {
                            log.warn("connection fail address {}, port {}", address.getHostString(), address.getPort());
                            channelFuture.cancel(true);
                        }
                    });
            return promise;
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