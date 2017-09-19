package com.base.engine.polydecomposition.triangulation;

import com.base.engine.polydecomposition.triangulation.delaunay.sweep.SweepConstraint;
import java.util.ArrayList;

/**
 * Stores all data relevant to each triangulation point, such as coordinates
 * 
 * @author Jordan
 */
public abstract class TriangulationVec
{
    //List of edges this point constitutes an upper ending point (CDT)
    private ArrayList<SweepConstraint> edges;
   
    //abstract methods to get the coordinates of the point
    public abstract double getX();
    public abstract double getY();

    //abstract methods to get the float coordinates of the point
    public abstract float getXf();
    public abstract float getYf();
   
    //abstract method to declare a triangulation point
    public abstract void set(double x, double y);
   
    @Override
    public String toString()
    {
        return "(" + getX() + ", " + getY() + ")";
    }

    /**
     * Get the edges associated with this point
     * 
     * @return List of edges on this point
     */
    public ArrayList<SweepConstraint> getEdges()
    {
        return edges;
    }

    /**
     * Add an edge to this point
     * 
     * @param e Edge to add to this point
     */
    public void addEdge(SweepConstraint e)
    {
        if(edges == null)
        {
            edges = new ArrayList<SweepConstraint>();
        }
        edges.add(e);
    }

    /**
     * Check if this point has any edges
     * 
     * @return determines if the point as any edges
     */
    public boolean hasEdges()
    {
        return edges != null;
    }
    
    /**
     * Check if this point object already exists
     * 
     * @param obj Point we will be checking against
     * 
     * @return determines if the points are equal
     */
    @Override
    public boolean equals(Object obj)
    {
        if(obj instanceof TriangulationVec)
        {
            TriangulationVec vector = (TriangulationVec)obj;
            return getX() == vector.getX() && getY() == vector.getY();
        }
        return super.equals(obj);
    }
}

