package server;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;



public class CommandsHandlerSession implements Runnable {
    Socket socket;
    static final Gson gson = new Gson();

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

            JsonObject messageToSend = executeCommand(messageReceived);
            //handling the special case of the exit request
            if (messageToSend.get("response").getAsString().equals("EXIT")) {
                messageToSend.addProperty("response", "OK");
                output.writeUTF(gson.toJson(messageToSend));
                System.out.println("Sent: " + gson.toJson(messageToSend));
                Main.close();

            } else {
                output.writeUTF(gson.toJson(messageToSend));
                System.out.println("Sent: " + gson.toJson(messageToSend));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* This method parses the request and delegates it to the JSONDatabase where the database
    and the implementation for methods are. Then, it creates the result message to be sent for
    the client responsible for making the request. */
    static JsonObject executeCommand(String JSONRequest) {
        JsonObject request = JsonParser.parseString(JSONRequest).getAsJsonObject();
        JsonObject result = new JsonObject();

        String command = request.get("type").getAsString();
        JsonElement key = request.get("key");
        //handling the special case of exit
        if (command.equals("exit")) {
            result.addProperty("response", "EXIT");
            return result;
        }

        switch (command) {
            case "set":
                JsonElement newValue = request.get("value");
                JSONDatabase.set(key, newValue);
                result.addProperty("response", "OK");
                break;
            case "get":
                JsonElement value = JSONDatabase.get(key);
                if (value == null) {
                    result.addProperty("response", "ERROR");
                    result.addProperty("reason", "No such key");
                } else {
                    result.addProperty("response", "OK");
                    result.add("value", value);
                }
                break;
            case "delete":
                if (JSONDatabase.delete(key)) {
                    result.addProperty("response", "OK");
                } else {
                    result.addProperty("response", "ERROR");
                    result.addProperty("reason", "No such key");
                }
                break;
        }
        return result;
    }

}
