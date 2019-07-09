/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package handling.netty;

import handling.MapleServerHandler;
import handling.ServerType;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

/**
 *
 * @author wubin
 */
public class ServerInitializer extends ChannelInitializer<SocketChannel> {

    private int world;
    private int channels;
    private int port;
    private ServerType type;

    public ServerInitializer() {
    }

    public ServerInitializer(int world, int channels, int port, ServerType type) {
        this.world = world;
        this.channels = channels;
        this.port = port;
        this.type = type;
    }

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipe = channel.pipeline();
        pipe.addLast(new IdleStateHandler(90, 90, 0));
        pipe.addLast("decoder", new MaplePacketDecoder()); // decodes the packet
        pipe.addLast("encoder", new MaplePacketEncoder()); //encodes the packet
        switch (type) {
            case 频道服务器:
            case 登录服务器:
            case 世界服务器:
            case 商城服务器:
                pipe.addLast("handler", new MapleServerHandler(channels, type)); //encodes the packet
                break;
        }
    }
}
