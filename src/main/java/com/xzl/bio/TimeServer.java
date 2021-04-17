package com.xzl.bio;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;

public class TimeServer {

    public static void main(String[] args) {
        int port = 8080;

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(() -> {
                    BufferedReader bufferedReader = null;
                    PrintWriter printWriter = null;
                    try {
                        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        printWriter = new PrintWriter(socket.getOutputStream(), true);
                        System.out.println("get client message:" + bufferedReader.readLine());
                        printWriter.println(LocalDateTime.now().toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            assert bufferedReader != null;
                            bufferedReader.close();
                            assert printWriter != null;
                            printWriter.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                assert serverSocket != null;
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
