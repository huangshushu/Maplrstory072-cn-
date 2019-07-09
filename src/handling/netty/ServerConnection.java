/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package handling.netty;

import constants.ServerConfig;
import constants.ServerConstants;
import handling.ServerType;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import tools.FileoutputUtil;

/**
 *
 * @author wubin
 */
public class ServerConnection {
    private int port;
    private int world = -1;
    private int channels = -1;
    private ServerType type = null;
    private ServerBootstrap boot;
    private EventLoopGroup bossGroup = new NioEventLoopGroup(1); //The initial connection thread where all the new connections go to
    private EventLoopGroup workerGroup = new NioEventLoopGroup(); //Once the connection thread has finished it will be moved over to this group where the thread will be managed
    private Channel channel;

    public ServerConnection(ServerType type, int port, int world, int channels) {
        this.port = port;
        this.world = world;
        this.channels = channels;
        this.type = type;
    }

    public void run() {
        try {
            boot = new ServerBootstrap().group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG,  ServerConfig.userLimit)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ServerInitializer(world, channels, port, type));
            try {
                channel = boot.bind(/*ServerConstants.ip_,*/ port).sync().channel().closeFuture().channel();
            } catch (Exception e) {
                FileoutputUtil.outputFileError("logs/异常输出.txt", e);
                e.printStackTrace();
            } finally {
                //System.out.println("Listening to port: " + port);
            }
        } catch (Exception e) {
            FileoutputUtil.outputFileError("logs/异常输出.txt", e);
            e.printStackTrace();
        }
    }

    public void close() {
        channel.close();
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getWorld() {
        return world;
    }

    public void setWorld(int world) {
        this.world = world;
    }

    public int getChannels() {
        return channels;
    }

    public void setChannels(int channels) {
        this.channels = channels;
    }

    public ServerBootstrap getBoot() {
        return boot;
    }

    public void setBoot(ServerBootstrap boot) {
        this.boot = boot;
    }

    public EventLoopGroup getBossGroup() {
        return bossGroup;
    }

    public void setBossGroup(EventLoopGroup bossGroup) {
        this.bossGroup = bossGroup;
    }

    public EventLoopGroup getWorkerGroup() {
        return workerGroup;
    }

    public void setWorkerGroup(EventLoopGroup workerGroup) {
        this.workerGroup = workerGroup;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

}

