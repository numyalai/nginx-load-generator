package org.example;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;

import java.util.concurrent.*;

public class DuratIonBasedLoadGenerator {

        private static int successfulRequests = 0;

    public static void main(String[] args) {
        startLoadGeneration();
    }

    private static void startLoadGeneration() {
        int numThreads = 20;
        int numRequests = 20;

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numRequests);

        long testDurationSeconds = 10;
        long startTime = System.currentTimeMillis();

        Runnable benchmarkTask = () -> {
            sendHttpRequest("http://192.168.1.42:8080/large_file_500.bin");
            successfulRequests++;

            latch.countDown();
        };

        ScheduledFuture<?> future = executorService.scheduleAtFixedRate(
                benchmarkTask,
                0,  // Initial delay
                1,  // Period (1 millisecond between each run)
                TimeUnit.MILLISECONDS
        );

        // Schedule a task to shutdown the executor service after the specified duration
        executorService.schedule(() -> {
            future.cancel(true);  // Stop the benchmark task
            executorService.shutdown();  // Shut down the executor service
        }, testDurationSeconds, TimeUnit.SECONDS);

        try {
            latch.await(); // Wait until all tasks are completed
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Total Time: " + totalTime + " ms");
        System.out.println("Successful Requests: " + successfulRequests);
        System.out.println("Throughput: " + (double) successfulRequests / (totalTime / 1000.0) + " requests/second");

    }


//    public static void main(String[] args) {
//        int numThreads = 1;
//        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(numThreads);
//        CountDownLatch latch = new CountDownLatch(numThreads);
//
//        long testDurationSeconds = 5;
//        long startTime = System.currentTimeMillis();
//
//        Runnable benchmarkTask = () -> {
//            sendHttpRequest("http://localhost:8080/1kb.bin");
//            latch.countDown();
//        };
//
//        ScheduledFuture<?> future = executorService.scheduleAtFixedRate(
//                benchmarkTask,
//                0,  // Initial delay
//                1,  // Period (1 millisecond between each run)
//                TimeUnit.MILLISECONDS
//        );
//
//        // Schedule a task to shutdown the executor service after the specified duration
//        executorService.schedule(() -> {
//            future.cancel(true);  // Stop the benchmark task
//            executorService.shutdown();  // Shut down the executor service
//        }, testDurationSeconds, TimeUnit.SECONDS);
//
//        try {
//            latch.await(); // Wait until all tasks are completed
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        long endTime = System.currentTimeMillis();
//        long totalTime = endTime - startTime;
//        System.out.println("Total Time: " + totalTime + " ms");
//    }

    private static void sendHttpRequest(String url) {
        try {
            HttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(url);
            HttpResponse response = httpClient.execute(httpGet);

            // Send the request
            response.getEntity().getContent().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
