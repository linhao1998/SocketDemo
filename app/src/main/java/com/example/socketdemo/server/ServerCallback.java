package com.example.socketdemo.server;

public interface ServerCallback {
    //接收客户端的消息
    public void receiveClientMsg(Boolean success,String msg);
    //其他消息
    public void otherMsg(String msg);
}
