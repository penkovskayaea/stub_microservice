package com.example.stub.config;

public class ResponseConfig {
    private Integer status;
    private String body;
    private Integer randomSize;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Integer getRandomSize() {
        return randomSize;
    }

    public void setRandomSize(Integer randomSize) {
        this.randomSize = randomSize;
    }
}