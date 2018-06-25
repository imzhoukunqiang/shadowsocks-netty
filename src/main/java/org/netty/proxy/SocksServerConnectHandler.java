package org.netty.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socks.SocksAddressType;
import io.netty.handler.codec.socks.SocksCmdRequest;
import io.netty.handler.codec.socks.SocksCmdResponse;
import io.netty.handler.codec.socks.SocksCmdStatus;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import org.netty.config.PacLoader;
import org.netty.config.RemoteServer;
import org.netty.encryption.CryptFactory;
import org.netty.encryption.ICrypt;
import org.netty.manager.RemoteServerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

@ChannelHandler.Sharable
public final class SocksServerConnectHandler extends SimpleChannelInboundHandler<SocksCmdRequest> {

    private static Logger logger = LoggerFactory.getLogger(SocksServerConnectHandler.class);

    private final Bootstrap b = new Bootstrap();
    private ICrypt _crypt;
    private RemoteServer remoteServer;
    private boolean isProxy = true;

    public SocksServerConnectHandler() {
        this.remoteServer = RemoteServerManager.getRemoteServer();
        this._crypt = CryptFactory.get(remoteServer.get_method(), remoteServer.get_password());
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, final SocksCmdRequest request) throws Exception {


        setProxy(request.host());
        final Channel inboundChannel = ctx.channel();

        InetAddress address = getAddress(inboundChannel);
        logger.info("address = " + String.valueOf(address) + ",host = " + request.host() + ",port = " + request.port() + ",isProxy = " + isProxy);

        if (!isProxy) {

            return;
        }

        Promise<Channel> promise = ctx.executor().newPromise();
        b.group(inboundChannel.eventLoop()).channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000).option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new DirectClientHandler(promise));


        promise.addListener(new GenericFutureListener<Future<Channel>>() {
            @Override
            public void operationComplete(final Future<Channel> future) throws Exception {
                final Channel outboundChannel = future.getNow();
                if (future.isSuccess()) {
                    final InRelayHandler inRelay = new InRelayHandler(ctx.channel(), SocksServerConnectHandler.this);
                    final OutRelayHandler outRelay = new OutRelayHandler(outboundChannel,
                            SocksServerConnectHandler.this);

                    ctx.channel().writeAndFlush(getSuccessResponse(request)).addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture channelFuture) {
                            try {
                                if (isProxy) {
                                    sendConnectRemoteMessage(request, outboundChannel);
                                }

                                ctx.pipeline().remove(SocksServerConnectHandler.this);
                                outboundChannel.pipeline().addLast(inRelay);
                                ctx.pipeline().addLast(outRelay);
                            } catch (Exception e) {
                                logger.error("", e);
                            }
                        }
                    });
                } else {
                    ctx.channel().writeAndFlush(getFailureResponse(request));
                    SocksServerUtils.closeOnFlush(ctx.channel());
                }
            }
        });


        b.connect(getIpAddr(request), getPort(request)).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    ctx.channel().writeAndFlush(getFailureResponse(request));
                    SocksServerUtils.closeOnFlush(ctx.channel());
                }
            }
        });
    }

    private InetAddress getAddress(Channel inboundChannel) {
        SocketAddress socketAddress = inboundChannel.remoteAddress();
        return socketAddress instanceof InetSocketAddress ? ((InetSocketAddress) socketAddress).getAddress() : null;
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        if (!isProxy) {
            SocksServerUtils.closeOnFlush(ctx.channel());
        }
        super.channelReadComplete(ctx);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        SocksServerUtils.closeOnFlush(ctx.channel());
    }

    public void setProxy(String host) {
        if (PacLoader.is_global_mode()) {
            isProxy = true;
        } else {
            isProxy = PacLoader.isProxy(host);
        }
    }

    /**
     * 获取远程ip地址
     *
     * @param request
     * @return
     */
    private String getIpAddr(SocksCmdRequest request) {
        if (isProxy) {
            return remoteServer.get_ipAddr();
        } else {
            return request.host();
        }
    }

    /**
     * 获取远程端口
     *
     * @param request
     * @return
     */
    private int getPort(SocksCmdRequest request) {
        if (isProxy) {
            return remoteServer.get_port();
        } else {
            return request.port();
        }
    }

    private SocksCmdResponse getSuccessResponse(SocksCmdRequest request) {
        return new SocksCmdResponse(SocksCmdStatus.SUCCESS, SocksAddressType.IPv4);
    }

    private SocksCmdResponse getFailureResponse(SocksCmdRequest request) {
        return new SocksCmdResponse(SocksCmdStatus.FAILURE, SocksAddressType.IPv4);
    }

    /**
     * localserver和remoteserver进行connect发送的数据
     *
     * @param request
     * @param outboundChannel
     */
    private void sendConnectRemoteMessage(SocksCmdRequest request, Channel outboundChannel) {
        ByteBuf buff = Unpooled.buffer();
        request.encodeAsByteBuf(buff);
        if (buff.hasArray()) {
            int len = buff.readableBytes();
            byte[] arr = new byte[len];
            buff.getBytes(0, arr);
            byte[] data = remoteByte(arr);
            sendRemote(data, data.length, outboundChannel);
        }
    }

    /**
     * localserver和remoteserver进行connect发送的数据
     * <p>
     * +-----+-----+-------+------+----------+----------+ | VER | CMD | RSV |
     * ATYP | DST.ADDR | DST.PORT |
     * +-----+-----+-------+------+----------+----------+ | 1 | 1 | X'00' | 1 |
     * Variable | 2 | +-----+-----+-------+------+----------+----------+
     * <p>
     * 需要跳过前面3个字节
     *
     * @param data
     * @return
     */
    private byte[] remoteByte(byte[] data) {
        int dataLength = data.length;
        dataLength -= 3;
        byte[] temp = new byte[dataLength];
        System.arraycopy(data, 3, temp, 0, dataLength);
        return temp;
    }

    /**
     * 给remoteserver发送数据--需要进行加密处理
     *
     * @param data
     * @param length
     * @param channel
     */
    public void sendRemote(byte[] data, int length, Channel channel) {
        ByteArrayOutputStream _remoteOutStream = null;
        try {
            _remoteOutStream = new ByteArrayOutputStream();
            if (isProxy) {
                _crypt.encrypt(data, length, _remoteOutStream);
                data = _remoteOutStream.toByteArray();
            }
            channel.writeAndFlush(Unpooled.wrappedBuffer(data));
        } catch (Exception e) {
            logger.error("sendRemote error", e);
        } finally {
            if (_remoteOutStream != null) {
                try {
                    _remoteOutStream.close();
                } catch (IOException e) {
                }
            }
        }
        logger.debug("sendRemote message:isProxy = " + isProxy + ",length = " + length + ",channel = " + channel);
    }

    /**
     * 给本地客户端回复消息--需要进行解密处理
     *
     * @param data
     * @param length
     * @param channel
     */
    public void sendLocal(byte[] data, int length, Channel channel) {
        ByteArrayOutputStream _localOutStream = null;
        try {
            _localOutStream = new ByteArrayOutputStream();
            if (isProxy) {
                _crypt.decrypt(data, length, _localOutStream);
                data = _localOutStream.toByteArray();
            }
            channel.writeAndFlush(Unpooled.wrappedBuffer(data));
        } catch (Exception e) {
            logger.error("sendLocal error", e);
        } finally {
            if (_localOutStream != null) {
                try {
                    _localOutStream.close();
                } catch (IOException e) {
                }
            }
        }
        logger.debug("sendLocal message:isProxy = " + isProxy + ",length = " + length + ",channel = " + channel);
    }

}
