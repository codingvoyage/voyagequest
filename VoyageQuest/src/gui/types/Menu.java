package gui.types;

import gui.Displayable;
import gui.VoyageGuiException;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.ShapeRenderer;
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
        y += DialogParser.DIALOG_PADDING;
        boolean first = true;
        for (String s : this.options) {
            Util.p(s + " being drawn at " + x + ", " + y);
            if (first) {
                box = new Rectangle(x, y, Util.FONT.getWidth(s), Util.FONT.getLineHeight());
                ShapeRenderer.draw(box);
                selected = 0;
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
    }

    /**
     * Update with delta time
     * @param delta delta time
     */
    @Override
    public void next(GameContainer gc, int delta) {

    }
}
