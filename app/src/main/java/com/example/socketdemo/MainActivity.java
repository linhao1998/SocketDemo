package com.example.socketdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.socketdemo.client.ClientCallback;
import com.example.socketdemo.client.SocketClient;
import com.example.socketdemo.databinding.ActivityMainBinding;
import com.example.socketdemo.server.ServerCallback;
import com.example.socketdemo.server.SocketServer;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ServerCallback, ClientCallback {

    private Boolean isServer = true;

    private Boolean openSocket = false;

    private Boolean connectSocket = false;

    private ActivityMainBinding binding;

    private SocketServer socketServer = null;

    private SocketClient socketClient = null;

    private StringBuilder sb;

    private List<Message> messageList = new ArrayList<>();

    private MsgAdapter msgAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initView();
    }

    private void initView() {

        socketServer = new SocketServer();

        socketClient = new SocketClient();

        sb = new StringBuilder();

        binding.tvIpAddress.setText("Ip地址:" + getIp());

        binding.rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_server) {
                    isServer = true;
                } else {
                    isServer = false;
                }
                binding.layServer.setVisibility(isServer?View.VISIBLE:View.GONE);
                binding.layClient.setVisibility(isServer?View.GONE:View.VISIBLE);
                binding.etMsg.setHint(isServer?"发送给客户端":"发送给服务端");
            }
        });

        binding.btnStartService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (openSocket) {
                    socketServer.stopServer();
                    openSocket = false;
                } else {
                   socketServer.startServer(MainActivity.this);
                   openSocket = true;
                }
                showMsg(openSocket?"开启服务":"关闭服务");
                binding.btnStartService.setText(openSocket?"关闭服务":"开启服务");
            }
        });

        binding.btnConnectService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip = binding.etIpAddress.getText().toString();
                if (ip.isEmpty()) {
                    showMsg("请输入IP地址");
                    return;
                }
                if (connectSocket) {
                    socketClient.closeConnect();
                    connectSocket = false;
                } else {
                    socketClient.connectServer(ip,MainActivity.this);
                    connectSocket = true;
                }
                showMsg(connectSocket?"连接服务":"关闭连接");
                binding.btnConnectService.setText(connectSocket?"关闭连接":"连接服务");
            }
        });

        binding.btnSendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = binding.etMsg.getText().toString();
                if (msg.isEmpty()) {
                    showMsg("请输入要发送的消息");
                    return;
                }
                Boolean isSend = false;
                if (openSocket) {
                    isSend = true;
                } else if (connectSocket) {
                    isSend = true;
                }
                if (!isSend) {
                    showMsg("当前未开启服务或连接服务");
                    return;
                }
                if (isServer) {
                    socketServer.sendToClient(msg);
                } else {
                    socketClient.sendToServer(msg);
                }
                binding.etMsg.setText("");
                updateList(isServer?1:0,msg);
            }
        });

        msgAdapter = new MsgAdapter(messageList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.rvMsg.setLayoutManager(layoutManager);
        binding.rvMsg.setAdapter(msgAdapter);
    }

    @SuppressWarnings("deprecation")
    private String getIp() {
        WifiManager wm = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
        int ipAddressInt = wm.getConnectionInfo().getIpAddress();
        String ip = Formatter.formatIpAddress(ipAddressInt);
        return ip;
    }

    @Override
    public void receiveServerMsg(String msg) {
        updateList(1,msg);
    }

    @Override
    public void receiveClientMsg(Boolean success, String msg) {
        updateList(0,msg);
    }

    @Override
    public void otherMsg(String msg) {
        Log.d("MainActivity",msg);
//        showMsg(msg);
    }

//    private void showInfo(String info) {
//        sb.append(info).append("\n");
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                binding.tvInfo.setText(sb.toString());
//            }
//        });
//    }

    private void showMsg(String msg) {
        Toast.makeText(MainActivity.this,msg,Toast.LENGTH_SHORT).show();
    }

    private void updateList(int type,String msg) {
        messageList.add(new Message(type,msg));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int position = messageList.size() == 0 ? 0 : messageList.size() - 1;
                msgAdapter.notifyItemInserted(position);
                binding.rvMsg.smoothScrollToPosition(position);
            }
        });
    }
}