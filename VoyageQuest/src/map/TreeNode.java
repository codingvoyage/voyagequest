package map;

import voyagequest.DoubleRect;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 *
 * @author Edmund Qiu
 */
public class TreeNode<E extends Rectangular> {
    public static final int UL = 0;
    public static final int UR = 1;
    public static final int BL = 2;
    public static final int BR = 3;
    
    private QuadTree tree;
    private TreeNode parent;
    private TreeNode[] children;
    
    public int level;
    public DoubleRect boundary;
    private boolean isLeaf;
    private LinkedList<E> entities = new LinkedList<>();
    
    public TreeNode(TreeNode parent, DoubleRect boundary, int level, QuadTree tree)
    {
        this.tree = tree;
        this.parent = parent;
        this.boundary = boundary;
        this.level = level;
        this.isLeaf = true;
    }

    public void addEntity(E e)
    {
        if (isLeaf)
        {
            entities.add(e);
            //Have we gone over the limit though? And are we under the level-limit?
            if (this.getSize() > tree.MAX_OBJECTS && level < tree.MAX_LEVELS)
            {
                //If so, then we must split.
                this.splitBranches();
                
                //Now, distribute the stuff in our List between our new branches.
                for (E toBeMoved : entities)
                {
                    for (int i = 0; i < 4; i++)
                    {
                        if (children[i].boundary.intersects(toBeMoved.getRect()))
                        {
                            children[i].addEntity(toBeMoved);
                        }
                    }
                }
                
                //Now, get rid of our entities
                entities = new LinkedList<>();
            }
        }
        else
        {
            //Not a leaf... then simply add to whichever child needs it
            for (int i = 0; i < 4; i++)
            {
                if (children[i].boundary.intersects(e.getRect()))
                {
                    children[i].addEntity(e);
                }
            }
        }
    }

    public void removeEntity(E e)
    {
        //If this is a leaf...
        if (isLeaf)
        {
            entities.remove(e);
        }
        else
        {
            //Recurse through ...
            for (TreeNode child : children)
                if (child.boundary.intersects(e.getRect())) child.removeEntity(e);
        }
    }
    
    /**
     * Helper method called after removing an Entity. Basically, recursively traces
     * the path that it took to remove Entity, and merges partitions with a size
     * of under the QuadTree's maximum.
     * @param e 
     */
    public void adjustPartitions(E e)
    {
        //Alright, if this is a leaf, then we're done.
        if (isLeaf) return;
        
        //Okay so this is not a leaf...
        for (TreeNode t : children)
        {
            if (t.boundary.intersects(e.getRect())) 
                t.adjustPartitions(e);
        }
        
        //Now, are we a penultimate node, with all-leaf children?
        if (children[UL].isLeaf == true &&
            children[UR].isLeaf == true &&
            children[BL].isLeaf == true &&
            children[BR].isLeaf == true)
        {
            //We need to check whether the number of entities (without repeats)
            //is less than or equal to the MAX. If it is less than or equal to,
            //then we merge. If at any point it's greater than, then we bail.
            
            //Get all the candidates.
            LinkedList<E> candidateList = new LinkedList<>();
            for (TreeNode t : children)
                candidateList.addAll(t.getEntities());
          
            //Make one pass through the list to remove the ones that fit.
            ListIterator iter = candidateList.listIterator();
            LinkedList<E> cleanedList = new LinkedList<>();
            
            while (iter.hasNext())
            {
                E ent = (E)iter.next();
                //If it fits perfectly...
                if (this.boundary.contains(ent.getRect()))
                {
                    cleanedList.add(ent);
                    iter.remove();
                    
                    //If we've hit the limit already, then we don't merge.
                    if (cleanedList.size() > tree.MAX_OBJECTS)
                        return;
                }
            }
            
            //Now, candidateList is filled with the duplicates!
            //For each given thing on candidateList, there is either one copy or
            //three copies. 
            while (iter.hasNext())
            {
                iter = candidateList.listIterator();
                E ent = (E)iter.next();
                iter.remove();
                boolean searching = true;
                int numberFound = 0;
                while (searching)
                {
                    E currentComparisonEnt = (E)iter.next();
                    if (currentComparisonEnt.equals(ent))
                    {
                        iter.remove();
                        numberFound++;
                        
                        if (numberFound > 1)
                        {
                            if (numberFound == 3)
                            {
                                searching = false;
                                cleanedList.add(ent);
                            }
                        }
                        else 
                        {
                            //Alright, well... are there two more to go? (Like, 
                            int collNumber = 0;
                            for (TreeNode t : children)
                            {
                                if (t.boundary.intersects(ent.getRect())) collNumber++;
                            }
                            if (collNumber == 2)
                            {
                                searching = false;
                                cleanedList.add(ent);
                            }
                        }
                    }
                }
                    
                //If at any point we can bail...
                if (cleanedList.size() > tree.MAX_OBJECTS)
                    return;
            }
            
            entities = cleanedList;
            children = null;
            isLeaf = true;
            
        }
        
    }
    
    
    private void merge()
    {
        this.entities = new LinkedList<>();
        
        //Take the this.entities from the children branch and put them in our branch
        this.entities.addAll(children[UL].getEntities());
        this.entities.addAll(children[UR].getEntities());
        this.entities.addAll(children[BL].getEntities());
        this.entities.addAll(children[BR].getEntities());
        
        //We are now a childless leaf
        children = null;
        isLeaf = true;
    }
    
    public LinkedList<E> rectQuery(DoubleRect queryRect)
    {
        if (isLeaf)
        {
            //If this is a leaf, then we will return its belongings.
            return entities;
        }
        else
        {
            //For each child node, see if it touches the rect. If so, recursively
            //make the child node inspect its options too.
            LinkedList<E> childrenEntities = new LinkedList<>();
            for (TreeNode t : children)
            {
                if (t.boundary.intersects(queryRect))
                    childrenEntities.addAll(t.rectQuery(queryRect));
            }
            return childrenEntities;
        }
    }
    
    private void splitBranches()
    {
        int halfWidth = (int)(boundary.getWidth() / 2);
        int halfHeight = (int)(boundary.getHeight() / 2);
        int topULX = (int)(boundary.getX());
        int topULY = (int)(boundary.getY());
        
        DoubleRect ULRect = new DoubleRect(topULX, topULY, halfWidth, halfHeight);
        DoubleRect URRect = new DoubleRect(topULX + halfWidth, topULY, halfWidth, halfHeight);
        DoubleRect BLRect = new DoubleRect(topULX, topULY + halfHeight, halfWidth, halfHeight);
        DoubleRect BRRect = new DoubleRect(topULX + halfWidth, topULY + halfHeight, halfWidth, halfHeight);
        
        //public TreeNode(TreeNode parent, DoubleRect boundary, int level, QuadTree tree)
        children = new TreeNode[4];
        children[UL] = new TreeNode(this, ULRect, this.level + 1, tree);
        children[UR] = new TreeNode(this, URRect, this.level + 1, tree);
        children[BL] = new TreeNode(this, BLRect, this.level + 1, tree);
        children[BR] = new TreeNode(this, BRRect, this.level + 1, tree);
        
        //We are no longer a leaf
        isLeaf = false;
    }
    
    public LinkedList<TreeNode> getPartitionBoxes()
    {
        LinkedList<TreeNode> containedBoxes = new LinkedList<>();
        containedBoxes.add(this);
        if (!isLeaf)
        {
            for (int i = 0; i < 4; i++)
            {
                containedBoxes.addAll(children[i].getPartitionBoxes());
            }
        }
        return containedBoxes;
    }
    
    public boolean isLeaf()
    { 
        //return isLeaf && children == null; 
        return isLeaf; 
    }
    
    public int getSize()
    {
        //If this is a leaf...
        if (isLeaf)
        {
            return entities.size();
        }
        else
        {
            //Recursively find sum
            int sum = children[UL].getSize() + children[UR].getSize() +
                    children[BL].getSize() + children[BR].getSize();
            return sum;
        }
    }
    
    public TreeNode getParent()
    {
        return parent;
    }
    
    public LinkedList<E> getEntities()
    {
        return entities;
    }
}