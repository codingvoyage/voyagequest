package gui.special;

import gui.Gui;
import gui.VoyageGuiException;
import gui.types.Dialog;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;

/**
 * A wrapper class to make creating and dealing with dialog boxes easier. 
 * @author Brian Yang
 */
public class DialogBox {
    
    /** The dialog element */
    private Dialog dialog;
    /** The GUI element */
    private Gui<Dialog> window;
    

    public static final int DEFAULT_WIDTH = 750;
    public static final int DEFAULT_HEIGHT = 160;

    public static final int DEFAULT_X = (voyagequest.VoyageQuest.X_RESOLUTION / 2) - DEFAULT_WIDTH / 2;
    public static final int DEFAULT_Y = voyagequest.VoyageQuest.Y_RESOLUTION - (int)(DEFAULT_HEIGHT * 1.4);

    public String animationId;
    
    /**
     * Default dialog box with default coordinates
     * @param text 
     */
    public DialogBox(String text) {
        dialog = new Dialog(DEFAULT_X, DEFAULT_Y, text, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        this.window = new Gui<>(DEFAULT_X, DEFAULT_Y, DEFAULT_WIDTH, DEFAULT_HEIGHT, dialog);
        dialog.setWindow(window);
    }
    
    /**
     * Dialog box with default coordinates and specified color
     * @param text
     * @param start
     * @param end 
     */
    public DialogBox(String text, Color start, Color end) {
        dialog = new Dialog(DEFAULT_X, DEFAULT_Y, text, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        this.window = new Gui<>(DEFAULT_X, DEFAULT_Y, DEFAULT_WIDTH, DEFAULT_HEIGHT, dialog);
        dialog.setWindow(window);
    }
    
    /**
     * Default dialog box with default coordinates
     * @param text 
     */
    public DialogBox(String text, String animationId) {
        dialog = new Dialog(DEFAULT_X, DEFAULT_Y, text, DEFAULT_WIDTH, DEFAULT_HEIGHT, animationId);
        this.window = new Gui<>(DEFAULT_X, DEFAULT_Y, DEFAULT_WIDTH, DEFAULT_HEIGHT, dialog);
        dialog.setWindow(window);
    }
    
    /**
     * Default dialog box with options and default coordinates
     * @param text 
     * @param options
     */
    public DialogBox(String text, String[] options) {
        dialog = new Dialog(DEFAULT_X, DEFAULT_Y, text, DEFAULT_WIDTH, DEFAULT_HEIGHT, options);
        this.window = new Gui<>(DEFAULT_X, DEFAULT_Y, DEFAULT_WIDTH, DEFAULT_HEIGHT, dialog);
        dialog.setWindow(window);
    }

    /**
     * Default dialog box with default coordinates
     * @param text
     */
    public DialogBox(String text, String animationId, String[] options) {
        dialog = new Dialog(DEFAULT_X, DEFAULT_Y, text, DEFAULT_WIDTH, DEFAULT_HEIGHT, animationId, options);
        this.window = new Gui<>(DEFAULT_X, DEFAULT_Y, DEFAULT_WIDTH, DEFAULT_HEIGHT, dialog);
        dialog.setWindow(window);
    }
    
    /**
     * Dialog box with options, default coordinates, and specified color
     * @param text
     * @param options
     * @param start
     * @param end 
     */
    public DialogBox(String text, String[] options, Color start, Color end) {
        dialog = new Dialog(DEFAULT_X, DEFAULT_Y, text, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        this.window = new Gui<>(DEFAULT_X, DEFAULT_Y, DEFAULT_WIDTH, DEFAULT_HEIGHT, dialog);
        dialog.setWindow(window);
    }
    
    public DialogBox(String text, String[] options, Color start, Color end, String AnimationId) {
        dialog = new Dialog(DEFAULT_X, DEFAULT_Y, text, DEFAULT_WIDTH, DEFAULT_HEIGHT, animationId);
        this.window = new Gui<>(DEFAULT_X, DEFAULT_Y, DEFAULT_WIDTH, DEFAULT_HEIGHT, dialog);
        dialog.setWindow(window);
    }
    
    /**
     * Start the dialog box
     */
    public void start() {
        window.start();
    }
    
    /**
     * Draw the window
     */
    public void draw() {
        window.draw();
    }
    
    /**
     * 
     * @param gc
     * @param delta 
     */
    public void next(GameContainer gc, int delta) {
        window.next(gc, delta);
    }
    
    /**
     * 
     * @throws VoyageGuiException 
     */
    public void printNext() throws VoyageGuiException {
        window.display();
    }
    
    /**
     * Get Gui element
     * @return the gui window
     */
    public Gui<Dialog> getGui() {
        return window;
    }
    
    /**
     * Get the dialog object
     * @return the dialog object
     */
    public Dialog getDialog() {
        return window.getObject();
    }
    
    /**
     * Get the text
     * @return the dialog text
     */
    public String getText() {
        return window.getObject().getText();
    }
    
    /**
     * Get status
     * @return whether or not it is continuing to print
     */
    public boolean continuePrinting() {
        return dialog.getParserStatus();
    }
}