package battle;

import voyagequest.DoubleRect;

/**
 * User: Edmund
 */
public class Enemy extends BattleEntity
{
    public Enemy(DoubleRect location,
                 DoubleRect collision) {
        super(location, collision);


        this.entityAllegiance = Allegiance.UNFRIENDLY;
    }




}
