package battle;

import voyagequest.DoubleRect;

/**
 * User: Edmund
 * Date: 6/30/13
 */
public class Projectile extends BattleEntity {


    public Projectile(DoubleRect location,
                      DoubleRect collision,
                      Allegiance projectileAllegiance) {
        super(location, collision);
        this.entityAllegiance = projectileAllegiance;
    }



}
