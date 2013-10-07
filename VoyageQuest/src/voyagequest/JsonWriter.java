package voyagequest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Modifier;

/**
 * Serializes objects into JSON and
 * writes this JSON representation into a save file.
 * @author Brian Yang
 * @version 1.0
 */
public class JsonWriter<T> {

    /** JSON file path */
    private String file;

    /** The type of object that JSON is being parsed into */
    private Class<T> type;

    /** JSON output */
    private String data;

    /**
     * Construct a new JsonWriter for Entities
     * @param type the type of Object that JSON is being parsed into
     * @param file path of JSON file
     */
    public JsonWriter(Class<T> type, String file) {
        this.type = type;
        this.file = file;
    }

    /**
     * Read and parse the JSON file
     * @return boolean indicating whether or not the JSON file was successfully parsed
     */
    public boolean writeJson(){


        // read the JSON file
        InputStream jsonPath = getClass().getClassLoader().getResourceAsStream(file);
        try (BufferedReader read = new BufferedReader(new InputStreamReader(jsonPath))) {

            Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.VOLATILE).create();
            data = gson.toJson(read);

        } catch (IOException ex) {
            return false;
        }

        return true;
    }

}
