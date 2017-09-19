package com.base.engine.polydecomposition.triangulation.delaunay.sweep;

import com.base.engine.polydecomposition.triangulation.Triangulatable;
import com.base.engine.polydecomposition.triangulation.TriangulationConstraint;
import com.base.engine.polydecomposition.triangulation.TriangulationContext;
import com.base.engine.polydecomposition.triangulation.TriangulationVec;
import com.base.engine.polydecomposition.triangulation.delaunay.DelaunayTriangle;
import com.base.engine.polydecomposition.triangulation.pointcoords.VectorCoords;
import com.base.simulation.GUI;
import java.util.Collections;

/**
 * The context for each DTSweep, storing data related to the current sweep
 * 
 * @author Jordan
 */
public class SweepContext extends TriangulationContext
{
    //Inital triangle factor, seed triangle will extend 30% of PointSet width to both left and right.
    private final float ALPHA = 0.3f;

    protected SweepingFront aFront;
    //head point used with advancing front
    private TriangulationVec head;
    //tail point used with advancing front
    private TriangulationVec tail;
    protected Basin basin = new Basin();
    protected EdgeEvent edgeEvent = new EdgeEvent();
   
    private SweepVectorComparator comparator = new SweepVectorComparator();
   
    /**
     * Clear the context in preparation for a sweep
     */
    public SweepContext()
    {
        clear();
    }

    /**
     * Remove a triangle from the list of triangles
     * 
     * @param triangle Triangle to remove from the list
     */
    public void removeFromList(DelaunayTriangle triangle)
    {
        triangleList.remove(triangle);
    }

    /**
     * Clean the triangle mesh
     * 
     * @param triangle triangle to clean
     */
    public void meshClean(DelaunayTriangle triangle)
    {
        meshCleanReq(triangle);
    }

    /**
     * Clean the triangle mesh
     * 
     * @param triangle triangle to clean
     */
    private void meshCleanReq(DelaunayTriangle triangle)
    {
        if(triangle != null && !triangle.isInterior())
        {
            triangle.isInterior(true);
            this.triangle.addTriangle(triangle);
            for(int i = 0; i < 3; i++)
            {
                if(!triangle.constrainedEdge[i])
                {
                    meshCleanReq(triangle.neighbours[i]);
                }
            }
        }
    }

    /**
     * Clear the list of triangles and this context as a whole
     */
    @Override
    public void clear()
    {
        super.clear();
        triangleList.clear();
    }
    
    /**
     * Set the head of the triangle
     * 
     * @param vector Vertex to set to the head
     */
    public void setHead(TriangulationVec vector)
    {
        head = vector;
    }
    
    /**
     * Get the head of the triangle
     * 
     * @return vertex which represents the head of the triangle
     */
    public TriangulationVec getHead()
    {
        return head;
    }

    /**
     * Set the tail of the triangle
     * 
     * @param vector Vertex to set to the tail
     */
    public void setTail(TriangulationVec vector)
    {
        tail = vector;
    }
    
    /**
     * Get the tail of the triangle
     * 
     * @return vertex which represents the tail of the triangle
     */
    public TriangulationVec getTail()
    {
        return tail;
    }

    /**
     * Locate an advancing node from the triangulation point provided
     * 
     * @param vector Point to check against
     * @return node that was located
     */
    public SweepingFrontNode locateNode(TriangulationVec vector)
    {
        return aFront.locateNode(vector);
    }

    /**
     * Setup the advancing front
     */
    public void createAdvancingFront()
    {
        SweepingFrontNode head, tail, middle;
        //Initial triangle
        DelaunayTriangle iTriangle = new DelaunayTriangle(vectors.get(0), getTail(), getHead());
        addToList(iTriangle);
       
        head = new SweepingFrontNode(iTriangle.vectors[1]);
        head.triangle = iTriangle;
        middle = new SweepingFrontNode(iTriangle.vectors[0]);
        middle.triangle = iTriangle;
        tail = new SweepingFrontNode(iTriangle.vectors[2]);

        aFront = new SweepingFront(head, tail);
       
        aFront.head.next = middle;
        middle.next = aFront.tail;
        middle.prev = aFront.head;
        aFront.tail.prev = middle;
        GUI.txtOutput.setText("Setup advancing front for triangulation");
    }
   
    /**
     * A basin in which triangles will be formed
     */
    class Basin
    {
        SweepingFrontNode leftNode;
        SweepingFrontNode bottomNode;
        SweepingFrontNode rightNode;
        public double width;
        public boolean leftHighest;        
    }
   
    /**
     * An edge around which triangles will be formed
     */
    class EdgeEvent
    {
        SweepConstraint constrainedEdge;
        public boolean right;
    }

    /**
     * Try to map a node to all sides of this triangle that don't have a neighbour.
     * 
     * @param tri current triangle
     */
    public void mapTriangleToNodes(DelaunayTriangle tri)
    {
        SweepingFrontNode node;
        for(int i = 0; i < 3; i++)
        {
            if(tri.neighbours[i] == null)
            {
                node = aFront.locatePoint(tri.pointClockwise(tri.vectors[i]));
                if(node != null)
                {
                    node.triangle = tri;
                }
            }            
        }        
    }

    /**
     * Prepare for triangulation
     * 
     * @param tri current triangle
     */
    @Override
    public void prepareTriangulation(Triangulatable tri)
    {
        super.prepareTriangulation(tri);

        double xMax, xMin;
        double yMax, yMin;

        xMax = xMin = vectors.get(0).getX();
        yMax = yMin = vectors.get(0).getY();
        
        //Calculate bounds. Should be combined with the sorting
        for(TriangulationVec vector : vectors)
        {
            if(vector.getX() > xMax)
                xMax = vector.getX();
            if(vector.getX() < xMin)
                xMin = vector.getX();
            if(vector.getY() > yMax)
                yMax = vector.getY();
            if(vector.getY() < yMin)
                yMin = vector.getY();
        }

        double deltaX = ALPHA * (xMax - xMin);
        double deltaY = ALPHA * (yMax - yMin);
        VectorCoords vector1 = new VectorCoords(xMax + deltaX, yMin - deltaY);
        VectorCoords vector2 = new VectorCoords(xMin - deltaX, yMin - deltaY);

        setHead(vector1);
        setTail(vector2);

        Collections.sort(vectors, comparator);
        GUI.txtOutput.setText("Triangulation boundaries calculated");
    }
    
    /**
     * New constraint setup for the sweep
     * 
     * @param vector1 First point of the constraint
     * @param vector2 Second point of the constraint
     * @return new triangulation constraint
     */
    @Override
    public TriangulationConstraint newConstraint(TriangulationVec vector1, TriangulationVec vector2)
    {
        return new SweepConstraint(vector1, vector2);        
    }
}
