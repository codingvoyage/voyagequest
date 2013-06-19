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
    
    /** Sprite sheet for Sebastian */
    public static StreamXMLPackedSheet sebastian; 
    public static StreamXMLPackedSheet njeri; 
    public static StreamXMLPackedSheet soldier; 
    public static StreamXMLPackedSheet lifei; 
    public static StreamXMLPackedSheet lifeisprite; 
    public static StreamXMLPackedSheet young;
    public static StreamXMLPackedSheet anika;
    
    public static HashMap<String, TiledMapPlus> allMaps;
    public static HashMap<String, String> idToJsonUrlMappings;
    public static LinkedList<LoadMaps> mapLoadingMappings;
    
    static {
        try {
            sebastian = new StreamXMLPackedSheet("res/sebastian.png", "res/sebastian.xml");
            njeri = new StreamXMLPackedSheet("res/njeri.png", "res/njeri.xml");
            soldier = new StreamXMLPackedSheet("res/soldier_npc01_walk.png", "res/soldier.xml");
            lifei = new StreamXMLPackedSheet("res/lifei.png", "res/lifei.xml");
            lifeisprite = new StreamXMLPackedSheet("res/lifeifront.png", "res/lifeisprite.xml");
            young = new StreamXMLPackedSheet("res/test/surprisetiles.png","res/young.xml");
            anika = new StreamXMLPackedSheet("res/anika.png","res/anika.xml");
            System.out.println("XML Loaded");
            
            
            //Loading the maps
            allMaps = new HashMap<>();
            idToJsonUrlMappings = new HashMap<>();
            
            JsonReader<Res> reader = new JsonReader<>(Res.class, "res/mapMappings.json");
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
     * Initialize all the remaining resources. 
     * Must be called <em>after</em> data is loaded from JSON
     */
    public static void init() {
        initAudio();
            
        ListIterator<LoadAnimations> animationIterator = animationData.listIterator();
        while (animationIterator.hasNext()) {
            LoadAnimations next = animationIterator.next();
            
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
            
            StreamXMLPackedSheet currentPackedSheet = null;
            System.out.println(next.getPackedSheetID());
            if (next.getPackedSheetID().equals("njeri")) currentPackedSheet = njeri;
            if (next.getPackedSheetID().equals("sebastian")) currentPackedSheet = sebastian;
            if (next.getPackedSheetID().equals("soldier")) currentPackedSheet = soldier;
            if (next.getPackedSheetID().equals("lifei")) currentPackedSheet = lifei;
            if (next.getPackedSheetID().equals("lifeisprite")) currentPackedSheet = lifeisprite;
            if (next.getPackedSheetID().equals("young")) currentPackedSheet = young;
            if (next.getPackedSheetID().equals("anika")) currentPackedSheet = anika;
            
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
     * Initialize music and sound effects
     */
    private static void initAudio() {
        ListIterator<LoadAudio> audioIterator = musicData.listIterator();
        while (audioIterator.hasNext()) {
            LoadAudio next = audioIterator.next();
            try {
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
}