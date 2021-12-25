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
    public static ServerSocket serverSocket;

    static {
        try {
            serverSocket = new ServerSocket(port, 50, InetAddress.getByName(address));
            System.out.println("Server started!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        handleNewComingSessions();
    }

    static void handleNewComingSessions(){
        //initiating a pool of threads to handle requests in parallel.
        ExecutorService executorService = Executors
                .newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        while (true) {
            //closing the server for the Exit request
            if (!serverRunning) {
                executorService.shutdown();
                break;
            }

            try {
                Socket socket = serverSocket.accept();
                executorService.submit(new CommandsHandlerSession(socket));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}