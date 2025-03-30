package org.example;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;


@Slf4j
public class MyThread implements Callable<String> {

    private final MyRequest request;

    public MyThread(MyRequest request) {
        this.request = request;

    }

    @Override
    public String call() {
        return request.doRequest();
    }
}
