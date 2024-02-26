package org.example;

import com.fasterxml.jackson.databind.ObjectMapper; // Jackson's JSON processor

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;

    public Client(Socket socket, String username) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = username;

            // Immediately send the username to the server
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch(IOException e) {
            closeAll(socket, bufferedReader, bufferedWriter);
        }
    }

    public void sendMessage() {
        try {
            ObjectMapper mapper = new ObjectMapper(); // Create an ObjectMapper instance for JSON serialization
            Scanner scanner = new Scanner(System.in); // Scanner for reading user input

            System.out.println("Enter the recipient's username (type 'everyone' for group messages):");
            String recipient = scanner.nextLine(); // Read the recipient's username

            while (socket.isConnected()) {
                String text = scanner.nextLine(); // Read the message text

                // Construct a Message object
                Message message = new Message();
                message.setSenderId(this.username); // Set the sender ID to this client's username
                message.setRecipientId(recipient.equals("everyone") ? "ALL" : recipient); // Set recipient; use "ALL" for group messages
                message.setMessage(text); // Set the actual message text
                message.setGroupMessage(recipient.equals("everyone")); // Determine if it's a group message based on the recipient input

                String jsonMessage = mapper.writeValueAsString(message); // Serialize the Message object to JSON

                System.out.println("Sending Message: " + jsonMessage);

                bufferedWriter.write(jsonMessage); // Write the JSON string to the buffered writer
                bufferedWriter.newLine(); // Write a newline to indicate the end of the message
                bufferedWriter.flush(); // Flush the stream to ensure the message is sent
            }
        } catch (IOException e) {
            closeAll(socket, bufferedReader, bufferedWriter); // Close all connections on exception
        }
    }

    public void listenForMessage(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String msgFromGChat;
                while(socket.isConnected()){
                    try{
                        msgFromGChat=bufferedReader.readLine();
                        System.out.println(msgFromGChat);
                    }catch(IOException e){
                        closeAll(socket,bufferedReader,bufferedWriter);
                    }
                }
            }
        }).start();
    }

    public void closeAll(Socket socket, BufferedReader bufferedReader,BufferedWriter bufferedWriter){
        try{
            if(bufferedReader !=null){
                bufferedReader.close();
            }
            if(bufferedWriter !=null){
                bufferedWriter.close();
            }
            if(socket != null){
                socket.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Define your username: ");
        String username= scanner.nextLine();
        Socket socket = new Socket("127.0.0.1",443);
        Client client = new Client(socket, username);
        client.listenForMessage();
        client.sendMessage();
    }
}
//20.119.8.58 Azure endpoint