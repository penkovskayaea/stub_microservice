package com.example.stub.config;

public class Rule {
    private String path;
    private String method;
    private ProxyConfig proxy;
    private ResponseConfig response;
    private Long delay;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public ProxyConfig getProxy() {
        return proxy;
    }

    public void setProxy(ProxyConfig proxy) {
        this.proxy = proxy;
    }

    public ResponseConfig getResponse() {
        return response;
    }

    public void setResponse(ResponseConfig response) {
        this.response = response;
    }

    public Long getDelay() {
        return delay;
    }

    public void setDelay(Long delay) {
        this.delay = delay;
    }
}