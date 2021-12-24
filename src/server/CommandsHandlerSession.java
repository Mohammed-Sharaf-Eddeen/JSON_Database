package server;

import com.google.gson.Gson;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;


public class CommandsHandlerSession implements Runnable {
    Socket socket;
    static final Gson gson = new Gson();
    //static Map<String, String> JSONMapDatabase = new HashMap<>();


    public CommandsHandlerSession(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        String messageReceived;
        try (DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output  = new DataOutputStream(socket.getOutputStream())) {
            messageReceived = input.readUTF();
            System.out.println("Received: " + messageReceived);

            HashMap<String, String> messageToSend = executeCommand(messageReceived);
            if (messageToSend.get("response").equals("EXIT")) {
                messageToSend.put("response", "OK");
                output.writeUTF(gson.toJson(messageToSend));
                System.out.println("Sent: " + gson.toJson(messageToSend));
                Main.serverRunning = false;
                Main.server.close();

            } else {
                output.writeUTF(gson.toJson(messageToSend));
                System.out.println("Sent: " + gson.toJson(messageToSend));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static HashMap<String, String> executeCommand(String JSONRequest) {
        HashMap<String, String> request = gson.fromJson(JSONRequest, HashMap.class);
        HashMap<String, String> result = new HashMap<>();


        String command = request.get("type");
        String key = request.get("key");
        if (command.equals("exit")) {
            result.put("response", "EXIT");
            return result;
        }

        switch (command) {
            case "set":
                String newValue = request.get("value");
                //JSONMapDatabase.put(key, newValue);
                JSONDatabase.set(key, newValue);
                result.put("response", "OK");
                break;
            case "get":
                //String value = JSONMapDatabase.getOrDefault(key, "ERROR");
                String value = JSONDatabase.get(key);
                if (value.equals("ERROR")) {
                    result.put("response", "ERROR");
                    result.put("reason", "No such key");
                } else {
                    result.put("response", "OK");
                    result.put("value", value);
                }
                break;
            case "delete":
                if (JSONDatabase.delete(key) == 1) {
                    //JSONMapDatabase.remove(key);
                    result.put("response", "OK");
                } else {
                    result.put("response", "ERROR");
                    result.put("reason", "No such key");
                }
                break;
        }
        return result;
    }

}
