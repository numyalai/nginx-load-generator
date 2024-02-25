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

        if (args.length < 2) {
            System.out.println("Usage: java LoadGenerator <concurrencyLevel> [testDurationSeconds]");
            System.exit(1);
        }
        int concurrencyLevel =args.length > 0 ? Integer.parseInt(args[0]) : 10;
        int testDurationSeconds = args.length > 1 ? Integer.parseInt(args[1]) : 5;
        System.out.println("No. of input parameters = "+args.length);
        if (args.length > 0) {
            System.out.println("The input parameters are listed below:");
            for (int i=0; i<=args.length-1; i++) {
                System.out.println("Parameter # "+i+ " => "+args[i]);
            }
        }
        String url = "http://34.159.80.133/1kb.bin";
////        int concurrencyLevel = 10;
////        int numRequests = 1000;
////        int testDurationSeconds = 5;
//
//        runLoadGenerator(url, concurrencyLevel, numRequests, testDurationSeconds);
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

    private static void runWithHttpRequest(long startTime, BufferedWriter writer, AtomicLong requestsHandled, String url, HttpClient client, AtomicLong totalLatency, AtomicLong totalBytesTransferred)
    {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url)).GET()
                    .build();

            HttpResponse<java.io.InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            try(InputStream inputStream = response.body()) {
                System.out.println("Request completed. Status code: " + response.statusCode());
//                HttpHeaders headers = response.headers();
//                String contentType = headers.firstValue("Content-Type").orElse(null);
//                System.out.println("Content-Type: " + contentType);

//                                System.out.println(inputStream.);

//                                String fileName = "downloaded_file_" + requestsHandled + ".bin";
//                                    Path filePath = Paths.get(fileName);
//                                    Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
//                                    System.out.println("Downloaded file: " + fileName);

                                byte[] responseBodyBytes = inputStream.readAllBytes();

                                // Measure the size of the response body
                                int responseBodySize = responseBodyBytes.length;
                                System.out.println("Response body size: " + responseBodySize + " bytes");


                // Record latency
                long endTime = System.currentTimeMillis();
                long latency = endTime - startTime;
                long elapsedTimeSeconds = (endTime - startTime) / 1000;

//                double transferRatePerRequest = (double) responseBodySize / elapsedTimeSeconds;

                totalBytesTransferred.addAndGet(responseBodySize);
                totalLatency.addAndGet(latency);
//                writer.write("Latency: " + latency + " ms\n");
                writer.flush();
                requestsHandled.getAndIncrement();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void runLoadGenerator(String url, int concurrencyLevel, int testDurationSeconds) {
        long startTime = System.currentTimeMillis();
        AtomicLong requestsHandled = new AtomicLong();

        HttpClient client = HttpClient.newHttpClient();
        AtomicLong totalLatency = new AtomicLong();
        AtomicLong transferRate = new AtomicLong();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("output/load_generator_results_6.txt"))) {
            while (System.currentTimeMillis() - startTime < testDurationSeconds * 1000) {
                for (int i = 0; i < concurrencyLevel; i++) {
                    Thread thread = new Thread(() -> {
//                        runWithHttpRequest(startTime, writer, requestsHandled, url, client, totalLatency, transferRate);
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


            writer.write("Requests handled: " + requestsHandled.get() + " in " + testDurationSeconds + " s\n");
            writer.write("Mean latency: " + meanLatency + " ms\n");
            writer.write("Transfer rate: " + meanTransferRate + " [Kbytes/sec] received\n");

            writer.write("Throughput: " + throughput + " requests/second\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
        private static void runLoadGenerator(String url, int concurrencyLevel, int numRequests, int testDurationSeconds) {
        long startTime = System.currentTimeMillis();
        HttpClient client = HttpClient.newHttpClient();

        for (int i = 0; i < numRequests; i++) {
            if (System.currentTimeMillis() - startTime > testDurationSeconds * 1000) {
                break; // Stop if the test duration reached
            }
            Thread thread = new Thread(() -> {
                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .build();
                    HttpResponse<java.io.InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());


//                    System.out.println("Request completed. Status code: " + response.statusCode());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            thread.start();

            if (i % concurrencyLevel == 0) {
                try {
                    thread.join(); // Wait for the spawned threads to finish to control concurrency
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
