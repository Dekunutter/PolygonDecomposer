package com.base.engine.polydecomposition.triangulation.pointcoords;

import com.base.engine.polydecomposition.triangulation.TriangulationVec;

/**
 * The object storing all the coordinate data that a triangulation point extends and uses
 * 
 * @author Jordan
 */
public class VectorCoords extends TriangulationVec
{
    private double x;
    private double y;
   
    /**
     * Setup the point on a 2D plane.
     * 
     * @param x point on the X axis
     * @param y point on the Y axis
     */
    public VectorCoords(double x, double y)
    {
        this.x = x;
        this.y = y;
    }

    @Override
    public double getX()
    {
        return this.x;
    }
    @Override
    public double getY()
    {
        return this.y;
    }
    
    @Override
    public float getXf()
    {
        return (float)this.x;
    }
    @Override
    public float getYf()
    {
        return (float)this.y;
    }

    @Override
    public void set(double x, double y)
    {
        this.x = x;
        this.y = y;
    }
}
