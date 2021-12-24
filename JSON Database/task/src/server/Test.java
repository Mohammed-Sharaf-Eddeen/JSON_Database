package server;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


public class Test {
    public static Gson gson = new Gson();


    public static void tmain(String[] args) {
        JsonObject database = new JsonObject();
        database.addProperty("1", 1);

        JsonObject e2 = new JsonObject();
        e2.addProperty("2", 2);
        JsonObject e3 = new JsonObject();
        e3.add("6", e2);
        database.add("nested", e3);

        System.out.println(database);


        JsonArray request = new JsonArray();
        request.add("nested");
        request.add("5");
        request.add("2");
        set(request, database);
        System.out.println(database);

        request.add("nested");
        request.add("5");
        request.add("2");
        deleteForKeyArray(request, database);
        System.out.println(database);

    }

    public static JsonElement getForKeyArray(JsonArray request, JsonObject database) {
        String key = gson.fromJson(request.get(0), String.class);
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

    public static void set(JsonArray request, JsonObject database) {
        String key = gson.fromJson(request.get(0), String.class);
        request.remove(0);

        JsonElement element = database.get(key);
        if (element == null && request.size() == 0) {
            database.addProperty(key, "done!!");
        } else if (element != null && request.size() == 0) {
            database.remove(key);
            database.addProperty(key, "done!!");
        } else if (element == null && request.size() != 0) {
            JsonObject newExtendedObject = new JsonObject();
            database.add(key, newExtendedObject);
            set(request, newExtendedObject);
        } else if (element != null && request.size() != 0){
            set(request, database.getAsJsonObject(key));
        }
    }

    public static int deleteForKeyArray(JsonArray request, JsonObject database) {
        String key = gson.fromJson(request.get(0), String.class);
        request.remove(0);

        if (database.has(key)) {
            JsonElement element = database.get(key);
            if (request.size() == 0) {
                database.remove(key);
                return 1;
            } else if (element.isJsonObject()){
                deleteForKeyArray(request, (JsonObject) element);
            } else {
                return -1;
            }
        }
        return -1;
    }
}
