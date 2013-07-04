package voyagequest.special;

import org.newdawn.slick.Animation;
import voyagequest.DoubleRect;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * User: Edmund
 */
public class LoadBattleEntity
{
    //Identification
    public String name;
    public String battleEntityID;
    public String description;

    //Basic dimensions
    public int width;
    public int height;
    public double velocityX;
    public double velocityY;

    //Don't forget the collisionbox
    public DoubleRect collisionBox;
    public int collisionX;
    public int collisionY;
    public int collisionWidth;
    public int collisionHeight;

    //Script...
    public String mainScriptID;

    //Animations
    public LinkedList<String> animations;
    public HashMap<String, Animation> loadedAnimations;
    public String startingAnimationName;

    //For projectiles
    public String explosionAnimation;

    //For enemies and projectiles
    public int contactDamage;

    //Stats
    public int hp;
    public int defense;
    public int attack;



}
