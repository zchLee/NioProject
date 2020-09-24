package com.lea.buffer;

import com.lea.utils.Utils;
import org.junit.Test;

import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * 一、缓冲区（Buffer）： 在java NIO中负责数据的存储，缓冲区就是数组。用户存储不同数据类型的数据
 *    根据数据类型不同（boolean除外，其他的都有），提供了对象类型的缓冲区，
 *    他们的管理方式几乎一致，通过allocate()获取缓冲区
 *
 * 二、缓冲区存取数据的两个核心方法
 *      put(); 存入数据到缓冲区
 *      get(); 从缓冲区中获取数据
 *
 *      常用方法：
 *          flip(); 切换到读模式， position位置变成了0
 *          rewind(); 重复读，position指向位置0 mark变成-1
 *
 *          mark(); 标记当前position指向位置。mark=position
 *          reset(); position回到上次标记位置，如果最近一次没标记或调用过rewind() mark=-1 会抛出异常BufferUnderflowException
 *
 *  三、四个缓冲区核心成员属性：
 *     private int mark = -1;       标记；表示记录当前position的位置，可以通过reset()恢复到mark位置
 *     private int position = 0;    位置：缓冲区正在操作数据的位置
 *     private int limit;           界限，表示缓冲区中可以操作数据的大小（limit之前的数据不能读写）
 *     private int capacity;        表示缓冲区最大存储的容量，一旦声明不可改变
 *
 *    0 <= mark <= position <= limit <= capacity
 *
 *
 * @author lzc
 * @create 2020/09/24 下午 8:26
 */
public class BufferTest {

    @Test
    public void test() {
        // 1.分配一个指定大小的缓冲区
        ByteBuffer buf = ByteBuffer.allocate(1024);
        showFiled(buf);
        // 2. 利用put() 存入数据到缓冲区
        buf.put("abcd".getBytes());
        showFiled(buf);

        // 3.切换读取数据模式  flip()
        buf.flip();
        showFiled(buf);

        // 4.利用get() 读取缓冲区中的数据
        byte[] bytes = new byte[buf.limit()];
        // 方式一、
//        buf.get(bytes);
        // 方式二、
        // 判断缓冲区是否还有剩余数据
        if (buf.hasRemaining()) {
            // 获取缓冲区可以操作的数据数量
            int remaining = buf.remaining();
            for (int i = 0; i < remaining; i++) {
                bytes[i] = buf.get(i);
            }
        }
        System.out.println(new String(bytes));

        // 5. rewind()  可重复读数据
        buf.rewind();
        showFiled(buf);

        // 6. clear() 清空缓冲区,但是缓冲区的数据依然存在，但是处于 “被遗忘” 状态
        buf.clear();
        showFiled(buf);

        System.out.println((char) buf.get());
    }


    @Test
    public void test2() {
        // 1.分配一个指定大小的缓冲区
        ByteBuffer buf = ByteBuffer.allocate(1024);

        // 2. 利用put() 存入数据到缓冲区
        buf.put("abcd".getBytes());

        // 3.切换读取数据模式  flip()
        buf.flip();

        // 4.利用get() 读取缓冲区中的数据
        byte[] bytes = new byte[buf.limit()];
        buf.get(bytes, 0, 2);
        System.out.println(new String(bytes, 0, 2));
        System.out.println("读的数据：" + buf.position());

        buf.mark(); // 将当前position的值赋给mark
        // 将buf中的数据从2开始读，读两个结束，在butes数组中，从2开始往后写两位
        buf.get(bytes, 2, 2);
        System.out.println(new String(bytes, 2, 2));
        System.out.println("读的数据：" + buf.position());

        // position恢复到mark标记的位置
        buf.reset();
        System.out.println("读的数据：" + buf.position());
    }


    /*
    显示缓冲区属性
     */
    public static void showFiled(Buffer buf) {
        System.out.print("缓冲区正在操作的位置：");
        System.out.print(buf.position());
        System.out.print("\tbuffer限制的操作位置:");
        System.out.print(buf.limit());
        System.out.print("\t缓冲区最大的存储容量：");
        System.out.print(buf.capacity());
        System.out.println();
    }



}
