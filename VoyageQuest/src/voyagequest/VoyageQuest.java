package voyagequest;

import battle.BattleField;
import battle.EntityManager;
import battle.WeaponManager;
import gui.GuiManager;
import gui.VoyageGuiException;
import item.ItemManager;
import map.Camera;
import map.Player;
import org.newdawn.slick.*;
import org.newdawn.slick.openal.SoundStore;
import scripting.ScriptManager;
import scripting.ScriptReader;
import scripting.Thread;
import scripting.ThreadManager;

/**
 * Voyage Quest RPG
 * Copyright (c) 2013 Team Coding Voyage.
 * 
 * @author Edmund Qiu
 * @author Brian Yang
 */

public class VoyageQuest extends BasicGame {
    /** x resolution */
    public static final int X_RESOLUTION = 1024;
    /** y resolution */
    public static final int Y_RESOLUTION = 768;
    /** rectangle of the screen */
    public static final DoubleRect SCREEN_RECT = new DoubleRect(0, 0, X_RESOLUTION, Y_RESOLUTION);
    /** full screen mode */
    public static final boolean FULLSCREEN = false;
    /** Are we debugging? */
    public static final boolean DEBUG_MODE = false;
    
    /** All the scripts to be read */
    public static ScriptManager scriptCollection;
    /** The actual script reader */
    public static ScriptReader scriptReader;

    /** Manages all the scripting threads for the RPG */
    public static ThreadManager threadManager;
    /** Manages all the scripting threads for the Combat */
    public static ThreadManager battleThreadManager;

    /** game state */
    public static GameState state = GameState.RPG;

    /**
     * Construct a new game
     */
    public VoyageQuest() {
       super("VoyageQuest 0.3 Pre-Beta - For Testing Purposes Only");
    }

    /**
     * Initialize the game container and start the scripting engine
     * @param gc the new game container
     * @throws SlickException something went wrong with the Slick engine
     */
    @Override
    public void init(GameContainer gc) throws SlickException {
        // Get rid of the default FPS count so we can use our own font
        gc.setShowFPS(false);
        
        // Set the minimum and maximum update intervals please
        gc.setMinimumLogicUpdateInterval(20);
        gc.setMaximumLogicUpdateInterval(20);
        
        initRpg(gc);

    }

    private void initRpg(GameContainer gc) throws SlickException {

        // Initialize the resource manager, which handles
        // animations and audio.
        Res.initAnimations();
        Res.initAudio();

        // Initialize all Itemdata
        ItemManager.init();

        // Initialize all the weapon data
        WeaponManager.init();

        // Load all the scripts
        loadScripts();

        // Pass GameContainer to the event listener
        EventListener.initGc(gc);

        //Create the player
        //The player needs to have a thread...
        Thread playerThread = new Thread("PLAYERDOESNOTHING");
            playerThread.setLineNumber(0);
            playerThread.setName("SebastianThread");
            threadManager.addThread(playerThread);

        Global.player = new Player(new DoubleRect(1400, 4300, 64, 128));
            Global.player.setMainScriptID("Cutscene.txt");
            Global.player.setMainThread(threadManager.getThreadAtName("SebastianThread"));
            Global.player.forward = Res.animations.get("Sebastian Forward");
            Global.player.backward = Res.animations.get("Sebastian Backwards");
            Global.player.left = Res.animations.get("Sebastian Left");
            Global.player.right = Res.animations.get("Sebastian Right");
            Global.player.name = "Sebastian";
            Global.player.profile = Res.animations.get("Sebastian Profile");
            Global.player.setAnimation(1);

        //Now create the Camera.
        Global.camera = new Camera();

        //Load the battle entities, shouldn't be too bad...
        EntityManager.init();

        //Now that we're done with the player and camera, we can load the map itself...
        threadManager.clear();
        Thread loadingThread = new Thread("INITIALSCRIPT");
        loadingThread.setLineNumber(0);
        threadManager.addThread(loadingThread);
        threadManager.act(0.0);
    }

     /**
     * Loads all scripting relating things
     */
    private void loadScripts() throws SlickException {
        //Initialize the ScriptManager, which loads every script
        scriptCollection = new ScriptManager();

        //Initialize ScriptReader, passing it the ScriptManager handle
        scriptReader = new ScriptReader(scriptCollection);

        //Initialize the collection of threads
        threadManager = new ThreadManager(scriptReader);
        battleThreadManager = new ThreadManager(scriptReader);

        scriptReader.setThreadHandle(threadManager);
    }
    
    /**
     * Update the screen
     * @param gc the game container
     * @param delta time interval
     * @throws SlickException something went horribly wrong with Slick
     */
    @Override
    public void update(GameContainer gc, int delta) throws SlickException {
        switch (state) {
            case RPG:
                //Update the scripting engine and update the keyboard input
                EventListener.keyboardControl(Global.player, delta);
                threadManager.act(delta);

                break;
            case COMBAT:
                //IMPORTANT: Tell ScriptReader that now, we're using the
                //battleThreadManager.
                VoyageQuest.scriptReader.setThreadHandle(VoyageQuest.battleThreadManager);
                battleThreadManager.act(delta);

                BattleField.update(delta);

                //Separate set of controls for battle
                EventListener.battleKeyboardControl(BattleField.player, delta);

                break;
            default:
                break;
        }

        // Stream polling
        SoundStore.get().poll(0);

        // Updating the GUI happens regardless of what mode we're in.
        GuiManager.update(gc, delta);

    }

    /**
     * Render the game window
     * @param gc the game container
     * @param g the graphics engine
     * @throws SlickException something went horribly wrong with Slick
     */
    @Override
    public void render(GameContainer gc, Graphics g) throws SlickException {

        switch (state) {
            case RPG:
                //If there isn't a full screen GUI... draw what the Camera sees
                Global.camera.display(g);

                Util.WHITE_FONT.drawString(10, 40,
                        "Coordinates of player: (" + Global.player.r.x + ", " + Global.player.r.y + ")");
                break;
            case COMBAT:
                //Render the background

                //Render everything in the BattleField
                BattleField.render(gc, g);

                //This might be good to know
                Util.WHITE_FONT.drawString(10, 30, "Instances: " + BattleField.getInstanceCount());
                Util.WHITE_FONT.drawString(10, 50, "Player HP: " + BattleField.player.health);

                break;
            default:
                break;


        }

        //The GUI gets drawn regardless of whether we're in RPG or COMBAT
        try
        {
            GuiManager.draw();
            GuiManager.display();
        }
        catch (VoyageGuiException ex) {}


        //We're using this for debugging no matter what, so...
        Util.WHITE_FONT.drawString(10, 10, "FPS: " + gc.getFPS());
    }

    
    /**
     * The main method<br/>
     * Create the game window and start the game!
     * @param args command line arguments
     * @throws SlickException something went horribly wrong with Slick
     */
    public static void main(String[] args) throws SlickException {
        AppGameContainer app = new AppGameContainer(new VoyageQuest());
        app.setDisplayMode(X_RESOLUTION, Y_RESOLUTION, FULLSCREEN);
        app.setAlwaysRender(true);
        app.setTargetFrameRate(60);
        app.start();
    }

    /**
     * Keypress Listener
     * @param key the key mapping
     * @param c character pressed
     */
    @Override
    public void keyPressed(int key, char c) {
        EventListener.keyPressed(key, c);
    }

   /**
    * @see org.newdawn.slick.InputListener#mouseMoved(int, int, int, int)
    */
   @Override
   public void mouseMoved(int oldx, int oldy, int newx, int newy) {
       //EventListener.mouseMoved(oldx, oldy, newx, newy);
   }

   /**
    * @see org.newdawn.slick.InputListener#mouseDragged(int, int, int, int)
    */
   @Override
   public void mouseDragged(int oldx, int oldy, int newx, int newy) {
       //EventListener.mouseDragged(oldx, oldy, newx, newy);
   }

   /**
    * @see org.newdawn.slick.InputListener#mouseClicked(int, int, int, int)
    */
   @Override
   public void mouseClicked(int button, int x, int y, int clickCount) {
      //EventListener.mouseClicked(button, x, y, clickCount);
   }
}
