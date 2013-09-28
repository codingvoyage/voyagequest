package voyagequest;

import battle.BattleEntity;
import gui.GuiManager;
import gui.types.Menu;
import map.Entity;
import map.GroupObjectWrapper;
import map.Rectangular;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;

import java.util.LinkedList;

/**
 * Listener for keyboard input and mouse interactions
 * @author Brian Yang
 */
public abstract class EventListener {
    
    public static GameContainer gc;
    public static final double STEP_SIZE = 0.25;
    public static Input input;

    public static Menu menu;
    public static boolean displayingMenu;

    public static String playerOrientation = "NORTH";
    public static final int clickDistance = 42;


    /**
     * Initializes the event listener
     * @param gc game container of the game
     */
    public static void initGc(GameContainer gc) {
        EventListener.gc = gc;
        input = gc.getInput();
    }
    
    /**
     * Controls an entity, typically the player by keyboard
     * @param player the entity to control
     * @param delta delta time
     */
    public static void keyboardControl(Entity player, int delta) throws SlickException {
        //We can't move if Input is frozen
        if (Global.isInputFrozen) return;
        
        double step = STEP_SIZE*delta;
        if(input.isKeyDown(Input.KEY_UP)) {
            player.attemptMove(0, -step, delta);
            player.setAnimation(0);
            playerOrientation = "NORTH";
        }

        if(input.isKeyDown(Input.KEY_DOWN)) {
            player.attemptMove(0, step, delta);
            player.setAnimation(1);
            playerOrientation = "SOUTH";
        }
        
        if (input.isKeyDown(Input.KEY_LEFT)) {
            player.attemptMove(-step, 0, delta);
            
            //unless we're moving up or down already in animation
            if (!input.isKeyDown(Input.KEY_DOWN) && !input.isKeyDown(Input.KEY_UP))
            {
                player.setAnimation(2);
                playerOrientation = "WEST";
            }

        }

        if(input.isKeyDown(Input.KEY_RIGHT)) {
            player.attemptMove(step, 0, delta);
            
            if (!input.isKeyDown(Input.KEY_DOWN) && !input.isKeyDown(Input.KEY_UP))
            {
                player.setAnimation(3);
                playerOrientation = "EAST";
            }
        }

        if(input.isKeyDown(Input.KEY_ENTER))
        {

        }

        if (input.isKeyDown(Input.KEY_E))
        {
            //Just going to hardcode the approximate center of the
            //player's collision box
            double playerCenterX = player.r.x + 30;
            double playerCenterY = player.r.y + 100;

            if (playerOrientation.equals("NORTH"))
                clickEvent(playerCenterX, playerCenterY - clickDistance);
            if (playerOrientation.equals("SOUTH"))
                clickEvent(playerCenterX, playerCenterY + clickDistance);
            if (playerOrientation.equals("EAST"))
                clickEvent(playerCenterX + clickDistance, playerCenterY);
            if (playerOrientation.equals("WEST"))
                clickEvent(playerCenterX - clickDistance, playerCenterY);

        }
    }

    /**
     * Controls an entity, typically the player by keyboard
     * @param player the entity to control
     * @param delta delta time
     */
    public static void battleKeyboardControl(BattleEntity player, int delta) throws SlickException {
        //We can't move if Input is frozen
        if (Global.isInputFrozen) return;

        double step = STEP_SIZE*delta;
        if(input.isKeyDown(Input.KEY_UP)) {
            player.attemptMove(0, -step, delta);
            player.changeAnimationDirection(1);
        }

        if(input.isKeyDown(Input.KEY_DOWN)) {
            player.attemptMove(0, step, delta);
            player.changeAnimationDirection(-1);
        }

        if (input.isKeyDown(Input.KEY_LEFT)) {
            player.attemptMove(-step, 0, delta);
            player.changeAnimationDirection(1);
        }

        if(input.isKeyDown(Input.KEY_RIGHT)) {
            player.attemptMove(step, 0, delta);
            player.changeAnimationDirection(1);
        }

        if(input.isKeyDown(Input.KEY_ENTER))
        {

        }
    }

    /**
     * Acting on a menu
     * @param menu the menu item
     */
    public static void menuListenStart(Menu menu) {
        EventListener.menu = menu;
        displayingMenu = true;
    }

    /**
     * No longer acting on a menu
     */
    public static void menuListenStop() {
        displayingMenu = false;
    }

    /**
     * Called when the up key is pressed
     */
    public static void keyPressed(int key, char c) {
        // controlling menus
        if (displayingMenu) {
            if (key == Input.KEY_UP)
                menu.up();
            if (key == Input.KEY_DOWN)
                menu.down();
            if (key == Input.KEY_Z)
                menu.select();
        }
    }


    /**
     * Called when the mouse is moved
     */
    public static void mouseMoved(int oldx, int oldy, int newx, int newy) {

    }

    /**
     * Called when the mouse is dragged
     */
    public static void mouseDragged(int oldx, int oldy, int newx, int newy) {
        GuiManager.mouseDragged(oldx, oldy, newx, newy);
    }

    /**
     * Called when the mouse is clicked (but not dragged)
     */
    public static void mouseClicked(int button, int x, int y, int clickCount) {
        double clickedMapX = Global.camera.getViewRect().x + x;
        double clickedMapY =  Global.camera.getViewRect().y + y;
        clickEvent(clickedMapX, clickedMapY);
    }

    /**
     * clickEvent will compensate for the fact that the screen rectangle is a certain
     * place on the map. Therefore, the x and y parameters should practically be
     * "mouse click" coordinates
     * @param clickedMapX
     * @param clickedMapY
     */
    public static void clickEvent(double clickedMapX, double clickedMapY)
    {
        //If input is frozen, that includes mouse. Return now.
        if (Global.isInputFrozen == true) return;

        //Later this will be configured so that this is ONLY EXECUTED
        //when the GUI isn't hit, but for now, let's pretend ...

        //////////////////////////////////////////////////////////
        //CLICKING AN EVENT BOUNDARY
        //////////////////////////////////////////////////////////
        LinkedList<GroupObjectWrapper> possibleBoundaries =
                Global.currentMap.events.rectQuery(
                        new DoubleRect(clickedMapX, clickedMapY, 0, 0));

        GroupObjectWrapper clickedBoundary = null;
        for (GroupObjectWrapper b : possibleBoundaries)
        {
            if (b.getRect().contains(clickedMapX, clickedMapY))
            {
                clickedBoundary = b;
                break;
            }
        }

        if (clickedBoundary != null &&
                clickedBoundary.getObject().type.equals("onClick"))
        {
            //From here on, clickedBoundary should contain a valid BoundaryWrapper
            new Interaction(clickedBoundary.getObject().props);
        }


        //////////////////////////////////////////////////////////
        //CLICKING AN ENTITY
        //////////////////////////////////////////////////////////
        //Get all closeby collisions...
        LinkedList<Rectangular> possibleEntities =
                Global.currentMap.collisions.rectQuery(
                        new DoubleRect(clickedMapX, clickedMapY, 0, 0));

        Entity clickedEntity = null;

        //Get the first Entity which collides
        for (Rectangular b : possibleEntities)
        {
            if (b instanceof Entity &&
                    b.getRect().contains(clickedMapX, clickedMapY))
            {
                clickedEntity = (Entity)b;
                break;
            }
        }

        if (clickedEntity != null &&
                clickedEntity.onClickScript != null)
        {
            //From here on, clickedBoundary should contain a valid BoundaryWrapper
            new Interaction(clickedEntity.onClickScript);
        }
    }
}
