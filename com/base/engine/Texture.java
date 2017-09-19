package com.base.engine;

import static org.lwjgl.opengl.GL11.*;

/**
 * Stores data related to each texture
 * 
 * @author Jordan
 */
public class Texture
{
    private int target, textureID, height, width;
    private float normalizedWidth, normalizedHeight;
    
    /**
     * Initialise a texture object with a target and an ID
     * 
     * @param target ID of the target object we will bind the texture to
     * @param textureID ID of the texture we want to bind
     */
    public Texture(int target, int textureID)
    {
        this.target = target;
        this.textureID = textureID;
    }
    
    /**
     * Bind a texture to its target in OpenGL
     */
    public void bind()
    {
        glBindTexture(target, textureID);
    }
    
    /**
     * Set the texture height
     * 
     * @param height New height of the texture
     */
    public void setHeight(int height)
    {
        this.height = height;
    }
    
    /**
     * Set the texture width
     * 
     * @param width New width of the texture
     */
    public void setWidth(int width)
    {
        this.width = width;
    }
    
    /**
     * Get the height of the texture
     * 
     * @return height of the texture
     */
    public int getHeight()
    {
        return height;
    }
    
    /**
     * Get the width of the texture
     * 
     * @return width of the texture
     */
    public int getWidth()
    {
        return width;
    }
    
    //setting and getting of the normalized sizes.
    //These can be used to calculate the difference in size a texture needs to consider after it's been converted from NPOT to POT
    public void setNormalizedWidth(int potWidth)
    {
        normalizedWidth = width/(float)potWidth;
    }
    
    public void setNormalizedHeight(int potHeight)
    {
        normalizedHeight = height/(float)potHeight;
    }
    
    public float getNormalizedWidth()
    {
        return normalizedWidth;
    }
    
    public float getNormalizedHeight()
    {
        return normalizedHeight;
    }
}
