package client;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.gson.Gson;
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

    static final String address = "127.0.0.1";
    static final int port = 23456;
    private static final String FOLDER_PATH  = System.getProperty("user.dir") + "\\src\\client\\data\\";
    static Gson gson = new Gson();


    public static void main(String[] args) throws IOException {
        JCommander.newBuilder()
                .addObject(new Main())
                .build()
                .parse(args);
        if (fileName != null) { //take request parameters from the file
            byte[] requestInputFileBytes = Files.readAllBytes(Paths.get(FOLDER_PATH + fileName));
            String requestInputFileString = new String(requestInputFileBytes);
            HashMap<String, String> requestInputFileMap = gson.fromJson(requestInputFileString, HashMap.class);
            type = requestInputFileMap.get("type");
            key = requestInputFileMap.get("key");
            if (requestInputFileMap.containsKey("value")) {
                valueArrayList = List.of(requestInputFileMap.get("value").split("\\s+"));
            }
        }

        Socket socket = new Socket(InetAddress.getByName(address), port);
        System.out.println("Client started!");
        DataInputStream input = new DataInputStream(socket.getInputStream());
        DataOutputStream output = new DataOutputStream(socket.getOutputStream());

        String messageToSend;
        Map<String, String> messageToSendMap = new HashMap<>();
        messageToSendMap.put("type", type);
        messageToSendMap.put("key", key);

        if (type.equals("set")) {
            String content = String.join(" ", valueArrayList);
            messageToSendMap.put("value", content);
        }

        messageToSend = gson.toJson(messageToSendMap);
        output.writeUTF(messageToSend);
        System.out.println("Sent: " + messageToSend);

        String messageReceived = input.readUTF();
        System.out.println("Received: " + messageReceived);

    }
}
