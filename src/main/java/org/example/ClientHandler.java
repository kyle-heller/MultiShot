package org.example;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper; // Jackson's JSON processor


public class ClientHandler implements Runnable{

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;
    private boolean isInitialized = false;

    public ClientHandler(Socket socket){
        try{
            this.socket=socket;
            this.bufferedWriter=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUsername=bufferedReader.readLine();
            clientHandlers.add(this);
        }catch(IOException e){
            closeAll(socket,bufferedReader,bufferedWriter);

        }
    }

    public void finishInitialization() {
        // Logic to send join notification
        Message joinMessage = new Message("SYSTEM", "ALL", "Server: " + this.clientUsername + " has entered the chat!", true);
        broadcastMSG(joinMessage);
        this.isInitialized = true;
    }

    public boolean isInitialized() {
        return isInitialized;
    }


    @Override
    public void run() {
        ObjectMapper mapper = new ObjectMapper();

        while (socket.isConnected()) {
            try {
                String messageFromClient = bufferedReader.readLine();
                Message message = mapper.readValue(messageFromClient, Message.class);

                // Check if the message is a regular group message and not from the system
                if (message.isGroupMessage() && !"SYSTEM".equals(message.getSenderId())) {
                    broadcastMSG(message); // This is a regular group chat message; broadcast it directly.
                }
                else if ("SYSTEM".equals(message.getSenderId())) {
                    // Handle system messages, like notifying clients of a user's connection/disconnection
                    // Depending on your implementation, you might create and send a specific system message here
                    broadcastSystemMessage(message); // Assuming you have a method to handle system-specific messages
                }
                else {
                    // Assuming non-group messages are direct messages
                    directMSG(message); // Handle direct messages
                }
            } catch(IOException e) {
                closeAll(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }


    public void broadcastMSG(Message message) {
        ObjectMapper mapper = new ObjectMapper();
        String jsonMessage;
        try {
            jsonMessage = mapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return; // Exit the method if JSON serialization fails
        }

        for (ClientHandler clientHandler : clientHandlers) {
            try {
                // Exclude the sender from receiving the message
                // This works for both user messages and system messages where the sender is SYSTEM
                if (!clientHandler.getClientUsername().equals(message.getSenderId())) {
                    clientHandler.bufferedWriter.write(jsonMessage);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeAll(clientHandler.socket, clientHandler.bufferedReader, clientHandler.bufferedWriter);
            }
        }
    }

    public void broadcastSystemMessage(Message systemMessage) {
        // Convert the Message object to JSON
        ObjectMapper mapper = new ObjectMapper();
        String jsonMessage;
        try {
            jsonMessage = mapper.writeValueAsString(systemMessage);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return; // Exit the method if JSON serialization fails
        }

        // Iterate over all connected client handlers and send the message
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                // Check if the client's BufferedWriter is not null
                if (clientHandler.bufferedWriter != null) {
                    clientHandler.bufferedWriter.write(jsonMessage);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                // Attempt to close resources if sending the message fails
                closeAll(clientHandler.socket, clientHandler.bufferedReader, clientHandler.bufferedWriter);
            }
        }
    }


    public void directMSG(Message message) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (clientHandler.getClientUsername().equals(message.getRecipientId())) { // Match the recipient
                    ObjectMapper mapper = new ObjectMapper();
                    String jsonMessage = mapper.writeValueAsString(message); // Convert the Message object to JSON

                    clientHandler.bufferedWriter.write(jsonMessage);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                    break; // Stop once the intended recipient is found and messaged
                }
            } catch (IOException e) {
                closeAll(clientHandler.socket, clientHandler.bufferedReader, clientHandler.bufferedWriter);
            }
        }
    }

    public Message createSystemMessage(String content) {

        return new Message("SYSTEM", "ALL", content, true);
    }

    public void removeCliHandler(){
        clientHandlers.remove(this);
        broadcastMSG(createSystemMessage(clientUsername + " left the room."));
    }

    public void closeAll(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        removeCliHandler();
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

    public static List<String> getAllUsernames() {
        List<String> usernames = new ArrayList<>();
        for (ClientHandler clientHandler : ClientHandler.clientHandlers) {
            usernames.add(clientHandler.getClientUsername());
        }
        return usernames;
    }

    public java.util.Enumeration<String> getAllUsersRequest() {
        return java.util.Collections.enumeration(getAllUsernames());
    }

    public String getClientUsername() {
        return clientUsername;
    }
}
