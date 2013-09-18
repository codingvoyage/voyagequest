package battle;

import map.QuadTree;
import map.Rectangular;
import map.TreeNode;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.tiled.TiledMapPlus;
import scripting.*;
import voyagequest.DoubleRect;
import voyagequest.Global;
import voyagequest.Res;
import voyagequest.VoyageQuest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Created with IntelliJ IDEA.
 * User: Edmund
 * Date: 6/28/13
 */
public class BattleField {
    /** QuadTree stores all of the Entities */
    public static QuadTree<BattleEntity> entityCollisions;

    /** HashMap<String, BattleEntity> maps String IDs to Entity instances */
    public static HashMap<String, BattleEntity> entityInstances;

    /** A list of the BattleEntities we can iterate through. */
    public static ArrayList<BattleEntity> entityList;

    // For spawning things and avoiding the repetition of names.
    private static int nextInstanceNumber;

    //Easy access to the player
    public static Player player;

    /** Current BattleField background map */
    public static TiledMapPlus backgroundMap;
    public static int backgroundMapWidth;
    public static int backgroundMapHeight;
    public static double currentScrollDistance;
    private static boolean scrollVertically = false;
    public static double scrollVelocity = 100;

    static
    {
        entityCollisions = new QuadTree<>(
                3, 30,
                new DoubleRect(0, 0, VoyageQuest.X_RESOLUTION,VoyageQuest.Y_RESOLUTION));
        entityInstances = new HashMap<>();
        entityList = new ArrayList<>();
        nextInstanceNumber = 0;

        //Temporary usage
        int playerX = VoyageQuest.X_RESOLUTION/2 - 32;
        int playerY = (int)(0.8 * VoyageQuest.Y_RESOLUTION);
        System.out.println("SPAWNING PLAYER");
        player = EntityManager.spawnPlayer("sebastianplayer", playerX, playerY);
        addBattleEntity(player);

        //Load the default scrolling battle map
        newBackgroundMap("Battle1");
    }

    /**
     * Set the BattleField current background map to a new one
     * @param newBackgroundMapID the String in mapMappings that refers to a TileMap
     */
    public static void newBackgroundMap(String newBackgroundMapID)
    {
        backgroundMap = Res.allMaps.get(newBackgroundMapID);
        backgroundMapWidth = backgroundMap.getWidth()*backgroundMap.getTileWidth();
        backgroundMapHeight = backgroundMap.getHeight()*backgroundMap.getTileWidth();
        currentScrollDistance = 0;

    }

    public static void render(GameContainer gc, Graphics g)
    {
        drawBackground(g);
        for (BattleEntity b : entityList)
        {
                b.draw(g, (float)b.r.x, (float)b.r.y);
        }
        drawCollRects(g);
        drawPartitions(g);
    }

    public static void drawBackground(Graphics g)
    {
        int scrollTile = (int)currentScrollDistance / 64;
        int inBetweenSpace = (int)currentScrollDistance % 64;

        if (scrollVertically)
            backgroundMap.render(0, -inBetweenSpace, 0, scrollTile,
                    VoyageQuest.X_RESOLUTION / 64 + 4,
                    VoyageQuest.Y_RESOLUTION / 64 + 4);
        else
            backgroundMap.render(-inBetweenSpace, 0, scrollTile, 0,
                VoyageQuest.X_RESOLUTION / 64 + 4,
                VoyageQuest.Y_RESOLUTION / 64 + 4);

    }


    public static void drawCollRects(Graphics g)
    {
        g.setColor(Color.red);
        LinkedList<BattleEntity> entList = entityCollisions.rectQuery(
                new DoubleRect(0, 0, VoyageQuest.X_RESOLUTION, VoyageQuest.Y_RESOLUTION));
        for (BattleEntity b : entList)
        {
            DoubleRect collRect = b.getCollRect();
            g.drawRect((float) collRect.x,
                       (float) collRect.y,
                       (float) collRect.getWidth(),
                       (float) collRect.getHeight());
        }
    }

    public static void drawPartitions(Graphics g)
    {
        LinkedList<TreeNode> partitions = entityCollisions.getPartitions();
        for (TreeNode node : partitions)
        {
            g.drawRect(
                    (float) node.boundary.getX(),
                    (float) node.boundary.getY(),
                    (float) node.boundary.getWidth(),
                    (float) node.boundary.getHeight());
        }
    }

    public static void update(int delta)
    {

        //UPDATE ALL THE ENTITIES
        boolean cont = !entityList.isEmpty();
        int index = 0;
        while (cont)
        {
            //Get current entity.
            BattleEntity currentEntity = entityList.get(index);

            if (currentEntity.isMarkedForDeletion())
            {

                removeEntity(currentEntity);
            }
            else
            {
                currentEntity.act(delta);
                index++;
            }

            if (index >= entityList.size()) {
                cont = false;
            }
        }

        //Update our cool scrolling map
        double projectedScrollLoc = currentScrollDistance + delta*(scrollVelocity/1000);//backgroundScrollVelocity;

        boolean resetScroll = (scrollVertically) ?
                projectedScrollLoc > backgroundMapHeight - VoyageQuest.Y_RESOLUTION :
                projectedScrollLoc > backgroundMapWidth - VoyageQuest.X_RESOLUTION;

        if (resetScroll)
            currentScrollDistance = 0;
        else
            currentScrollDistance = projectedScrollLoc;






    }


    public static void addBattleEntity(BattleEntity newEntity, String instanceID)
    {
        entityCollisions.addEntity(newEntity);
        entityInstances.put(instanceID, newEntity);
        entityList.add(newEntity);
    }

    //For projectiles without an instanceID
    public static void addBattleEntity(BattleEntity newEntity)
    {
        entityCollisions.addEntity(newEntity);
        entityList.add(newEntity);
    }

    public static void removeEntity(String instanceID)
    {
        entityCollisions.removeEntity(entityInstances.get(instanceID));
        entityInstances.remove(instanceID);
    }

    public static void removeEntity(BattleEntity entityToRemove)
    {
        entityCollisions.removeEntity(entityToRemove);
        entityInstances.remove(entityToRemove);
        entityList.remove(entityToRemove);
    }

    public static boolean hasEntity(String instanceID)
    {
        return entityInstances.containsKey(instanceID);

    }

    public static boolean hasEntity(BattleEntity instance)
    {
        return entityList.contains(instance);
    }

    public static void clear()
    {
        //Untested
        entityCollisions.clear();
        entityInstances.clear();
    }

    public static int getInstanceCount()
    {
        return entityList.size();
    }

    /**
     * For generating unique instanceIDs
     * @return
     */
    public static int getInstanceNumber()
    {
        nextInstanceNumber++;
        return nextInstanceNumber;
    }

}
