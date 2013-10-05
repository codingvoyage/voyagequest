package gui;

import org.newdawn.slick.GameContainer;

import java.util.LinkedList;
import java.util.ListIterator;

/**
 * GUIManager manages all active Gui objects
 * @author Brian Yang
 * @version 2.0
 */
public abstract class GuiManager {

    /** sorted list of active GUI objects, arranged with the active object at the end */
    private static LinkedList<Gui> activeGui = new LinkedList<>();

    /** deleted objects */
    private static LinkedList<Gui> removedGui = new LinkedList<>();

    /**
     * Draw the Gui element
     */
    public static void draw() throws VoyageGuiException {
        ListIterator<Gui> activeIterator = activeGui.listIterator();
        while (activeIterator.hasNext())
            activeIterator.next().draw();
    }

    /**
     * Set a Gui element as active (draw on top)
     * @param select the selected Gui
     */
    public static void select(Gui select) {
        activeGui.remove(select);
        activeGui.offer(select);
    }

    /**
     * Add a new Gui element to the screen
     * @param open the Gui to open
     */
    public static void add(Gui open) {
        activeGui.offer(open);
    }

    /**
     * Close a Gui element by marking it for deletion
     * @param close the Gui to close
     */
    public static void close(Gui close) {
        removedGui.offer(close);
    }

    /**
     * Removes closed items and call the update method of each Gui element
     * @param gc game container
     * @param delta update time
     */
    public static void update(GameContainer gc, int delta) {
        ListIterator<Gui> removeIterator = removedGui.listIterator();
        while (removeIterator.hasNext())
            activeGui.remove(removeIterator.next());
        removedGui.clear();

        ListIterator<Gui> activeIterator = activeGui.listIterator();
        while (activeIterator.hasNext())
            activeIterator.next().next(gc, delta);
    }

    /**
     * Display the contained element in each Gui
     * @throws VoyageGuiException something went wrong
     */
    public static void display() throws VoyageGuiException {
        ListIterator<Gui> activeIterator = activeGui.listIterator();
        while (activeIterator.hasNext())
            activeIterator.next().draw();
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
        ListIterator<Gui> activeIterator = activeGui.listIterator();
        while (activeIterator.hasNext()) {
            Gui next = activeIterator.next();
            if (next.getRect().contains(oldx, oldy)) {
                next.setX(newx);
                next.setY(newy);
            }
        }
    }

    /**
     * Called when the mouse is clicked (but not dragged)
     */
    public static void mouseClicked(int button, int x, int y, int clickCount) {

    }

    /**
     * Retrieve a list of Gui elements
     * @return linked list of Gui element
     */
    public static LinkedList<Gui> getList() {
        return activeGui;
    }

    /**
     * Retrieve the size of the linked list
     * @return the number of active Gui elements
     */
    public int getSize() {
        return activeGui.size();
    }

}
