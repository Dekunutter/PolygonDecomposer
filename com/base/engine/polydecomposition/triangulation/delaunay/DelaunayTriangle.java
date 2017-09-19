package com.base.engine.polydecomposition.triangulation.delaunay;

import com.base.engine.polydecomposition.triangulation.TriangulationVec;
import com.base.engine.polydecomposition.triangulation.delaunay.sweep.SweepConstraint;

/**
 * Stores all data related to each delaunay triangle and provides some related functions
 * 
 * @author Jordan
 */
public class DelaunayTriangle
{
    //list of neighbouring triangles
    public final DelaunayTriangle[] neighbours = new DelaunayTriangle[3];
    //Flags to determine if an edge is a Constrained edge
    public final boolean[] constrainedEdge = new boolean[] {false, false, false};
    //Flags to determine if an edge is a Delauney edge
    public final boolean[] delaunayEdge = new boolean[] {false, false, false};
    //Has this triangle been marked as an interior triangle?
    protected boolean interior = false;

    //the 3 points that formulate a triangle
    public final TriangulationVec[] vectors = new TriangulationVec[3];

    /**
     * Setup the triangle with 3 points
     * 
     * @param vector1 First vertex
     * @param vector2 Second vertex
     * @param vector3 Third vertex
     */
    public DelaunayTriangle(TriangulationVec vector1, TriangulationVec vector2, TriangulationVec vector3)
    {
        vectors[0] = vector1;
        vectors[1] = vector2;
        vectors[2] = vector3;
    }

    /**
     * Called to determine the edge type of a given triangulation point within this triangle
     * 
     * @param vector Point we are checking
     * @return Edge type
     */
    public int index(TriangulationVec vector)
    {
        if(vector == vectors[0])
        {
            return 0;
        }
        else if(vector == vectors[1])
        {
            return 1;
        }
        else if(vector == vectors[2])
        {
            return 2;
        }
        throw new RuntimeException("Calling index with a point that doesn't exist in triangle");
    }
   
    /**
     * Determine whether this triangle contains the given point or not
     * 
     * @param vector Point we are checking
     * @return determines whether the point is contained within this triangle
     */
    public boolean contains(TriangulationVec vector)
    {
        return (vector == vectors[0] || vector == vectors[1] || vector == vectors[2]);
    }

    /**
     * Determine whether this triangle contains these two triangulation points
     * 
     * @param vector1 First point we are checking
     * @param vector2 Second point we are checking
     * @return determines whether the points are contained within this triangle
     */
    public boolean contains(TriangulationVec vector1, TriangulationVec vector2)
    {
        return (contains(vector1) && contains(vector2));
    }

    /**
     * Update pointers to neighbouring triangles
     * 
     * @param vector1 First point to check
     * @param vector2 Second point to check
     * @param tri Neighbouring triangle
     */
    private void markNeighbor(TriangulationVec vector1, TriangulationVec vector2, DelaunayTriangle tri)
    {
        if((vector1 == vectors[2] && vector2 == vectors[1]) || (vector1 == vectors[1] && vector2 == vectors[2]))
        {
            neighbours[0] = tri;
        }
        else if((vector1 == vectors[0] && vector2 == vectors[2]) || (vector1 == vectors[2] && vector2 == vectors[0]))
        {
            neighbours[1] = tri;
        }
        else if((vector1 == vectors[0] && vector2 == vectors[1]) || (vector1 == vectors[1] && vector2 == vectors[0]))
        {
            neighbours[2] = tri;
        }
        else
        {
            System.out.println("Neighbor error, please report!");
        }
    }

    /**
     * Search to update neighbor pointers
     * 
     * @param tri Neighbouring triangle
     */
    public void markNeighbor(DelaunayTriangle tri)
    {
        if(tri.contains(vectors[1], vectors[2]))
        {
            neighbours[0] = tri;
            tri.markNeighbor(vectors[1], vectors[2], this);
        }
        else if(tri.contains(vectors[0], vectors[2]))
        {
            neighbours[1] = tri;
            tri.markNeighbor(vectors[0], vectors[2], this);
        }
        else if(tri.contains(vectors[0], vectors[1]))
        {
            neighbours[2] = tri;
            tri.markNeighbor(vectors[0], vectors[1], this);
        }
        else
        {
            System.out.println("marking neighbor failed");
        }
    }

    /**
     * Clear the list of neighbouring triangles
     */
    public void clearNeighbors()
    {
        neighbours[0] = neighbours[1] = neighbours[2] = null;
    }

    /**
     * Clear a neighbour from the list of neighbouring triangles
     * 
     * @param triangle Neighbouring triangle
     */
    public void clearNeighbor(DelaunayTriangle triangle)
    {
        if(neighbours[0] == triangle)
        {
            neighbours[0] = null;
        }
        else if(neighbours[1] == triangle)
        {
            neighbours[1] = null;            
        }
        else
        {
            neighbours[2] = null;
        }
    }

    /**
     * Get the opposite point from this triangle
     * 
     * @param tri Triangle to check against
     * @param vector Vertex to check against
     * @return Opposite point from the triangle
     */
    public TriangulationVec oppositePoint(DelaunayTriangle tri, TriangulationVec vector)
    {
        assert tri != this : "self-pointer error";
        return pointClockwise(tri.pointClockwise(vector));
    }

    /**
     * The neighbouring triangle, clockwise to the given point
     * 
     * @param vector Vertex to check against
     * @return Neighbouring triangle
     */
    public DelaunayTriangle neighborClockwise(TriangulationVec vector)
    {
        if(vector == vectors[0])
        {
            return neighbours[1];
        }
        else if(vector == vectors[1])
        {
            return neighbours[2];
        }
        return neighbours[0];
    }

    /**
     * The neighbouring triangle, counter-clockwise to the given point
     * 
     * @param vector Vertex to check against
     * @return Neighbouring triangle
     */
    public DelaunayTriangle neighborCounterClockwise(TriangulationVec vector)
    {
        if(vector == vectors[0])
        {
            return neighbours[2];
        }
        else if(vector == vectors[1])
        {
            return neighbours[0];
        }
        return neighbours[1];
    }

    /**
     * The neighbouring triangle, across to the given point
     * 
     * @param vector Vertex to check against
     * @return Neighbouring triangle
     */
    public DelaunayTriangle neighborAcross(TriangulationVec vector)
    {
        if(vector == vectors[0])
        {
            return neighbours[0];
        }
        else if(vector == vectors[1])
        {
            return neighbours[1];
        }
        return neighbours[2];
    }

    /**
     * The point counter-clockwise to the given point
     * 
     * @param vector Point to check against
     * @return Counter-clockwise point to the given one
     */
    public TriangulationVec pointCounterClockwise(TriangulationVec vector)
    {
        if(vector == vectors[0])
        {
            return vectors[1];
        }
        else if(vector == vectors[1])
        {
            return vectors[2];
        }
        else if(vector == vectors[2])
        {
            return vectors[0];
        }
        throw new RuntimeException("Point location error, invalid triangulation point");
    }

    /**
     * The point clockwise to the given point
     * 
     * @param vector Point to check against
     * @return Clockwise point to the given one
     */
    public TriangulationVec pointClockwise(TriangulationVec vector)
    {
        if(vector == vectors[0])
        {
            return vectors[2];
        }
        else if(vector == vectors[1])
        {
            return vectors[0];
        }
        else if(vector == vectors[2])
        {
            return vectors[1];
        }
        System.out.println("point location error, invalid pointset parsed");
        throw new RuntimeException("[FIXME] point location error");
    }

    /**
     * Legalize the triangle by rotating it clockwise around the origin point
     * 
     * @param originVector Origin of the triangle
     * @param nVector Point to rotate
     */
    public void legalize(TriangulationVec originVector, TriangulationVec nVector)
    {
        if(originVector == vectors[0])
        {
            vectors[1] = vectors[0];
            vectors[0] = vectors[2];
            vectors[2] = nVector;
        }
        else if(originVector == vectors[1])
        {
            vectors[2] = vectors[1];
            vectors[1] = vectors[0];
            vectors[0] = nVector;
        }
        else if(originVector == vectors[2])
        {
            vectors[0] = vectors[2];
            vectors[2] = vectors[1];
            vectors[1] = nVector;
        }
        else
        {
            System.out.println("legalization error");
            throw new RuntimeException("legalization bug");
        }
    }

    /**
     * Mark this point as a constrained edge
     * 
     * @param index Index of point to mark
     */
    public void markConstrainedEdge(int index)
    {
        constrainedEdge[index] = true;
    }
   
    /**
     * Mark these points as constrained edges
     * 
     * @param edge Edge we are marking
     */
    public void markConstrainedEdge(SweepConstraint edge)
    {
        markConstrainedEdge(edge.vector1, edge.vector2);
        if((edge.vector2 == vectors[0] && edge.vector1 == vectors[1]) || (edge.vector2 == vectors[1] && edge.vector1 == vectors[0]))
        {
            constrainedEdge[2] = true;
        }
        else if((edge.vector2 == vectors[0] && edge.vector1 == vectors[2]) || (edge.vector2 == vectors[2] && edge.vector1 == vectors[0]))
        {
            constrainedEdge[1] = true;
        }
        else if((edge.vector2 == vectors[1] && edge.vector1 == vectors[2]) || (edge.vector2 == vectors[2] && edge.vector1 == vectors[1]))
        {
            constrainedEdge[0] = true;
        }
    }

    /**
     * Mark edge as constrained
     * 
     * @param vector1 First point of the edge
     * @param vector2 Second point of the edge
     */
    public void markConstrainedEdge(TriangulationVec vector1, TriangulationVec vector2)
    {
        if((vector2 == vectors[0] && vector1 == vectors[1]) || (vector2 == vectors[1] && vector1 == vectors[0]))
        {
            constrainedEdge[2] = true;
        }
        else if((vector2 == vectors[0] && vector1 == vectors[2]) || (vector2 == vectors[2] && vector1 == vectors[0]))
        {
            constrainedEdge[1] = true;
        }
        else if((vector2 == vectors[1] && vector1 == vectors[2]) || (vector2 == vectors[2] && vector1 == vectors[1]))
        {
            constrainedEdge[0] = true;
        }
    }

    /**
     * Get the area of the triangle
     * 
     * @return area of the triangle
     */
    public double area()
    {

        double base = vectors[0].getX() - vectors[1].getX();
        double height = vectors[2].getY() - vectors[1].getY();

        return Math.abs((base * height * 0.5f));
    }

    /**
     * Get the neighbouring triangle that shares this edge
     * 
     * @param vector1 First point of the edge
     * @param vector2 Second point of the edge
     * @return Index of neighbouring triangle
     */
    public int edgeIndex(TriangulationVec vector1, TriangulationVec vector2)
    {
        if(vectors[0] == vector1)
        {
            if(vectors[1] == vector2)
            {
                return 2;
            }
            else if(vectors[2] == vector2)
            {
                return 1;                
            }
        }
        else if(vectors[1] == vector1)
        {
            if(vectors[2] == vector2)
            {
                return 0;
            }
            else if(vectors[0] == vector2)
            {
                return 2;                
            }
        }
        else if(vectors[2] == vector1)
        {
            if(vectors[0] == vector2)
            {
                return 1;
            }
            else if(vectors[1] == vector2)
            {
                return 0;                
            }            
        }
        return -1;
    }

    /**
     * Get a constrained edge counter-clockwise from this point
     * 
     * @param vector Point to check against
     * @return determines whether edge is constrained
     */
    public boolean getConstrainedEdgeCCW(TriangulationVec vector)
    {
        if(vector == vectors[0])
        {
            return constrainedEdge[2];
        }
        else if(vector == vectors[1])
        {
            return constrainedEdge[0];
        }
        return constrainedEdge[1];
    }

    /**
     * Get a constrained edge clockwise from this point
     * 
     * @param vector Point to check against
     * @return determines whether edge is constrained
     */
    public boolean getConstrainedEdgeClockwise(TriangulationVec vector)
    {
        if(vector == vectors[0])
        {
            return constrainedEdge[1];
        }
        else if(vector == vectors[1])
        {
            return constrainedEdge[2];
        }
        return constrainedEdge[0];
    }

    /**
     * Get a constrained edge across from this point
     * 
     * @param vector Point to check against
     * @return  determines whether edge is constrained
     */
    public boolean getConstrainedEdgeAcross(TriangulationVec vector)
    {
        if(vector == vectors[0])
        {
            return constrainedEdge[0];
        }
        else if(vector == vectors[1])
        {
            return constrainedEdge[1];
        }
        return constrainedEdge[2];
    }

    /**
     * Set a constrained edge counter-clockwise from this point
     * 
     * @param vector Point to check against
     * @param isConstrained  is the given edge constrained or not
     */
    public void setConstrainedEdgeCCW(TriangulationVec vector, boolean isConstrained)
    {
        if(vector == vectors[0])
        {
            constrainedEdge[2] = isConstrained;
        }
        else if(vector == vectors[1])
        {
            constrainedEdge[0] = isConstrained;
        }
        else
        {
            constrainedEdge[1] = isConstrained;            
        }
    }

    /**
     * Set a constrained edge clockwise from this point
     * 
     * @param vector Point to check against
     * @param isConstrained is the given edge constrained or not
     */
    public void setConstrainedEdgeCW(TriangulationVec vector, boolean isConstrained)
    {
        if(vector == vectors[0])
        {
            constrainedEdge[1] = isConstrained;
        }
        else if(vector == vectors[1])
        {
            constrainedEdge[2] = isConstrained;
        }
        else
        {
            constrainedEdge[0] = isConstrained;            
        }
    }

    /**
     * Set a constrained edge across from this point
     * 
     * @param vector Point to check against
     * @param isConstrained is the given edge constrained or not
     */
    public void setConstrainedEdgeAcross(TriangulationVec vector, boolean isConstrained)
    {
        if(vector == vectors[0])
        {
            constrainedEdge[0] = isConstrained;
        }
        else if(vector == vectors[1])
        {
            constrainedEdge[1] = isConstrained;
        }
        else
        {
            constrainedEdge[2] = isConstrained;            
        }
    }

    /**
     * Get the delaunay edge counter-clockwise from this point
     * 
     * @param vector Point to check against
     * @return determines if the edge was found
     */
    public boolean getDelunayEdgeCCW(TriangulationVec vector)
    {
        if(vector == vectors[0])
        {
            return delaunayEdge[2];
        }
        else if(vector == vectors[1])
        {
            return delaunayEdge[0];
        }
        return delaunayEdge[1];
    }

    /**
     * Get the delaunay edge clockwise from this point
     * 
     * @param vector Point to check against
     * @return determines if the edge was found
     */
    public boolean getDelunayEdgeCW(TriangulationVec vector)
    {
        if(vector == vectors[0])
        {
            return delaunayEdge[1];
        }
        else if(vector == vectors[1])
        {
            return delaunayEdge[2];
        }
        return delaunayEdge[0];
    }

    /**
     * Get the delaunay edge across from this point
     * 
     * @param vector Point to check against
     * @return determines if the edge was found
     */
    public boolean getDelunayEdgeAcross(TriangulationVec vector)
    {
        if(vector == vectors[0])
        {
            return delaunayEdge[0];
        }
        else if(vector == vectors[1])
        {
            return delaunayEdge[1];
        }
        return delaunayEdge[2];
    }

    /**
     * Set the delaunay edge counter-clockwise from this point
     * 
     * @param vector Point to check against
     * @param isEdge is this point on an edge
     */
    public void setDelunayEdgeCCW(TriangulationVec vector, boolean isEdge)
    {
        if(vector == vectors[0])
        {
            delaunayEdge[2] = isEdge;
        }
        else if(vector == vectors[1])
        {
            delaunayEdge[0] = isEdge;
        }
        else
        {
            delaunayEdge[1] = isEdge;            
        }
    }

    /**
     * Set the delaunay edge clockwise from this point
     * 
     * @param vector Point to check against
     * @param isEdge is this point on an edge
     */
    public void setDelunayEdgeCW(TriangulationVec vector, boolean isEdge)
    {
        if(vector == vectors[0])
        {
            delaunayEdge[1] = isEdge;
        }
        else if(vector == vectors[1])
        {
            delaunayEdge[2] = isEdge;
        }
        else
        {
            delaunayEdge[0] = isEdge;            
        }
    }

    /**
     * Set the delaunay edge across from this point
     * 
     * @param vector Point to check against
     * @param isEdge is this point on an edge
     */
    public void setDelunayEdgeAcross(TriangulationVec vector, boolean isEdge)
    {
        if(vector == vectors[0])
        {
            delaunayEdge[0] = isEdge;
        }
        else if(vector == vectors[1])
        {
            delaunayEdge[1] = isEdge;
        }
        else
        {
            delaunayEdge[2] = isEdge;            
        }
    }

    /**
     * Clear the delaunay edges associated with this triangle
     */
    public void clearDelunayEdges()
    {
        delaunayEdge[0] = false;
        delaunayEdge[1] = false;
        delaunayEdge[2] = false;
    }

    /**
     * Check if this triangle is an interior triangle
     * 
     * @return determines if a triangle is totally enclosed in an object
     */
    public boolean isInterior()
    {
        return interior;
    }

    /**
     * Set whether or not the triangle is an interior triangle
     * 
     * @param interior value to set the boolean to
     */
    public void isInterior(boolean interior)
    {
        this.interior = interior;        
    }
}
