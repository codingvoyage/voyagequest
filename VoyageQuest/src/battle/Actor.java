package battle;

import voyagequest.DoubleRect;

/**
 * User: Edmund
 * Date: 6/30/13
 */
public class Actor extends BattleEntity {

    public Actor(DoubleRect location,
                 DoubleRect collision,
                 Allegiance actorAllegiance) {

        super(location, collision);
        this.entityAllegiance = actorAllegiance;
    }

}
