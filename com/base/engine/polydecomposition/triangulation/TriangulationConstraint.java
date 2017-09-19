package com.base.engine.polydecomposition.triangulation;

/**
 * The constraint by which two triangulation points will be checked in the DTSweep
 * 
 * @author Jordan
 */
public class TriangulationConstraint
{
    protected TriangulationVec vector1;
    protected TriangulationVec vector2;

    public TriangulationVec getVector1()
    {
        return vector1;
    }

    public TriangulationVec getVector2()
    {
        return vector2;
    }
}
