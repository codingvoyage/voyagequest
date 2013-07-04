package battle;

import map.QuadTree;
import map.Rectangular;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import voyagequest.DoubleRect;
import voyagequest.Global;
import voyagequest.VoyageQuest;

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

    /** A list of the BattleEntities we can iterate through. Excludes Projectiles*/
    public static LinkedList<BattleEntity> entityList;

    /** We need a data structure for all our projectiles */
   // public static LinkedList<Projectile> projectiles;

    private static int nextInstanceNumber;


    static
    {
        entityCollisions = new QuadTree<>(
                20, 10,
                new DoubleRect(0, 0, VoyageQuest.X_RESOLUTION,VoyageQuest.Y_RESOLUTION));
        entityInstances = new HashMap<>();
        entityList = new LinkedList<>();
        //projectiles = new LinkedList<>();
        nextInstanceNumber = 0;
    }

    public static void render(GameContainer gc, Graphics g)
    {
        for (BattleEntity b : entityList)
        {
            b.draw(g, (float)b.r.x, (float)b.r.y);
        }
        drawCollRects(g);
    }

    public static void drawCollRects(Graphics g)
    {
        g.setColor(Color.red);
        LinkedList<BattleEntity> entList = BattleField.entityCollisions.rectQuery(
                new DoubleRect(0, 0, VoyageQuest.X_RESOLUTION, VoyageQuest.Y_RESOLUTION));
        for (BattleEntity b : entList)
        {
            g.drawRect((float)b.r.x, (float)b.r.y, (float)b.r.getWidth(), (float)b.r.getHeight());
        }
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

    public static boolean hasEntity(String instanceID)
    {
        return entityInstances.containsKey(instanceID);
    }

    public static void update(int delta)
    {
        ListIterator<BattleEntity> iter = entityList.listIterator();
        while (iter.hasNext())
        {
            BattleEntity currentEntity = iter.next();
            if (currentEntity.isMarkedForDeletion())
            {
                iter.remove();
            }
            else
            {
                currentEntity.act(delta);
            }


        }


    }


    public static void clearBattleField()
    {
        //Untested
        entityCollisions.clear();

        entityInstances.clear();
    }

    /**
     *
     * @return
     */
    public static int getInstanceNumber()
    {
        nextInstanceNumber++;
        return nextInstanceNumber;
    }

}
