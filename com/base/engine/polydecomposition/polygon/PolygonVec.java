package com.base.engine.polydecomposition.polygon;

import com.base.engine.polydecomposition.triangulation.pointcoords.VectorCoords;

/**
 * Stores all data relevant to a point within a polygon
 * 
 * @author Jordan
 */
public class PolygonVec extends VectorCoords
{
    /**
     * Initialise the point coordinates
     * 
     * @param x Point on the X axis
     * @param y Point on the Y axis
     */
    public PolygonVec(float x, float y)
    {
        super(x, y);
    }
}
