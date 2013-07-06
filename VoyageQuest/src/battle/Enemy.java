package battle;

import voyagequest.DoubleRect;

import java.util.LinkedList;
import java.util.ListIterator;

/**
 * User: Edmund
 */
public class Enemy extends BattleEntity
{
    public int contactDamage;


    public Enemy(DoubleRect location,
                 DoubleRect collision) {
        super(location, collision);


        this.entityAllegiance = Allegiance.UNFRIENDLY;
    }



    public void processCollision(LinkedList<BattleEntity> collisions)
    {
        if (isGhost || collisions.size() == 1) return;

        //Iterate through the LinkedList of BattleEntities.
        ListIterator<BattleEntity> iter = collisions.listIterator();

        while (iter.hasNext())
        {
            BattleEntity collided = iter.next();

            //If the target is a ghost, then we don't hit them.
            if (collided.isGhost) continue;

            boolean causeDamage = false;

            //I guess I'm keeping it like this just because...
            switch(entityAllegiance)
            {
                case FRIENDLY:
                    causeDamage = (collided.entityAllegiance == Allegiance.UNFRIENDLY);
                    break;
            }

            if (causeDamage) collided.health -= contactDamage;

        }
    }

}
