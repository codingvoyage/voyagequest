package battle;

import voyagequest.DoubleRect;

/**
 * User: Edmund
 * Date: 6/30/13
 */
public class Player extends BattleEntity
{
    public Player(DoubleRect location,
                  DoubleRect collision) {

        super(location, collision);
        this.entityAllegiance = Allegiance.FRIENDLY;
    }



}
