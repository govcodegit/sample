package com.justtest;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Base64;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    OkHttpClient client = new OkHttpClient();
    ExecutorService singleThreadExecutor = Executors.newCachedThreadPool();
    volatile long bytes = 0;
    String host = System.getenv("MYHOST");

    void run() throws IOException {

        InputStream inputStream = getClass().getResourceAsStream("data2");
        byte[] data = new byte[inputStream.available()];
        IOUtils.read(inputStream, data);
        data = Base64.getDecoder().decode(data);
        //  System.err.println(new String(data));
        Collection<String> lines = IOUtils.readLines(new StringReader(new String(data)));
        for (String line : lines) {
            singleThreadExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    System.err.println("add task ...");
                    Request request = new Request.Builder()
                            .url(String.format("%s/%s", host, line))
                            .build();
                    try {
                        Response response = client.newCall(request).execute();
                        if (response == null || response.body() == null) {
                            return;
                        }
                        bytes = bytes + response.body().contentLength();

                        response.close();
                    } catch (IOException e) {
                        System.err.println(e.getMessage());

                    }


                }
            });
        }
        try {
            singleThreadExecutor.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.err.println("system shutdown");


    }

    public static void main(String[] args) throws IOException {
        Main example = new Main();
        example.run();
        System.err.println("done");

    }
}