package com.dingmk.consistency.service;

public interface RedisService {
    void set(String key, String value);

    String get(String key);

    void del(String key);

    void expire(String key, int timeout);
}
