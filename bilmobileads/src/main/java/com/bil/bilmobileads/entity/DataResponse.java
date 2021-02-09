package com.bil.bilmobileads.entity;

public class DataResponse<T> {
    public T data;
    public Exception error;

    public DataResponse(T result) {
        this.data = result;
    }

    public DataResponse(Exception err) {
        this.error = err;
    }
}
