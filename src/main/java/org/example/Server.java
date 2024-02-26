package org.example;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.net.*;
import java.util.Hashtable;

public class Server {


    private Socket socket;
    private ServerSocket server;
    private DataInputStream in;

    public Server(int port) {
        try {
            server = new ServerSocket(port);
            System.out.println("Server started");

            System.out.println("Waiting for clients");

            String line = "";

            while(!server.isClosed()) {
                try {

                    socket = server.accept();
                    System.out.println("Client accepted");

                    ClientHandler clientHandler = new ClientHandler(socket);

                    Thread thread = new Thread(clientHandler);
                    thread.start();

                    in = new DataInputStream(
                            new BufferedInputStream(socket.getInputStream()));

                }
                catch(IOException i) {
//                    System.out.println(i);
                }
            }
            System.out.println("Closing connection");
            socket.close();
            in.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void closeServerSocket() {
        try {
            if (server != null) {
                server.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) {
    }

}
