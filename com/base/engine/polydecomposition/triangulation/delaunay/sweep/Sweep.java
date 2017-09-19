package com.base.engine.polydecomposition.triangulation.delaunay.sweep;

import com.base.engine.polydecomposition.triangulation.TriangulationVec;
import com.base.engine.polydecomposition.triangulation.TriangulationUtil;
import com.base.engine.polydecomposition.triangulation.TriangulationUtil.Orientation;
import com.base.engine.polydecomposition.triangulation.delaunay.DelaunayTriangle;
import com.base.simulation.GUI;
import java.util.List;

/**
 * Most of the functions related to the triangulation sweep is here
 * 
 * @author Jordan
 */
public class Sweep
{
    /**
     * Triangulate a simple polygon
     * 
     * @param sweepContext Sweep context
     */
    public static void triangulate(SweepContext sweepContext)
    {
        sweepContext.createAdvancingFront();
        sweep(sweepContext);
        finalizationPolygon(sweepContext);
    }

    /**
     * Start sweeping the Y-sorted point set from bottom to top
     * 
     * @param sweepContext  Sweep context
     */
    private static void sweep(SweepContext sweepContext)
    {
        GUI.txtOutput.setText("Begin triangulation sweep");
        List<TriangulationVec> vectors;
        TriangulationVec vector;
        SweepingFrontNode node;
       
        vectors = sweepContext.getVectors();
        
        for(int i = 1; i < vectors.size(); i++)
        {
            vector = vectors.get(i);

            node = pointEvent(sweepContext, vector);

            if(vector.hasEdges())
            {
                for(SweepConstraint edge : vector.getEdges())
                {
                    edgeEvent(sweepContext, edge, node);
                }
            }
        }
    }
    
    /**
     * Finalize the polygon after it has been swept
     * 
     * @param sweepContext Sweep context
     */
    private static void finalizationPolygon(SweepContext sweepContext)
    {
        GUI.txtOutput.setText("Finalize triangulated polygon");
        //Get an Internal triangle to start with
        DelaunayTriangle tri = sweepContext.aFront.head.next.triangle;
        TriangulationVec vector = sweepContext.aFront.head.next.vector;
        while(!tri.getConstrainedEdgeClockwise(vector))
        {
            tri = tri.neighborCounterClockwise(vector);
        }
       
        //Collect interior triangles constrained by edges
        sweepContext.meshClean(tri);
    }

    /**
     * Find closest node to the left of the new point and create a new triangle
     * If needed new holes and basins will be filled to.
     * 
     * @param sweepContext Sweep context
     * @param vector New point to add
     * @return closest node to the left of the new point
     */
    private static SweepingFrontNode pointEvent(SweepContext sweepContext, TriangulationVec vector)
    {
        SweepingFrontNode node,newNode;

        node = sweepContext.locateNode(vector);
        newNode = newFrontTriangle(sweepContext, vector, node);
        
        //Only need to check positive epsilon value since the point never have a smaller x value than the node due to how we fetch nodes from the front
        if(vector.getX() <= node.vector.getX() + TriangulationUtil.EPSILON)
        {
            fill(sweepContext, node);
        }
         
        fillAdvancingFront(sweepContext, newNode);
        return newNode;
    }

    /**
     * Creates a new front triangle and legalizes it
     * 
     * @param sweepContext Sweep context
     * @param vector First point of the triangle
     * @param node closest node to the right of the new point
     * @return closest node to the left of the new point
     */
    private static SweepingFrontNode newFrontTriangle(SweepContext sweepContext, TriangulationVec vector, SweepingFrontNode node)    
    {
        SweepingFrontNode newNode;
        DelaunayTriangle triangle;
   
        triangle = new DelaunayTriangle(vector, node.vector, node.next.vector);
        triangle.markNeighbor(node.triangle);
        sweepContext.addToList(triangle);          

        newNode = new SweepingFrontNode(vector);
        newNode.next = node.next;
        newNode.prev = node;
        node.next.prev = newNode;
        node.next = newNode;  
                     
        if(!legalize(sweepContext, triangle))
        {
            sweepContext.mapTriangleToNodes(triangle);
        }

        return newNode;
    }

    /**
     * Create a new edge event
     * 
     * @param sweepContext Sweep context
     * @param edge Previous edge
     * @param node closest node to the right of the edge
     */
    private static void edgeEvent(SweepContext sweepContext, SweepConstraint edge, SweepingFrontNode node)
    {
        sweepContext.edgeEvent.constrainedEdge = edge;
        sweepContext.edgeEvent.right = edge.vector1.getX() > edge.vector2.getX();

        if(isEdgeSideOfTriangle(node.triangle, edge.vector1, edge.vector2))
        {
            return;
        }

        fillEdgeEvent(sweepContext, edge, node);
        edgeEvent(sweepContext, edge.vector1, edge.vector2, node.triangle, edge.vector2);            
    }
   
    /**
     * Complete an edge event according to which edge it is
     * 
     * @param sweepContext Sweep context
     * @param edge Previous edge
     * @param node closest node to the right of the edge
     */
    private static void fillEdgeEvent(SweepContext sweepContext, SweepConstraint edge, SweepingFrontNode node)
    {
        if(sweepContext.edgeEvent.right)
        {
            fillRightAboveEdgeEvent(sweepContext, edge, node);
        }
        else
        {
            fillLeftAboveEdgeEvent(sweepContext, edge, node);                
        }      
    }
   
    /**
     * Complete an edge event against a right concave edge
     * 
     * @param sweepContext sweep context
     * @param edge Previous edge
     * @param node closest node to the right of the edge
     */
    private static void fillRightConcaveEdgeEvent(SweepContext sweepContext, SweepConstraint edge, SweepingFrontNode node)
    {
        fill(sweepContext, node.next);
        if(node.next.vector != edge.vector1)
        {
            //Is the next edge an above or below edge?
            if(TriangulationUtil.orient(edge.vector2, node.next.vector, edge.vector1) == Orientation.CounterClockwise)
            {
                //if below
                if(TriangulationUtil.orient( node.vector, node.next.vector, node.next.next.vector) == Orientation.CounterClockwise)
                {
                    //then the next is concave
                    fillRightConcaveEdgeEvent(sweepContext, edge, node);
                }
                else
                {
                    //then the next is convex
                }
            }
        }
    }

    /**
     * Complete an edge event against a right convex edge
     * 
     * @param sweepContext sweep context
     * @param edge Previous edge
     * @param node closest node to the right of the edge
     */
    private static void fillRightConvexEdgeEvent(SweepContext sweepContext, SweepConstraint edge, SweepingFrontNode node)
    {
        //Is the next edge concave or convex?
        if(TriangulationUtil.orient(node.next.vector, node.next.next.vector, node.next.next.next.vector) == Orientation.CounterClockwise)
        {
            //If concave
            fillRightConcaveEdgeEvent(sweepContext, edge, node.next);
        }
        else
        {
            //If convex
            //Is the next above or below edge?
            if(TriangulationUtil.orient(edge.vector2, node.next.next.vector, edge.vector1) == Orientation.CounterClockwise)
            {
                //If below
                fillRightConvexEdgeEvent(sweepContext, edge, node.next);
            }
            else
            {
                //If above
            }
        }
    }

    /**
     * Complete an edge event against a right edge below the current
     * 
     * @param sweepContext sweep context
     * @param edge Previous edge
     * @param node closest node to the right of the edge
     */
    private static void fillRightBelowEdgeEvent(SweepContext sweepContext, SweepConstraint edge, SweepingFrontNode node)
    {
        if(node.vector.getX() < edge.vector1.getX())
        {
            if(TriangulationUtil.orient(node.vector, node.next.vector, node.next.next.vector) == Orientation.CounterClockwise)
            {
                //if concave
                fillRightConcaveEdgeEvent(sweepContext, edge, node);
            }
            else
            {
                //if convex
                fillRightConvexEdgeEvent(sweepContext, edge, node);
                //Retry this one
                fillRightBelowEdgeEvent(sweepContext, edge, node);
            }

        }        
    }

    /**
     * Complete an edge event against a right edge above the current
     * 
     * @param sweepContext sweep context
     * @param edge Previous edge
     * @param node closest node to the right of the edge
     */
    private static void fillRightAboveEdgeEvent(SweepContext sweepContext ,SweepConstraint edge, SweepingFrontNode node)
    {
        while(node.next.vector.getX() < edge.vector1.getX())
        {
            //Check if next node is below the edge
            Orientation o1 = TriangulationUtil.orient(edge.vector2, node.next.vector, edge.vector1);
            if(o1 == Orientation.CounterClockwise)
            {
                fillRightBelowEdgeEvent(sweepContext, edge, node);                        
            }
            else
            {
                node = node.next;
            }            
        }        
    }

    /**
     * Complete an edge event against a left convex edge
     * 
     * @param sweepContext sweep context
     * @param edge Previous edge
     * @param node closest node to the right of the edge
     */
    private static void fillLeftConvexEdgeEvent(SweepContext sweepContext, SweepConstraint edge, SweepingFrontNode node)
    {
        //Is next edge concave or convex?
        if(TriangulationUtil.orient(node.prev.vector, node.prev.prev.vector, node.prev.prev.prev.vector) == Orientation.Clockwise)
        {
            //if concave
            fillLeftConcaveEdgeEvent(sweepContext, edge, node.prev);
        }
        else
        {
            //if convex
            //Is the next edge an above or below edge?
            if(TriangulationUtil.orient(edge.vector2, node.prev.prev.vector, edge.vector1) == Orientation.Clockwise)
            {
                //if below
                fillLeftConvexEdgeEvent(sweepContext, edge, node.prev);
            }
            else
            {
                //if above
            }
        }
    }
   
    /**
     * Complete an edge event against a left concave edge
     * 
     * @param sweepContext sweep context
     * @param edge Previous edge
     * @param node closest node to the right of the edge
     */
    private static void fillLeftConcaveEdgeEvent(SweepContext sweepContext, SweepConstraint edge, SweepingFrontNode node)
    {
        fill(sweepContext, node.prev);
        if(node.prev.vector != edge.vector1)
        {
            //is the next edge an above or below edge?
            if(TriangulationUtil.orient(edge.vector2, node.prev.vector, edge.vector1) == Orientation.Clockwise)
            {
                //if below
                if(TriangulationUtil.orient(node.vector, node.prev.vector, node.prev.prev.vector) == Orientation.Clockwise)
                {
                    //if the next edge is concave
                    fillLeftConcaveEdgeEvent(sweepContext, edge, node);
                }
                else
                {
                    //if the next edge is convex
                }
            }
        }
    }

    /**
     * Complete an edge event against a left edge below the current
     * 
     * @param sweepContext sweep context
     * @param edge Previous edge
     * @param node closest node to the right of the edge
     */
    private static void fillLeftBelowEdgeEvent(SweepContext sweepContext, SweepConstraint edge, SweepingFrontNode node)
    {
        if(node.vector.getX() > edge.vector1.getX())
        {
            if(TriangulationUtil.orient(node.vector, node.prev.vector, node.prev.prev.vector) == Orientation.Clockwise)
            {
                //if concave
                fillLeftConcaveEdgeEvent(sweepContext, edge, node);
            }
            else
            {
                //if convex
                fillLeftConvexEdgeEvent(sweepContext, edge, node);
                //Retry this one
                fillLeftBelowEdgeEvent(sweepContext, edge, node);
            }

        }        
    }
   
    /**
     * Complete an edge event against a left edge above the current
     * 
     * @param sweepContext sweep context
     * @param edge Previous edge
     * @param node closest node to the right of the edge
     */
    private static void fillLeftAboveEdgeEvent(SweepContext sweepContext, SweepConstraint edge, SweepingFrontNode node)
    {
        while(node.prev.vector.getX() > edge.vector1.getX())
        {
            //Check if the next node is below the edge
            Orientation o1 = TriangulationUtil.orient(edge.vector2, node.prev.vector, edge.vector1);
            if(o1 == Orientation.Clockwise)
            {
                fillLeftBelowEdgeEvent(sweepContext, edge, node);                        
            }
            else
            {
                node = node.prev;
            }            
        }        
    }

    /**
     * Check if the edge is a side (not foot) of the triangle
     * 
     * @param triangle Trangle to check
     * @param edgeVector1 First edge to check
     * @param edgeVector2 Second edge to check
     * @return determines if the edge is on the side
     */
    private static boolean isEdgeSideOfTriangle(DelaunayTriangle triangle, TriangulationVec edgeVector1, TriangulationVec edgeVector2)
    {
        int index;
        index = triangle.edgeIndex(edgeVector1, edgeVector2);
        if(index != -1)
        {
            triangle.markConstrainedEdge(index);
            triangle = triangle.neighbours[index];
            if(triangle != null)
            {
                triangle.markConstrainedEdge(edgeVector1, edgeVector2);
            }
            return true;
        }        
        return false;
    }
   
    /**
     * Set up edge data
     * 
     * @param sweepContext Sweep context
     * @param edgeVector1 First vector to check
     * @param edgeVector2 Second vector to check
     * @param triangle Triangle to check
     * @param vector  Vector to add
     */
    private static void edgeEvent(SweepContext sweepContext, TriangulationVec edgeVector1, TriangulationVec edgeVector2, DelaunayTriangle triangle, TriangulationVec vector)
    {
        TriangulationVec vector1, vector2;
               
        if(isEdgeSideOfTriangle(triangle, edgeVector1, edgeVector2))
        {
            return;
        }
       
        vector1 = triangle.pointCounterClockwise(vector);
        Orientation orientation1 = TriangulationUtil.orient(edgeVector2, vector1, edgeVector1);

        vector2 = triangle.pointClockwise(vector);
        Orientation orientation2 = TriangulationUtil.orient(edgeVector2, vector2, edgeVector1);

        if(orientation1 == orientation2)
        {
            //Need to decide if we are rotating clockwise or counter-clockwise to get to
            //a triangle that will cross the edge
            if(orientation1 == Orientation.Clockwise)
            {
                triangle = triangle.neighborCounterClockwise(vector);
            }
            else
            {
                triangle = triangle.neighborClockwise(vector);                
            }
            edgeEvent(sweepContext, edgeVector1, edgeVector2, triangle, vector);
        }
        else
        {
            //This triangle crosses the constraint so lets start here
            flipEdgeEvent(sweepContext, edgeVector1, edgeVector2, triangle, vector);
        }        
    }

    /**
     * Triangle is across the constraint
     * 
     * @param sweepContext Sweep context
     * @param edgeVector1 First vector to check
     * @param edgeVector2 Second vector to check
     * @param triangle Triangle to check
     * @param vector  Vector to add
     */
    private static void flipEdgeEvent(SweepContext sweepContext, TriangulationVec edgeVector1, TriangulationVec edgeVector2, DelaunayTriangle triangle, TriangulationVec vector)
    {
        TriangulationVec originVector, newVector;
        DelaunayTriangle originTriangle;
        boolean inScanArea;
       
        originTriangle = triangle.neighborAcross(vector);
        originVector = originTriangle.oppositePoint(triangle, vector);

        if(originTriangle == null)
        {
            //If we want to integrate the fillEdgeEvent do it here with current implementation we should never get here
            throw new RuntimeException( "[BUG:FIXME] FLIP failed due to missing triangle");
        }

        inScanArea = TriangulationUtil.inScanArea(vector, triangle.pointCounterClockwise(vector), triangle.pointClockwise(vector), originVector);
        if(inScanArea)
        {
            //Lets rotate shared edge one vertex clockwise
            rotateTrianglePair(triangle, vector, originTriangle, originVector);
            sweepContext.mapTriangleToNodes(triangle);
            sweepContext.mapTriangleToNodes(originTriangle);
           
            if(vector == edgeVector2 && originVector == edgeVector1)
            {
                if(edgeVector2 == sweepContext.edgeEvent.constrainedEdge.vector2 && edgeVector1 == sweepContext.edgeEvent.constrainedEdge.vector1)
                {
                    triangle.markConstrainedEdge(edgeVector1, edgeVector2);
                    originTriangle.markConstrainedEdge(edgeVector1, edgeVector2);
                    legalize(sweepContext, triangle);                    
                    legalize(sweepContext, originTriangle);  
                }
            }                          
            else
            {
                Orientation orientation = TriangulationUtil.orient(edgeVector2, originVector, edgeVector1);
                triangle = nextFlipTriangle(sweepContext, orientation, triangle, originTriangle, vector, originVector);
                flipEdgeEvent(sweepContext, edgeVector1, edgeVector2, triangle, vector);
            }
        }
        else
        {
            newVector = nextFlipPoint(edgeVector1, edgeVector2, originTriangle, originVector);
            flipScanEdgeEvent(sweepContext, edgeVector1, edgeVector2, triangle, originTriangle, newVector);
            edgeEvent(sweepContext, edgeVector1, edgeVector2, triangle, vector);                
        }
    }

    /**
     * When we need to traverse from one triangle to the next we need the point in the current triangle that is the opposite point to the next triangle.
     * 
     * @param edgeVector1 First point on the edge
     * @param edgeVector2 Second point on the edge
     * @param originTriangle Current triangle to traverse from
     * @param originVector Origin point of the current triangle
     * @return 
     */
    private static TriangulationVec nextFlipPoint(TriangulationVec edgeVector1, TriangulationVec edgeVector2, DelaunayTriangle originTriangle, TriangulationVec originVector)
    {
        Orientation orientation = TriangulationUtil.orient(edgeVector2, originVector, edgeVector1);
        if(orientation == Orientation.Clockwise)
        {
            //Right
            return originTriangle.pointCounterClockwise(originVector);
        }
        else if(orientation == Orientation.CounterClockwise)
        {
            //Left                
            return originTriangle.pointClockwise(originVector);
        }
        else
        {
            return null;
        }                    
    }
   
    /**
     * After a flip we have two triangles and know that only one will still be intersecting the edge.
     * So decide which to continue with and legalize the other
    * 
     * @param sweepContext sweep context
     * @param orientation orientation of the current triangle
     * @param tri current triangle
     * @param originTriangle origin triangle we will perform the flip around
     * @param vector Point on the first triangle that shares an edge with the other
     * @param originVector Point on the second triangle that shares an edge with the other
     * @return Flipped triangle
     */
    private static DelaunayTriangle nextFlipTriangle(SweepContext sweepContext, Orientation orientation, DelaunayTriangle tri, DelaunayTriangle originTriangle, TriangulationVec vector, TriangulationVec originVector)
    {
        int edgeIndex;
        if(orientation == Orientation.CounterClockwise)
        {
            //the origin triangle is not crossing an edge after flip
            edgeIndex = originTriangle.edgeIndex(vector, originVector);
            originTriangle.delaunayEdge[edgeIndex] = true;
            legalize(sweepContext, originTriangle);
            originTriangle.clearDelunayEdges();
            return tri;
        }
        //the other triangle is not crossing an edge after flip
        edgeIndex = tri.edgeIndex(vector, originVector);
        tri.delaunayEdge[edgeIndex] = true;
        legalize(sweepContext, tri);
        tri.clearDelunayEdges();            
        return originTriangle;
    }
   
    /**
     * Scan part of the FlipScan algorithm, when a triangle pair isn't flippable we will scan for the next point that is inside the flip triangle scan area.
     * When found we generate a new flipEdgeEvent
     * @param sweepContext sweep context
     * @param edgeVector1 First point of the edge
     * @param edgeVector2 Second point of the edge
     * @param flipTriangle Triangle we will flip
     * @param tri Second triangle that we cant flip
     * @param vector Point inside the scan area we will flip the triangle around
     */
    private static void flipScanEdgeEvent(SweepContext sweepContext, TriangulationVec edgeVector1, TriangulationVec edgeVector2, DelaunayTriangle flipTriangle, DelaunayTriangle tri, TriangulationVec vector)
    {
        DelaunayTriangle originTriangle;
        TriangulationVec originVector, newVector;
        boolean inScanArea;
               
        originTriangle = tri.neighborAcross(vector);
        originVector = originTriangle.oppositePoint(tri, vector);        
       
        inScanArea = TriangulationUtil.inScanArea(edgeVector2, flipTriangle.pointCounterClockwise(edgeVector2), flipTriangle.pointClockwise(edgeVector2), originVector);
        if(inScanArea)
        {
            flipEdgeEvent(sweepContext, edgeVector2, originVector, originTriangle, originVector);                
        }
        else
        {
            newVector = nextFlipPoint(edgeVector1, edgeVector2, originTriangle, originVector);
            flipScanEdgeEvent(sweepContext, edgeVector1, edgeVector2, flipTriangle, originTriangle, newVector);    
        }
    }

    /**
     * Fills holes in the Advancing Front
     * 
     * @param sweepContext sweep context
     * @param node current node we want to fill
     */
    private static void fillAdvancingFront(SweepContext sweepContext, SweepingFrontNode node)
    {
        SweepingFrontNode newNode;
        double angle;
       
        //Fill right holes
        newNode = node.next;
        while(newNode.next != null)
        {
            angle = holeAngle(newNode);
            if(angle > Math.PI/2 || angle < -Math.PI/2)
            {
                break;
            }
            fill(sweepContext, newNode);                    
            newNode = newNode.next;
        }

        //Fill left holes
        newNode = node.prev;
        while(newNode.prev != null)
        {
            angle = holeAngle(newNode);
            if(angle > Math.PI/2 || angle < -Math.PI/2)
            {
                break;
            }
            fill(sweepContext, newNode);
            newNode = newNode.prev;
        }

        //Fill right basins
        if(node.hasNext() && node.next.hasNext())
        {
            angle = basinAngle(node);
            if(angle < (3 * Math.PI/4))
            {
                fillBasin(sweepContext, node);
            }
        }
    }

    /**
     * Fills a basin that has formed on the Advancing Front to the right of given node.
     * First we decide a left, bottom and right node that forms the boundaries of the basin
     * Then we do a recursive fill.
     * 
     * @param sweepContext sweep context
     * @param node current node
     */
    private static void fillBasin(SweepContext sweepContext, SweepingFrontNode node)
    {
        if(TriangulationUtil.orient(node.vector, node.next.vector, node.next.next.vector) == Orientation.CounterClockwise)
        {
            sweepContext.basin.leftNode = node;
        }
        else
        {
            sweepContext.basin.leftNode = node.next;
        }
       
        //Find the bottom and right node
        sweepContext.basin.bottomNode = sweepContext.basin.leftNode;
        while(sweepContext.basin.bottomNode.hasNext() && sweepContext.basin.bottomNode.vector.getY() >= sweepContext.basin.bottomNode.next.vector.getY())
        {
            sweepContext.basin.bottomNode = sweepContext.basin.bottomNode.next;
        }
        if(sweepContext.basin.bottomNode == sweepContext.basin.leftNode)
        {
            //No valid basin
            return;
        }
       
        sweepContext.basin.rightNode = sweepContext.basin.bottomNode;
        while(sweepContext.basin.rightNode.hasNext() && sweepContext.basin.rightNode.vector.getY() < sweepContext.basin.rightNode.next.vector.getY())
        {
            sweepContext.basin.rightNode = sweepContext.basin.rightNode.next;
        }        
        if(sweepContext.basin.rightNode == sweepContext.basin.bottomNode)
        {
            //No valid basins
            return;
        }
       
        sweepContext.basin.width = sweepContext.basin.rightNode.getVector().getX() - sweepContext.basin.leftNode.getVector().getX();
        sweepContext.basin.leftHighest = sweepContext.basin.leftNode.getVector().getY() > sweepContext.basin.rightNode.getVector().getY();
       
        fillBasinReq(sweepContext, sweepContext.basin.bottomNode);        
    }
   
    /**
     * Recursive algorithm to fill a Basin with triangles
     * 
     * @param sweepContext sweep context
     * @param node current node
     */
    private static void fillBasinReq(SweepContext sweepContext, SweepingFrontNode node)
    {
        //if shallow stop filling
        if(isShallow(sweepContext, node))
        {            
            return;
        }
       
        fill(sweepContext, node);
        if(node.prev == sweepContext.basin.leftNode && node.next == sweepContext.basin.rightNode)
        {
            return;
        }
        else if(node.prev == sweepContext.basin.leftNode)
        {
            Orientation orientation = TriangulationUtil.orient(node.vector, node.next.vector, node.next.next.vector);
            if(orientation == Orientation.Clockwise)
            {
                return;
            }
            node = node.next;
        }
        else if(node.next == sweepContext.basin.rightNode)
        {
            Orientation orientation = TriangulationUtil.orient(node.vector, node.prev.vector, node.prev.prev.vector);
            if(orientation == Orientation.CounterClockwise)
            {
                return;
            }
            node = node.prev;
        }
        else
        {
            //Continue with the neighbor node with lowest Y value
            if(node.prev.vector.getY() < node.next.vector.getY())
            {
                node = node.prev;
            }
            else
            {
                node = node.next;
            }
        }        
        fillBasinReq(sweepContext, node);
    }
   
    /**
     * Check if the current node is a shallow (has higher width than height)
     * 
     * @param sweepContext sweep context
     * @param node current node
     * @return determines if the current node is shallow
     */
    private static boolean isShallow(SweepContext sweepContext, SweepingFrontNode node)
    {
        double height;

        if(sweepContext.basin.leftHighest)
        {
            height = sweepContext.basin.leftNode.getVector().getY() - node.getVector().getY();
        }
        else
        {
            height = sweepContext.basin.rightNode.getVector().getY() - node.getVector().getY();            
        }
        if(sweepContext.basin.width > height)
        {
            return true;
        }        
        return false;
    }
   
    /**
     * Get the angle between 3 nodes to determine the hole angle
     * 
     * @param node current node
     * @return angle of the hole
     */
    private static double holeAngle(SweepingFrontNode node)
    {
        final double vectorX = node.vector.getX();
        final double vectorY = node.vector.getY();
        final double angleAX = node.next.vector.getX() - vectorX;
        final double angleAY = node.next.vector.getY() - vectorY;
        final double angleBX = node.prev.vector.getX() - vectorX;
        final double angleBY = node.prev.vector.getY() - vectorY;
        return Math.atan2(angleAX * angleBY - angleAY * angleBX, angleAX * angleBX + angleAY * angleBY);
    }

    /**
     * The basin angle is decided against the horizontal line [1,0]
     * 
     * @param node current node
     * @return angle of the basin
     */
    private static double basinAngle(SweepingFrontNode node)
    {
        double angleX = node.vector.getX() - node.next.next.vector.getX();
        double angleY = node.vector.getY() - node.next.next.vector.getY();
        return Math.atan2(angleY, angleX);
    }

    /**
     * Adds a triangle to the advancing front to fill a hole.
     * 
     * @param sweepContext sweep context
     * @param node  current node
     */
    private static void fill(SweepContext sweepContext, SweepingFrontNode node)
    {
        DelaunayTriangle triangle = new DelaunayTriangle(node.prev.vector, node.vector, node.next.vector);
        
        triangle.markNeighbor(node.prev.triangle);
        triangle.markNeighbor(node.triangle);
        sweepContext.addToList(triangle);

        //Update the advancing front
        node.prev.next = node.next;
        node.next.prev = node.prev;
       
        //If it was legalized the triangle has already been mapped
        if(!legalize(sweepContext, triangle))
        {
            sweepContext.mapTriangleToNodes(triangle);
        }
    }
   
    /**
     * Returns true if triangle was legalized
     * 
     * @param sweepContext sweep context
     * @param tri current triangle
     * @return determines if a triangle is legal or not
     */
    private static boolean legalize(SweepContext sweepContext, DelaunayTriangle tri)
    {
        int originIndex;
        boolean inside;
        TriangulationVec triVector, originVector;
        DelaunayTriangle originTri;
        //To legalize a triangle we start by finding if any of the three edges violate the Delaunay condition
        for(int i = 0; i < 3; i++)
        {
            if(tri.delaunayEdge[i])
            {
                continue;
            }
            originTri = tri.neighbours[i];
            if(originTri != null)
            {
                triVector = tri.vectors[i];
                originVector = originTri.oppositePoint(tri, triVector);
                originIndex = originTri.index(originVector);
                //If this is a Constrained Edge or a Delaunay Edge (only during recursive legalization) then we should not try to legalize
                if(originTri.constrainedEdge[originIndex] || originTri.delaunayEdge[originIndex])
                {
                    tri.constrainedEdge[i] = originTri.constrainedEdge[originIndex];
                    continue;
                }
                inside = TriangulationUtil.inCircle(triVector, tri.pointCounterClockwise(triVector), tri.pointClockwise(triVector), originVector);
                if(inside)
                {
                    boolean notLegalized;
                   
                    //Lets mark this shared edge as Delaunay
                    tri.delaunayEdge[i] = true;
                    originTri.delaunayEdge[originIndex] = true;

                    //Lets rotate shared edge one vertex clockwise to legalize it
                    rotateTrianglePair(tri, triVector, originTri, originVector);

                    //We now got one valid Delaunay Edge shared by two triangles
                    //This gives us 4 new edges to check for Delaunay

                    //Make sure that triangle to node mapping is done only one time for a specific triangle
                    notLegalized = !legalize(sweepContext, tri);
                    if(notLegalized)
                    {
                        sweepContext.mapTriangleToNodes(tri);                        
                    }
                    notLegalized = !legalize(sweepContext, originTri);
                    if(notLegalized)
                    {
                        sweepContext.mapTriangleToNodes(originTri);                        
                    }

                    tri.delaunayEdge[i] = false;
                    originTri.delaunayEdge[originIndex] = false;

                    //If triangle has been legalized then there is no need to check the other edges since the recursive legalization will handle those so we can end here.
                    return true;
                }
            }
        }  
        return false;
    }    
   
    /**
     * Rotates a triangle pair one vertex clockwise
     * 
     * @param triangle Current triangle
     * @param vector Vertex to rotate
     * @param originTriangle Origin triangle to rotate around
     * @param originVector Origin vertex to rotate around
     */
    private static void rotateTrianglePair(DelaunayTriangle triangle, TriangulationVec vector, DelaunayTriangle originTriangle, TriangulationVec originVector)
    {
        DelaunayTriangle triangle1, triangle2, triangle3, triangle4;
        triangle1 = triangle.neighborCounterClockwise(vector);
        triangle2 = triangle.neighborClockwise(vector);
        triangle3 = originTriangle.neighborCounterClockwise(originVector);
        triangle4 = originTriangle.neighborClockwise(originVector);

        boolean constrainedEdge1, constrainedEdge2, constrainedEdge3, constrainedEdge4;
        constrainedEdge1 = triangle.getConstrainedEdgeCCW(vector);
        constrainedEdge2 = triangle.getConstrainedEdgeClockwise(vector);
        constrainedEdge3 = originTriangle.getConstrainedEdgeCCW(originVector);
        constrainedEdge4 = originTriangle.getConstrainedEdgeClockwise(originVector);
       
        boolean delaunayEdge1, delaunayEdge2, delaunayEdge3, delaunayEdge4;
        delaunayEdge1 = triangle.getDelunayEdgeCCW(vector);
        delaunayEdge2 = triangle.getDelunayEdgeCW(vector);
        delaunayEdge3 = originTriangle.getDelunayEdgeCCW(originVector);
        delaunayEdge4 = originTriangle.getDelunayEdgeCW(originVector);
       
        triangle.legalize(vector, originVector);
        originTriangle.legalize(originVector, vector);
       
        //Remap delaunay edge
        originTriangle.setDelunayEdgeCCW(vector, delaunayEdge1);
        triangle.setDelunayEdgeCW(vector, delaunayEdge2);
        triangle.setDelunayEdgeCCW(originVector, delaunayEdge3);
        originTriangle.setDelunayEdgeCW(originVector, delaunayEdge4);

        //Remap constrained edge
        originTriangle.setConstrainedEdgeCCW(vector, constrainedEdge1);
        triangle.setConstrainedEdgeCW(vector, constrainedEdge2);
        triangle.setConstrainedEdgeCCW(originVector, constrainedEdge3);
        originTriangle.setConstrainedEdgeCW(originVector, constrainedEdge4);
       
        //Remap neighbours
        triangle.clearNeighbors();
        originTriangle.clearNeighbors();
        if(triangle1 != null)
        {
            originTriangle.markNeighbor(triangle1);
        }
        if(triangle2 != null)
        {
            triangle.markNeighbor(triangle2);
        }
        if(triangle3 != null)
        {
            triangle.markNeighbor(triangle3);
        }
        if(triangle4 != null)
        {
            originTriangle.markNeighbor(triangle4);
        }
        triangle.markNeighbor(originTriangle);
    }    
}
