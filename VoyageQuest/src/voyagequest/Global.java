package voyagequest;

import map.*;
import org.newdawn.slick.Image;
import scripting.Thread;
import scripting.Parameter;
import java.util.HashMap;

/**
 *
 * @author Edmund
 */
public class Global {
    public static Map currentMap;
    public static Camera camera;
    
    public static boolean isFrozen = false;
    public static Thread unfrozenThread = null;
    public static boolean isInputFrozen = false;
    
    public static HashMap<String, Parameter> globalMemory = new HashMap<>();

    public static Entity player;
}
