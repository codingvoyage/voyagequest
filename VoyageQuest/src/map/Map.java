package map;

import org.newdawn.slick.SlickException;
import org.newdawn.slick.tiled.GroupObject;
import org.newdawn.slick.tiled.Layer;
import org.newdawn.slick.tiled.ObjectGroup;
import org.newdawn.slick.tiled.TiledMapPlus;
import scripting.Thread;
import voyagequest.DoubleRect;
import voyagequest.JsonReader;
import voyagequest.Res;
import voyagequest.special.LoadEntity;

import java.util.ArrayList;
import java.util.LinkedList;

import static voyagequest.VoyageQuest.threadManager;


/**
 *
 * @author Edmund
 */
public class Map {
    /** For future purposes it might be useful to have maps have a name */
    public String mapName;
    
    /** The tile based map provided by slick and tiled */
    public TiledMapPlus tileMap;
    
    /**The script associated with this map... */
    public static String mapBackgroundScript;
    
    /**For collision detection and such */
    public QuadTree collisions;
    public QuadTree boundaries;
    public QuadTree events;
    public static LinkedList<Rectangular> allCollisions;
    public static LinkedList<Rectangular> allBoundaries;
    
    /**all the entities */
    public static LinkedList<Entity> entities;

    /**The length and width of each tile. */
    public static int TILE_LENGTH;

    /** */
    public int MAP_TILE_LAYERS;

    /** */
    public int MAP_ENTITY_LAYER;
    
    /**The width and length of the entire map in pixels. */
    public static int MAP_WIDTH;
    public static int MAP_HEIGHT;
    
    /**A rectangle of the entire map. */
    public static DoubleRect MAP_RECT;
    
    /**For loading all the entities... */
    public static LinkedList<LoadEntity> allEntities = new LinkedList<>();
    
    /** map music */
    private static String music;

    /** the chance of an encounter happening in this tick, as a percentage*/
    private static double encounterChance;

    /** The enemies which spawn in danger-areas of the map */
    private static ArrayList<ChanceGroup> spawns;
    
    public Map(String mapID) throws SlickException
    {
        //Load the TiledMapPlus
        tileMap = Res.allMaps.get(mapID);

        //Extract important information from it
        TILE_LENGTH = tileMap.getTileHeight();
        MAP_WIDTH = tileMap.getWidth() * TILE_LENGTH;
        MAP_HEIGHT = tileMap.getHeight() * TILE_LENGTH;
        MAP_RECT = new DoubleRect(0, 0, MAP_WIDTH, MAP_HEIGHT);

        ArrayList<Layer> tileLayers = tileMap.getLayers();
        MAP_TILE_LAYERS = tileLayers.size();
        for (Layer l : tileLayers)
        {
            if (l.name.equalsIgnoreCase("entity layer"))
                MAP_ENTITY_LAYER = l.index;
        }

        System.out.println(MAP_ENTITY_LAYER);
        System.out.println(MAP_TILE_LAYERS);

        //Create the LinkedList of all entities
        allCollisions = new LinkedList<>();
        allBoundaries = new LinkedList<>();
        entities = new LinkedList<>();
        
        //Initialize the QuadTrees of collisions and boundaries
        collisions = new QuadTree<>(20, 10, MAP_RECT);
        boundaries = new QuadTree<>(20, 10, MAP_RECT);
        events = new QuadTree<>(20, 10, MAP_RECT);
        
        //Now that QuadTree is initialized, we are free to initialize the
        //collision rects.
        ArrayList<ObjectGroup> objLayers = tileMap.getObjectGroups();

        //Get the collision layer
        //Get the boundary layer
        ArrayList<GroupObject> collisionLayer = objLayers.get(0).getObjects();
        ArrayList<GroupObject> boundaryLayer = objLayers.get(1).getObjects();
        ArrayList<GroupObject> eventLayer = objLayers.get(2).getObjects();

        //First up, fill the collisionlayer. I favor the wrapper over the GroupObject approach, since 
        //that enables easy access to the properties and other methods provided by GroupObject.
        for (GroupObject o : collisionLayer)
        {
            GroupObjectWrapper collisionBox = new GroupObjectWrapper(o);
            collisions.addEntity(collisionBox);
            allCollisions.add(collisionBox);
        }
        
        //Now, fill up the boundaryLayer...
        for (GroupObject o : boundaryLayer)
        {
            //Find the collision GroupObject which fits entirely within the
            //boundary GroupObject!
            
            GroupObjectWrapper boundaryWrapper = new GroupObjectWrapper(o);
            for (Rectangular candidate : allCollisions)
            {
                //If the candidate is a GroupObjectWrapper, and if it contains
                //the candidate...
                if (candidate instanceof GroupObjectWrapper
                 && boundaryWrapper.getRect().contains(candidate.getRect()))
                {
                    //... then create a TwoFieldGroupObjectWrapper with the extra
                    //GroupObject 
                    BoundaryWrapper newBoundary =
                            new BoundaryWrapper(o, (GroupObjectWrapper)candidate);
                    allBoundaries.add(newBoundary);
                    boundaries.addEntity(newBoundary);
                    
                    //STOP CHECKING AND GO ON TO THE NEXT ELEMENT IN BOUNDARYLAYER
                    break;
                }
                    
            }
        }
        
        //Now fill up the event layer. These are either on click, or on contact, depending on the
        //specifications done in TILED.
        //Types: onTouch and onClick
        for (GroupObject o : eventLayer)
        {
            GroupObjectWrapper collisionBox = new GroupObjectWrapper(o);
            events.addEntity(collisionBox);
        }
        
        //Now we load the entities for this map from a Json file.
        //Bakesale! I understand your JsonReader class now! I understand why you did all of this.
        //Json is INCREDIBLE. It's such a convenient way of defining data. Thank you.  
        
        //Alright, if the map file is at "res/mapname.tmx" the json will be at
        //"res/mapname.json". so...
        String jsonFileLocation = Res.idToJsonUrlMappings.get(mapID);

        JsonReader<Map> reader = new JsonReader<>(Map.class, jsonFileLocation);
        reader.readJson();
        
        //Run the background Script for this map...
        Thread backgroundThread = new Thread(this.mapBackgroundScript);
            backgroundThread.setName("MAPSCRIPT" + this.mapBackgroundScript);
            backgroundThread.setLineNumber(0);
            backgroundThread.setRunningState(false);
        threadManager.addThread(backgroundThread);

        for (LoadEntity l : allEntities)
        {
            Entity e = new Entity(
                    new DoubleRect(l.getInitialX(), l.getInitialY(), 
                    l.getWidth(), l.getHeight()));
            
            String mainScriptID = l.getMainScriptID();
            String mainThreadName = l.getMainThreadName();
            
            e.onClickScript = l.getOnClickScript();
            e.onTouchScript = l.getOnTouchScript();
            
            Thread newThread = new Thread(mainScriptID);  
                newThread.setName(mainThreadName);
                newThread.setRunningState(false);
                
            threadManager.addThread(newThread);
            threadManager.getThreadAtName(mainThreadName).setScriptable(e);
            
            e.setMainThread(newThread);
            e.setMainScriptID(mainScriptID);
        
            e.name = l.getName();
            
            LinkedList<String> animations = l.getAnimations();
            e.forward = Res.animations.get(animations.get(0));
            e.backward = Res.animations.get(animations.get(1));
            e.left = Res.animations.get(animations.get(2));
            e.right = Res.animations.get(animations.get(3));

            if (animations.size() > 4) {
                e.profile = Res.animations.get(animations.get(4));
                e.profLeft = l.getOrientation();

            }

            e.setAnimation(l.getStartingAnimationDirection());
            
            entities.add(e);
            collisions.addEntity(e);
        }
    }

    public boolean encounterPoll()
    {
        if (encounterChance == 0 ) return false;

        double rand = (Math.random() * 1000);
        double range = encounterChance * 10;
        return rand <= range;
    }


    /**
     * Return the name of the map's background music.
     * If no music exists, then use "Route 3"
     * @return name of map's background music
     */
    public static String getMusic() {
        if (music == null)
            music = "Route 3";
        return music;
    }
    
}
