package org.sluckframework.demo.test.member.guava;

/**
 * Author: sunxy
 * Created: 2015-11-28 21:34
 * Since: 1.0
 */
public class GuavaTest {

    public static void main(String[] args) {

        //Opational类用于对null避免,可以在返回参数中使用Opational<T>,jdk8中已有替代
//        Optional<Integer> possiable = Optional.fromNullable(5);
//        System.out.println(possiable.isPresent());
//        System.out.println(possiable.or(6));

        //前置条件,让判断更简单,不用再自己写IF ELSE, 可以将此类导入到编译器的静态类库中
//        System.out.println(Preconditions.checkNotNull(1));
//        Preconditions.checkArgument(4<3, "Error: %s<3", 4);
//        Object test = checkNotNull(null, "error: is null");

        //Objects equals  MoreObjects tostring hashCode
//        System.out.println(Objects.equal("a", null));
//        System.out.println(Objects.equal(null, null));
//        System.out.println(Objects.hashCode("1","2"));
//        System.out.println(MoreObjects.toStringHelper(GuavaTest.class).add("shuxing", 1).toString());
//        System.out.println(MoreObjects.firstNonNull(null, 5));

        //链式比较,如果哪个比较不为0,直接返回后面将不会再继续比较
//        ComparisonChain.start()
//                .compare(1, 2)
//                .compare(2. 3)
//                .compare(4, 5, Ordering.natural().nullsLast())
//                .result();

        //table表,两个键值确定一个键,构造如Map<E,Map<K,V>>的结构
//        HashBasedTable<String, String, Integer> table = HashBasedTable.create();
//        table.put("1", "2", 12);
//        table.put("1", "3", 13);
//        table.put("2", "3", 23);
//        System.out.println(table.row("1").get("3") /* return map 2->12  3->13*/);
//        System.out.println(table.column("3").get("2") /*return map 1->13 2->23 低效*/);
//        System.out.println(table.rowMap().size()); //lazy load
//        System.out.println(table.columnMap().size()); //lazy load

        // map key 为type 值为对应的对象
//        MutableClassToInstanceMap ci = MutableClassToInstanceMap.create();
//        ci.putInstance(CommandGenerator.class, new CommandGenerator());

    }
}
