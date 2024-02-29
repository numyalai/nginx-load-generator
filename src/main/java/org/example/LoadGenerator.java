package org.example;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

public class LoadGenerator {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java LoadGenerator <SUT_IP> <FileName> <initialConcurrencyLevel> <maxConcurrencyLevel> <rampUpIntervalSeconds> [testDurationSeconds] <BenchmarkName>");
            System.exit(1);
        }
        String sutIp = args[0];
        String fileName = args[1];
        int initialConcurrencyLevel = args.length > 2 ? Integer.parseInt(args[2]) : 1;
        int maxConcurrencyLevel = args.length > 3 ? Integer.parseInt(args[3]) : 10;
        int rampUpIntervalSeconds = args.length > 4 ? Integer.parseInt(args[4]) : 5;
        int testDurationSeconds = args.length > 5 ? Integer.parseInt(args[5]) : 30;
        String benchMarkName = args.length > 6 ? args[6] : "Benchmark";


        String url = "http://" + sutIp + "/" + fileName;


        System.out.println("No. of input parameters = " + args.length);

        runLoadGenerator(url, initialConcurrencyLevel, maxConcurrencyLevel, testDurationSeconds, rampUpIntervalSeconds, benchMarkName);
    }

    private static void runWithHttpConnection(long startTime, BufferedWriter writer, AtomicLong requestsHandled,  LongAdder totalLatency, LongAdder transferRate, String url)
    {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000); // Adjust timeout as needed
            connection.setReadTimeout(5000); // Adjust timeout as needed

            long requestStartTime = System.currentTimeMillis();

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (InputStream inputStream = connection.getInputStream()) {
                    System.out.println("Request completed. Status code: " + responseCode);
                    long requestEndTime = System.currentTimeMillis();

                    // Record latency
                    long latency = requestEndTime - requestStartTime;
                    totalLatency.add(latency);

                    byte[] responseBodyBytes = inputStream.readAllBytes();
                    int responseBodySize = responseBodyBytes.length;
                    int responseBodySizeKB = responseBodySize / 1024;
                    transferRate.add(responseBodySizeKB);

                    writer.flush();
                    requestsHandled.getAndIncrement();
                }
            } else {
                System.out.println("Error: non-success response code: " + responseCode);
            }

        }
        catch(SocketTimeoutException e)
        {
            System.out.println("Socket timeout occurred: " + e.getMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static double formatDecimal(double value)
    {
        DecimalFormat df = new DecimalFormat("#.##");
        return Double.parseDouble(df.format(value));
    }
    private static void runLoadGenerator(String url, int initialConcurrency, int maxConcurrency, int testDurationSeconds, int rampUpIntervalSeconds, String benchMarkName) {
        long startTime = System.currentTimeMillis();
        AtomicLong requestsHandled = new AtomicLong();
        LongAdder totalLatency =  new LongAdder();
        LongAdder transferRate = new LongAdder();

        List<Thread> threads = new ArrayList<>();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("output/load_generator_results.csv"))) {
            writer.write("Iteration,ConcurrencyLevel,RequestsHandled,MeanLatency (ms),TransferRate (Kbytes/sec),Throughput (requests/second),Iteration_execution_time,Benchmark_Name\n");

            int iteration = 1;
            int concurrencyLevel = initialConcurrency;
            while (System.currentTimeMillis() - startTime < testDurationSeconds * 1000) {
                long currentTime = System.currentTimeMillis() - startTime;
                long iterationStartTime = System.currentTimeMillis();
                int currentSeconds = (int) (currentTime / 1000);
                System.out.println("Concurrancy Level: " + concurrencyLevel);
                System.out.println("Iteration : " + iteration);

                for (int i = 0; i < concurrencyLevel; i++) {
                    Thread thread = new Thread(() -> {
                        runWithHttpConnection(startTime, writer, requestsHandled, totalLatency, transferRate, url);
                    });
                    threads.add(thread);
                    thread.start();
//                    thread.join(); // Wait for the spawned thread to finish to control concurrency
                }
                for (Thread thread : threads) {
                    thread.join();
                }
                threads.clear();

                long iterationEndTime = System.currentTimeMillis();
                double iterationTimeSeconds = (iterationEndTime - iterationStartTime) / 1000.0;
                System.out.println("Iteration Took: " + iterationTimeSeconds);
                long totalRequests = requestsHandled.get();
                double meanLatency = formatDecimal(totalLatency.doubleValue() / totalRequests) ;
                double throughput = formatDecimal((double) totalRequests / iterationTimeSeconds );
                double meanTransferRateKBs = formatDecimal((double) transferRate.sum() / iterationTimeSeconds);

                writer.write(iteration + "," + concurrencyLevel + "," + totalRequests + "," + meanLatency + "," + meanTransferRateKBs + "," + throughput + "," + iterationTimeSeconds + "," + benchMarkName + "\n");
                iteration++;
                totalLatency.reset();
                transferRate.reset();
                requestsHandled.set(0);

                if (currentSeconds % rampUpIntervalSeconds == 0 && concurrencyLevel < maxConcurrency) {
                    concurrencyLevel++;
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
