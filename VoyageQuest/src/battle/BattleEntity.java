package battle;

import map.Entity;
import org.newdawn.slick.Animation;
import org.newdawn.slick.GameContainer;
import voyagequest.DoubleRect;
import voyagequest.Global;
import voyagequest.VoyageQuest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Battle Entity
 *
 * @author Brian Yang
 * @author Edmund Qiu
 */
public class BattleEntity extends Entity {
    public int health;

    /** BattleEntity handles Animation quite differently, so do this instead */
    public HashMap<String, Animation> animations;

    /** Determines what hurts what */
    public Allegiance entityAllegiance;

    /** When the BattleEntity is a ghost, it can't be collided with by anything*/
    public boolean isGhost;

    /** The scriptID of the main thread*/
    public String mainScriptID;

    /** All associated threads */
    public ArrayList<String> associatedThreadInstances;


    public BattleEntity(DoubleRect rect)
    {
        super(rect);
        this.isGhost = false;
        isMarkedForDeletion = false;
        associatedThreadInstances = new ArrayList<>();
    }

    public BattleEntity(DoubleRect rect, DoubleRect collRect)
    {
        super(rect);
        this.collRect = collRect;
        this.isGhost = false;
        isMarkedForDeletion = false;
        associatedThreadInstances = new ArrayList<>();
    }

    public void setAnimation(String animationID)
    {
        currentAnimation = animations.get(animationID);
        resetAnimationTiming();
    }


    /**
     * It becomes necessary for BattleEntities to use something different
     * @param newX
     * @param newY
     */
    public void place(double newX, double newY)
    {
        //Move to the proposed location
        BattleField.entityCollisions.removeEntity(this);
        r.x = newX;
        r.y = newY;
        BattleField.entityCollisions.addEntity(this);
    }

    //This is the attemptMove's checkCollision. It's useless in the battle portion
    //of the game, so just make it return false here.
    public boolean checkCollision(DoubleRect collCandidate)
    {
        return false;
    }

    public void act(int delta)
    {
        //Query BattleField for things to check collision with.
        LinkedList<BattleEntity> candidates = BattleField.entityCollisions.rectQuery(this.getCollRect());
        LinkedList<BattleEntity> collidedEntities = new LinkedList<>();

        //Now eliminate from this list anything that doesn't collide:
        ListIterator<BattleEntity> iter = candidates.listIterator();
        while (iter.hasNext())
        {
            BattleEntity candidate = iter.next();
            if (candidate.getCollRect().intersects(this.getCollRect()))
                collidedEntities.add(candidate);
        }

        //Now with this adjusted list, process collisions
        processCollision(collidedEntities);

        //Die if hp < 0
        if (health < 0)
        {
            markForDeletion();

            //Also remove all associated thread instances:
            for (String deadThread : associatedThreadInstances)
            {
                VoyageQuest.battleThreadManager.markForDeletion(deadThread);
            }
        }

    }

    public void processCollision(LinkedList<BattleEntity> collisions)
    {

    }

}
