package gui.types;

/**
 * An object mapped to coordinates
 * @author Brian Yang
 */
public class Coordinate<E> {

    /** the object */
    private E object;
    /** x coordinate */
    private float x;
    /** y coordinate */
    private float y;

    /**
     * Map an object to coordinates
     * @param object the object
     * @param x object's x coordinate
     * @param y object's y coordinate
     */
    public Coordinate(E object, float x, float y) {
        this.object = object;
        this.x = x;
        this.y = y;
    }

    /**
     * Retrieve the object
     * @return the object
     */
    public E getObject() {
        return object;
    }

    /**
     * Retrieve the object's position
     * @return an array of the x and y positions
     */
    public float[] getPosition() {
        float[] position = {x, y};
        return position;
    }

}