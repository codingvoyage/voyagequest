package battle;

import org.newdawn.slick.Animation;
import voyagequest.DoubleRect;
import voyagequest.JsonReader;
import voyagequest.Res;
import voyagequest.special.LoadBattleEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Loads all the BattleEntity data into memory. Provides a number of methods
 * which create the individual ...
 *
 * @author Brian
 * @version 06 2013
 */
public class EntityManager {

    //public LinkedList<Entity> entities;
    //private static LinkedList<BattleEntity> entityData;

    //This is for loading all of them from JSON
    private static ArrayList<LoadBattleEntity> loadBattleEntities;
    //This is the String-->LoadBattleEntity reference table
    private static HashMap<String, LoadBattleEntity> entityData;


    static {

        //loadBattleEntities is placed into memory
        JsonReader<EntityManager> reader =
                new JsonReader<>(EntityManager.class, "res/BattleEntities.json");
        reader.readJson();

        entityData = new HashMap<>();

        //Need to prepare each LoadBattleEntity just a bit more
        for (LoadBattleEntity loadBattleEntity : loadBattleEntities)
        {
            //We want to create a HashMap of the Animations for easy reference
            //It's faster than searching it up on Res.animations everytime
            loadBattleEntity.loadedAnimations = new HashMap<>();
            for (String animationName : loadBattleEntity.animations)
            {
                Animation namedAnimation = Res.animations.get(animationName);
                loadBattleEntity.loadedAnimations.put(animationName, namedAnimation);
            }

            //Let's also prep the collisionBox
            loadBattleEntity.collisionBox = new DoubleRect(
                    loadBattleEntity.collisionX,
                    loadBattleEntity.collisionY,
                    loadBattleEntity.collisionWidth,
                    loadBattleEntity.collisionHeight);


            //More eventually...

            //Now place in HashMap entityData
            entityData.put(loadBattleEntity.battleEntityID, loadBattleEntity);

        }



    }

    public static void init() { }

    //Reminder: EntityManager just loads the Entity. It doesn't create threads, it doesn't
    //add the Enemy to the BattleField... the Scripting Engine is responsible for that.
    public static Enemy spawnEnemy(String loadEntityID, int xLocation, int yLocation)
    {
        LoadBattleEntity spawnBase = entityData.get(loadEntityID);

        //Load and set positioning
        DoubleRect newEntityLocation = new DoubleRect(
                xLocation,
                yLocation,
                spawnBase.width,
                spawnBase.height);
        DoubleRect newEntityCollision = spawnBase.collisionBox;
        Enemy newEnemy = new Enemy(newEntityLocation, newEntityCollision);

        //Loads the things general to all BattleEntities
        loadFromBase(newEnemy, loadEntityID);


        return newEnemy;
    }




    public static void loadFromBase(BattleEntity needsLoading, String loadEntityID)
    {
        LoadBattleEntity spawnBase = entityData.get(loadEntityID);

        needsLoading.velocityX = spawnBase.velocityX;
        needsLoading.velocityY = spawnBase.velocityY;
        needsLoading.animations = spawnBase.loadedAnimations;
        needsLoading.currentAnimation = spawnBase.loadedAnimations.get(
                spawnBase.startingAnimationName);
        needsLoading.mainScriptID = spawnBase.mainScriptID;

    }

}
