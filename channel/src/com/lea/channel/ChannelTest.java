package com.lea.channel;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

/**
 *  一、通道（Channel） ：用于源节点与目标节点的连接。在java NIO中负责缓冲区中数据的传输。
 *      Channel本身不存储数据，因此配合缓冲区（Buffer）进行传输
 *
 *  二、通道的主要实现类
 *      java.nio.channels.Channel 接口:
 *          |-- FileChannel                 文件通道
 *          |-- SocketChannel               TCP
 *          |-- ServerSocketChannel         TCP
 *          |-- DatagramChannel             UDP
 *
 *  三、获取通道
 *      1.java针对支持通道的类提供了getChannel() 方法
 *          本地IO:
 *              FileInputStream/FileOutputStream/RandomAccessFile
 *          网络IO:
 *              Socket/ServerSocket/DatagramSocket
 *      2.jdk 1.7 中提供的NIO.2针对了各个通道提供了静态方法 open();
 *      3.jdk 1.7 中提供的NIO.2的Files工具类的newByteChannel();
 *
 *  四、通道之间的数据传输
 *      transferFrom()
 *      transferTo()
 *
 *  五、分散（Scatter）读取(Gather)
 *      分散读取（Scattering Reads）: 将通道中的数据分散到多个缓冲区中
 *      聚集写入（Gathering Writes）: 将多个缓冲区中的数据聚集到通道中
 *  六、字符集：charset
 *      编码：将字符串转换成字节数组
 *      解码：将字节数组转换成字符串
 * @author lzc
 * @create 2020-9-25 11:18
 */
public class ChannelTest {

    /*
    1. 利用通道完成文件复制（非直接缓冲区）
     */
    @Test
    public void test1() {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            // IDEA 测试方法的相对路径是当前module下
            fis = new FileInputStream("test.txt");
            fos = new FileOutputStream("test2.txt");

            // 获取通道
            inChannel = fis.getChannel();
            outChannel = fos.getChannel();

            // 分配指定缓冲区
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            // 将通道中的数据存入缓冲区中
            while ((inChannel.read(buffer)) != -1) {
                // 切换读取数据的模式
                buffer.flip();
                // 将缓冲区中的数据写入通道，
                outChannel.write(buffer);
                // 清空缓冲区
                buffer.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != outChannel) {
                try {
                    outChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != inChannel) {
                try {
                    inChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != fos) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != fis) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /*
    使用直接缓冲区完成文件复制 （内存映射文件）
     */
    @Test
    public void test2() throws IOException {
        FileChannel inChannel = FileChannel.open(Paths.get("test.txt"), StandardOpenOption.READ);
        // 存在文件就报错，不存在就新建
        FileChannel outChannel = FileChannel.open(Paths.get("testcopy.txt"), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW);

        // 直接将此通道文件的区域映射到内存中。
        // 只读
        MappedByteBuffer inMappedBuf = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());
        // 读写
        MappedByteBuffer outMappedBuf = outChannel.map(FileChannel.MapMode.READ_WRITE, 0, inChannel.size());

        // 去掉通道

        // 直接对缓冲区进行数据的读写操作
        byte[] dst = new byte[inMappedBuf.limit()];
        inMappedBuf.get(dst);
        outMappedBuf.put(dst);

        inChannel.close();
        outChannel.close();
    }

    /*
        通道之间的数据传输(使用直接缓冲区)
        a.transferTo(position, count,b) 将a通道中的数据传输到b通道
        b.transferFrom(a, position, count) b通道的数据 从a中直接缓冲区拿来
     */
    @Test
    public void test3() throws IOException {
        FileChannel inChannel = FileChannel.open(Paths.get("test.txt"), StandardOpenOption.READ);
        // 存在文件就报错，不存在就新建
        FileChannel outChannel = FileChannel.open(Paths.get("testCopy3.txt"), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW);

//        inChannel.transferTo(0, inChannel.size(), outChannel);
        outChannel.transferFrom(inChannel, 0, inChannel.size());
        inChannel.close();
        outChannel.close();
    }

    /*
        分散和聚集
     */
    @Test
    public void test4() throws Exception {
        RandomAccessFile raf = new RandomAccessFile("test.txt", "rw");
        // 1.获取通道
        FileChannel channel1 = raf.getChannel();
        // 2.分配指定大小的缓冲区
        ByteBuffer b1 = ByteBuffer.allocate(100);
        ByteBuffer b2 = ByteBuffer.allocate(1024);
        ByteBuffer[] buffers = {b1, b2};
        // 3.将数据通过通道传入buffers
        channel1.read(buffers);
        for (ByteBuffer buffer : buffers) {
            buffer.flip(); // 改变成读数据模式
        }
        System.out.println(new String(buffers[0].array(), 0, buffers[0].limit()));
        System.out.println("-------------------------------------------------------------");
        System.out.println(new String(buffers[1].array(), 0, buffers[1].limit()));

        // 4.聚集写入
        RandomAccessFile raf2 = new RandomAccessFile("tesCopy4.txt", "rw");
        FileChannel channel2 = raf2.getChannel();
        channel2.write(buffers);
    }

    /*
    字符集
     */
    @Test
    public void test5() throws CharacterCodingException {
        Charset gbk = Charset.forName("GBK");
        // 编码器
        CharsetEncoder ce = gbk.newEncoder();
        // 解码器
        CharsetDecoder cd = gbk.newDecoder();
        // 新建通道
        CharBuffer cBuf = CharBuffer.allocate(2048 + 1024);
        cBuf.put("中国迟早要重回世界第一的");
        cBuf.flip(); // 切换读模式
        // 将数据编码
//        System.out.println(cBuf.limit());
        ByteBuffer bBuf = ce.encode(cBuf);
//        System.out.println(cBuf.limit());
//        for (int i = 0; i < cBuf.limit(); i++) {
//            System.out.println(bBuf.get());
//        }

        // 切换只读模式 此处limit是原来limit的一半 不知道是为什么，导致读数据只有原来的一半，故不让它读并重置到读模式，
//       // 直接解码
//        bBuf.flip();
        // 解码
        CharBuffer decode = cd.decode(bBuf);
        System.out.println(decode.toString());

//        SortedMap<String, Charset> map = Charset.availableCharsets();
//        Set<Map.Entry<String, Charset>> entries = map.entrySet();
//        for (Map.Entry<String, Charset> entry : entries) {
//            System.out.println(entry.getKey() + "--" + entry.getValue());
//        }
    }
}
