package org.example;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable{

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;

    public ClientHandler(Socket socket){
        try{
            this.socket=socket;
            this.bufferedWriter=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUsername=bufferedReader.readLine();
            clientHandlers.add(this);
            System.out.println(clientHandlers);
            broadcastMSG("Server: "+clientUsername+" has entered the chat!");

        }catch(IOException e){
            closeAll(socket,bufferedReader,bufferedWriter);

        }
    }


    @Override
    public void run() {
        String messageFromClient;
        while(socket.isConnected()){
            try {
                messageFromClient = bufferedReader.readLine();
                // Assuming the format is "username: command/message"
                String[] parts = messageFromClient.split(": ", 2); // Split into at most 2 parts, username and the rest
                if (parts.length > 1) { // Make sure there is a command/message part
                    String command = parts[1]; // This is the actual command or message
                    if ("GET_ALL_USERS".equalsIgnoreCase(command)) {
                        String allUsernames = String.join(", ", getAllUsernames());
                        bufferedWriter.write("Connected users: " + allUsernames);
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                    } else {
                        // Handle regular messages
                        broadcastMSG(messageFromClient);
                    }
                } else {
                    // Handle cases where the message does not follow the expected format
                    broadcastMSG(messageFromClient);
                }
            } catch(IOException e) {
                closeAll(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    public void broadcastMSG(String messageToSend){
        for(ClientHandler clientHandler: clientHandlers){
            try{
                if(!clientHandler.clientUsername.equals(clientUsername)){
                    clientHandler.bufferedWriter.write(messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            }catch(IOException e){
                closeAll(socket, bufferedReader,bufferedWriter);
            }
        }
    }
    public void removeCliHandler(){
        clientHandlers.remove(this);
        broadcastMSG("Server: "+clientUsername+" left the room.");
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
        System.out.println(usernames);
        return usernames;
    }

    public java.util.Enumeration<String> getAllUsersRequest() {
        return java.util.Collections.enumeration(getAllUsernames());
    }

    public String getClientUsername() {
        return clientUsername;
    }
}
