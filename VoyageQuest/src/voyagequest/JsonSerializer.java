package voyagequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;

import java.io.*;
import java.lang.reflect.Modifier;

/**
 * Serializes objects into JSON and
 * writes this JSON representation into a save file.
 * @author Brian Yang
 * @version 1.0
 */
public class JsonSerializer<T> {

    /** JSON file path */
    private String file;

    /** The type of object that JSON is being parsed into */
    private Class<T> type;

    /** the object being serialized */
    private Object object;

    /**
     * Construct a new JsonSerializer for Entities
     * @param type the type of Object that JSON is being parsed into
     * @param file path of JSON file
     */
    public JsonSerializer(Object object, Class<T> type, String file) {
        this.object = object;
        this.type = type;
        this.file = file;
    }

    /**
     * Read and parse the JSON file
     * @return boolean indicating whether or not the JSON file was successfully parsed
     */
    public boolean writeJson(){

        // read the JSON file
        try {

            JsonWriter jsonWriter = new JsonWriter(new FileWriter(file));

            Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.VOLATILE).create();
            gson.toJson(object, type, jsonWriter);

            jsonWriter.close();


        } catch (IOException ex) {
            return false;
        }

        return true;
    }

}