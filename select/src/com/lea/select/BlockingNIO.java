package com.lea.select;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * 一、使用NIO完成网络通信的三个核心
 *  通道（Channel）：负责连接
 *      java.nio.channels.Channel接口：
 *          |--SelectableChannel
 *              |--SocketChannel            网路
 *              |--ServerSocketChannel
 *              |--DatagramChannel
 *
 *              |--Pipe.SinkChannel
 *              |--Pipe.SourceChannel
 *  缓冲区（Buffer）：存储数据
 *  选择器（Select）：SelectableChannel多路复用器。用于监控SelectableChannel的IO状况
 * @author lzc
 * @create 2020.09.27 10:47
 */
public class BlockingNIO {

    // 1. 客户端
    @Test
    public void test1() throws IOException {
        // 1.获取通道
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("localhost", 9898));

        // 2.创建读入文件通道
        FileChannel fileChannel = FileChannel.open(Paths.get("微信图片_20191104133859.jpg"), StandardOpenOption.READ);

        // 3. 创建缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        // 将文件从文件通道中读入缓冲区，然后写入socket通道中
        while ((fileChannel.read(buffer))  != -1) {
            // 将缓冲区position 指针重置，进入读模式
            buffer.flip();
            socketChannel.write(buffer);
            buffer.clear();
        }

        // 5. 关闭资源
        fileChannel.close();
        socketChannel.close();
    }

    // 2. 服务端
    @Test
    public void test2() throws IOException {
        // 1.创建服务端通道
        ServerSocketChannel ssChannel = ServerSocketChannel.open();
        // 2.socket绑定端口
        ssChannel.bind(new InetSocketAddress(9898));
        System.out.println("启动------------------->");
        // 4.获取客户端连接的Socket通道
        SocketChannel socketChannel = ssChannel.accept();
        System.out.println("客户端连接进来了------------------->");
        // 3.创建写入文件通道
        FileChannel fileChannel = FileChannel.open(Paths.get("copyPic.jpg"), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        // 5.分配缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        // 写入文件
        while ((socketChannel.read(buffer)) != -1) {
            buffer.flip();
            fileChannel.write(buffer);
            buffer.clear();
        }
        System.out.println("客户端信息传输完成------------------->");
        // 关闭通道
        socketChannel.close();
        fileChannel.close();
        ssChannel.close();

    }
}
