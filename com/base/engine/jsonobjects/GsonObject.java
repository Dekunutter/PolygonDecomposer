package com.base.engine.jsonobjects;

import org.jbox2d.common.Vec2;

/**
 * Stores image data in GSON format
 * 
 * @author Jordan
 */
public class GsonObject
{
    private String name;
    private String imagepath;
    private Vec2 origin;
    private GsonShape[] shapes;
    
    public GsonObject(String name, String imagePath, Vec2 origin, GsonShape[] shapes)
    {
        this.name = name;
        this.imagepath = imagePath;
        this.origin = origin;
        this.shapes = shapes;
    }
}
