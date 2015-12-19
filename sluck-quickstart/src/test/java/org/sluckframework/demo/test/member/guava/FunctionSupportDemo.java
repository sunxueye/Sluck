package org.sluckframework.demo.test.member.guava;

import com.google.common.base.*;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;

import java.util.Map;

/**
 * Author: sunxy
 * Created: 2015-12-19 17:34
 * Since: 1.0
 */
public class FunctionSupportDemo {

    //特殊的断言,已经实现了断言接口并提供了对应的良好的支持
    public static void specialPredicate() {
        CharMatcher.any().matches('c');
        CharMatcher.inRange('a','z').matches('b');

        //配合RangeSet使用
        Range.open(1, 10); //[1,10]
        Range.closed(1, 2);
        Range.atLeast(2);
    }

    //函数接口对象的工具类,可以方便的构建有相应功能的函数接口对象
    public static void functionUtils() {
        Map<Integer, String> map = Maps.newHashMap();
        map.put(1, "1");
        Function<Integer, String> funcation = Functions.forMap(map);  // input key   return value;
        Function<String, String> funcation2 = Functions.identity();  // input key   return value;

        Functions.compose(funcation2, funcation); // input a  return  c     a->b  b->c

        //断言工具方法
        Predicate<String> predicate = Predicates.in(Sets.newHashSet("1", "2"));
        Predicate predicate1 = Predicates.instanceOf(String.class);
        Predicates.or(predicate, predicate1);
        Predicates.and(predicate, predicate1);
        Predicates.compose(predicate1, funcation); //使用function获取后作为输入判断

    }

    public static void main(String[] args) {

    }
}
