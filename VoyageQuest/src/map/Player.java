package map;

import org.newdawn.slick.SlickException;
import voyagequest.DoubleRect;

/**
 *
 * @author Edmund
 */
public class Player extends Entity {
    
    private double playerVelocity;
    
    public Player(DoubleRect boundaryRect) throws SlickException
    {
        super(boundaryRect);
        playerVelocity = 0.15;
    }
}
