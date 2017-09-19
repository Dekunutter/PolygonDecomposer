package com.base.engine;

import com.base.simulation.GUI;
import com.base.simulation.Simulation;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import static org.lwjgl.opengl.GL11.*;

/**
 * Abstract implementation with which every single Box2d object within the game world will inherit from
 * 
 * @author Jordan
 */
public abstract class WorldObject
{    
    protected Body body;
    protected float sizeX, sizeY;    
    protected float r, g, b;
    protected Sprite spr;                                                       
    protected int type;                                                          
    protected boolean[] flags = new boolean[2];                                        
    
    /**
     * Common render code for objects in the world.
     * Most objects won't need to override this
     */
    public void render()                                                        
    {
        glPushMatrix();                                                                                                
        {
            //get the world coordinates of the Box2d objects in pixels, for OpenGL to render with
            Vec2 bodyPosition = body.getPosition().mul(WorldScale.RATIO);       
            glTranslatef(bodyPosition.x, bodyPosition.y, 0);
            glRotated(Math.toDegrees(body.getAngle()), 0, 0, 1);
            if(GUI.showSprite)
            {
                //render the object as intended (with texture)    
                spr.render();                                                
            }
            else
            {
                //render the object using data from it's Box2d body definition
                spr.render(body);           
            }
        }
        glPopMatrix();                                                          
    }
    
    /**
     * Get the X axis position of the Box2d body in the world
     * 
     * @return position on the X axis
     */
    public float getX()                                                        
    {
        return body.getPosition().x;
    }
    
    /**
     * Get the Y axis position of the Box2d body in the world
     * 
     * @return position on the Y axis
     */
    public float getY()                                                         
    {
        return body.getPosition().y;
    }
    
    /**
     * Get the width of the Box2d body
     * 
     * @return width of the body
     */
    public float getSizeX()                                                     
    {
        return sizeX;
    }
    
    /**
     * Get the height of the Box2d body
     * 
     * @return height of the body
     */
    public float getSizeY()                                                     
    {
        return sizeY;
    }
    
    /**
     * Check whether or not this object is queued for deletion from memory
     * 
     * @return boolean marking objects for removal
     */
    public boolean getRemove()                                                  
    {
        return flags[0];
    }
    
    /**
     * Check of the object is solid
     * 
     * @return boolean marking objects for collision
     */
    public boolean getSolid()                                                   
    {
        return flags[1];
    }
    
    /**
     * Set the object into the queue for removal from memory by the end of this frame
     */
    public void remove()                                                        
    {
        flags[0] = true;                                                        
    }
    
    /**
     * Set the object to solid
     * 
     * @param value whether the object will be a solid or not
     */
    public void setSolid(boolean value)                                         
    {
        flags[1] = value;
    }
    
    /**
     * Get the body definition of the Box2d body
     * 
     * @return Box2D body of this object
     */
    public Body getBody()
    {
        return body;
    }
    
    /**
     * Remove the box2d body definition from the object
     * Not the same as removing from the simulation entirely
     */
    public void removeBody()
    {
        Simulation.getWorld().destroyBody(body);
    }
}
