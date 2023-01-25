package com.example.socketdemo.client;

public interface ClientCallback {
    //接收服务端的消息
    public void receiveServerMsg(String msg);
    //其他消息
    public void otherMsg(String msg);
}
