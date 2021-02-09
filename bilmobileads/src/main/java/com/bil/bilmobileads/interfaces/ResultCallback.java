package com.bil.bilmobileads.interfaces;

public interface ResultCallback<T, E> {
    public void success(T data);

    public void failure(E error);
}