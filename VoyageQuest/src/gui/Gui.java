package gui;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.fills.GradientFill;
import org.newdawn.slick.geom.RoundedRectangle;
import org.newdawn.slick.geom.ShapeRenderer;
import org.newdawn.slick.geom.Vector2f;

/**
 * Voyage GUI
 * GUI elements can display all types of interactable elements that display
 * useful information, from dialog boxes to menus to inventory. Elements object in
 * a GUI must implement Displayable
 * @author Brian Yang
 * @version 2.0
 *
 * Changes from v1.0
 * - Absolutely no dependencies on the rest of the game.
 * - Minimized the number of options available in the constructors
 * - Options like colors are now set via a separate method. Default color available.
 * - (Coming soon) Gui elements by default cannot be dragged. This option is toggled by a method.
 * - (Coming soon) Gui elements are not clickable by default. This option is toggled by a method.
 * - Easier to understand wrapper classes for game-specific Gui objects, such as dialogs and windows.
 * - Dialog parser is now a nested class since its only used within Dialog
 * - Due to modifications with the scripting command, dialog options are now accepted as a String[] instead of an Object[]
 * - New font: Roboto from Android 4.0 (Source: Google Web Fonts)
 */
public class Gui<E extends Displayable> implements Displayable {

    /** position of GUI element */
    private Vector2f position;
    /** width of GUI element */
    private float width;
    /** height of GUI element */
    private float height;

    /** corner radius of GUI element */
    private static float CORNER_RADIUS = 40.0f;
    /** rectangle of the GUI object */
    private RoundedRectangle rect;
    /** rectangle gradient start color */
    private Color start = new Color(0, 29, 255, 195); // Color: #001dff with alpha
    /** rectangle gradient end color */
    private Color end = new Color(205, 255, 145, 195); // Color #CDFF91 with alpha

    /** Displayable element object in Gui */
    private E object;

    /**
     *
     * @param width
     * @param height
     * @param object
     */
    public Gui (float x, float y, float width, float height, E object) {
        position = new Vector2f(x, y);
        this.width = width;
        this.height = width;
        rect = new RoundedRectangle(x, y, width, height, CORNER_RADIUS);

        this.object = object;
    }

    /**
     *
     */
    @Override
    public void draw() throws VoyageGuiException {
        ShapeRenderer.fill(rect, (new GradientFill(0, 0, start, width / 3, height / 3, end, true)));
        object.draw();
    }

    /**
     * Set Color
     * @param start start color
     * @param end end color
     */
    public void setColor(Color start, Color end) {
        this.start = start;
        this.end = end;
    }

    /**
     * Add this GUI object to the GuiManager to be displayed
     */
    public void start() {
        GuiManager.add(this);
    }

    /**

     */
    @Override
    public void next(GameContainer gc, int delta) {
        object.next(gc, delta);
    }

    /**
     * 
     * @return
     */
    public RoundedRectangle getRect() {
        return rect;
    }

    /**
     * 
     * @param x
     */
    public void setX(float x) {
        position.x = x;
    }

    /**
     * 
     * @param y
     */
    public void setY(float y) {
        position.y = y;
    }

    /**
     * @return the nested Displayable object
     */
    public E getObject() {
        return object;
    }
}
