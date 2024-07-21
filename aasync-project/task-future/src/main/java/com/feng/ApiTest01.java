package com.feng;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class ApiTest01 {
    public static void main(String[] args) {
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            simulateTask("加载用户数据");
            return "用户小黑";
        });

        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            simulateTask("加载配置信息");
            return "配置信息";
        });

        // 组合两个future，等待它们都完成
        CompletableFuture<String> combinedFuture = future1.thenCombine(future2, (user, config) -> "处理结果: " + user + "，" + config);

        try {
            System.out.println(combinedFuture.get());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private static void simulateTask(String str) {
        try {
            System.out.println("执行任务!");
            System.out.println(str);
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
