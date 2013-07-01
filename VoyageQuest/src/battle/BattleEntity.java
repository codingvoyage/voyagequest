package battle;

import map.Entity;
import org.newdawn.slick.Animation;
import voyagequest.DoubleRect;
import voyagequest.Global;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Battle Entity
 *
 * @author Brian Yang
 * @author Edmund Qiu
 */
public class BattleEntity extends Entity {

    /** BattleEntity handles Animation quite differently, so do this instead */
    public HashMap<String, Animation> animations;

    /** Determines what hurts what */
    public Allegiance entityAllegiance;

    /** When the BattleEntity is a ghost, it can't be collided with by anything*/
    public boolean isGhost;

    /** The scriptID of the main thread*/
    public String mainScriptID;


    public BattleEntity(DoubleRect rect)
    {
        super(rect);
        this.isGhost = false;
    }

    public BattleEntity(DoubleRect rect, DoubleRect collRect)
    {
        super(rect);
        this.collRect = collRect;
        this.isGhost = false;
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

    public boolean checkCollision(DoubleRect collCandidate)
    {
        return false;
    }


}
