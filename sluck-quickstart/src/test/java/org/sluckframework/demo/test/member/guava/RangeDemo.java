package org.sluckframework.demo.test.member.guava;

import com.google.common.collect.Range;

/**
 * Author: sunxy
 * Created: 2015-12-27 23:16
 * Since: 1.0
 */
public class RangeDemo {

    //一些常用的构造方法
    public static void createAPI() {
        Range range = Range.closed(2, 3); //[2,3]
        Range.lessThan(10); // (.. , 10)
        Range.atMost(10); //(.., 10]
        Range.atLeast(10); //[10, ...)
        Range.greaterThan(10); //(10, ..)
    }

    //一些常用的查询API
    public static void queryAPI() {
        Range.closedOpen(4, 4).isEmpty();//是否为空区间 true
        Range.open(3, 4).lowerEndpoint();//返回区间低端点值 3
        Range.open(3, 10).upperEndpoint();//返回区间高端点值 10
        Range.closed(3, 4).upperBoundType();// 返回去电高端的区间类型 closed
    }

    //一些常用的区间运算
    public static void encolse() {
        Range.closed(3, 10).encloses(Range.closed(4,6));//前面区间是否包含后面区间 true
        Range.open(3,5).isConnected(Range.closed(5,10));//是否是相连的区间 true
        Range.open(3,5).isConnected(Range.open(5,10));//是否是相连的区间 false
        Range.closed(3, 6).intersection(Range.closed(5, 7));//交集 [5,6]
        Range.closed(3, 5).span(Range.closed(5,10));//返回通知包含两个区间的最小区间,如果是相连的区间则是并集 [3,10]
        Range.open(3, 4).span(Range.open(5,10)).contains(5); // true

    }

    public static void main(String[] args) {

        boolean t = Range.open(3, 4).span(Range.open(5,10)).contains(5);
        boolean t1 = Range.open(3, 5).span(Range.open(5,10)).contains(6);
        boolean t2 = Range.open(3, 5).span(Range.open(5,10)).contains(3);
        System.out.println(t);
        System.out.println(t1);
        System.out.println(t2);

    }
}
