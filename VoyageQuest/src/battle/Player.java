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


    @Override
    public void act(int delta) {
        super.act(delta);

        //If we're still alive and for some reason the collision thing doesn't contain us, re-add ourselves
        this.place(r.x, r.y);

    }

}
