package org.sluckframework.demo.test.member.guava;

import com.google.common.cache.*;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Author: sunxy
 * Created: 2015-12-17 17:50
 * Since: 1.0
 */
public class GuavaCacheDeom {

    //底层实现类似ConcurrentMap,但是性能不如其,功能比其强大, 且带有loading功能(不存在则增加)
    public static void loadingCache() {
        LoadingCache<Integer, java.lang.String> cache = CacheBuilder.newBuilder()
                .maximumSize(10)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build(new CacheLoader<Integer, java.lang.String>() {
                    @Override
                    public java.lang.String load(Integer key) throws Exception {
                        return key.toString();
                    }
                });
        System.out.println(cache.size());
        // unchecked  get()为check方法,且如果存在返回,不存在就增加
        String one = cache.getUnchecked(1);
        System.out.println(one);
    }

    //不使用Loading缓存,而使用一个简单缓存使用 callable代替 loading的功能
    public static void callable() {
        Cache<Integer,String> cache = CacheBuilder.newBuilder()
                .maximumSize(10)
                .build(); //构造一个简单缓存
        try {
            String res = cache.get(1, new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return "default";
                }
            });
            System.out.println(res);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        //也可以主动put 覆盖值
        cache.put(1, "1");
        System.out.println(cache.getIfPresent(1));

    }

    //根据权重函数和权重值进行回收
    public static void weigherGC() {
        LoadingCache<Integer, String> cache = CacheBuilder.newBuilder()
                .maximumWeight(1000)
                .weigher((Weigher<Integer, String>) (key, value) -> key.hashCode()).build(new CacheLoader<Integer, java.lang.String>() {
                    @Override
                    public java.lang.String load(Integer key) throws Exception {
                        return key.toString();
                    }
                });
    }

    //可以让jvm回收的GC策略
    public static void jvmGcCache() {
        CacheBuilder.newBuilder()
                .maximumSize(10)
                .weakKeys() //设置KEY为弱引用
                .weakValues() //设置值为弱引用
                .softValues() //设置值为软引用
                .build();
    }

    //主动删除元素
    public static void deletElement() {
        Cache<Integer, String> cache = CacheBuilder.newBuilder().build();
        cache.invalidate(1); //个别清楚
        cache.invalidateAll(Lists.newArrayList(1, 2, 3)); //批量清楚
        cache.invalidateAll(); //清除所有缓存项
    }

    //移除元素的监听器,回调函数
    public static void removeListener() {
        RemovalListener<Integer, String> removalListener = notification -> {
            String value = notification.getValue();
            System.out.println("key:" + notification.getKey() + ", value:" +value + ", had removed");
        };

        Cache<Integer, String> cache = CacheBuilder.newBuilder().removalListener(removalListener)
                .build();
        cache.put(1, "1");
        cache.invalidate(1);

        //异步监听器 性能更高
        RemovalListeners.asynchronous(removalListener, Executors.newSingleThreadExecutor());
    }

    public static void refresh() {
        LoadingCache<Integer, String> cache = CacheBuilder.newBuilder()
                .build(new CacheLoader<Integer, String>() {
                    @Override
                    public String load(Integer key) throws Exception {
                        return key.toString();
                    }
                });
        cache.refresh(1); //在刷新的过程中,如果有其它线程访问,会返回旧值,直到新值完成

        //也可以配置自动刷新,可以重写 CacheLoader的 reload方法来定制,注:只有真正的检索的时候才会刷新缓存,需要异步的话
        //提供异步CacheLoader即可
       LoadingCache<Integer, String> cache2 =  CacheBuilder.newBuilder()
                .refreshAfterWrite(1, TimeUnit.MINUTES)
                .build(CacheLoader.asyncReloading(
                        new CacheLoader<Integer, String>() {
                    @Override
                    public String load(Integer key) throws Exception {
                        return key.toString();
                    }

                    //在这里可以看出其是使用异步的方式来实现自动刷新,异步执行TASK
                    @Override
                    public ListenableFuture<String> reload(Integer key, String oldValue) throws Exception {
                        ListenableFutureTask task =  ListenableFutureTask.create(new Callable<String>() {
                            @Override
                            public String call() throws Exception {
                                return "";
                            }
                        });
                        return task;
                    }
                }, Executors.newCachedThreadPool()));
    }

    public static void otherFunction() {
        CacheStats stats = CacheBuilder.newBuilder().recordStats().build().stats();// 开启统计功能
        stats.hitRate(); //缓存命中率
        stats.averageLoadPenalty();//加载新值的平均时间,单位为纳秒
        stats.evictionCount(); //缓存被回收的总数,不包括新值

        CacheBuilder.newBuilder().build().asMap(); //返回ConcurrentMap的形式
    }

    public static void main(String[] args) {
//        loadingCache();
//        callable();
        removeListener();
    }
}
