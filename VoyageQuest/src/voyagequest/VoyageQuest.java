package voyagequest;

import battle.BattleField;
import battle.EntityManager;
import battle.WeaponManager;
import gui.GuiManager;
import gui.VoyageGuiException;
import gui.special.DialogBox;
import item.ItemManager;
import map.Camera;
import map.Entity;
import map.Player;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.*;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.openal.SoundStore;
import scripting.ScriptManager;
import scripting.ScriptReader;
import scripting.Thread;
import scripting.ThreadManager;

import java.io.InputStream;

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
    
    public static Entity player;
    public DialogBox dialog;

    // The alpha map being applied for cave effect
    private Image alphaMap;

    //Fade is how visible the screen is. 255 for max, 0 for completely dark.
    public static int fade = 255;
    
    
    /**
     * Construct a new game
     */
    public VoyageQuest() {
       super("Voyage Quest Alpha");
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

        // Initialize the rest of the resource manager
        Res.initAnimations();
        Res.initAudio();

        //Initialize all Itemdata
        ItemManager.init();

        //Initialize all the weapon data
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

        player = new Player(new DoubleRect(1400, 4300, 64, 128));
            player.setMainScriptID("Cutscene.txt");
            player.setMainThread(threadManager.getThreadAtName("SebastianThread"));
            player.forward = Res.animations.get("Sebastian Forward");
            player.backward = Res.animations.get("Sebastian Backwards");
            player.left = Res.animations.get("Sebastian Left");
            player.right = Res.animations.get("Sebastian Right");
            player.name = "Sebastian";
            player.profile = Res.animations.get("Sebastian Profile");
            player.setAnimation(0);

        //Now create the Camera.
        Global.camera = new Camera();

        //Load the battle entities, shouldn't be too bad...
        EntityManager.init();

        //Load the lighting... This will be changed later, of course.
        InputStream is = getClass().getClassLoader().getResourceAsStream("res/alphamini.png");
        alphaMap = new Image(is, "res/alphamini.png", false, Image.FILTER_NEAREST);

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
                //Not sure of this actually does anything... I don't think it does.
                //No. It really doesn't. I'll keep this here just in case in the future
                //we somehow need to call act on all the RPG entities.
//                for (int i = 0; i < Global.currentMap.entities.size(); i++) {
//                    Entity e = Global.currentMap.entities.get(i);
//                    if (e != null)
//                        e.act(delta);
//                }

                EventListener.keyboardControl(player, delta);
                threadManager.act(delta);

                break;
            case COMBAT:
                //IMPORTANT: Tell ScriptReader that now, we're using the
                //battleThreadManager.
                VoyageQuest.scriptReader.setThreadHandle(VoyageQuest.battleThreadManager);
                battleThreadManager.act(delta);

                BattleField.update(delta);

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
                fading(g);

                Util.WHITE_FONT.drawString(10, 40,
                        "Coordinates of player: (" + player.r.x + ", " + player.r.y + ")");
                break;
            case COMBAT:
                //Render the background

                //Render everything in the BattleField
                BattleField.render(gc, g);

                //This might be good to know
                Util.WHITE_FONT.drawString(10, 30, "Instances: " + BattleField.getInstanceCount());

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

    
    /*
     * This technique I gained from the slick2D forums - Edmund
     * The idea is to draw to the alpha channel.
     * Basically, drawing black makes things light
     * Drawing white makes things dark.
     */
    private void fading(Graphics g) {

        //Scale the light map down so it can be a small light map
        float invSizeX = 1f / 20;
        float invSizeY = 1f / 15;
        g.scale(20, 15);

        //Setting alpha channel ready
        g.clearAlphaMap();
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        //This light map works because of reasons stated below - it is like a light source,
        //revealing a part of the map where the AlphaMap image is dark. Black = light, basically
        //drawCentered(X_RESOLUTION/2 * invSizeX, Y_RESOLUTION/2 * invSizeY);
        
        //The faded variable... the color is black, but the alpha channel basically changes,
        //Taking the color from black, to grey, to white. As a result because of the alpha channel
        //the game environment grows from bright to dark.
        //255 --> 0
        g.setColor(new Color(0, 0, 0, fade)); 

        //#faded
        g.fill(new Rectangle(0, 0, X_RESOLUTION * invSizeX, Y_RESOLUTION * invSizeY));

        //Now scale the light map up again
        g.scale(invSizeX, invSizeY);

        //Setting alpha channel for clearing everything but light map just added
        GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_DST_ALPHA);

        //Paint everything else with black
        g.fillRect(0, 0, X_RESOLUTION, Y_RESOLUTION);

        //Setting drawing mode back to normal
        g.setDrawMode(Graphics.MODE_NORMAL);

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
       EventListener.mouseDragged(oldx, oldy, newx, newy);
   }

   /**
    * @see org.newdawn.slick.InputListener#mouseClicked(int, int, int, int)
    */
   @Override
   public void mouseClicked(int button, int x, int y, int clickCount) {
      EventListener.mouseClicked(button, x, y, clickCount);
   }
}
