package map;

import java.util.HashMap;
import java.util.Properties;

import org.newdawn.slick.*;
import org.newdawn.slick.tiled.GroupObject;

import gui.special.DialogBox;
import java.util.LinkedList;

import scripting.Thread;
import voyagequest.*;
import scripting.*;

/**
 *
 * @author user
 */
public class Entity extends ScriptableClass implements Rectangular {
    public String name;
    public String instanceID;
    
    /** The entity's boundary rectangle. For this, r.x and r.y are
      * the actual coordinates of the Entity in Map space. Width and
      * height are the width and height of the entity, which are likely
      * to be 64, 128 respectively.
      */
    public DoubleRect r;
    
    /** 
     * The entity's collision rectangle RELATIVE to its boundary.
     */
    public DoubleRect collRect;
    
    //Used for rendering Entities, set by another class.
    public RenderSetting renderSetting;
    
    //Animation
    protected double accumulatedDelta = 0.0d;
    protected int currentFrame = 0;
    public Animation currentAnimation;
    
    //Automated movement with scripting uses these
    public double velocityX;
    public double velocityY;

    /** BattleEntity handles Animation quite differently, so do this instead */
    public HashMap<String, Animation> animations;

    public Animation forward;
    public Animation backward;
    public Animation left;
    public Animation right;
    
    public Animation profile;
    public boolean profLeft;
    
    public String onClickScript;
    public String onTouchScript;

    public int animationDirection = 1;
    public int rotationInDegrees = 0;

    /**
     * Constructs an Entity with only its boundary Rectangle
     * 
     * @param boundaryRect the boundary Rectangle
     * @throws SlickException 
     */
    public Entity(DoubleRect boundaryRect)
    {
        this.r = boundaryRect;
        this.collRect = new DoubleRect(5.0d, 70.0d, 50.0d, 50.0d);
        
        //-1 unless set otherwise later.
        onTouchScript = null;
        onClickScript = null;
    }

    public void setAnimation(int index)
    {
        switch (index)
        {
            case 0:
                if (currentAnimation != backward) {
                    currentAnimation = backward;
                    resetAnimationTiming();
                }
                break;
            case 1:
                if (currentAnimation != forward) {
                    currentAnimation = forward;
                    resetAnimationTiming();
                }
                break;
            case 2:
                if (currentAnimation != left) {
                    currentAnimation = left;
                    resetAnimationTiming();
                }
                break;
            case 3:
                if (currentAnimation != right) {
                    currentAnimation = right;
                    resetAnimationTiming();
                }
                break;
        }
    }

    public void setAnimation(String animationID)
    {
        currentAnimation = animations.get(animationID);
        resetAnimationTiming();
    }
    
    public void resetAnimationTiming()
    {
        currentFrame = 0;
        accumulatedDelta = 0.0d;
    }

    public void changeAnimationDirection(int newAnimationDirection)
    {
        animationDirection = newAnimationDirection;
    }

    public DoubleRect getCollRect()
    {
        return new DoubleRect(
                r.x + collRect.x,
                r.y + collRect.y,
                collRect.width,
                collRect.height);
    }

    public void setRotation(int newRotationDegrees)
    {
        rotationInDegrees = newRotationDegrees;
    }
    
    public void updateAnimation(double delta)
    {
        accumulatedDelta += delta;
        double currentFrameDuration = currentAnimation.getDuration(currentFrame);

        if (accumulatedDelta > currentFrameDuration)
        {
            currentFrame += animationDirection;
            if (currentFrame >= currentAnimation.getFrameCount())
            {
                currentFrame = 0;
            }
            if (currentFrame < 0)
            {
                currentFrame = currentAnimation.getFrameCount() - 1;
            }

            accumulatedDelta =- currentFrameDuration;

        }
    }

    public void draw(Graphics g, float xOffset, float yOffset)
    {
        //Lawd I am sorry for this code.
        Image img = currentAnimation.getImage(currentFrame);
        img.setRotation(rotationInDegrees);
        img.draw(xOffset, yOffset);
        img.setRotation(0);

        //If debugging, then draw the boundary boxes and the collision boxes
        if (VoyageQuest.DEBUG_MODE == true)
        {
            g.drawRect(xOffset, yOffset, (float)r.width, (float)r.height);
            g.drawRect((float)(xOffset + collRect.x),
                       (float)(yOffset + collRect.y),
                       (float)collRect.width,
                       (float)collRect.height);
        }

    }

    public void setVelocity(double vx, double vy)
    {
        velocityX = vx;
        velocityY = vy;
    }

    public void beginMove(double pixelsToMove, Thread actingThread) {
        actingThread.setTemporaryParameter(new Parameter(pixelsToMove));
        actingThread.setRunningState(true);
    }

    public void beginMove(int tilesToMove, Thread actingThread) {
        int pixelsToMove = tilesToMove * Global.currentMap.TILE_LENGTH;
        actingThread.setTemporaryParameter(new Parameter(pixelsToMove));
        actingThread.setRunningState(true);
    }


    /**
     * Checks if the entity should continue moving (straight line)
     * @param delta elapsed time between checks
     * @return boolean indicating whether or not the entity should continue moving
     */
    public boolean continueMove(double delta, Thread actingThread) {
        //Get the distance left to move...
        Parameter tempParam = actingThread.getTemporaryParameter();

        double xStep = velocityX * delta;
        double yStep = velocityY * delta;
        
        boolean didWeMove = attemptMove(xStep, yStep, delta);
        
        if (didWeMove)
        {
            //Successfully moved, update temporary variable
            // !! Why am I using sqrt() D: D: D: D:
            double movedDistance = Math.sqrt(xStep*xStep + yStep*yStep);

            tempParam.setDoubleValue(
                   tempParam.getDoubleValue() - 
                   movedDistance);

            actingThread.setTemporaryParameter(tempParam);

            if (tempParam.getDoubleValue() < 0)
            {
                //Oh, so we're done moving. Great.
                actingThread.setRunningState(false);
                return false;
            }
        }
        
        //Alright, we still have to move. Keep moving.
        return true;
    }


    public boolean checkCollision(DoubleRect collCandidate)
    {

        //Now query the QuadTree for any possible collisions
        LinkedList<Rectangular> collisionCandidates = Global.currentMap.collisions.rectQuery(collCandidate);

        Rectangular collRectangular = null;

        //Now we see if collides
        boolean collides = false;
        for (Rectangular e : collisionCandidates)
        {
            if (e.equals(this)) continue;

            if (e instanceof Entity && collCandidate.intersects(((Entity)e).getCollRect()))
            {
                collRectangular = e;
                collides = true;
                break;
            }

            if (!(e instanceof Entity) && collCandidate.intersects(e.getRect()))
            {
                collRectangular = e;
                collides = true;
                break;
            }
        }

        //No matter what, we must check with events...
        LinkedList<Rectangular> events = Global.currentMap.events.rectQuery(collCandidate);
        for (Rectangular e : events)
        {
            if (collCandidate.intersects(e.getRect()))
            {
                GroupObject thisObject = ((GroupObjectWrapper)e).getObject();

                if (thisObject.type.equals("onTouch"))
                {
                    Properties asdf = ((GroupObjectWrapper)e).getObject().props;
                    if (asdf != null)
                        new Interaction(asdf);
                }
            }
        }


        if (collides)
        {

            //If we were the player and we collided with an Entity, then see
            //if there is a TouchScript associated with the Entity and
            //respond accordingly.
            if (this instanceof Player &&
                    collRectangular instanceof Entity &&
                    ((Entity)collRectangular).onTouchScript != null)
            {
                new Interaction(((Entity)collRectangular).onTouchScript);
            }


            //On the other hand if we were the Entity and we collided with the player...
            if (this instanceof Entity &&
                    collRectangular instanceof Player)
                System.out.println("OMGGG");
        }
        return collides;
    }

    //The collision --> event code sucks and should be replaced with something in 
    //the overridden version of Entity --> Player since all of them work only
    //for the player!
    public boolean attemptMove(double xMove, double yMove, double delta)
    {
        //Where would we end up at
        DoubleRect collCandidate = this.getCollRect();
        collCandidate.x += xMove;
        collCandidate.y += yMove;

        boolean collides = this.checkCollision(collCandidate);


        if (collides)
        {
            //Could not move
            return false;
        }
        else
        {

            //Update the animation:
            this.updateAnimation(delta);
        
        
            //Move to the proposed location
            place(r.x + xMove, r.y + yMove);
            
            return true;
        }
        
    }
    
    public void place(double newX, double newY)
    {
        //Move to the proposed location
        Global.currentMap.collisions.removeEntity(this);
        r.x = newX;
        r.y = newY;
        Global.currentMap.collisions.addEntity(this);
    }
    
    /**
     * Remember, collision box is the box of collision, with its position in space
     * as well as its width and height.
     * Boundary box is the entity's x, y location in space, and its width and height.
     * 
     * @return the double rect of the boundaries of this Entity.
     */
    public DoubleRect getRect()
    {
        return r;
    }
    
    public void act(int delta)
    {
        
    }
    
    
}