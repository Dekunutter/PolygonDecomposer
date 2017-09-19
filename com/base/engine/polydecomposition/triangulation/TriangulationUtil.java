package com.base.engine.polydecomposition.triangulation;

/**
 * Utility class with static functions used during the triangulation process
 * 
 * @author Jordan
 */
public class TriangulationUtil
{
    public final static double EPSILON = 1e-12;

    /**
     * Check if the triangulation points are within a circle, meaning they are candidates to form a triangle
     * 
     * @param vectorA First vertex
     * @param vectorB Second vertex
     * @param vectorC Third vertex
     * @param vectorD Fourth vertex
     * @return determines whether the 4 given vertices are in a circle
     */
    public static boolean inCircle(final TriangulationVec vectorA, final TriangulationVec vectorB, final TriangulationVec vectorC, final TriangulationVec vectorD)
    {
        final double vectorDX = vectorD.getX();
        final double vectorDY = vectorD.getY();
        final double vectorAtoVectorDX = vectorA.getX() - vectorDX;
        final double vectorAtoVectorDY = vectorA.getY() - vectorDY;        
        final double vectorBtoVectorDX = vectorB.getX() - vectorDX;
        final double vectorBtoVectorDY = vectorB.getY() - vectorDY;

        final double adXbdY = vectorAtoVectorDX * vectorBtoVectorDY;
        final double bdXadY = vectorBtoVectorDX * vectorAtoVectorDY;
        final double abd = adXbdY - bdXadY;

        if(abd <= 0)
        {
            return false;
        }

        final double vectorCtoVectorDX = vectorC.getX() - vectorDX;
        final double vectorCtoVectorDY = vectorC.getY() - vectorDY;

        final double cdXadY = vectorCtoVectorDX * vectorAtoVectorDY;
        final double adXcdY = vectorAtoVectorDX * vectorCtoVectorDY;
        final double cad = cdXadY - adXcdY;

        if(cad <= 0)
        {
            return false;
        }
       
        final double bdXcdY = vectorBtoVectorDX * vectorCtoVectorDY;
        final double cdXbdY = vectorCtoVectorDX * vectorBtoVectorDY;
       
        final double vectorALift = vectorAtoVectorDX * vectorAtoVectorDX + vectorAtoVectorDY * vectorAtoVectorDY;
        final double vectorBLift = vectorBtoVectorDX * vectorBtoVectorDX + vectorBtoVectorDY * vectorBtoVectorDY;
        final double vectorCLift = vectorCtoVectorDX * vectorCtoVectorDX + vectorCtoVectorDY * vectorCtoVectorDY;

        final double result = vectorALift * (bdXcdY - cdXbdY) + vectorBLift * cad + vectorCLift * abd;

        return result > 0;
    }
   
    /**
     * Check if the trianglulation points are in a common area under scan
     * 
     * @param vectorA First vertex
     * @param vectorB Second vertex
     * @param vectorC Third vertex
     * @param vectorD Fourth vertex
     * @return  determines whether the 4 given vertices are in the scan area
     */
    public static boolean inScanArea(final TriangulationVec vectorA, final TriangulationVec vectorB, final TriangulationVec vectorC, final TriangulationVec vectorD)
    {
        final double vectorDX = vectorD.getX();
        final double vectorDY = vectorD.getY();
        final double vectorAtoVectorDX = vectorA.getX() - vectorDX;
        final double vectorAtoVectorDY = vectorA.getY() - vectorDY;        
        final double vectorBtoVectorDX = vectorB.getX() - vectorDX;
        final double vectorBtoVectorDY = vectorB.getY() - vectorDY;

        final double adXbdY = vectorAtoVectorDX * vectorBtoVectorDY;
        final double bdXadY = vectorBtoVectorDX * vectorAtoVectorDY;
        final double abd = adXbdY - bdXadY;
        
        if(abd <= 0)
        {
            return false;
        }

        final double vectorCtoVectorDX = vectorC.getX() - vectorDX;
        final double vectorCtoVectorDY = vectorC.getY() - vectorDY;

        final double cdXadY = vectorCtoVectorDX * vectorAtoVectorDY;
        final double adXcdY = vectorAtoVectorDX * vectorCtoVectorDY;
        final double cad = cdXadY - adXcdY;

        if(cad <= 0)
        {
            return false;
        }
        return true;
    }
   
    /**
     * Forumla to calculate signed area. Positive if counter-clockwise. Negative if clockwise
     * 
     * @param vectorA First vertex
     * @param vectorB Second vertex
     * @param vectorC Third vertex
     * @return Orientation of the points in the area
     */
    public static Orientation orient(TriangulationVec vectorA, TriangulationVec vectorB, TriangulationVec vectorC)
    {
        double left = (vectorA.getX() - vectorC.getX()) * (vectorB.getY() - vectorC.getY());
        double right = (vectorA.getY() - vectorC.getY()) * (vectorB.getX() - vectorC.getX());
        double value = left - right;
        if(value > -EPSILON && value < EPSILON)
        {
            return Orientation.Collinear;                    
        }
        else if(value > 0)
        {
            return Orientation.CounterClockwise;
        }
        return Orientation.Clockwise;
    }

    /**
     * enum class for the different orientations used during triangulation
     */
    public enum Orientation
    {
        Clockwise, CounterClockwise, Collinear;
    }
}
