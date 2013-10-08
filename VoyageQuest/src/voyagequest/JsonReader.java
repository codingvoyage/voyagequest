package voyagequest;

import com.google.gson.*;

import java.io.*;
import java.lang.reflect.Modifier;

/**
 * Reads JSON Data Files with the GSON Library<br/><br/>
 * Assumes the following JSON format:<br/><br/>
 * <code>
 * { "type1": [ { "value1" : "value", "value2" : "value" } ], "type2": [ { "value1" : "value", "value2" : "value" } ] }
 * </code><br/>
 * <ul>
 * <li>The entire JSON file will be passed to a parent class (Object T)</li>
 * <li>Every child value will be treated as instance variables</li>
 * <li>Child arrays will be treated as ArrayLists of type object (specified in the instance field that declares the List).</li>
 * </ul>
 * @author Brian Yang
 */
public class JsonReader<T> {
    
    /** JSON file path */
    private String file;
    
    /** The type of object that JSON is being parsed into */
    private Class<T> type;
    
    /** The final parsed object */
    private T data;

    /** whether the json file needs to be a buffered stream (class level json or external) */
    private boolean buffered = false;

    /**
     * Construct a new JsonReader for Entities
     * @param type the type of Object that JSON is being parsed into
     * @param file path of JSON file
     */
    public JsonReader(Class<T> type, String file) {
        this.type = type;
        this.file = file;
    }

    /**
     * Construct a new JsonReader for Entities
     * @param type the type of Object that JSON is being parsed into
     * @param file path of JSON file
     * @param buffered whether or not this is a buffered stream
     */
    public JsonReader(Class<T> type, String file, boolean buffered) {
        this.type = type;
        this.file = file;
        this.buffered = buffered;
    }
    
    /**
     * Read and parse the JSON file
     * @return boolean indicating whether or not the JSON file was successfully parsed
     */
    public boolean readJson(){

        Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.VOLATILE).create();

        if (!buffered) {
            // read the JSON file
            InputStream jsonPath = getClass().getClassLoader().getResourceAsStream(file);
            try (BufferedReader read = new BufferedReader(new InputStreamReader(jsonPath))) {

                data = gson.fromJson(read, type);

                read.close();

            } catch (IOException ex) {
                return false;
            }
        } else {
            // read the JSON file
            try (FileReader read = new FileReader(file)) {

                data = gson.fromJson(read, type);

                read.close();

            } catch (IOException ex) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Accessors for the final object
     * @return the parsed Object
     */
    public T getObject() {
        return data;
    }
}