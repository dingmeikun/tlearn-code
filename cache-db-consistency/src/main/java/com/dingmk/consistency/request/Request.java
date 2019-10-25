package com.dingmk.consistency.request;


public interface Request {

    void process();

    String getProductId();

    boolean isForceRefresh();
}