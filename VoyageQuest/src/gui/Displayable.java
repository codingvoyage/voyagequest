package gui;

import org.newdawn.slick.GameContainer;

/**
 * Displayable elements can be nested inside a Gui object
 * @author Brian Yang
 * @version 2.0
 */
public interface Displayable {

    public void draw() throws VoyageGuiException;

    public void next(GameContainer gc, int delta);
}
