package org.sluckframework.demo.test.member.guava;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Optional;

/**
 * Author: sunxy
 * Created: 2015-11-28 21:34
 * Since: 1.0
 */
public class GuavaTest {

    public static void main(String[] args) {

        //Opational类用于对null避免,可以在返回参数中使用Opational<T>,jdk8中已有替代
        Optional<Integer> possiable = Optional.fromNullable(5);
        System.out.println(possiable.isPresent());
        System.out.println(possiable.or(6));

        //前置条件,让判断更简单,不用再自己写IF ELSE, 可以将此类导入到编译器的静态类库中
//        System.out.println(Preconditions.checkNotNull(1));
//        Preconditions.checkArgument(4<3, "Error: %s<3", 4);
//        Object test = checkNotNull(null, "error: is null");

        //Objects equals  MoreObjects tostring hashCode
        System.out.println(Objects.equal("a", null));
        System.out.println(Objects.equal(null, null));
        System.out.println(Objects.hashCode("1","2"));
        System.out.println(MoreObjects.toStringHelper(GuavaTest.class).add("shuxing", 1).toString());

        //链式比较,如果哪个比较不为0,直接返回后面将不会再继续比较
//        ComparisonChain.start()
//                .compare(1, 2)
//                .compare(2. 3)
//                .compare(4, 5, Ordering.natural().nullsLast())
//                .result();

        

        System.out.println(MoreObjects.firstNonNull(null, 5));
    }
}
