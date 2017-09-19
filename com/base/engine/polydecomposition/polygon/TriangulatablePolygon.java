package com.base.engine.polydecomposition.polygon;

import com.base.engine.polydecomposition.triangulation.Triangulatable;
import com.base.engine.polydecomposition.triangulation.TriangulationContext;
import com.base.engine.polydecomposition.triangulation.TriangulationVec;
import com.base.engine.polydecomposition.triangulation.delaunay.DelaunayTriangle;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores all data related to a "polygon" object by which the algorithm computes triangles
 * 
 * @author Jordan
 */
public class TriangulatablePolygon implements Triangulatable
{
    public ArrayList<TriangulationVec> vectors = new ArrayList<TriangulationVec>();
    protected ArrayList<TriangulatablePolygon> holes = new ArrayList<TriangulatablePolygon>();

    protected List<DelaunayTriangle> triangles;

    protected PolygonVec last;

    /**
     * Requires at least 3 points, no duplicates to create a polygon object
     * 
     * @param vectors List of points in the polygon
     */
    public TriangulatablePolygon(List<PolygonVec> vectors)
    {
        //NOTE: Would be cool if the duplicate removing was done here, but PolygonPoints passed into here seem to be modified for the algorithm, making it impossible.
        
        //Lets do one sanity check that first and last point hasn't got same position
        //Its something that often happen when importing polygon data from other formats
        if(vectors.get(0).equals(vectors.get(vectors.size() - 1)))
        {
            vectors.remove(vectors.size() - 1);
        }
        this.vectors.addAll(vectors);
    }

    /**
     * Return the amount of points in this polygon
     * 
     * @return count of all points in the polygon
     */
    public int vectorCount()
    {
        return vectors.size();
    }

    /**
     * Add a hole to the polygon
     * 
     * @param poly Polygon we are adding the hole to
     */
    public void addHole(TriangulatablePolygon poly)
    {
        holes.add(poly);
    }

    /**
     * Get the last point in the polygon's points list
     * 
     * @return last point in the polygon
     */
    public PolygonVec getLastVector()
    {
        return last;
    }
   
    /**
     * Get the list of points
     * 
     * @return List of triangle points in the polygon
     */
    public List<TriangulationVec> getVectors()
    {
        return vectors;
    }

    /**
     * Get the list of triangles
     * 
     * @return Triangles in the polygon
     */
    @Override
    public List<DelaunayTriangle> getTriangles()
    {
        return triangles;
    }
   
    /**
     * Add a triangle to the polygon
     * 
     * @param tri Triangle we are adding to the polygon
     */
    @Override
    public void addTriangle(DelaunayTriangle tri)
    {
        triangles.add(tri);
    }

    /**
     * Creates constraints and populates the context with points
     * 
     * @param context Triangulation context we are using
     */
    @Override
    public void prepare(TriangulationContext context)
    {
        if(triangles == null)
        {
            triangles = new ArrayList<DelaunayTriangle>(vectors.size());
        }
        else
        {
            triangles.clear();
        }

        //Outer constraints
        for(int i = 0; i < vectors.size() - 1 ; i++)
        {
            context.newConstraint(vectors.get(i), vectors.get(i + 1));
        }
        context.newConstraint(vectors.get(0), vectors.get(vectors.size() - 1));
        context.addVectors(vectors);

        //Hole constraints
        if(holes != null)
        {
            for(TriangulatablePolygon p : holes)
            {
                for(int i = 0; i < p.vectors.size() - 1 ; i++)
                {
                    context.newConstraint(p.vectors.get(i), p.vectors.get(i + 1));
                }
                context.newConstraint(p.vectors.get(0), p.vectors.get(p.vectors.size() - 1));            
                context.addVectors(p.vectors);
            }
        }
    }
    
    /**
     * Check if the polygon is valid
     * Mainly checking if it is convex, but also ensuring it doesn't exceed the maximum number of sides a polygon should have in Box2D
     * 
     * @return is the polygon valid
     */
    public boolean isConvex()
    {
        boolean isPositive = false;
        for(int i = 0; i < vectors.size(); i++)
        {
            int lower;
            if(i == 0)
            {
                lower = vectors.size() - 1;
            }
            else
            {
                lower = i - 1;
            }
            
            int middle = i;
            
            int upper;
            if(i == vectors.size() - 1)
            {
                upper = 0;
            }
            else
            {
                upper = i + 1;
            }
            
            float dx0 = vectors.get(middle).getXf() - vectors.get(lower).getXf();
            float dy0 = vectors.get(middle).getYf() - vectors.get(lower).getYf();
            float dx1 = vectors.get(upper).getXf() - vectors.get(middle).getXf();
            float dy1 = vectors.get(upper).getYf() - vectors.get(middle).getYf();
            float cross = dx0 * dy1 - dx1 * dy0;
            
            boolean newIsPositive;
            if(cross > 0)
            {
                newIsPositive = true;
            }
            else
            {
                newIsPositive = false;
            }
            
            if(i == 0)
            {
                isPositive = newIsPositive;
            }
            else if(isPositive != newIsPositive)
            {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Add an existing triangle to this polygon
     * 
     * @param tri Triangle we are adding to the polygon
     * @return Remaining polygon area to triangulate
     */
    public TriangulatablePolygon add(DelaunayTriangle tri)
    {
        //if the polygon has over 5 vectors, then its not going to accept another triangle, so exit the function as null
        if(vectors.size() > 5)
        {
            return null;
        }
        
        int firstPolyPoint = -1;
        int firstTriPoint = -1;
        int secondPolyPoint = -1;
        int secondTriPoint = -1;
        for(int i = 0; i < vectors.size(); i++)
        {
            if((tri.vectors[0].getXf() == vectors.get(i).getXf()) && (tri.vectors[0].getYf() == vectors.get(i).getYf()))
            {
                if(firstPolyPoint == -1)
                {
                    firstPolyPoint = i;
                    firstTriPoint = 0;
                }
                else
                {
                    secondPolyPoint = i;
                    secondTriPoint = 0;
                }
            }
            else if((tri.vectors[1].getXf() == vectors.get(i).getXf()) && (tri.vectors[1].getYf() == vectors.get(i).getYf()))
            {
                if(firstPolyPoint == -1)
                {
                    firstPolyPoint = i;
                    firstTriPoint = 1;
                }
                else
                {
                    secondPolyPoint = i;
                    secondTriPoint = 1;
                }
            }
            else if((tri.vectors[2].getXf() == vectors.get(i).getXf()) && (tri.vectors[2].getYf() == vectors.get(i).getYf()))
            {
                if(firstPolyPoint == -1)
                {
                    firstPolyPoint = i;
                    firstTriPoint = 2;
                }
                else
                {
                    secondPolyPoint = i;
                    secondTriPoint = 2;
                }
            }
        }
        
        //correct the order of the vertices
        if((firstPolyPoint == 0) && (secondPolyPoint == vectors.size() - 1))
        {
            firstPolyPoint = vectors.size() - 1;
            secondPolyPoint = 0;
        }
        
        //if order wasn't fixed, exit as null
        if(secondPolyPoint == -1)
        {
            return null;
        }
        
        //get the point that is the tip of the elonggated triangle
        int triTip = 0;
        if((triTip == firstTriPoint) || (triTip == secondTriPoint))
        {
            triTip = 1;
        }
        if(triTip == firstTriPoint || triTip == secondTriPoint)
        {
            triTip = 2;
        }
        
        //store the computed vertice data in float arrays
        float[] newX = new float[vectors.size() + 1];
        float[] newY = new float[vectors.size() + 1];
        int currentVec = 0;
        for(int i = 0; i < vectors.size(); i++)
        {
            newX[currentVec] = vectors.get(i).getXf();
            newY[currentVec] = vectors.get(i).getYf();
            if(i == firstPolyPoint)
            {
                currentVec++;
                newX[currentVec] = tri.vectors[triTip].getXf();
                newY[currentVec] = tri.vectors[triTip].getYf();
            }
            currentVec++;
        }
        
        //convert the float arrays to an arraylist of PolygonVec objects, to be passed into the constructor as a new Polygon object
        ArrayList<PolygonVec> newVecs = new ArrayList<PolygonVec>();
        for(int i = 0; i < newX.length; i++)
        {
            newVecs.add(new PolygonVec(newX[i], newY[i]));
        }
        
        //final sanity check to ensure the new vector data for this polygon does not exceed 8
        if(newVecs.size() > 8)
        {
            return null;
        }
        
        return new TriangulatablePolygon(newVecs);
    }
}
