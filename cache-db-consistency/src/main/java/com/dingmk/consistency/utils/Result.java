package com.dingmk.consistency.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {
    private int code;
    private String msg;
    private T data;

    public static Result<String> success() {
        return new Result<>(0, "success", null);
    }

    public static Result<String> error() {
        return new Result<>(500, "error", null);
    }
}