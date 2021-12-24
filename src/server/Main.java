package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    static final String address = "127.0.0.1";
    static final int port = 23456;
    public static volatile boolean serverRunning = true;
    public static ServerSocket server;

    static {
        try {
            server = new ServerSocket(port, 50, InetAddress.getByName(address));
            System.out.println("Server started!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        handleNewSessions();
    }

    static void handleNewSessions(){
        ExecutorService executorService = Executors
                .newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        while (true) {
            if (!serverRunning) {
                executorService.shutdown();
                break;
            }

            try {
                Socket socket = server.accept();
                executorService.submit(new CommandsHandlerSession(socket));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}