package battle;

import map.Entity;
import voyagequest.DoubleRect;

/**
 * Battle Entity
 *
 * @author Brian Yang
 * @author Edmund Qiu
 */
public class BattleEntity extends Entity {

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
