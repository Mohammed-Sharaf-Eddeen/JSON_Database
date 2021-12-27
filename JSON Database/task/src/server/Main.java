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
    private static volatile boolean serverRunning = false;
    private static ServerSocket serverSocket;
    private static ExecutorService executorService;

    static {
        try {
            serverSocket = new ServerSocket(port, 50, InetAddress.getByName(address));
            serverRunning = true;
            //initiating a pool of threads to handle requests in parallel.
            executorService = Executors
                    .newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            System.out.println("Server started!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        handleNewComingSessions();
    }

    static void handleNewComingSessions(){

        while (serverRunning) {
            try {
                Socket socket = serverSocket.accept();
                executorService.submit(new CommandsHandlerSession(socket));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void close() {
        try {
            serverRunning = false;
                /*
                The serverSocket has to be closed because it is blocking his thread
                It is blocked on server.accept() call and the recommended way to pass
                the blocking call is to stop the object calling it
                This can't happen from the same thread as it is already blocked on it.
                That's why another thread should do it.
                */
            serverSocket.close();
            executorService.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}