package com.base.engine.imageparsing;

import org.jbox2d.common.Vec2;

/**
 * Holds all data associated to a pixel for the image parsing algorithms
 * 
 * @author Jordan
 */
public class Pixel
{
    public static final int NORTH = 1;
    public static final int SOUTH = -1;
    public static final int EAST = 1;
    public static final int WEST = -1;
    public static final int CENTRE = 0;
    
    public int x;
    public int y;
    
    private int nspX = -1;
    private int nspY = 0;
    private int lastX = -1;
    private int lastY = 0;
    
    /**
     * Declare a pixel via its coordinates in the image
     * 
     * @param x Coordinates along the X axis
     * @param y Coordinates along the Y axis
     */
    public Pixel(int x, int y)
    {
        this.x = x;
        this.y = y;
    }
    
    /**
     * Declare a pixel via its coordinates and its positional relation to the previous pixel in the list
     * 
     * @param x Coordinates along the X axis
     * @param y Coordinates along the Y axis
     * @param nspX Positional relation to the previous pixel along X
     * @param nspY Positional relation to the previous pixel along Y
     */
    public Pixel(int x, int y, int nspX, int nspY)
    {
        this.x = x;
        this.y = y;
        
        this.nspX = nspX;
        this.nspY = nspY;
    }
    
    /**
     * Set the position of the pixel
     * 
     * @param x Coordinates along the X axis
     * @param y Coordinates along the Y axis
     */
    public void set(int x, int y)
    {
        this.x = x;
        this.y = y;
    }
    
    /**
     * Add this pixel's coordinates to another's
     * 
     * @param p Pixel whose coordinates we are adding
     * @return New resulting pixel
     */
    public Pixel add(Pixel p)
    {
        return new Pixel(x + p.x, y + p.y);
    }
    
    /**
     * Subtract this pixel's coordinates from another's
     * 
     * @param p Pixel whose coordinates we are subtracting
     * @return New resulting pixel
     */
    public Pixel sub(Pixel p)
    {
        return new Pixel(x - p.x, y - p.y);
    }
    
    /**
     * Convert a pixel to a Box2d Vec2 variable type
     * 
     * @return Box2D vertex representing this pixel
     */
    public Vec2 toVec2()
    {
        return new Vec2(x, y);
    }

    /**
     * Multiply this pixel's coordinates by a scalar
     * 
     * @param i Scalar we are multiplying by
     * @return New resulting pixel
     */
    public Pixel mult(int i)
    {
        return new Pixel(x * i, y * i);
    }
    
    @Override
    public String toString()
    {
        return "x:" + x + " y:" + y;
    }
    
    /**
     * Set positional relation values of this pixel using a another pixel's coordinates
     * 
     * @param p Other pixel we will use
     */
    public void setNSP(Pixel p)
    {
        System.out.println(p.x + " " + p.y);
        nspX = p.x;
        nspY = p.y;
    }
    
    /**
     * Get counter clockwise pixel from a default value of NSP
     * 
     * @return Resulting pixel
     */
    public Pixel counterClockwise()
    {
        Pixel nsp = counterClockwise(nspX, nspY);
        nspX = nsp.x;
        nspY = nsp.y;
        return add(nsp);
    }
    
    /**
     * Overload for the counterClockwise check, returning the check's pixel value with the parameter coordinates injected
     * 
     * @param x Coordinates along the X axis
     * @param y Coordinates along the Y axis
     * @return Resulting pixel
     */
    public Pixel counterClockwise(int x, int y)
    {
        return counterClockwise(new Pixel(x, y));
    }

    /**
     * Store the location of the last pixel that was checked
     */
    public void setLastPixel()
    {
        lastX = nspX;
        lastY = nspY;
        Pixel last = new Pixel(lastX, lastY);
    }
    
    //NOTE: (-1, 1) not calculated yet. No situation yet has needed it
    //PROBLEMS: Cannot handle situations where part of the image is only one pixel thick
    //
    /**
     * Check the positions of the current pixel and the last pixel checked to determine the direction that the parser will take next
     * Ensures every single boundary pixel is processed
     * Allows for a pixel-to-pixel accurate list of points that will allow the image to be reconstructed as a polygon later
     * 
     * @param sp Pixel we will be starting from
     * @return Previous pixel checked
     */
    public Pixel counterClockwise(Pixel sp)
    {
        //if the last pixel parsed was west-centre to the current
        if(lastX == -1 && lastY == 0)       
        {
            if(sp.x == WEST)
            {
                if(sp.y == SOUTH)
                {
                    return new Pixel(WEST, CENTRE);
                }
                else if(sp.y == CENTRE)
                {
                    return new Pixel(CENTRE, SOUTH);
                }
                else if(sp.y == NORTH)
                {
                    return new Pixel(WEST, SOUTH);
                }
            }

            if(sp.x == CENTRE)
            {
                if(sp.y == SOUTH)
                {
                    return new Pixel(EAST, CENTRE);
                }
                else if(sp.y == NORTH)
                {
                    return new Pixel(EAST, NORTH);
                }
            }

            if(sp.x == EAST)
            {
                if(sp.y == SOUTH)
                {
                    return new Pixel(WEST, NORTH);
                }
                else if(sp.y == CENTRE)
                {
                    return new Pixel(CENTRE, NORTH);
                }
                else if(sp.y == NORTH)
                {
                    return new Pixel(EAST, SOUTH);
                }
            }
        }
        
        if(lastX == 0 && lastY == 1)
        {
            if(sp.x == WEST)
            {
                if(sp.y == SOUTH)
                {
                    return new Pixel(WEST, NORTH);
                }
                else if(sp.y == CENTRE)
                {
                    return new Pixel(CENTRE, SOUTH);
                }
                else if(sp.y == NORTH)
                {
                    return new Pixel(CENTRE, NORTH);
                }
            }

            if(sp.x == CENTRE)
            {
                if(sp.y == SOUTH)
                {
                    return new Pixel(EAST, CENTRE);
                }
                else if(sp.y == NORTH)
                {
                    return new Pixel(WEST, CENTRE);
                }
            }

            if(sp.x == EAST)
            {
                if(sp.y == SOUTH)
                {
                    return new Pixel(EAST, NORTH);
                }
                else if(sp.y == CENTRE)
                {
                    return new Pixel(EAST, SOUTH);
                }
                else if(sp.y == NORTH)
                {
                    return new Pixel(WEST, SOUTH);
                }
            }
        }
        
        if(lastX == 0 && lastY == -1)
        {
            if(sp.x == WEST)
            {
                if(sp.y == SOUTH)
                {
                    return new Pixel(EAST, NORTH);
                }
                else if(sp.y == CENTRE)
                {
                    return new Pixel(WEST, SOUTH);
                }
                else if(sp.y == NORTH)
                {
                    return new Pixel(EAST, SOUTH);
                }
            }

            if(sp.x == CENTRE)
            {
                if(sp.y == SOUTH)
                {
                    return new Pixel(EAST, CENTRE);
                }
                else if(sp.y == NORTH)
                {
                    return new Pixel(WEST, CENTRE);
                }
            }

            if(sp.x == EAST)
            {
                if(sp.y == SOUTH)
                {
                    return new Pixel(CENTRE, SOUTH);
                }
                else if(sp.y == CENTRE)
                {
                    return new Pixel(CENTRE, NORTH);
                }
                else if(sp.y == NORTH)
                {
                    return new Pixel(WEST, NORTH);
                }
            }
        }
        
        if(lastX == 1 && lastY == 0)
        {
            if(sp.x == WEST)
            {
                if(sp.y == SOUTH)
                {
                    return new Pixel(EAST, SOUTH);
                }
                else if(sp.y == CENTRE)
                {
                    return new Pixel(CENTRE, SOUTH);
                }
                else if(sp.y == NORTH)
                {
                    return new Pixel(WEST, SOUTH);
                }
            }

            if(sp.x == CENTRE)
            {
                if(sp.y == SOUTH)
                {
                    return new Pixel(EAST, NORTH);
                }
                else if(sp.y == NORTH)
                {
                    return new Pixel(WEST, CENTRE);
                }
            }

            if(sp.x == EAST)
            {
                if(sp.y == SOUTH)
                {
                    return new Pixel(EAST, CENTRE);
                }
                else if(sp.y == CENTRE)
                {
                    return new Pixel(CENTRE, NORTH);
                }
                else if(sp.y == NORTH)
                {
                    return new Pixel(WEST, NORTH);
                }
            }
        }
        
        if(lastX == -1 && lastY == -1)
        {
            if(sp.x == WEST)
            {
                if(sp.y == SOUTH)
                {
                    return new Pixel(EAST, CENTRE);
                }
                else if(sp.y == CENTRE)
                {
                    return new Pixel(CENTRE, SOUTH);
                }
                else if(sp.y == NORTH)
                {
                    return new Pixel(EAST, SOUTH);
                }
            }

            if(sp.x == CENTRE)
            {
                if(sp.y == SOUTH)
                {
                    return new Pixel(EAST, NORTH);
                }
                else if(sp.y == NORTH)
                {
                    return new Pixel(WEST, CENTRE);
                }
            }

            if(sp.x == EAST)
            {
                if(sp.y == SOUTH)
                {
                    return new Pixel(WEST, SOUTH);
                }
                else if(sp.y == CENTRE)
                {
                    return new Pixel(CENTRE, NORTH);
                }
                else if(sp.y == NORTH)
                {
                    return new Pixel(WEST, NORTH);
                }
            }
        }

        if(lastX == 1 && lastY == 1)
        {
            if(sp.x == WEST)
            {
                if(sp.y == SOUTH)
                {
                    return new Pixel(WEST, NORTH);
                }
                else if(sp.y == CENTRE)
                {
                    return new Pixel(EAST, CENTRE);
                }
                else if(sp.y == NORTH)
                {
                    return new Pixel(EAST, SOUTH);
                }
            }

            if(sp.x == CENTRE)
            {
                if(sp.y == SOUTH)
                {
                    return new Pixel(CENTRE, NORTH);
                }
                else if(sp.y == NORTH)
                {
                    return new Pixel(WEST, SOUTH);
                }
            }

            if(sp.x == EAST)
            {
                if(sp.y == SOUTH)
                {
                    return new Pixel(WEST, CENTRE);
                }
                else if(sp.y == CENTRE)
                {
                    return new Pixel(EAST, NORTH);
                }
                else if(sp.y == NORTH)
                {
                    return new Pixel(CENTRE, SOUTH);
                }
            }
        }
        
        if(lastX == 1 && lastY == -1)
        {
            if(sp.x == WEST)
            {
                if(sp.y == SOUTH)
                {
                    return new Pixel(EAST, CENTRE);
                }
                else if(sp.y == CENTRE)
                {
                    return new Pixel(CENTRE, NORTH);
                }
                else if(sp.y == NORTH)
                {
                    return new Pixel(WEST, SOUTH);
                }
            }

            if(sp.x == CENTRE)
            {
                if(sp.y == SOUTH)
                {
                    return new Pixel(EAST, SOUTH);
                }
                else if(sp.y == NORTH)
                {
                    return new Pixel(WEST, NORTH);
                }
            }

            if(sp.x == EAST)
            {
                if(sp.y == SOUTH)
                {
                    return new Pixel(WEST, CENTRE);
                }
                else if(sp.y == CENTRE)
                {
                    return new Pixel(EAST, NORTH);
                }
                else if(sp.y == NORTH)
                {
                    return new Pixel(CENTRE, SOUTH);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Check if this pixel equals another from their coordinate values
     * 
     * @param p Other pixel to check against
     * @return boolean to determine if these pixels match or not
     */
    public boolean equals(Pixel p)
    {
        if(x == p.x && y == p.y)
        {
            return true;
        }
        return false;
    }    
}
