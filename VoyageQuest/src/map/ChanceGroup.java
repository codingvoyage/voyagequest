package map;

/**
 * Created with IntelliJ IDEA.
 * To manage the chances of choosing something.
 * User: Edmund
 * Date: 6/20/13
 */
public class ChanceGroup {
    public int lowerBound;
    public int upperBound;
    public String result;

    /**
     * Returns whether the rolledValue is within [lowerBound, upperBound)
     * @param rolledValue a randomly generated value to be compared to this ChanceGroup's bounds
     * @return whether the rolledValue is within [lowerBound, upperBound)
     */
    public boolean determine(int rolledValue)
    {
         return lowerBound <= rolledValue && rolledValue < upperBound;
    }
}
