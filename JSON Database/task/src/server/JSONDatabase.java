package server;

import com.google.gson.*;


import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class JSONDatabase {
    private static final String DATABASE_PATH  = System.getProperty("user.dir") + "\\src\\server\\data\\db.json";
    private static final Gson gson = new Gson();
    private static JsonObject database;
    private static final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private static final Lock readLock = readWriteLock.readLock();
    private static final Lock writeLock = readWriteLock.writeLock();


    //read the database from the file
    private static void readDatabase() {
        readLock.lock();
        try {
            byte[] databaseBytes = Files.readAllBytes(Path.of(DATABASE_PATH));
            String databaseString = new String(databaseBytes);
            database = JsonParser.parseString(databaseString).getAsJsonObject();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            readLock.unlock();
        }
    }

    //write to the database file
    private static void writeToDatabase() {
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

    //set function calls setForKeyArray inside it.
    public static void set(JsonElement key, JsonElement newValue) {
        writeLock.lock();
        readDatabase();
        if (key.isJsonPrimitive()){
            String stringKey = key.getAsString();
            database.add(stringKey, newValue);
        } else {
            setForKeyArray(key.getAsJsonArray(), database, newValue);
        }
        writeToDatabase();
        writeLock.unlock();
    }

    private static void setForKeyArray(JsonArray request, JsonObject database, JsonElement value) {
        String key = request.get(0).getAsString();
        request.remove(0);

        JsonElement element = database.get(key);
        if (element == null && request.size() == 0) {
            database.add(key, value);
        } else if (element != null && request.size() == 0) {
            database.remove(key);
            database.add(key, value);
        } else if (element == null && request.size() != 0) {
            JsonObject newExtendedObject = new JsonObject();
            database.add(key, newExtendedObject);
            setForKeyArray(request, newExtendedObject, value);
        } else if (element != null && request.size() != 0){
            setForKeyArray(request, database.getAsJsonObject(key), value);
        }
    }

    //get function calls getForKeyArray inside it.
    public static JsonElement get(JsonElement key) {
        readDatabase();
        if (key.isJsonPrimitive()) {
            String keyString = key.getAsString();
            if (database.has(keyString)) {
                return database.get(keyString);
            } else {
                return null;
            }
        } else {
            return getForKeyArray(key.getAsJsonArray(), database);
        }
    }

    private static JsonElement getForKeyArray(JsonArray request, JsonObject database) {
        String key = request.get(0).getAsString();
        request.remove(0);

        if (database.has(key)) {
            JsonElement element = database.get(key);
            if (request.size() == 0) {
                return database.get(key);
            } else if (element.isJsonPrimitive()) {
                return null;
            }
            else {
                return getForKeyArray(request, (JsonObject) element);
            }
        }
        return null;
    }

    //delete function calls deleteWithoutLock which calls deleteWithoutLockForKeyArray inside it
    //delete function is implemented to add the writeLock functionality easily
    public static boolean delete(JsonElement key) {
        writeLock.lock();
        readDatabase();
        boolean deleted = deleteWithoutLock(key);
        writeToDatabase();
        writeLock.unlock();
        return deleted;
    }

    private static boolean deleteWithoutLock(JsonElement key) {
        if (key.isJsonPrimitive()) {
            String stringKey = key.getAsString();

            if (database.has(stringKey)) {
                database.remove(stringKey);
                return true;
            }

        } else {
            return deleteWithoutLockForKeyArray(key.getAsJsonArray(), database);
        }

        return false;
    }

    private static boolean deleteWithoutLockForKeyArray(JsonArray request, JsonObject database) {
        String key = request.get(0).getAsString();
        request.remove(0);

        if (database.has(key)) {
            JsonElement element = database.get(key);
            if (request.size() == 0) {
                database.remove(key);
                return true;
            } else if (element.isJsonObject()){
                return deleteWithoutLockForKeyArray(request, (JsonObject) element);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }





}
