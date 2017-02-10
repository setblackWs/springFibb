package pl.setblack.fb;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.Response;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;

@RestController
@EnableAutoConfiguration
public class Example {

 private final  AsyncHttpClient asyncHttpClient = new DefaultAsyncHttpClient();

    @RequestMapping("/fibb")
    long fibb(@RequestParam(value = "n", defaultValue = "1") long n) {
        //System.out.println("getting fibb(" + n + ") THREADS: " + java.lang.Thread.activeCount());
        if (n < 2) {
            return 1;
        } else {
            CompletionStage<Long> fib_n_1 = requestFibb(n - 1);
            CompletionStage<Long> fib_n_2 = requestFibb(n - 2);
            try {
                return fib_n_1.thenCombine(fib_n_2, (n_1, n_2) -> n_1 + n_2).toCompletableFuture().get();
            } catch (Exception ie) {
                throw new IllegalStateException(ie);
            }
        }
    }

    private CompletionStage<Long> requestFibb(long n) {
        AsyncHttpClient asyncHttpClient = new DefaultAsyncHttpClient();
        CompletableFuture<Response> f = asyncHttpClient.prepareGet("http://localhost:8080/fibb?n=" + n).execute()
                .toCompletableFuture();
        return f.thenApply(resp -> {
            try {
                asyncHttpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return Long.parseLong(resp.getResponseBody());
        });

    }
    private CompletionStage<Long> requestFibba(long n) {

        CompletableFuture<Response> f = asyncHttpClient.prepareGet("http://localhost:8080/fibba?n=" + n).execute()
                .toCompletableFuture();
        return f.thenApply(resp -> {

            return Long.parseLong(resp.getResponseBody());
        });

    }

    @RequestMapping("/fibba")
    public DeferredResult<Long> executeSlowTask(@RequestParam(value = "n", defaultValue = "1") long n) {

        DeferredResult<Long> deferredResult = new DeferredResult<>();
        //System.out.println("getting fibba(" + n + ") THREADS: " + java.lang.Thread.activeCount());
        if (n < 2) {
            deferredResult.setResult(1L);

        } else {
            CompletionStage<Long> fib_n_1 = requestFibba(n - 1);
            CompletionStage<Long> fib_n_2 = requestFibba(n - 2);

            fib_n_1.thenCombine(fib_n_2, (n_1, n_2) -> n_1 + n_2).thenAccept((res)->deferredResult.setResult(res));
        }
        return deferredResult;
    }




    public static void main(String[] args) throws Exception {
        SpringApplication.run(Example.class, args);
    }

}
