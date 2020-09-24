package com.lea.utils;

import java.nio.Buffer;

/**
 * @author lzc
 * @create 2020/09/24 下午 8:52
 */
public class Utils {

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
