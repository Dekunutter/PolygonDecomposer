package com.base.engine.polydecomposition.triangulation;

import com.base.engine.polydecomposition.PolyDecomposition;
import com.base.engine.polydecomposition.polygon.TriangulatablePolygon;
import java.util.ArrayList;

/**
 * The thread that handles the proper triangulation process
 * 
 * @author Jordan
 */
public class TriangulationProcess implements Runnable
{
    private TriangulationContext context;
    private Thread thread;
   
    private ArrayList<Triangulatable> triangulations = new ArrayList<>();
   
    /**
     * Uses SweepLine algorithm
     */
    public TriangulationProcess()
    {
        context = PolyDecomposition.createContext();
    }

    /**
     * Triangulate a Polygon
     * 
     * @param polygon Polygon we wish to triangulate
     */
    public void triangulate(TriangulatablePolygon polygon)
    {
        triangulations.clear();
        triangulations.add(polygon);
        start();
    }

    /**
     * Start the thread
     */
    private void start()
    {
        thread = new Thread(this, "Sweep");
        thread.start();
    }

    /**
     * Run the thread
     */
    @Override
    public void run()
    {
        try
        {
            //for each triangulatable object, clear the context and call the algorithm
            for(Triangulatable tri:  triangulations)
            {
                context.clear();                            
                context.prepareTriangulation(tri);            
                PolyDecomposition.triangulate(context);     
            }
        }
        catch(RuntimeException e)
        {
            System.out.println("Thread " + thread.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        catch( Exception e )
        {
            System.out.println("Triangulation exception " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Fetch the triangulatable context of this triangulation object
     * 
     * @return Triangulation context
     */
    public TriangulationContext getContext()
    {
        return context;
    }
}
