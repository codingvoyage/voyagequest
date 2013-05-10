package gui;

/**
 * Voyage GUI is a custom designed graphical user interface 
 * intended to handle display of items such as the player's 
 * inventory, quest manager, dialog boxes, and more.
 * 
 * @author Brian Yang
 * @version 1.0
 */
public interface Gui<E extends Displayable> {
    
    /** Draw the GUI element */
    public void draw();
    
    /** Return the object contained in the GUI element */
    public E getObject();
}
