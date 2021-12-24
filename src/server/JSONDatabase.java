package server;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class JSONDatabase {
    private static final String DATABASE_PATH  = System.getProperty("user.dir") + "\\src\\server\\data\\db.json";
    private static final Gson gson = new Gson();
    static HashMap<String, String> database;
    private static final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private static Lock readLock = readWriteLock.readLock();
    private static Lock writeLock = readWriteLock.writeLock();


    public static void readDatabase() {
        readLock.lock();
        try {
            byte[] databaseBytes = Files.readAllBytes(Path.of(DATABASE_PATH));
            String databaseString = new String(databaseBytes);
            database = gson.fromJson(databaseString, HashMap.class);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            readLock.unlock();
        }
    }

    public static void writeToDatabase() {
        writeLock.lock();
        String databaseJsonString = gson.toJson(database);
        try (FileWriter writer = new FileWriter(DATABASE_PATH)) {
            writer.write(databaseJsonString);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            writeLock.unlock();
        }
    }

    public static void set(String key, String newValue) {
        readDatabase();
        database.put(key, newValue);
        writeToDatabase();
    }

    public static String get(String key) {
        readDatabase();
        System.out.println(database);
        if (database.containsKey(key)) {
            String value = database.get(key);
            return gson.fromJson(value, String.class);
        } else {
            return "ERROR";
        }
    }

    public static int delete(String key) {
        readDatabase();
        if (database.containsKey(key)) {
            database.remove(key);
            writeToDatabase();
            return 1;
        } else {
            return -1;
        }
    }


}
