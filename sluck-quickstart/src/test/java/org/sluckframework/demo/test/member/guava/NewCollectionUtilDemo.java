package org.sluckframework.demo.test.member.guava;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Author: sunxy
 * Created: 2015-12-09 23:36
 * Since: 1.0
 */
public class NewCollectionUtilDemo {
    //使用静态工厂方法来创建需要的集合 lists sets maps...
    public static void staticMethodUtil() {
        List<String> list = Lists.newArrayListWithCapacity(1);
        list.add("s");
        list.add("s2");
        List<String> list2 = Lists.newArrayListWithExpectedSize(1);
        list2.add("s");
        list2.add("s2");

        System.out.println(list.size());
        System.out.println(list2.size());
    }

    //用于操作Iterable,合并 获取 信息等
    public static void iterables() {
        Iterable<Integer> ables = Iterables.concat(Ints.asList(1,2,3), Ints.asList(3,5,6));
        System.out.println(Iterables.getLast(ables));
        System.out.println(Iterables.frequency(ables, 3));
    }

    public static void listsAndSets() {
        List<String> lists = Lists.newArrayList();
        lists.add("s1");
        lists.add("s2");
        List<List<String>> listss = Lists.partition(lists, 1); // {[s1],[s2]}
        Lists.reverse(lists); //{s2,s1}

        Set<String> set1 = Sets.newHashSet();
        set1.add("1");
        Set<String> set2 = Sets.newHashSet();
        set2.add("2");
        Sets.union(set1, set2); //{1,2}
        set2.add("1");
        Sets.intersection(set1, set2); // {1}
        Sets.difference(set1, set2); //{2}

        Sets.cartesianProduct(Lists.newArrayList(set1, set2)); // 返回所有集合的 卡迪尔积
        Set<Set<String>> setss = Sets.powerSet(set2); //返回所有子集 {} {1} {1,2} {2}

    }

    public static void maps() {
        Iterable<String> iterable = Lists.newArrayList("1", "22", "333");
        iterable.forEach(System.out::println);

        Map<Integer, String> needs =  Maps.uniqueIndex(iterable, new Function<String, Integer>() {
            public Integer apply(String input) {
                return input.length();
            }
        });

        needs.forEach((k,v) -> {
            System.out.println("key:" + k + ",value:" + v);
        });

        Map<String, Integer> map1 = Maps.newHashMap();
        Map<String, Integer> map2 = Maps.newHashMap();
        map1.put("1",1);
        map2.put("2",2);
        Maps.difference(map1,map2).entriesInCommon(); //MAP中都有的映射项 包括 key and value
        Maps.difference(map1,map2).entriesDiffering(); //key相同但是value不同的项 返回的map value为ValueDifferent类型
        Maps.difference(map1,map2).entriesOnlyOnLeft();//只存在左边的项
        Maps.difference(map1,map2).entriesOnlyOnRight();//只存在右边的项
    }

    public static void main(String[] args) {
//        iterables();
        maps();
    }
}
