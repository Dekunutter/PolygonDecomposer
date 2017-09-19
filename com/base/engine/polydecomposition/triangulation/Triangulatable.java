package com.base.engine.polydecomposition.triangulation;

import com.base.engine.polydecomposition.triangulation.delaunay.DelaunayTriangle;
import java.util.List;

/**
 * Interface that all triangulatable shapes will extend (such as polygon)
 * 
 * @author Jordan
 */
public interface Triangulatable
{
    //Preparations needed before triangulation starts should be handled here
    public void prepare(TriangulationContext context);
   
    //fetch a list of all triangles within the object
    public List<DelaunayTriangle> getTriangles();
    
    //add a triangle to the object
    public void addTriangle(DelaunayTriangle tri);   
}

