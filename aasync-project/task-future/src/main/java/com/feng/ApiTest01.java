package com.feng;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class ApiTest01 {
    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        // 主线程跑
        // syncTest();

        // 协同跑
        asyncTest();

        long end = System.currentTimeMillis();
        System.out.println("共耗时：" + ( end - start));
    }

    // 同步
    private static void syncTest() {
        simulateTask("加载用户数据");
        simulateTask("加载配置信息");
        String config = "同步配置";
        String user = "小黑";
        System.out.println(Thread.currentThread().getName() + ":处理结果: " + user + "，" + config);
    }

    // 异步
    private static void asyncTest() {
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            simulateTask("加载用户数据");
            return "用户小黑";
        });

        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            simulateTask("加载配置信息");
            return "配置信息";
        });

        // 组合两个future，等待它们都完成
        // future1和future2联合起来，都完成之后执行后面的，后面的参数为前两者的返回值
        CompletableFuture<String> combinedFuture = future1.thenCombine(future2, (user, config) -> "处理结果: " + user + "，" + config);

        try {
            System.out.println(Thread.currentThread().getName() + ":" + combinedFuture.get());
        } catch(InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private static void simulateTask(String str) {
        try {
            TimeUnit.SECONDS.sleep(1);
            System.out.println(Thread.currentThread().getName() + "执行任务完成!" + str);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
