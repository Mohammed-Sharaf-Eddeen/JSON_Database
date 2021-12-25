package client;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.gson.*;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


public class Main {
    @Parameter(names = "-t", description = "type of the request")
    static String type;
    @Parameter(names = "-k", description = "key of the cell")
    static String key;
    @Parameter(names = "-v",
            description = "value to save in the database: you only need it in case of a set request",
            variableArity = true)
    static List<String> valueArrayList = new ArrayList<>();
    @Parameter(names = "-in", description = "request from an input file")
    static String fileName;
    //variables to be initialized at the case of input file instead of key and valueArrayList.
    static JsonArray keyJsonArray;
    static JsonElement valueJsonElement;

    static final String address = "127.0.0.1";
    static final int port = 23456;
    private static final String FOLDER_PATH  = System.getProperty("user.dir") + "\\src\\client\\data\\";
    private static final Gson gson = new Gson();


    public static void main(String[] args) throws IOException {
        JCommander.newBuilder()
                .addObject(new Main())
                .build()
                .parse(args);
        if (fileName != null) { //take request parameters from the file
            byte[] requestInputFileBytes = Files.readAllBytes(Paths.get(FOLDER_PATH + fileName));
            String requestInputFileString = new String(requestInputFileBytes);
            JsonObject requestInput = JsonParser.parseString(requestInputFileString).getAsJsonObject();
            type = gson.fromJson(requestInput.get("type"), String.class);
            if (requestInput.get("key").isJsonPrimitive()){
                key = gson.fromJson(requestInput.get("key"), String.class);
            } else {
                key = null;
                keyJsonArray = requestInput.get("key").getAsJsonArray();
            }
            if (requestInput.has("value")) {
                valueJsonElement = requestInput.get("value");
                valueArrayList = null;
            }
        }

        //Starting Connection
        Socket socket = new Socket(InetAddress.getByName(address), port);
        System.out.println("Client started!");
        DataInputStream input = new DataInputStream(socket.getInputStream());
        DataOutputStream output = new DataOutputStream(socket.getOutputStream());

        String messageToSend;
        JsonObject messageToSendJSON = new JsonObject();
        messageToSendJSON.addProperty("type", type);

        //choosing between key and keyJsonArray
        if (key == null) {
            messageToSendJSON.add("key", keyJsonArray);
        } else {
            messageToSendJSON.addProperty("key", key);
        }

        if (type.equals("set")) {
            //choosing between key and keyJsonArray
            if (valueArrayList != null) {
                String content = String.join(" ", valueArrayList);
                messageToSendJSON.addProperty("value", content);
            } else {
                messageToSendJSON.add("value", valueJsonElement);
            }
        }

        messageToSend = gson.toJson(messageToSendJSON);
        output.writeUTF(messageToSend);
        System.out.println("Sent: " + messageToSend);

        String messageReceived = input.readUTF();
        System.out.println("Received: " + messageReceived);

    }
}
