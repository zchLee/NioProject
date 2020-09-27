package com.lea.select;

import org.junit.Test;
import sun.nio.ByteBuffered;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * @author lzc
 * @create 2020.09.27 11:14
 */
public class BlockingNIO2 {

    // 客户端
    @Test
    public void client() throws IOException {
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("localhost", 9898));
        FileChannel fileChannel = FileChannel.open(Paths.get("微信图片_20191104133859.jpg"), StandardOpenOption.READ);

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        while ((fileChannel.read(buffer)) != -1) {
            buffer.flip();
            socketChannel.write(buffer);
            buffer.clear();
        }

        // shutdownOutput 停止输出数据
        // 告诉服务器，客户端发送数据完毕了
        socketChannel.shutdownOutput();

        // 接受服务端返回的消息
        int len;
        while ((len = socketChannel.read(buffer)) != -1) {
            buffer.flip();
            System.out.println(new String(buffer.array(), 0, len));
            buffer.clear();
        }

        fileChannel.close();
        socketChannel.close();
    }

    // 服务端
    @Test
    public void server() throws IOException {
        ServerSocketChannel ssChannel = ServerSocketChannel.open();
        ssChannel.bind(new InetSocketAddress(9898));

        SocketChannel socketChannel = ssChannel.accept();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        FileChannel fileChannel = FileChannel.open(Paths.get("copyPic2.jpg"), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        while ((socketChannel.read(buffer)) != -1) {
            buffer.flip();
            fileChannel.write(buffer);
            buffer.clear();
        }

        buffer.put("服务端接收数据成功".getBytes());
        buffer.flip();
        socketChannel.write(buffer);

        socketChannel.close();
        fileChannel.close();
        ssChannel.close();
    }
}
