package battle;

import map.Entity;
import org.newdawn.slick.Animation;
import voyagequest.DoubleRect;

import java.util.ArrayList;

/**
 * Battle Entity
 *
 * @author Brian Yang
 * @author Edmund Qiu
 */
public class BattleEntity extends Entity {

    /** BattleEntity handles Animation quite differently, so do this instead */
    public ArrayList<Animation> animationArrayList;

    /** Determines what hurts what */
    public Allegiance entityAllegiance;

    /** When the BattleEntity is a ghost, it can't be collided with by anything*/
    public boolean isGhost;


    public BattleEntity(DoubleRect rect)
    {
        super(rect);
    }

    public BattleEntity(DoubleRect rect, DoubleRect collRect)
    {
        super(rect);
        this.collRect = collRect;
    }

    public void changeAnimationDirection(int newAnimationDirection)
    {
        animationDirection = newAnimationDirection;
    }


}
