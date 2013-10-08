package voyagequest;

import org.newdawn.slick.Animation;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.openal.Audio;
import org.newdawn.slick.openal.AudioLoader;
import org.newdawn.slick.tiled.TiledMapPlus;
import org.newdawn.slick.util.ResourceLoader;
import voyagequest.special.LoadAnimations;
import voyagequest.special.LoadAudio;
import voyagequest.special.LoadMaps;
import voyagequest.special.LoadStreamXMLPackedSheets;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;


/**
 * Global resources, like animations, sprites, etc...
 * @author Brian Yang
 */
public class Res {
    public static HashMap<String, TiledMapPlus> allMaps;
    public static LinkedList<LoadMaps> mapLoadingMappings;
    public static HashMap<String, String> idToJsonUrlMappings;

    public static HashMap<String, StreamXMLPackedSheet> idToPackedSheetMappings;
    public static LinkedList<LoadStreamXMLPackedSheets> packedSheetLoadingMappings;

    /** Sebastian animation, data mapped by JSON */
    public static LinkedList<LoadAnimations> animationData = new LinkedList<>();
    /** The actual animations, mapped by ID strings */
    public static HashMap<String, Animation> animations = new HashMap<>();

    /** Music data mapped by JSON */
    public static LinkedList<LoadAudio> musicData = new LinkedList<>();
    /** Hashmap of background music */
    public static HashMap<String, Audio> music = new HashMap<>();
    /** Currently playing music */
    public static Audio currentMusic;


    /**
     * Gets the StreamXMLPackedSheets loaded in the HashMap, and loads the maps.
     */
    static {
        try {
            //LOAD THE PACKED SHEETS FOR THE ANIMATIONS
            JsonReader<Res> reader =
                    new JsonReader<>(Res.class, "res/entityimages/imagePackedSheetMappings.json");
            reader.readJson();

            idToPackedSheetMappings = new HashMap<>();
            for (LoadStreamXMLPackedSheets packedSheet : packedSheetLoadingMappings)
            {
                StreamXMLPackedSheet pack =
                        new StreamXMLPackedSheet(packedSheet.imageFile, packedSheet.xmlFileLocation);
                idToPackedSheetMappings.put(packedSheet.streamXMLPackedSheetID, pack);
            }


            //LOAD THE MAPS
            allMaps = new HashMap<>();
            idToJsonUrlMappings = new HashMap<>();
            
            reader = new JsonReader<>(Res.class, "res/maps/mapMappings.json");
            reader.readJson();

            ListIterator<LoadMaps> iterator = mapLoadingMappings.listIterator();
            while (iterator.hasNext())
            {
                LoadMaps currentMap = iterator.next();

                InputStream is = Res.class.getClassLoader().getResourceAsStream(
                        currentMap.mapURL);
                TiledMapPlus tileMap = new TiledMapPlus(is, "res");
                
                allMaps.put(currentMap.mapID, tileMap);
                idToJsonUrlMappings.put(currentMap.mapID, currentMap.mapJsonURL);
            }
            
        } catch (SlickException ex) {}
        
        
            
    }


    /**
     * Initialize the Animations
     * Must be called <em>after</em> data is loaded from JSON
     */
    public static void initAnimations() {
        JsonReader<Res> reader = new JsonReader<>(Res.class, "res/entityimages/Animations.json");
        reader.readJson();
            
        ListIterator<LoadAnimations> animationIterator = animationData.listIterator();
        while (animationIterator.hasNext()) {

            //For lack of better implementation, I will hardcode this now.
            //Here's the problem: before, it was sebastian.getSprite(frameIterator.next());
            //Basically, since you didn't write something that transferred the images from
            //the StreamXMLPackedSheets into a huge HashMap of Image objects, you went directly from
            //StreamXMLPackedSheet --> Animation with this method. Unfortunately, we end up with two
            //issues:
            //     1. How do we load any images that don't belong with Animations?
            //     2. How do we tell between which StreamXMLPackedSheet to choose from? I decided to give
            //     a new JSON field called packedSheetID which solves the problem for now, but please
            //     later help me write something that creates a huge Hashmap<String, Image> please.

            //Edit by Edmund as of 6/19/13. The hardcodedness has been solved, but the issue of not having
            //a repository of Image objects has not been solved yet.

            LoadAnimations next = animationIterator.next();
            StreamXMLPackedSheet currentPackedSheet = idToPackedSheetMappings.get(next.getPackedSheetID());

            Image[] frames = new Image[next.getImages().size()];
            int i = 0;
            ListIterator<String> frameIterator = next.getImages().listIterator();
            while (frameIterator.hasNext()) {
                frames[i] = currentPackedSheet.getSprite(frameIterator.next());
                i++;
            }
                
            animations.put(next.getName(), new Animation(frames, next.getDuration()));
        }

    }

    /**
     * Initialize music and sound effects.
     * Background music are loaded as streams while sound effects
     * are loaded directly into memory
     */
    public static void initAudio() {
        JsonReader<Res> reader = new JsonReader<>(Res.class, "res/Audio.json");
        reader.readJson();

        ListIterator<LoadAudio> audioIterator = musicData.listIterator();
        while (audioIterator.hasNext()) {
            LoadAudio next = audioIterator.next();
            try {
                if (next.getType().equals("music"))
                    music.put(next.getName(), AudioLoader.getStreamingAudio("OGG", ResourceLoader.getResource(next.getPath())));
                else
                    music.put(next.getName(), AudioLoader.getAudio("OGG", ResourceLoader.getResourceAsStream(next.getPath())));
            } catch (IOException e) {}
        }
    }

    /**
     * Play Music (shorthand)
     * @param name audio name
     */
    public static void play(String name) {
        playMusic(name);
    }

    /**
     * Play Music if it exists
     * @param name audio name
     */
    public static void playMusic(String name) {
        if (music.containsKey(name) && currentMusic != getAudio(name)) {
                if (currentMusic != null)
                    currentMusic.stop();
                music.get(name).playAsMusic(1.0f, 1.0f, true);
                currentMusic = music.get(name);
                System.out.println("Playing " + name);
        }
    }

    /**
     * Play Effect if it exists
     * @param name audio name
     */
    public static void playEffect(String name) {
        if (music.containsKey(name))
            music.get(name).playAsSoundEffect(1.0f, 7.0f, false);
    }

    /**
     * Get Audio by Name
     * @param name name of audio file
     * @return audio file or null if it doesn't exist
     */
    public static Audio getAudio(String name) {
        if (music.containsKey(name))
            return music.get(name);
        else
            return null;
    }

    /**
     * Saves the game by serializing data into a JSON file
     */
    public static void saveGame() {

        System.out.println("Attempting a Game Save");

        Object[] saveObjects = {VoyageQuest.threadManager};
        Class[] saveClasses = {VoyageQuest.threadManager.getClass()};
        String[] saveNames = {VoyageQuest.threadManager.getClass().getName()};

        for (int i = 0; i < saveObjects.length; i++) {
            JsonSerializer jsonWriter = new JsonSerializer(saveObjects[i], saveClasses[i], saveNames[i]);
            jsonWriter.writeJson();
        }

        System.out.println("Game might have been saved.");
    }

    /**
     * Loads a saved game using a JSON save file
     * @param type the type of object being loaded
     * @param file the filename of the input JSON
     */
    public static void loadGame(Class type, String file) {
        JsonReader jsonReader = new JsonReader(type, file, true);
    }
}