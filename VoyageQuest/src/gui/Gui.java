package gui;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.fills.GradientFill;
import org.newdawn.slick.geom.RoundedRectangle;
import org.newdawn.slick.geom.ShapeRenderer;
import org.newdawn.slick.geom.Vector2f;

import java.util.LinkedList;
        
/**
 * Fixed dimension GUI element
 * @author Brian Yang
 */
public class Gui<E extends Displayable> implements Displayable {
    
    /** position */
    private Vector2f position;
    
    /** width */
    public final int width;
    /** height */
    public final int height;
    
    /** background image */
    private RoundedRectangle rect;
    /** corner radius */
    public final float CORNER_RADIUS;
    
    /** gradient start color */
    private Color start = new Color(0, 29, 255, 195); // Color: #001dff with alpha
    /** gradient end color */
    private Color end = new Color(205, 255, 145, 195); // Color #CDFF91 with alpha
    
    /** the object contained in the GUI element */
    private E object;
    
    /** nested Gui objects */
    private LinkedList<Gui> nested = new LinkedList<>();
    
    /**
     * Construct a new default bounded GUI
     * @param x top left x-coordinate
     * @param y top left y-coordinate
     * @param width width of window
     * @param height height of window
     * @param object the object contained in the GUI element
     */
    public Gui(float x, float y, int width, int height, E object) {
        position = new Vector2f(x, y);
        this.width = width;
        this.height = width;
        
        CORNER_RADIUS = 20.0f;
        rect = new RoundedRectangle(x, y, width, height, CORNER_RADIUS);
        
        this.object = object;
    }
    
    /**
     * Construct a new bounded GUI with specified corner radius
     * @param x top left x-coordinate
     * @param y top left y-coordinate
     * @param width width of window
     * @param height height of window
     * @param cornerRadius rounded corner radius
     * @param object the object contained in the GUI element
     */
    public Gui(float x, float y, int width, int height, float cornerRadius, E object) {
        position = new Vector2f(x, y);
        this.width = width;
        this.height = width;
        
        CORNER_RADIUS = cornerRadius;
        rect = new RoundedRectangle(x, y, width, height, CORNER_RADIUS);
        
        this.object = object;
    }
    
    /**
     * Construct a new bounded GUI with a specified corner radius and a constant color
     * @param x top left x-coordinate
     * @param y top left y-coordinate
     * @param width width of window
     * @param height height of window
     * @param cornerRadius rounded corner radius
     * @param color the color throughout
     * @param object the object contained in the GUI element
     */
    public Gui(float x, float y, int width, int height, float cornerRadius, Color color, E object) {
        position = new Vector2f(x, y);
        this.width = width;
        this.height = width;
        
        CORNER_RADIUS = cornerRadius;
        rect = new RoundedRectangle(x, y, width, height, CORNER_RADIUS);
        
        start = color;
        end = color;
        
        this.object = object;
    }
    
    /**
     * Construct a new bounded GUI with a specified corner radius and gradient coloring
     * @param x top left x-coordinate
     * @param y top left y-coordinate
     * @param width width of window
     * @param height height of window
     * @param cornerRadius rounded corner radius
     * @param start the start gradient color
     * @param end the end gradient color
     * @param object the object contained in the GUI element
     */
    public Gui(float x, float y, int width, int height, float cornerRadius, Color start, Color end, E object) {
        position = new Vector2f(x, y);
        this.width = width;
        this.height = width;
        
        CORNER_RADIUS = cornerRadius;
        rect = new RoundedRectangle(x, y, width, height, CORNER_RADIUS);
        
        this.start = start;
        this.end = end;
        
        this.object = object;
    }
    
    /**
     * Construct a new bounded GUI with default specified corner radius and a constant color
     * @param x top left x-coordinate
     * @param y top left y-coordinate
     * @param width width of window
     * @param height height of window
     * @param color the color throughout
     * @param object the object contained in the GUI element
     */
    public Gui(float x, float y, int width, int height, Color color, E object) {
        position = new Vector2f(x, y);
        this.width = width;
        this.height = width;
        CORNER_RADIUS = 40.0f;
        rect = new RoundedRectangle(x, y, width, height, CORNER_RADIUS);
        
        start = color;
        end = color;
        
        this.object = object;
    }
    
    /**
     * Construct a new bounded GUI with default specified corner radius and gradient coloring
     * @param x top left x-coordinate
     * @param y top left y-coordinate
     * @param width width of window
     * @param height height of window
     * @param start the start gradient color
     * @param end the end gradient color
     * @param object the object contained in the GUI element
     */
    public Gui(float x, float y, int width, int height, Color start, Color end, E object) {
        position = new Vector2f(x, y);
        this.width = width;
        this.height = width;
        
        CORNER_RADIUS = 40.0f;
        rect = new RoundedRectangle(x, y, width, height, CORNER_RADIUS);
        
        this.start = start;
        this.end = end;
        
        this.object = object;
    }
    
    /**
     * Starts the Gui element
     */
    
    public void start() {
        GuiManager.add(this);
    }
    
    /**
     * Draw the GUI element
     */
    @Override
    public void print() {
        draw();
    }
    
    /**
     * Draw the GUI element
     */
    public void draw() {
        ShapeRenderer.fill(rect, (new GradientFill(0, 0, start, width/3, height/3, end, true)) );
    }
    
    /**
     * Update with delta time
     */
    public void next(GameContainer gc, int delta) {
        object.next(gc, delta);
    }
    
    /**
     * Display the contained object
     */
    
    public void display() throws VoyageGuiException {
        object.print();
    }
    
    /**
     * 
     */
    public void add(Gui nested) {
        this.nested.add(nested);
    }
    
    /**
     * Retrieve the rounded rectangle, used by event listener
     * @return the rectangle used by the Gui element
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
        rect.setX(x);
    }
    
    /**
     * 
     * @param y 
     */
    public void setY(float y) {
        position.y = y;
        rect.setY(y);
    }
    
    /**
     * 
     * @return 
     */
    public Vector2f getPos() {
        return position;
    }
    
    /**
     * Retrieve x position
     * @return the x position
     */
    public float getX() {
        return position.x;
    }
    
    /**
     * Retrieve y position
     * @return the y position
     */
    public float getY() {
        return position.y;
    }
    
    /**
     * Get the contained object
     * @return the object contained the GUI element
     */
    
    public E getObject() {
        return object;
    }
}
