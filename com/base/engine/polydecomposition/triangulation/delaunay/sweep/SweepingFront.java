package com.base.engine.polydecomposition.triangulation.delaunay.sweep;

import com.base.engine.polydecomposition.triangulation.TriangulationVec;

/**
 * Algorithm by which the advancing nodes operate
 * 
 * @author Jordan
 */
public class SweepingFront
{
    public SweepingFrontNode head;
    public SweepingFrontNode tail;
    protected SweepingFrontNode search;
   
    /**
     * Initialise the advancing front
     * 
     * @param head head node of the front
     * @param tail tail node of the front
     */
    public SweepingFront(SweepingFrontNode head, SweepingFrontNode tail)
    {
        this.head = head;
        this.tail = tail;
        this.search = head;
    }
   
    /**
     * Fetch the node dedicated to searching for the next point
     * 
     * @return search node of the front
     */
    private SweepingFrontNode findSearchNode()
    {
        return search;
    }

    /**
     * We use a balancing tree to locate a node smaller or equal to given key value
     * 
     * @param vector key value to locate a node
     * @return located node
     */
    public SweepingFrontNode locateNode(TriangulationVec vector)
    {
        return locateNode(vector.getX());
    }
    
    /**
     * Locate a node smaller or equal to the given X coordinate
     * 
     * @param x key value to locate a node
     * @return located node
     */
    private SweepingFrontNode locateNode(double x)
    {
        SweepingFrontNode node = findSearchNode();
        if(x < node.value)
        {
            while((node = node.prev) != null)
            {
                if(x >= node.value)
                {
                    search = node;
                    return node;
                }
            }
        }
        else
        {
            while((node = node.next) != null)
            {
                if(x < node.value)
                {
                    search = node.prev;
                    return node.prev;
                }
            }
        }
        return null;
    }
   
    /**
     * This implementation will use simple node traversal algorithm to find a point on the front through the X-axis
     * 
     * @param vector key value to locate point by
     * @return located point
     */
    public SweepingFrontNode locatePoint(final TriangulationVec vector)
    {
        final double vectorX = vector.getX();
        SweepingFrontNode node = findSearchNode();
        final double nodeX = node.vector.getX();
        
        if(vectorX == nodeX)
        {
            if(vector != node.vector)
            {
                //We might have two nodes with same x value
                if(vector == node.prev.vector)
                {
                    node = node.prev;
                }
                else if(vector == node.next.vector)
                {
                    node = node.next;
                }
                else
                {
                    throw new RuntimeException("Failed to find Node for given afront point");
                }
            }
        }
        else if(vectorX < nodeX)
        {
            while((node = node.prev) != null)
            {
                if(vector == node.vector)
                {
                    break;
                }
            }
        }
        else
        {
            while((node = node.next) != null)
            {
                if(vector == node.vector)
                {
                    break;
                }
            }
        }
        search = node;
        return node;
    }
}
