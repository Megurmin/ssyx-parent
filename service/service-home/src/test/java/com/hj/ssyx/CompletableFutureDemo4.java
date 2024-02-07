package com.hj.ssyx;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author HuangJin
 * @date 15:17 2023/12/30
 */

// 串行化
public class CompletableFutureDemo4 {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //创建线程池
        ExecutorService executorService = Executors.newFixedThreadPool(3);

        //1 任务1 返回结果 1024
        CompletableFuture<Integer> futureA = CompletableFuture.supplyAsync(() -> {
            int value = 1024;
            System.out.println("任务1：" + value);
            return value;
        }, executorService);

        //2 任务2 获取任务1返回结果
        CompletableFuture<Integer> futureB = futureA.thenApplyAsync((res) -> {
            System.out.println("任务2：" + res);
            return res;
        }, executorService);

        //3 任务3 往下执行
        futureA.thenRunAsync(()->{
            System.out.println("任务3：");
        },executorService);
    }
}
