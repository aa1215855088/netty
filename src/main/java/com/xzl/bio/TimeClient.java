package com.xzl.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TimeClient {
    public static void main(String[] args) throws IOException, InterruptedException {
        for (int i = 0; i < 10000; i++) {

            int port = 8080;

            Socket socket = new Socket("127.0.0.1", port);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);

            printWriter.println("hello world");

            System.out.println("get server message:" + bufferedReader.readLine());

            bufferedReader.close();
            printWriter.close();
            socket.close();

        }
    }
}
