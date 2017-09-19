package com.base.engine.polydecomposition.triangulation.delaunay.sweep;

import com.base.engine.polydecomposition.triangulation.TriangulationVec;
import java.util.Comparator;

/**
 * Compares two triangulation points to each other during the sweep
 * 
 * @author Jordan
 */
public class SweepVectorComparator implements Comparator<TriangulationVec>
{
    @Override
    public int compare(TriangulationVec vector1, TriangulationVec vector2)
    {
        if(vector1.getY() < vector2.getY())
        {
            return -1;
        }
        else if(vector1.getY() > vector2.getY())
        {
            return 1;
        }
        else
        {
            if(vector1.getX() < vector2.getX())
            {
                return -1;
            }
            else if(vector1.getX() > vector2.getX())
            {
                return 1;
            }
            else
            {
                return 0;
            }
        }            
    }
}

