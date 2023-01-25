package com.example.socketdemo.server;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class SocketServer {

    private static final String TAG = "SocketServer";

    public static final int PORT = 9527;

    private Socket socket = null;

    private ServerSocket serverSocket = null;

    private OutputStream outputStream = null;

    private ServerCallback mCallback;

    /**
     * 开启服务
     */
    public void startServer(ServerCallback callback){
        mCallback = callback;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket(PORT);
                    while (true) {
                        socket = serverSocket.accept();
                        mCallback.otherMsg(socket.getInetAddress() + " to connected");
                        if (socket != null) {
                            ServerThread serverThread = new ServerThread(socket,mCallback);
                            new Thread(serverThread).start();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 关闭服务
     */
    public void stopServer() {
        try {
            if (outputStream != null) {
                outputStream.close();
            }
            if (socket != null) {
                socket.shutdownInput();
                socket.shutdownOutput();
                socket.close();
            }
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送到客户端
     */
    public void sendToClient(String msg) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (socket.isClosed()) {
                    Log.e(TAG,"sendToClient: Socket is closed");
                    return;
                }
                try {
                    outputStream = socket.getOutputStream();
                    outputStream.write(msg.getBytes(StandardCharsets.UTF_8));
                    outputStream.flush();
                    mCallback.otherMsg("toClient: " + msg);
                    Log.d(TAG,"发送到客户端成功");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG,"向客户端发送消息失败");
                }
            }
        }).start();
    }

    class ServerThread implements Runnable{

        private Socket socket;

        private ServerCallback callback;

        public ServerThread(Socket socket, ServerCallback callback) {
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
                while((len = inputStream.read(buffer))!=-1){
                    receiveStr += new String(buffer,0,len,StandardCharsets.UTF_8);
                    if (len < 1024) {
                        callback.receiveClientMsg(true,receiveStr);
                        receiveStr = "";
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                callback.receiveClientMsg(false,"");
            }
        }
    }
}
