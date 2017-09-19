package com.base.simulation;

import java.util.concurrent.TimeUnit;

/**
 * Handles all time manipulation
 * 
 * @author Jordan
 */
public class Time
{
    //constant value used to moderate the speed of the simulation
    public static final float DAMPING = 0.00000004f;         
    
    private static long currentTime;                                            
    private static long lastTime;                                      
    
    /**
     * Get the current system time for the current frame
     * 
     * @return current system time
     */
    public static long getTime()                                                
    {
        return System.nanoTime();                                              
    }

    /**
     * Get the delta (time between this frame and the last)
     * 
     * @return delta time
     */
    public static float getDelta()                                              
    {
        return (currentTime - lastTime) * DAMPING;                              
    }
    
    /**
     * Remove the damping from the delta for the timestep.
     * 
     * @return non-dampened delta time
     */
    public static float getDeltaMillis()
    {
        long t = (long)(getDelta()/DAMPING);
        return TimeUnit.MILLISECONDS.convert(t, TimeUnit.NANOSECONDS);        
    }
    
    /**
     * Update the time values for the new frame
     */
    public static void update()                                                 
    {
        lastTime = currentTime;                                                 
        currentTime = getTime();                                                
    }
    
    /**
     * Initialise the time object
     */
    public static void init()                                                   
    {
        currentTime = getTime();                                               
        lastTime = getTime();                                         
    }
}
