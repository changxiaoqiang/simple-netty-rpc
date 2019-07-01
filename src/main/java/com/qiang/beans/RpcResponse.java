package com.qiang.beans;

import java.io.Serializable;

public class RpcResponse implements Serializable {
    private static final long serialVersionUID = -3535478031115936695L;

    private String responseId;
    private Object result;
    private Throwable throwable;

    public String getResponseId() {
        return responseId;
    }

    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
}
