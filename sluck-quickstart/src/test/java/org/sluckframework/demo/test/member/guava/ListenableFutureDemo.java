package org.sluckframework.demo.test.member.guava;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.*;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

/**
 * Author: sunxy
 * Created: 2015-12-21 16:49
 * Since: 1.0
 */
public class ListenableFutureDemo {

    //增加监听器,每个任务执行后会执行监听器
    //增加监听器的时候内部实现有两种方案:(1)如果此时future还未执行完,则构造一个监听器链,等待执行完后执行
    //(2)如果已经执行完了,则直接使用对应的线程池开始执行执行监听器
    public static void addListener() {
        ListenableFutureTask future = ListenableFutureTask.create(() -> {
            System.out.println("running");
            return "null";
        });
        future.addListener(() -> {
            System.out.println("run over");
        }, Executors.newSingleThreadExecutor());

        Executors.newCachedThreadPool().execute(future);
    }

    //内部实现其实就是构造一个监听器,获取结果并执行对应方法,注册这个监听器给future
    public static void callback() {
        ListenableFutureTask future = ListenableFutureTask.create(() -> {
            System.out.println("running");
            return "null";
        });
        FutureCallback callback = new FutureCallback() {
            @Override
            public void onSuccess(Object result) {
                System.out.println("success");
            }

            @Override
            public void onFailure(Throwable t) {
                System.out.println("failed");
            }
        };
        Futures.addCallback(future, callback);

        Executors.newCachedThreadPool().execute(future);
    }

    //创建future的方式,使用execurtorService直接创建,ListenFuture也有对应的Service创建方法
    public static void createFuture() throws InterruptedException {
        ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
        ListenableFuture future = service.submit(() -> {
            System.out.println("running");
            return null;
        });
        Thread.currentThread().sleep(1000);
        Futures.addCallback(future, new FutureCallback() {
            @Override
            public void onSuccess(Object result) {
                System.out.println("success");
            }

            @Override
            public void onFailure(Throwable t) {
                System.out.println("failed");
            }
        });
    }

    //listenableFuture工具类Futures 创建future
    public static void futures() {
        ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
        ListenableFuture future = service.submit(() -> {
            System.out.println("running a");
            return "a";
        });
        //transformAsync(future<A>, AsyncFunction<A, B> 通过一个future和函数对象来创建一个future,结果为B
        ListenableFuture future1 = Futures.transformAsync(future, new AsyncFunction() {
            @Override
            public ListenableFuture apply(Object input) throws Exception {
                return service.submit((Callable<Object>) () -> {
                    System.out.println("running b");
                    return "b";
                });
            }
        });

        List<ListenableFuture> lists = Lists.newArrayList();
        lists.add(future);
        lists.add(future1);
        //所有iterable中的future的result组成的list 便为这个 future的返回值,如果有一个为failed或者cancel,则这个
        //future也为failed或者cancel
//        ListenableFuture future2 = Futures.allAsList(lists);
        //所有成功的future的值组成的list便为其返回值,如果有failed的则对应的位置为null
//        ListenableFuture future3 = Futures.successfulAsList(future, future1, future2);

        //返回可以抛出指定异常的checkedFuture
//        Futures.makeChecked(future3, input -> new RuntimeException("error"));
    }

    public static void main(String[] args) throws InterruptedException {
//        addListener();
//        callback();
        createFuture();
    }
}
