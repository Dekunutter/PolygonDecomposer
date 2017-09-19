package com.base.engine.polydecomposition.triangulation;

import com.base.engine.polydecomposition.triangulation.delaunay.DelaunayTriangle;
import java.util.ArrayList;
import java.util.List;

/**
 * The context for each triangulation object, storing the list of triangles and points in each
 * 
 * @author Jordan
 */
public abstract class TriangulationContext
{   
    protected ArrayList<DelaunayTriangle> triangleList = new ArrayList<>();

    protected ArrayList<TriangulationVec> vectors = new ArrayList<>(200);
    protected Triangulatable triangle;
   
    /**
     * Prepare for triangulation
     * 
     * @param Trianglulatable shape we are preparing to triangulate
     */
    public void prepareTriangulation(Triangulatable tri)
    {
        triangle = tri;
        tri.prepare(this);
    }
   
    /**
     * Declare a constraint for triangulation
     * 
     * @param vec1 First vector
     * @param vec2 Second vector
     * @return Triangulation constraint
     */
    public abstract TriangulationConstraint newConstraint(TriangulationVec vec1, TriangulationVec vec2);
   
    /**
     * Add a triangle to the list of triangles
     * 
     * @param triangle Triangle to add to the list
     */
    public void addToList(DelaunayTriangle triangle)
    {
        triangleList.add(triangle);
    }

    /**
     * Get the object being triangulated
     * 
     * @return Triangulatable object
     */
    public Triangulatable getTriangulatable()
    {
        return triangle;
    }
   
    /**
     * Get all the triangulation points within the object
     * 
     * @return List of triangulation points in the object
     */
    public List<TriangulationVec> getVectors()
    {
        return vectors;
    }
   
    /**
     * Clear all the triangulation points within the object
     */
    public void clear()
    {
        vectors.clear();
    }

    /**
     * Add a list of triangulation points to the current list of points
     * 
     * @param vectors Sublist of triangulation points to add to the overall list
     */
    public void addVectors(List<TriangulationVec> vectors)
    {
        this.vectors.addAll(vectors);
    }
}
