package com.github.fishlikewater.proxyp2p.codec;

import com.github.fishlikewater.proxyp2p.kit.ProbufData;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.MessageLite;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author: fishlikewater@126.com
 * @since: 2022年10月30日 12:06
 **/
@ChannelHandler.Sharable
public class MyProtobufDecoder extends MessageToMessageDecoder<DatagramPacket> {


    private static final boolean HAS_PARSER;

    static {
        boolean hasParser = false;
        try {
            // MessageLite.getParserForType() is not available until protobuf 2.5.0.
            MessageLite.class.getDeclaredMethod("getParserForType");
            hasParser = true;
        } catch (Throwable t) {
            // Ignore
        }

        HAS_PARSER = hasParser;
    }

    private final MessageLite prototype;
    private final ExtensionRegistryLite extensionRegistry;

    /**
     * Creates a new instance.
     */
    public MyProtobufDecoder(MessageLite prototype) {
        this(prototype, null);
    }

    public MyProtobufDecoder(MessageLite prototype, ExtensionRegistry extensionRegistry) {
        this(prototype, (ExtensionRegistryLite) extensionRegistry);
    }

    public MyProtobufDecoder(MessageLite prototype, ExtensionRegistryLite extensionRegistry) {
        if (prototype == null) {
            throw new NullPointerException("prototype");
        }
        this.prototype = prototype.getDefaultInstanceForType();
        this.extensionRegistry = extensionRegistry;
    }

    @Override
    public void decode(ChannelHandlerContext ctx, DatagramPacket packet, List<Object> out)
            throws Exception {
        final ByteBuf msg = packet.content();
        final byte[] array;
        final int offset;
        final int length = msg.readableBytes();
        if (msg.hasArray()) {
            array = msg.array();
            offset = msg.arrayOffset() + msg.readerIndex();
        } else {
            array = ByteBufUtil.getBytes(msg, msg.readerIndex(), length, false);
            offset = 0;
        }

        if (extensionRegistry == null) {
            if (HAS_PARSER) {
                out.add(new ProbufData(packet.sender(), packet.recipient(), prototype.getParserForType().parseFrom(array, offset, length)));
            } else {
                out.add(new ProbufData(packet.sender(), packet.recipient(), prototype.newBuilderForType().mergeFrom(array, offset, length).build()));
            }
        } else {
            if (HAS_PARSER) {
                out.add(new ProbufData(packet.sender(), packet.recipient(), prototype.getParserForType().parseFrom(
                        array, offset, length, extensionRegistry)));
            } else {
                out.add(new ProbufData(packet.sender(), packet.recipient(), prototype.newBuilderForType().mergeFrom(
                        array, offset, length, extensionRegistry).build()));
            }
        }
    }
}
