package com.zihyan.adbshellcommand;

import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SocketClient {
    private String TAG = "SocketClient";
    private String HOST = "127.0.0.1";
    PrintWriter printWriter;
    onServiceSend mOnServiceSend;
    String cmd;
    BufferedReader bufferedReader;
    int PORT = 4521;
    int TIMEOUT = 3000;

    public SocketClient(String command,onServiceSend onServiceSend){
        cmd = command;
        mOnServiceSend = onServiceSend;

        try{
            Log.d(TAG,"Addr:" + HOST + ":" + PORT);

            // Create Socket
            Socket socket = new Socket();

            // Socket Connect
            socket.connect(new InetSocketAddress(HOST,PORT),TIMEOUT);

            // Set TimeOut 3s
            socket.setSoTimeout(TIMEOUT);
            Log.d(TAG,"TimeOut:" + TIMEOUT);

            // Create PrintWriter
            printWriter = new PrintWriter(socket.getOutputStream(),true);

            // Create BufferedReader
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            new CreateServerThread(socket);
            send(cmd);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG,"Error Exception:" + e.toString());
            mOnServiceSend.getSend("#ShellRunError:" + e.toString());
        }
    }

    class CreateServerThread extends Thread{
        Socket socket;
        InputStreamReader inputStreamReader;
        BufferedReader bufferedReader;

        public CreateServerThread(Socket s) throws IOException{
            Log.d(TAG,"Create Socket");
            socket = s;
            start();
        }

        public void run(){
            try{
                inputStreamReader = new InputStreamReader(socket.getInputStream());
                bufferedReader = new BufferedReader(inputStreamReader);
                String line;

                while((line = bufferedReader.readLine()) != null){
                    mOnServiceSend.getSend(line);
                }
                Log.d(TAG,"Client Done");
            }catch (Exception e){
                e.printStackTrace();
                Log.d(TAG,"Error Exception:" + e.toString());
            }
            finally {
                printWriter.close();

                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void send(String cmd){
        printWriter.println(cmd );
        printWriter.flush();
    }

    public interface onServiceSend{
        void getSend(String result);
    }
}
