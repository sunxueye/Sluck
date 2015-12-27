package org.sluckframework.demo.test.member.guava;

import com.google.common.collect.Sets;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import java.util.List;

/**
 * Author: sunxy
 * Created: 2015-12-25 00:02
 * Since: 1.0
 */
public class BaseTypeSupportDemo {

    //可以直接操作基本类型,而不用使用其包装类
    public static void baseSupport() {
        List<Integer> integerList = Ints.asList(1, 2 ,3);
        int[] ints = Ints.toArray(Sets.newHashSet(1, 2, 3));

        Longs.max(12, 11);
    }

    //一些基本类型可用byte来表示或者创建自己
    public static void byteCreated() {
        System.out.println(Ints.BYTES); //int 占用的字节数
        System.out.println(Longs.BYTES); //long 占用的字节数
        System.out.println(Doubles.BYTES); //double 占用的字节数

        //由字节构建自己
        int t = Ints.fromByteArray(new byte[]{123, 12, 11, -11}); //如果byte[].length > Ints.BYTE会抛出移除
        byte[] tt = Ints.toByteArray(123); //将基本类型转为字节数组

    }

    public static void main(String[] args) {
        byteCreated();

    }
}
