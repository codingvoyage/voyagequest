package gui.types;

import gui.Displayable;
import gui.VoyageGuiException;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.ShapeRenderer;
import voyagequest.EventListener;
import voyagequest.Util;

import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Menus and lists <br/>
 * Used for dialog box options as well
 * @author Brian Yang
 */
public class Menu implements Displayable {

    /** x coordinate */
    private float x;
    /** y coordinate */
    private float y;
    /** width */
    private int width;
    /** height */
    private int height;
    /** options */
    private String[] options;
    /** coordinates of each option string */
    private LinkedList<Coordinate> coordinates = new LinkedList<>();
    /** index of selected item */
    private int selected;
    /** the box around the selected item */
    private Rectangle box;
    /** are we done? */
    private boolean done = false;

    /** selected box padding */
    public static final int BOX_PADDING = 10;

    public Menu(float x, float y, int width, int height, Object[] options) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.options = new String[options.length];

        for (int i = 0; i < options.length; i++) {
            this.options[i] = options[i].toString();
        }

        x += DialogParser.DIALOG_PADDING;
        y += DialogParser.DIALOG_PADDING * 0.5;
        boolean first = true;
        for (String s : this.options) {
            Util.p(s + " being drawn at " + x + ", " + y);
            if (first) {
                box = new Rectangle(x - BOX_PADDING / 2, y, Util.FONT.getWidth(s) + BOX_PADDING, Util.FONT.getLineHeight());
                selected = 0;
                first = false;
            }

            coordinates.add(new Coordinate(s, x, y));
            y += Util.FONT.getLineHeight();
        }
    }

    /**
     * Print the Menu
     */
    @Override
    public void print() throws VoyageGuiException {
        ListIterator<Coordinate> print = coordinates.listIterator();
        while (print.hasNext()) {
            Coordinate<String> next = print.next();
            Util.FONT.drawString(next.getPosition()[0], next.getPosition()[1], next.getObject());
        }
        ShapeRenderer.draw(box);
    }

    /**
     * Update with delta time
     * @param delta delta time
     */
    @Override
    public void next(GameContainer gc, int delta) {
        EventListener.menuListenStart(this);
    }

    /**
     * Move selected up
     */
    public int up() {
        if (selected != 0) {
            selected--;
            System.out.println("Moving to: " + selected);
            box.setY(box.getY() - Util.FONT.getHeight(options[selected]) - BOX_PADDING / 2);
            box.setWidth(Util.FONT.getWidth(options[selected]) + BOX_PADDING);
        } else
            System.out.println("Can't move up");
        return 0;
    }

    /**
     * Move selected down
     */
    public int down() {
        if (selected < options.length - 1) {
            selected++;
            System.out.println(options.length + " Moving to: " + selected);
            box.setY(box.getY() + Util.FONT.getHeight(options[selected]) + BOX_PADDING / 2);
            box.setWidth(Util.FONT.getWidth(options[selected]) + BOX_PADDING);
        } else
            System.out.println("Can't move down");
        return 0;
    }

    /**
     * Select the current item
     */
    public void select() {
        done = true;
        EventListener.menuListenStop();
    }

    /**
     * Has an option been selected?
     */
    public boolean isDone() {
        return done;
    }

    /**
     * Get the selected item index
     * @return index of selected item or -1 if none
     */
    public int getOption() {
        if (done)
            return selected;
        else
            return -1;
    }
}
