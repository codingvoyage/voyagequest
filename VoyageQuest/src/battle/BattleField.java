package battle;

import map.QuadTree;
import map.Rectangular;
import map.TreeNode;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import scripting.*;
import voyagequest.DoubleRect;
import voyagequest.Global;
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


    static
    {
        entityCollisions = new QuadTree<>(
                5, 20,
                new DoubleRect(0, 0, VoyageQuest.X_RESOLUTION,VoyageQuest.Y_RESOLUTION));
        entityInstances = new HashMap<>();
        entityList = new ArrayList<>();
        nextInstanceNumber = 0;
    }

    public static void render(GameContainer gc, Graphics g)
    {
        for (BattleEntity b : entityList)
        {
            b.draw(g, (float)b.r.x, (float)b.r.y);
        }
        drawCollRects(g);
        drawPartitions(g);
    }

    public static void drawCollRects(Graphics g)
    {
        g.setColor(Color.red);
        LinkedList<BattleEntity> entList = entityCollisions.rectQuery(
                new DoubleRect(0, 0, VoyageQuest.X_RESOLUTION, VoyageQuest.Y_RESOLUTION));
        for (BattleEntity b : entList)
        {
            g.drawRect((float) b.r.x, (float) b.r.y, (float) b.r.getWidth(), (float) b.r.getHeight());
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
        boolean cont = !entityList.isEmpty();
        int index = 0;
        while (cont)
        {
            //Get current entity.
            BattleEntity currentEntity = entityList.get(index);

            if (currentEntity.isMarkedForDeletion())
            {
                System.out.println("REMOVING");
                removeEntity(currentEntity);
            }
            else
            {
//                System.out.println("Collision content size: " + entityCollisions.getSize());
                currentEntity.act(delta);
                index++;
            }

            if (index >= entityList.size()) {
                cont = false;
            }
        }


    }


    public static void addBattleEntity(BattleEntity newEntity, String instanceID)
    {
        entityCollisions.addEntity(newEntity);
//        System.out.println("adding " + instanceID + "! Number of total entities: " +
//            entityCollisions.getSize());
        entityInstances.put(instanceID, newEntity);
        entityList.add(newEntity);
    }

    //For projectiles without an instanceID
    public static void addBattleEntity(BattleEntity newEntity)
    {
        entityCollisions.addEntity(newEntity);
        entityList.add(newEntity);

        System.out.println("adding a new projectile! Number of total entities: " +
                entityCollisions.getSize());
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
