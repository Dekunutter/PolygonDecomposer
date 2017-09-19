package com.base.engine.polydecomposition.triangulation.delaunay.sweep;

import com.base.engine.polydecomposition.triangulation.TriangulationConstraint;
import com.base.engine.polydecomposition.triangulation.TriangulationVec;

/**
 * The constraints on which the DTSweep operates by
 * 
 * @author Jordan
 */
public class SweepConstraint extends TriangulationConstraint
{
    public TriangulationVec vector1;
    public TriangulationVec vector2;
   
    /**
     * Give two points in any order.
     * Will always be ordered so that v1.y > v2.y, or v1.x > v2.x if they share the same y value
     * 
     * @param v1 First vertex
     * @param v2 Second vertex
     */
    public SweepConstraint(TriangulationVec v1, TriangulationVec v2)
    {
        vector1 = v1;
        vector2 = v2;
        if(v1.getY() > v2.getY())
        {
            vector2 = v1;
            vector1 = v2;
        }
        else if(v1.getY() == v2.getY())
        {
            if(v1.getX() > v2.getX())
            {
                vector2 = v1;
                vector1 = v2;
            }
            else if(v1.getX() == v2.getX())
            {
                System.out.println( "Failed to create constraint " +  v1 + " " + v2 );
            }
        }
        vector2.addEdge(this);
    }

    @Override
    public TriangulationVec getVector1()
    {
        return vector1;
    }

    @Override
    public TriangulationVec getVector2()
    {
        return vector2;
    }
}
