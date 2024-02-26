package org.example;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.atomic.AtomicLong;

public class LoadGenerator {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java LoadGenerator <SUT_IP> <FileName> <concurrencyLevel> [testDurationSeconds]");
            System.exit(1);
        }
        String sutIp = args[0];
        String fileName = args[1];
        int concurrencyLevel = args.length > 2 ? Integer.parseInt(args[2]) : 1;
        int testDurationSeconds = args.length > 3 ? Integer.parseInt(args[3]) : 5;
        String url = "http://" + sutIp + "/" + fileName;

        System.out.println("No. of input parameters = " + args.length);
        runLoadGenerator(url, concurrencyLevel, testDurationSeconds);
    }

    private static void runWithHttpConnection(long startTime, BufferedWriter writer, AtomicLong requestsHandled, String url)
    {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000); // Adjust timeout as needed
            connection.setReadTimeout(5000); // Adjust timeout as needed

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String contentType = connection.getHeaderField("Content-Type");
                System.out.println(contentType);
                try (InputStream inputStream = connection.getInputStream()) {
                    System.out.println("Request completed. Status code: " + responseCode);
                    long endTime = System.currentTimeMillis();

                    byte[] responseBodyBytes = inputStream.readAllBytes();

                    // Measure the size of the response body
                    int responseBodySize = responseBodyBytes.length;

                    System.out.println("Response body size: " + responseBodySize + " bytes");

                    // Record latency
                    long latency = System.currentTimeMillis() - startTime;
//                    writer.write("Latency: " + latency + " ms\n");
                    writer.flush();
                    requestsHandled.getAndIncrement();
                }
            } else {
                                // Handle non-success response codes
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void runLoadGenerator(String url, int concurrencyLevel, int testDurationSeconds) {
        long startTime = System.currentTimeMillis();
        AtomicLong requestsHandled = new AtomicLong();
        AtomicLong totalLatency = new AtomicLong();
        AtomicLong transferRate = new AtomicLong();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("output/load_generator_results.csv"))) {
            writer.write("RequestsHandled,MeanLatency (ms),TransferRate (Kbytes/sec),Thrhowqoughput (requests/second)\n");

            while (System.currentTimeMillis() - startTime < testDurationSeconds * 1000) {
                for (int i = 0; i < concurrencyLevel; i++) {
                    Thread thread = new Thread(() -> {
                        runWithHttpConnection(startTime, writer, requestsHandled, url);
                    });
                    thread.start();
                    thread.join(); // Wait for the spawned thread to finish to control concurrency
                }
            }
            // Calculate throughput
            long totalRequests = requestsHandled.get();

            double meanLatency = totalLatency.get() / (double) totalRequests;
            double throughput = totalRequests / (double) testDurationSeconds;
            double meanTransferRate = transferRate.get() / (double) testDurationSeconds;

            writer.write(totalRequests + "," + meanLatency + "," + meanTransferRate + "," + throughput + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
