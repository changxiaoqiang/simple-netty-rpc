package com.qiang.rpc.exception;

public class RequestNotSupportExistException extends Exception {
    private String requestPath;
    private Throwable e;

    public RequestNotSupportExistException(String requestPath) {
        this.requestPath = requestPath;
    }

    public RequestNotSupportExistException(String requestPath, Throwable e) {
        this.requestPath = requestPath;
        this.e = e;
    }

    public Throwable getE() {
        return e;
    }

    public void setE(Throwable e) {
        this.e = e;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }
}
