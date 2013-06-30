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
 * battle
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

//        //Let's do a few tests...
//        LoadBattleEntity anika = entityData.get("Anika");
//            System.out.println(anika.description);
//            System.out.println(anika.velocityX);
//            System.out.println(anika.collisionBox.getHeight());
//            System.out.println(anika.startingAnimationName);


    }

    public static void init() { }




}
