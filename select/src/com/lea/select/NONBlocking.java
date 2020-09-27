package com.lea.select;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

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
 *  二、缓冲区（Buffer）：存储数据
 *  三、选择器（Select）：SelectableChannel多路复用器。用于监控SelectableChannel的IO状况
 * @author lzc
 * @create 2020.09.27 15:01
 */
public class NONBlocking {

    public static void main(String[] args) throws IOException {
        // 1. 获取通道
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("localhost", 9898));
        // 2.切换非阻塞模式
        socketChannel.configureBlocking(false);

        // 3.分配指定大小的缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        Scanner sc = new Scanner(System.in);
        while (sc.hasNextLine()) {
            String s = sc.nextLine();
            if ("exit".equals(s)) {
                socketChannel.shutdownOutput();
            }
            // 发送数据
            buffer.put((LocalDateTime.now() + "\n" + s).getBytes());
            buffer.flip();
            socketChannel.write(buffer);
            buffer.clear();
        }



    }

    @Test
    public void server() throws IOException {
        // 1.创建通道
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // 2.切换非阻塞模式
        serverSocketChannel.configureBlocking(false);
        // 3.绑定端口
        serverSocketChannel.bind(new InetSocketAddress(9898));
        // 4.获取选择器
        Selector selector = Selector.open();
         /*
            SelectionKey:表示SelectableChannel和Select之间的注册关系
         */
        // 5.将通道注册到选择器上,并且指定“监听接收事件”
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        // 6. 轮询式的获取选择器上已经“准备就绪”的事件
        while (selector.select() > 0) {
            // 7. 获取Select（选择器）所有注册的 就绪事件的选择键
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            // 8. 迭代获取
            while (it.hasNext()) {
                // 获取准备就绪的事件
                SelectionKey sk = it.next();
                // 9.判断准备就绪的事件是什么事件
                if (sk.isAcceptable()) {
                    // 10.接收事件就绪
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    // 11.切换非阻塞模式
                    socketChannel.configureBlocking(false);
                    // 12. 将通道注册到选择器上
                    socketChannel.register(selector, SelectionKey.OP_READ);
                } else if (sk.isReadable()) {
                    // 13.获取当前选择器上“读就绪”状态的通道
                    SocketChannel socketChannel = (SocketChannel) sk.channel();

                    // 14、创建缓冲区，读取数据
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int len;
                    while ((len = socketChannel.read(buffer)) > 0) {
                        buffer.flip();
                        System.out.println(new String(buffer.array(), 0, len));
                        buffer.clear();
                    }
//                    socketChannel.close();
                }
                // 15. 取消选择键 SelectionKey
                it.remove();
            }
        }
    }
}
