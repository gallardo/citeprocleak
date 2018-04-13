package de.ustutt.citeproc;

import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static java.lang.System.in;
import static java.lang.System.out;

public class TestParallel {
    private static final int N_THREADS = 4;
    private static final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(N_THREADS);
    private static final Scanner scanner = new Scanner(in);

    public static void main(String[] args) throws InterruptedException {
        executeInParallel(Benchmarks::benchmarkSameItemUsingAdhoc);

//         AG 2018-04-13: Don't know why, but the first rendering after this loop takes again a while when using Nashorn
        for (int j = 1; j <= 1; j++) {
            executeInParallel(Benchmarks::benchmarkRandomItemUsingAdhoc);
        }

        executeInParallel(Benchmarks::benchmarkSameItemUsingItemDataProviderRecreatingCSL);

        executeInParallel(Benchmarks::benchmarkSameItemUsingItemDataProviderReuseCSL);

        executor.shutdown();
        out.println("Press enter to end");
        scanner.nextLine();
    }

    private static void executeInParallel(Callable<String> f) throws InterruptedException {
        CountDownLatch endController = new CountDownLatch(N_THREADS);
        out.println("Press enter to begin next test");
        scanner.nextLine();

        for (int i = 1; i <= N_THREADS; i++) {
            int finalI = i;
            out.println("Starting benchmark loop " + finalI);
            executor.execute(() -> {
                String result = null;
                try {
                    result = f.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                endController.countDown();
                out.println("Benchmark loop " + finalI + " finished. Result: " + result);
            });
        }
        out.println("Waiting for all loops to end");
        endController.await();
    }
}
