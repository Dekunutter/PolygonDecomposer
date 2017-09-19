package com.base.engine.polydecomposition;

import com.base.engine.polydecomposition.polygon.TriangulatablePolygon;
import com.base.engine.polydecomposition.triangulation.TriangulationContext;
import com.base.engine.polydecomposition.triangulation.delaunay.sweep.Sweep;
import com.base.engine.polydecomposition.triangulation.delaunay.sweep.SweepContext;

/**
 * Calling class of the polygon decomposition classes
 * 
 * @author Jordan
 */
public class PolyDecomposition
{
    /**
     * Wrapper for the triangulate method. Primary method by which the triangulation algorithms are called
     * 
     * @param poly Polygon we will attempt to triangulate
     */
    public static void triangulate(TriangulatablePolygon poly)
    {
        TriangulationContext context;          
        context = createContext();
        context.prepareTriangulation(poly);
        Sweep.triangulate((SweepContext)context);           
    }

    /**
     * Set up the triangulation context
     * 
     * @return Triangulation context we will use for triangulating polygons
     */
    public static TriangulationContext createContext()
    {
        return new SweepContext();
    }
   
    /**
     * Begin triangulation
     * 
     * @param context Triangulation context used
     */
    public static void triangulate(TriangulationContext context)
    {
        Sweep.triangulate((SweepContext)context);
    }
}
