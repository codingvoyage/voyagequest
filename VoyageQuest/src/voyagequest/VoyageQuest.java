package voyagequest;

import gui.GuiManager;
import gui.VoyageGuiException;
import gui.special.DialogBox;
import map.Camera;
import map.Entity;
import map.Player;
import org.newdawn.slick.*;
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
    /** full screen mode */
    public static final boolean FULLSCREEN = false;
    /** Are we debugging? */
    public static final boolean DEBUG_MODE = false;
    
    /** All the scripts to be read */
    public static ScriptManager scriptCollection;
    /** The actual script reader */
    public static ScriptReader scriptReader;
    /** Manages all the scripting threads */
    public static ThreadManager threadManager;
    
    /** game state */
    public static GameState state = GameState.RPG;
    
    public static Entity player;
    double time;
    public DialogBox dialog;
    
    //testing
    public static boolean haschangedmaps = false;
    
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
        // Load animation data
        JsonReader<Res> reader = new JsonReader<>(Res.class, "res/Animations.json");
        reader.readJson();

        // Load audio data
        reader = new JsonReader<>(Res.class, "res/Audio.json");
        reader.readJson();

        // Initialize the rest of the resource manager
        Res.init();

        // Load all the scripts
        loadScripts();

        // IDK what this is for.
        EventListener.initGc(gc);

        //Create the player
        //The player needs to have a thread...
        Thread playerThread = new Thread(1);
        playerThread.setLineNumber(0);
        playerThread.setName("SebastianThread");
        threadManager.addThread(playerThread);
        player = new Player(new DoubleRect(1400, 4300, 64, 128));

        player.setMainScriptID(1);
        player.setMainThread(threadManager.getThreadAtName("SebastianThread"));

        player.forward = Res.animations.get("Sebastian Forward");
        player.backward = Res.animations.get("Sebastian Backwards");
        player.left = Res.animations.get("Sebastian Left");
        player.right = Res.animations.get("Sebastian Right");

        player.name = "Sebastian";

        player.profile = Res.animations.get("Sebastian Profile");
        if (player.profile == null)
            System.out.println("Still null");


        player.setAnimation(0);

        //Now create the Camera.
        Global.camera = new Camera();

        //Now that we're done with the player and camera, we can load the map itself...
        threadManager.clear();
        Thread loadingThread = new Thread(42);
        loadingThread.setName("LOADINITIALSCRIPT");
        loadingThread.setLineNumber(0);
        threadManager.addThread(loadingThread);
        threadManager.act(0.0);
    }

     /**
     * Loads all scripting relating things
     */
    private void loadScripts() throws SlickException {
        //Initialize the ScriptManager
        scriptCollection = new ScriptManager();
        
        //Load the loader script...
        scriptCollection.loadScript("loader.cfg", 0);
        
        //Initialize ScriptReader, passing it the ScriptManager handle
        scriptReader = new ScriptReader(scriptCollection);
        
        //Initialize the collection of threads
        threadManager = new ThreadManager(scriptReader);
        scriptReader.setThreadHandle(threadManager);
        
        //Now create a thread that uses the loading script, 
        //adding it to threadManager and running it
        Thread loadingThread = new Thread(0);
        loadingThread.setName("LOADINGTHREAD");
        loadingThread.setLineNumber(0);
        threadManager.addThread(loadingThread);
        threadManager.act(0.0);
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
                for (int i = 0; i < Global.currentMap.entities.size(); i++) {
                    Entity e = Global.currentMap.entities.get(i);
                    if (e != null) {
                        e.act(gc, delta);
                    }
                }

                Input input = gc.getInput();

                EventListener.keyboardControl(player, delta);

                threadManager.act(delta);

                GuiManager.update(gc, delta);
                break;
            case COMBAT:
                break;
            default:
                break;
        }
        
    }

    /**
     * Render the game window
     * @param gc the game container
     * @param g the graphics engine
     * @throws SlickException something went horribly wrong with Slick
     */
    @Override
    public void render(GameContainer gc, Graphics g) throws SlickException
    {
        switch (state) {
            case RPG:
                //If there isn't a full screen GUI... draw what the Camera sees
                Global.camera.display(g);

                try {
                    GuiManager.draw();
                    GuiManager.display();
                } catch (VoyageGuiException ex) {}


                Util.FONT.drawString(10, 10, "FPS: " + gc.getFPS());

                double entityX = player.r.x;
                double entityY = player.r.y;


                Util.FONT.drawString(10, 40, "Coordinates of player: (" + entityX + ", " + entityY + ")");

                break;
            case COMBAT:
                break;
            default:
                break;
        }
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
