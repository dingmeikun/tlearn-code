package com.dingmk.consistency.service;

import com.dingmk.consistency.request.Request;

/**
 * 请求异步执行的service
 *
 * 请求路由处理
 */
public interface RequestAsyncProcessService {
    void process(Request request);
}
