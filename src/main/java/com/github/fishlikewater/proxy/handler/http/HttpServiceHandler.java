package com.github.fishlikewater.proxy.handler.http;

import com.github.fishlikewater.proxy.handler.BootStrapFactroy;
import com.github.fishlikewater.proxy.kit.PassWordCheck;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;

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

    private boolean isAuth;

    private Bootstrap bootstrap;

    public HttpServiceHandler(boolean isAuth){
        this.isAuth = isAuth;
    }

    //channelActive方法中将ctx保留为全局变量
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.ctx = ctx;
    }

    //Complete方法中刷新数据
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if (msg instanceof HttpRequest) {
            //转成 HttpRequest
            FullHttpRequest req = (FullHttpRequest) msg;
            boolean isContinue = !isAuth || PassWordCheck.basicLogin(req);
            if (isContinue) { //检测密码
                HttpMethod method = req.method();    //获取请求方式，http的有get post ...， https的是 CONNECT
                String headerHost = req.headers().get("Host");    //获取请求头中的Host字段
                String host = "";
                int port = 80;                                    //端口默认80
                if(headerHost == null){
                    log.warn("not host this request {}", req);
                    ctx.close();
                    return;
                }
                String[] split = headerHost.split(":");            //可能有请求是 host:port的情况，
                host = split[0];
                if (split.length > 1) {
                    port = Integer.valueOf(split[1]);
                }
                if (method.equals(HttpMethod.CONNECT)) {
                    handlerHttps(host, port);
                }else {
                    handlerHttp(host, port, req);
                }
            } else {
                    FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.PROXY_AUTHENTICATION_REQUIRED);
                    resp.headers().add("Proxy-Authenticate", "Basic realm=\"Text\"");
                    resp.headers().setInt("Content-Length", resp.content().readableBytes());
                    //允许跨域访问
                    resp.headers().set("Access-Control-Allow-Origin", "*");
                    resp.headers().set("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS");
                    resp.headers().set("Access-Control-Allow-Headers","Content-Type, CDS-REQ-TYPE, CDS-SM-VERSION");
                    resp.headers().set("ACCESS-CONTROL-ALLOW-CREDENTIALS","true");
                    this.ctx.writeAndFlush(resp);
                }
            } else {
                log.warn("只支持http/https请求代理");
            }
        }

        /**
         * 处理http
         * @param host
         * @param port
         */
        private void handlerHttp(String host, int port, FullHttpRequest req){
            Promise<Channel> promise = createPromise(new InetSocketAddress(host, port));    //根据host和port创建连接到服务器的连接
            //如果是http连接，首先将接受的请求转换成原始字节数据
            log.info("处理http 请求");
            ReferenceCountUtil.retain(req);
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
                        ChannelPipeline p = ctx.pipeline();
                        p.remove("httpcode");
                        p.remove("httpservice");
                        p.addLast(new NoneHandler(channelFuture.get()));
                        //添加handler
                        ChannelPipeline pipeline = channelFuture.get().pipeline();
                        pipeline.addLast(new NoneHandler(ctx.channel()));
                        channelFuture.get().writeAndFlush(o).addListener(t->{
                            log.info("count:{}", ReferenceCountUtil.refCnt(req));
                        });
                    }else {
                        log.info("连接http web 失败");
                    }
                }
            });
        }


        /**
         * 处理https
         * @param host
         * @param port
         */
        private void handlerHttps(String host, int port){
            Promise<Channel> promise = createPromise(new InetSocketAddress(host, port));    //根据host和port创建连接到服务器的连接
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
                        ctx.channel().writeAndFlush(resp).addListener(new ChannelFutureListener() {
                            @Override
                            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                                if(channelFuture.isSuccess()){
                                    log.info("向浏览器发送200响应, 成功");
                                    ChannelPipeline p = ctx.pipeline();
                                    p.remove("httpcode");
                                    p.remove("aggregator");
                                    p.remove("httpservice");
                                    p.addLast(new NoneHandler(webchannelFuture.get()));
                                    //添加handler
                                    ChannelPipeline pipeline = webchannelFuture.get().pipeline();
                                    pipeline.addLast(new NoneHandler(ctx.channel()));
                                }else {
                                    log.info("向浏览器发送200响应, 失败");
                                    ctx.close();
                                }
                            }
                        });
                    } else {
                        log.info("连接https web 失败");
                    }
                }
            });
        }


        //根据host和端口，创建一个连接web的连接
        private Promise<Channel> createPromise(InetSocketAddress address) {
            Bootstrap bootstrap = BootStrapFactroy.bootstrapConfig(ctx);
            final Promise<Channel> promise = ctx.executor().newPromise();
            bootstrap.remoteAddress(address);
            bootstrap.connect()
                    .addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture channelFuture) throws Exception {
                            if (channelFuture.isSuccess()) {
                                log.info("connection success address {}, port {}", address.getHostString(), address.getPort());
                                promise.setSuccess(channelFuture.channel());
                            } else {
                                log.warn("connection fail address {}, port {}", address.getHostString(), address.getPort());
                                channelFuture.cancel(true);
                            }
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
