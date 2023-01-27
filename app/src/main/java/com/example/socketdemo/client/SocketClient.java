package com.example.socketdemo.client;

import android.util.Log;

import com.example.socketdemo.server.SocketServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class SocketClient {

    private static final String TAG = "SocketClient";

    private Socket socket = null;

    private ClientCallback mCallback;

    private OutputStream outputStream = null;

    private InputStreamReader inputStreamReader = null;

    /**
     * 连接服务
     */
    public void connectServer(String ipAddress, ClientCallback callback){
        mCallback = callback;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket(ipAddress, SocketServer.PORT);
                    if (socket != null) {
                        ClientThread clientThread = new ClientThread(socket,mCallback);
                        new Thread(clientThread).start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 关闭连接
     */
    public void closeConnect() {
        try {
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            if (socket != null) {
                socket.shutdownInput();
                socket.shutdownOutput();
                socket.close();
                Log.d(TAG,"关闭连接");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送数据至服务器
     */
    public void sendToServer(String msg) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (socket.isClosed()) {
                    Log.d(TAG,"sendToServer: Socket is closed");
                    return;
                }
                try {
                    outputStream = socket.getOutputStream();
                    outputStream.write(msg.getBytes(StandardCharsets.UTF_8));
                    outputStream.flush();
                    mCallback.otherMsg("toServer: " + msg);
                    Log.d(TAG,"发送到服务端成功");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG,"向服务端发送消息失败");
                }
            }
        }).start();
    }

    class ClientThread implements Runnable{

        private Socket socket;

        private ClientCallback callback;

        public ClientThread(Socket socket, ClientCallback callback) {
            this.socket = socket;
            this.callback = callback;
        }

        @Override
        public void run() {
            InputStream inputStream = null;
            try {
                inputStream = socket.getInputStream();
                byte buffer[] = new byte[1024];
                int len = 0;
                String receiveStr = "";
                // 在发送端不发送数据时，接收端会阻塞在read处
                while((len = inputStream.read(buffer))!=-1){
                    receiveStr += new String(buffer,0,len,StandardCharsets.UTF_8);
                    //当接收一次消息时，如果len恰好等于1024，当前消息不显示(bug)
                    if (len < 1024) {
                        callback.receiveServerMsg(receiveStr);
                        receiveStr = "";
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
