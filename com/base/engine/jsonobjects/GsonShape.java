package com.base.engine.jsonobjects;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.BodyType;

/**
 * Store's shape data in GSON format
 * 
 * @author Jordan
 */
public class GsonShape
{
    private BodyType type;
    private Vec2[] vertices;
    
    public GsonShape(BodyType type, Vec2[] vertices)
    {
        this.type = type;
        this.vertices = vertices;
    }
}
