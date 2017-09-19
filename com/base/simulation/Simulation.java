package com.base.simulation;

import com.base.engine.CameraController;
import com.base.engine.WorldObject;
import com.base.engine.TextureLoader;
import com.base.simulation.worldobjects.VectorObject;
import java.util.ArrayList;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.lwjgl.input.Keyboard;

/**
 * Main class of the entire program.
 * Handles the simulation, all the OpenGL rendering, logic and input is spun off from here
 * 
 * @author Jordan
 */
public class Simulation
{   
    public static Simulation simulation;                                                    
    private static World world;
    private ArrayList<WorldObject> objects;                                      
    private ArrayList<WorldObject> remove;                                      
    
    private boolean paused;
    private boolean pausePressed, pauseWasPressed;
    
    private String str;
    
    /**
     * Initialise the simulation
     */
    public Simulation()
    {                 
        //set up a gravity value for the simulation to abide by (disable it essentially)
        Vec2 gravity = new Vec2(0.0f, 0.0f);          
        world = new World(gravity);        
                
        //store all active world objects in here  
        objects = new ArrayList<>();                  
        //store all world objects flagged for removal in here    
        remove = new ArrayList<>();                          
            
        CameraController.controller = new CameraController();
        
        paused = false;
        pausePressed = false;
        pauseWasPressed = false;
    }
    
    /**
     * Get the textureloader object
     * 
     * @return textureloader class
     */
    public static TextureLoader getTextureLoader()
    {
        return TextureLoader.textureLoader;
    }
    
    /**
     * Get the Box2d world class
     * 
     * @return Box2d world
     */
    public static World getWorld()
    {
        return world;
    }
    
    /**
     * Get and handle user input every frame
     */
    public void getInput()                                                      
    {
        pausePressed = Keyboard.isKeyDown(Keyboard.KEY_P);   
        
        if(pausePressed && !pauseWasPressed)
        {
            if(!paused)
            {
                paused = true;
            }
            else
            {
                paused = false;
            }
        }
        pauseWasPressed = pausePressed;
        
        //if not paused, fetch input from the controller object
        if(!paused)             
        {
            CameraController.controller.getInput();                                                     
        }
    }
    
    /**
     * Handle updated logic every frame
     */
    public void update()                                                        
    {
        if(!paused)
        {   
            int millistep = 16;
            float timeStep = (float)millistep/1000;
            int accumulator = 0;
            accumulator += (Time.getDeltaMillis());
            while(accumulator >= millistep)
            {
                //increment through the Box2d physics logic with a timestep value
                world.step(timeStep, 8, 3);             
                accumulator -= millistep;
            }
            
            world.clearForces();
            for(WorldObject wo : objects)
            {
                //as long as an object isn't flagged for removal, call it's update method
                if(wo.getRemove())                     
                {
                    remove.add(wo);
                }
            }
            
            //remove each object that is flagged for removal and its associated Box2d body
            for(WorldObject wo : remove)                
            {
                objects.remove(wo);
                if(wo.getBody() != null)
                {
                    wo.removeBody();
                }
            }     
            
            //spawns a queued object from the mouse's current position
            if(str != null)
            {   
                GUI.txtOutput.setText("STRING " + str);
                VectorObject vo = null;
                try
                {
                    vo = new VectorObject(str);
                    if(vo.getBody() != null)
                    {
                        CameraController.controller.setAngles(new Vec2(0, 0));
                        if(vo.getSizeX() >= vo.getSizeY())
                        {
                            CameraController.controller.zoomToObjectWidth(vo.getSizeX());
                        }
                        else
                        {
                            CameraController.controller.zoomToObjectHeight(vo.getSizeY());
                        }
                    }
                    else
                    {
                        vo.remove();
                    }
                }
                catch(Exception ex)
                {
                    //exit the function in the case of an error creating the object
                    GUI.txtOutput.setText("OBJECT COULD NOT BE CREATED, POSSIBLE TRIANGULATION ERROR");
                    str = null;
                    return;                             
                }
                
                objects.add(vo);
                str = null;
            }
        }
    }
    
    /**
     * Call the render method for each world object and the controller
     */
    public void render()                                                        
    {
        CameraController.controller.render();
        for(WorldObject go : objects)
        {
            go.render();
        }
    }
    
    /**
     * Set the paused state
     * 
     * @param paused new value of the paused state
     */
    public void setPaused(boolean paused)
    {
        this.paused = paused;
    }
    
    /**
     * Get the list of world objects
     * 
     * @return list of objects in the world
     */
    public ArrayList<WorldObject> getObjects()                                  
    {
        return objects;
    }

    /**
     * Remove a world object from world objects list
     * 
     * @param target object to remove from the list
     */
    public void removeObject(WorldObject target)                                
    {
        for(WorldObject go : objects)                                            
        {
            if(go == target)                                                  
            {
                objects.remove(go);                                             
            }
        }
    }
    
    /**
     * Queue a object for spawn in the world
     * 
     * @param str Object name to queue for spawning
     */
    public void queueSpawns(String str)
    {
        this.str = str;

        //whenever a new object is queued to spawn, make sure the current one is removed
        for(WorldObject wo : objects)                               
        {
            if(!wo.getRemove())
            {
                remove.add(wo);
            }
        }
    }
}
