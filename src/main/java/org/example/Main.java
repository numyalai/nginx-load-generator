








//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;
//
//import org.apache.http.HttpResponse;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.impl.client.HttpClients;
//
//public class Main {
//    public static void main(String[] args) {
//
//        int numThreads = 10;
//        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
//        long startTime = System.currentTimeMillis();
//
//        for (int i = 0; i < 10; i++) {
//
//            executorService.submit(() -> {
////                long startTime = System.currentTimeMillis();
//
//                sendHttpRequest("http://localhost:8080/1kb.bin");
////                long endTime = System.currentTimeMillis();
////                long responseTime = endTime - startTime;
//            });
//        }
//
//        executorService.shutdown();
//        try {
//            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        long endTime = System.currentTimeMillis();
//        long totalTime = endTime - startTime;
//        System.out.println("Total Time: " + totalTime / 1000 + " s");
//
//
//
//    }
//
//
//    private static void sendHttpRequest(String url) {
//        try {
//            HttpClient httpClient = HttpClients.createDefault();
//            HttpGet httpGet = new HttpGet(url);
//            HttpResponse response = httpClient.execute(httpGet);
//
//            // Send the request
//
//            response.getEntity().getContent().close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}