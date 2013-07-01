package map;

import voyagequest.DoubleRect;
import java.util.LinkedList;

/**
 *
 * @author Edmund
 */
public class QuadTree<E extends Rectangular> {
    public final int MAX_LEVELS;
    public final int MAX_OBJECTS;
    private TreeNode<E> treeRootNode;
    
    public QuadTree(int maxLevel, int maxObjects, DoubleRect boundary)
    {
        MAX_LEVELS = maxLevel;
        MAX_OBJECTS = maxObjects;
        treeRootNode = new TreeNode<>(null, boundary, 0, this);
    }

    public void clear()
    {
        for (E e : treeRootNode.getEntities())
            treeRootNode.removeEntity(e);
    }

    public void addEntity(E e)
    {  
        treeRootNode.addEntity(e);
    }
    
    public void removeEntity(E e)
    {
        treeRootNode.removeEntity(e);
        treeRootNode.adjustPartitions(e);
    }
    
    public int getSize()
    {
        return treeRootNode.getSize();
    }
    
    public int getPartitionCount()
    {
        return getPartitions().size();
    }
    
    public LinkedList<TreeNode> getPartitions()
    {
        return treeRootNode.getPartitionBoxes();
    }
    
    public LinkedList<E> rectQuery(DoubleRect queryRect)
    {
        return treeRootNode.rectQuery(queryRect);
    }
}
