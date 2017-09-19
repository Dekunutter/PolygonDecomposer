package com.base.engine;

import org.jbox2d.collision.AABB;
import org.jbox2d.common.Vec2;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import static org.lwjgl.opengl.GL11.*;

/**
 * Controls the position of the camera by tracking the user's mouse input in the OpenGL window
 * 
 * @author Jordan
 */
public class CameraController
{
    public static CameraController controller;
    private int mouseX, mouseY;
    public float translationX, translationY, translationZ, mouseInWorldX, mouseInWorldY;
    private AABB aabb;
    
    /**
     * Initialise the controller class and it's variables related to mouse input
     */
    public CameraController()
    {
        mouseX = 0;
        mouseY = 0;
        translationX = 0;
        translationY = 0;
        translationZ = 1;
    }
    
    /**
     * Handle input from the mouse within the OpenGL window
     */
    public void getInput()
    {
        repositionCamera();
                
        zoomCamera();
    }
    
    /**
     * Camera position handled by the mouse position
     */
    private void repositionCamera()
    {
        //before updating the mouse position in this frame store the old position seperately, for use in camera movement
        boolean mouseRightPressed = Mouse.isButtonDown(1);
        int oldX = mouseX;  
        int oldY = mouseY;
        mouseX = Mouse.getX();
        mouseY = Mouse.getY();
         
        //the mouse's position in the OpenGL world, rather than it's position within the window
        mouseInWorldX = (mouseX/translationZ) - translationX;
        mouseInWorldY = (mouseY/translationZ) - translationY;
        
        //printMouseValues();

        //The axis-aligned bounding box of the mouse's true position in the world
        //Used by Box2D as a medium between the interactions of the mouse and the world
        aabb = new AABB();
        aabb.lowerBound.x = (mouseInWorldX - 1f)/30;
        aabb.lowerBound.y = (mouseInWorldY - 1f)/30;
        aabb.upperBound.x = (mouseInWorldX + 1f)/30;
        aabb.upperBound.y = (mouseInWorldY + 1f)/30;
            
        //reposition camera
        if(mouseRightPressed)
        {
            translationX += (mouseX - oldX)/translationZ;
            translationY += (mouseY - oldY)/translationZ;
        }
    }
    
    /**
     * Camera zooming handled by the mouse wheel
     */
    private void zoomCamera()
    {
        int dWheel = Mouse.getDWheel();
        if(dWheel < 0)
        {
            //don't let it zoom into negative space (getting "behind" the camera and flipping the image awkwardly)
            if(translationZ > 0.075f)
            {
                translationZ -= 0.075f;
            }
        }
        else if(dWheel > 0)
        {
            translationZ += 0.075f;
        }
    }
    
    /**
     * Print out values related to the mouse for debugging
     */
    private void printMouseValues()
    {
        System.out.println("MOUSE " + mouseInWorldX + " " + mouseInWorldY);
        System.out.println("ANGLE " + mouseX + " " + mouseY);
        System.out.println("ZOOM " + translationZ);
    }
    
    /**
     * Camera renderer, updates the camera view and position visually
     */
    public void render()
    {
        glMatrixMode(GL_PROJECTION);
	glLoadIdentity();
        glOrtho(0, Display.getWidth() / translationZ, 0, Display.getHeight() / translationZ, -1, 1);  
        
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glTranslatef(translationX, translationY, 0);
    }
    
    /*
     * Fetch mouse position in the world
     */
    public Vec2 getMousePos()
    {
        return new Vec2((Mouse.getX() / translationZ) - translationX, (Mouse.getY() / translationZ) - translationY);
    }
    
    /*
     * Fetch the world's current translation values
     */
    public Vec2 getAngles()
    {
        return new Vec2(translationX, translationY);
    }
    
    /*
     * Fetch the world's current zoom value
     */
    public float getZ()
    {
        return translationZ;
    }
    
    /*
     * Set the X and Y translation, repositioning the view
     * 
     * @param translation 2D Vector representing the new translation of the camera
     */
    public void setAngles(Vec2 translation)
    {
        translationX = translation.x;
        translationY = translation.y;
    }
    
    /*
     * Set the zoom orientated to fit an object to the screen's width
     * 
     * @param width Width of the object
     */
    public void zoomToObjectWidth(float width)
    {
        translationZ = Display.getWidth() / width;
    }
    
    /**
     * Set the zoom orientated to fit an object to the screen's height
     * 
     * @param height Height of the object 
     */
    public void zoomToObjectHeight(float height)
    {
        translationZ = Display.getHeight() / height;
    }
}
