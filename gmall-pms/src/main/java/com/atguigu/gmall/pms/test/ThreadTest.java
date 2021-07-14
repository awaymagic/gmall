package com.atguigu.gmall.pms.test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * Callable线程使用步骤：
 *
 * 1. **创建Callable的实现类**，并重写call()方法，该方法为线程执行体，并且该方法有返回值
 * 2. **创建Callable的实例。**
 * 3. **实例化FutureTask类**，参数为Callable接口实现类的对象，FutureTask封装了Callable对象call()方法的返回值
 * 4. **创建多线程Thread对象**来启动线程，参数为FutureTask对象。
 * 5. 通过FutureTask类的对象的get()方法来获取线程结束后的返回值
 *
 *
 * FutureTask：未来的任务，用它就干一件事，**异步调用。通常用它解决耗时任务，挂起堵塞问题。2
 * isDone 轮询方法 判断方法是否执行完毕
 * get() 获取执行结果 会阻塞线程(FutureTask仅在call方法完成时才能get结果；如果计算尚未完成，则阻塞 get 方法。
 * @param args
 * @throws ExecutionException
 * @throws InterruptedException
 */
public class ThreadTest {

    /*
        还可以这样写：java8的新特性
        new Thread(new FutureTask<T>(() -> {
            System.out.println("方法的执行体");
            return "返回值类型和T泛型一致";
        })).start();
     */
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        FutureTask<Integer> task = new FutureTask<>(new MyCallableThread());
        new Thread(task,"threadName").start();
        new Thread(task).start();

//        while (!task.isDone()) {
//            System.out.println("wait...");
//        }
        System.out.println(task.get());
        System.out.println(Thread.currentThread().getName() + "执行完毕");

    }
}

class MyCallableThread implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        // 线程执行的主体，可以通过get()返回值
        System.out.println(Thread.currentThread().getName() + "方法执行体");

        return 200;
    }
}