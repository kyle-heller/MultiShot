package org.example;

import java.io.*;
import java.net.*;
public class Server {


    private Socket socket;
    private ServerSocket server;
    private DataInputStream in;

    public Server(int port) {
        try {
            server = new ServerSocket(port);
            System.out.println("Server started");

            System.out.println("Waiting for client");

            socket = server.accept();
            System.out.println("Client accepted");

            in = new DataInputStream(
                    new BufferedInputStream(socket.getInputStream()));

            String line = "";

            while(!line.equals("Stop")) {
                try {
                    line = in.readUTF();
                    System.out.println(line);
                }
                catch(IOException i) {
                    System.out.println(i);
                }
            }
            System.out.println("Closing connection");
            socket.close();
            in.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
    }

}
