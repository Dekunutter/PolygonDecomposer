package com.base.engine.imageparsing;

import com.base.simulation.GUI;
import com.base.simulation.Simulation;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import org.jbox2d.common.Vec2;

/**
 * Algorithms for taking an image and parsing it into a series of points
 * Class is final since some overridable functions are called in the constructor
 * 
 * @author Jordan
 */
public final class ParseImage
{
    private BufferedImage img;
    private ArrayList<Vec2> points  = new ArrayList<Vec2>();
    private ArrayList<ArrayList<Vec2>> holes = new ArrayList<ArrayList<Vec2>>();
    private ArrayList<Vec2> holePoints = new ArrayList<Vec2>();
    private ArrayList<Vec2> holeTotals = new ArrayList<Vec2>();
    private boolean doHoles = false;
    
    /**
     * Initialise the priliminary algorithm
     * 
     * @param path Filepath of the image to parse 
     */
    public ParseImage(String path)
    {
        try
        {
            img = Simulation.getTextureLoader().loadImage(path);
        }
        catch (IOException e)
        {
            System.out.println("Error: Image not found");
        }
        
        BufferedImage invertedImage = null;
        Graphics2D graphics2D;
        AffineTransform at;
        if(!GUI.doubleBuffer)
        {
            //ensure the image can handle transparency
            invertedImage = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);

            //transform the image into a graphics2d object for transformation
            graphics2D = invertedImage.createGraphics();
            at = new AffineTransform();
            at.scale(1, -1);
            at.translate(0, -img.getHeight());

            graphics2D.drawImage(img, at, null);
        }
        else
        {
            //don't understand it, but it works!
            //Also it's really slow! AT LEAST doubling the processing time
            //It is also essentially doubling the number of points within the same space
            //This skews polygonization a bit
            invertedImage = new BufferedImage(img.getWidth() * 2, img.getHeight() * 2, BufferedImage.TYPE_INT_ARGB);
            graphics2D = invertedImage.createGraphics();
            at = AffineTransform.getScaleInstance(1, -1);
            at.translate(0, -img.getHeight(null));
            AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
            img = op.filter(img, null);
            graphics2D.drawImage(img, 0, 0, img.getWidth() * 2, img.getHeight() * 2, null);
        }
        graphics2D.dispose();

        GUI.txtOutput.setText("Begin parsing...");
        img = invertedImage;
        
        //begin the parser algorithm
        loopOverImage();
    }
    
    /**
     * Get the vertices extracted from the image
     * 
     * @return Arraylist of vertices that make up the image's pixel outline
     */
    public ArrayList<Vec2> getPoints()
    {
	return points;
    }
    
    /**
     * Get the list of holes in the image and their respective points extracted from the image
     * 
     * @return Nested Arraylist of vertices, each nested arraylist representing an outline of a hole in the image
     */
    public ArrayList<ArrayList<Vec2>> getHoles()
    {
        return holes;
    }
    
    /**
     * Determine if a pixel has an alpha value in a BufferedImage
     * 
     * @param x coordinate of the pixel along X
     * @param y coordinate of the pixel along Y
     * @return boolean value determining if the checked pixel has an alpha value
     */
    public boolean hasAlpha(int x, int y)
    {
        if(!((img.getRGB(x, y) >>> 24) == 0x00))
        {
            return true;
        }
        return false;
    }
    
    /**
     * Test a pixel for alpha
     * If the pixel is beyond the image range, return false
     * 
     * @param x coordinate of the pixel along X
     * @param y coordinate of the pixel along Y
     * @return boolean value determining if the checked pixel has an alpha value or if it is even in range of the image
     */
    public boolean pointHasAlpha(int x, int y)
    {
        if(x < 0 || x > (img.getWidth() - 1) || y < 0 || y > (img.getHeight() - 1))
        {
            return false;
        }
        else
        {
            if(hasAlpha(x, y))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
    }
    
    /**
     * Test to see if a pixel on the image is a boundary pixel of the shape
     * 
     * @param point Pixel we wish to check
     * @return determines if the pixel is on the shape's boundary
     */
    private boolean isBoundary(Pixel point)
    {
	return isBoundary(point.x, point.y);
    }
    
    /**
     * Test to see if a coordinate on the image is on the shape's boundary
     * 
     * @param x coordinate of the pixel along X
     * @param y coordinate of the pixel along Y
     * @return determines if the pixel is on the shape's boundary
     */
    public boolean isBoundary(int x, int y)
    {
        if(!pointHasAlpha(x, y))
        {
            return false;
        }
        
        for(int i = -1; i < 2; i++)
        {
            for(int j = -1; j < 2; j++)
            {
                if(!pointHasAlpha(x + i, y + j))
                {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Loop over image until first boundary pixel is defined
     */
    public void loopOverImage()
    {
        if(img != null)
        {
            outerloop:
            
            //start on the left of the image and slowly head right after each column of pixels is processed
            for(int cx = 0; cx < img.getWidth(); cx++)      
            {
                //read from bottom to top of the image
                for(int cy = 0; cy < img.getHeight(); cy++) 
                {
                    if(!points.contains(new Vec2(cx, cy)) && !holeTotals.contains(new Vec2(cx, cy)))
                    {
                        if(doHoles)
                        {
                            GUI.txtOutput.setText("Searching for holes... (" + cx + ", " + cy + ")");
                        }
                        //if this is the first boundary pixel detected
                        if(isBoundary(cx, cy))          
                        {
                            //traverse the boundary of the image to extract it's coordinates
                            traverseBoundary(cx, cy);   
                            if(GUI.checkHoles)
                            {
                                //the boundary of the shape is secured, so now do the holes
                                doHoles = true;            
                                //if a hole was found and mapped out
                                if(holePoints.size() > 0)   
                                {
                                    //add to the list of holes
                                    ArrayList<Vec2> t = new ArrayList<Vec2>();
                                    holes.add(t);           
                                }
                                for(int i = 0; i < holePoints.size(); i++)
                                {
                                    holeTotals.add(holePoints.get(i));
                                    holes.get(holes.size() - 1).add(holePoints.get(i));
                                }
                                holePoints.clear();
                            }
                            else
                            {
                                break outerloop;
                            }
                        }
                    }
                }
            }
            GUI.txtOutput.setText("Image boundaries formed");
        }
        else
        {
            System.out.println("Image not found for parsing");
        }
    }
    
    /**
     * Traverse over the boundaries of the shape the image contains, pixel by pixel
     * 
     * @param x coordinate of the pixel along X
     * @param y coordinate of the pixel along Y
     */
    public void traverseBoundary(int x, int y)
    {
        //store the details of the pixel being examined
        Pixel start = new Pixel(x, y, Pixel.WEST, Pixel.CENTRE);    
        Pixel next = start;
        Pixel old = new Pixel(-1, -1);
        
        //if this is not a check for holes in the image
        if(!doHoles)            
        {
            points.add(start.toVec2());
            GUI.txtOutput.setText("Beginning at point (" + points.get(points.size() - 1).x + ", " + points.get(points.size() - 1).y + ") parsed");
        }
        else                    
        {
            //else if it is a check for holes in the image
            holePoints.add(start.toVec2());
            GUI.txtOutput.setText("Beginning at point (" + holePoints.get(holePoints.size() - 1).x + ", " + holePoints.get(holePoints.size() - 1).y + ") parsed");
        }
        
        //TODO: change to while loop maybe. this method limits the size of the image but does provide an easy escape whereas the while loop wouldn't
        for(int i = 0; i < 10000000; i++)                       
        {
            old = next;
            
            //time to check for the next pixel on the shape boundary
            next = findNextBoundaryPoint(next);                 
            next.setNSP(old.sub(next));
            
            //if the boundary loop has completed
            if(next.equals(start))                              
            {
                break;
            }
                
            //if this is an image boundary loop
            if(!doHoles)                                        
            {
                //if, at this point, a duplicate point is being added, then the program runs the risk of running infinite loops on a never ending circuit.
                if(points.contains(next.toVec2()))              
                {
                    System.out.println("ALREADY GOT THIS POINT");
                    GUI.txtOutput.setText("Point (" + next.toVec2().x + ", " + next.toVec2().y + ") already part of array. Exiting method to avoid risk of looping");
                    //empty the points found thus far so that an exception is thrown
                    points = null;     
                    //this has broken the boundary traversal. Break out of the for loop
                    break;                                      
                }
                points.add(next.toVec2());
                GUI.txtOutput.setText("Point (" + points.get(points.size() - 1).x + ", " + points.get(points.size() - 1).y + ") parsed");
                System.out.println("POINTS " + points.get(points.size() - 1).x + " " + points.get(points.size() - 1).y);
            }
            else                                                
            {
                //if this is an image hole boundary loop
                if(holePoints.contains(next.toVec2()))
                {
                    System.out.println("ALREADY GOT THIS POINT");
                    GUI.txtOutput.setText("Point (" + next.toVec2().x + ", " + next.toVec2().y + ") already part of array. Exiting method to avoid risk of looping");
                    points = null;
                    break;
                }
                holePoints.add(next.toVec2());
                GUI.txtOutput.setText("Point (" + holePoints.get(holePoints.size() - 1).x + ", " + holePoints.get(holePoints.size() - 1).y + ") parsed");
                System.out.println("HOLE POINTS " + holePoints.get(holePoints.size() - 1).x + " " + holePoints.get(holePoints.size() - 1).y);
            }
        }
    }
    
    /**
     * Find the next pixel on the shape's boundary
     * 
     * @param p Pixel we are currently positioned at
     * @return Next pixel along the shape's boundary
     */
    public Pixel findNextBoundaryPoint(Pixel p)
    {
        p.setLastPixel();
        //loop through each pixel "block" (containing the current pixel and the 8 surrounding pixels)
        for(int i = 0; i < 9; i++)                  
        {
            Pixel next = null;
            //check each pixel in the block counter-clockwise till one of these pixels are found to be a boundary
            next = p.counterClockwise();       
            
            if(isBoundary(next))                    
            {
                return next;
            }
        }
        
        //break out of the search, seems to be something wrong!
        System.out.println("Error: Next point not found");
        return null;                                
    }

    /**
     * Take the list of points found and average them for a more refined list of points
     * 
     * @param vertices Vertices we found in the image so far
     * @return Average vertex of the vertices
     */
    private Vec2 average(ArrayList<Vec2> vertices)
    {
        Vec2 average = new Vec2(0, 0);
        for(Vec2 v : vertices)
        {
            average = average.add(v);
        }
        average.set(average.x/vertices.size(), average.y/vertices.size());
        return average;
    }
    
    /**
     * Calculate the midpoint of two points
     * 
     * @param v1 First vertex
     * @param v2 Second vertex
     * @return Mid point between the two given vertices
     */
    private Vec2 midPoint(Vec2 v1, Vec2 v2)
    {
	return new Vec2((v1.x + v2.x)/2, (v1.y + v2.y)/2);
    }
    
    /**
     * Get the square root of a value
     * 
     * @param f1 Value we wish to square
     * @return Squared value
     */
    private float sq(float f1)
    {
	return f1 * f1;
    }
    
    //TODO: I think this is what cuts out corners from accidently reoccuring, by getting midpoints. Maybe try see what happens with this removed at some point
    /**
     * "Curves" images by getting an averaged midpoint from 3 vertices
     * 
     * @param v1 First vertex
     * @param v2 Second vertex
     * @param v3 Third vertex
     * @return Gets a midway value between the three vertices to help create some curvature at corners
     */
    private float curvature(Vec2 v1, Vec2 v2, Vec2 v3)
    {
        Vec2 midPoint = midPoint(v1, v3);
        return sq(midPoint.x - v2.x) + sq(midPoint.y - v2.y);
    }
    
    /**
     * Menu for accessing the different simplification algorithms depending on input
     * 
     * @param points Vertices we will be passing into the simplifier algorithms
     * @return New set of simplified vertices
     */
    public ArrayList<Vec2> simplifyMenu(ArrayList<Vec2> points)
    {
        ArrayList<Vec2> returns;
        if(GUI.selectedSimplifier == GUI.RAMER_SIMPLIFIER)
        {
            returns = simplifyDouglas(points);
            GUI.txtOutput.setText("Points simplified with Ramer-Peucker-Douglas Algorithm");
        }
        else if(GUI.selectedSimplifier == GUI.CIRCLE_SIMPLIFIER)
        {
            returns = simplifyCircle(points);
            GUI.txtOutput.setText("Points simplified with personal circle-based algorithm");
        }
        else if(GUI.selectedSimplifier == GUI.FLEXIBLE_SIMPLIFIER)
        {
            returns = simplifyBoth5(points);
            GUI.txtOutput.setText("Points simplified with personal general-purpose algorithm");
        }
        else if(GUI.selectedSimplifier == GUI.ORIG_SIMPLIFIER)
        {
            returns = simplifyold(points);
            GUI.txtOutput.setText("Points simplified with original personal algorithm");
        }
        else
        {
            returns = points;
            GUI.txtOutput.setText("Points not simplified");
        }
        return returns;
    }
    
    /**
     * Simplify a set of points into a smoother line
     * 
     * @param points Original set of points that we wish to simplify
     * @return Simplified set of points
     */
    public ArrayList<Vec2> simplify(ArrayList<Vec2> points)
    {
        ArrayList<Vec2> smoothedLine = new ArrayList<>();
        
        smoothedLine.add(points.get(0));
                
        for(int i = 0; i < points.size(); i++)
        {
            int alphaPix = 0;
            for(int j = -1; j < 2; j++)
            {
                for(int k = -1; k < 2; k++)
                {
                    if(!pointHasAlpha((int)points.get(i).x + j, (int)points.get(i).y + k))
                    {
                        alphaPix++;
                    }
                }
            }
            if(alphaPix > 1)
            {
                smoothedLine.add(points.get(i));
            }
        }
        
        return smoothedLine;
    }
    
    /**
     * Simplify a set of points using the circle algorithm
     * 
     * @param points Original set of points that we wish to simplify
     * @return Simplified set of points
     */
    public ArrayList<Vec2> simplifyCircle(ArrayList<Vec2> points)
    {      
        ArrayList<Vec2> smoothedLine = simplify(points);
        ArrayList<Vec2> simplifiedLine = new ArrayList<Vec2>();
        
        float curvatureTotal = 0;
        float curvature = 0;
        
        //this default seems good for circles
        for(int i = 0; i < smoothedLine.size() - 3; i++)
        {
            curvature = curvature(smoothedLine.get(i), smoothedLine.get(i + 1), smoothedLine.get(i + 2));
            
            curvatureTotal += curvature;
            
            if(curvatureTotal > GUI.limit)
            {
                curvatureTotal = 0;
                simplifiedLine.add(smoothedLine.get(i));
            }
        }
        return simplifiedLine;
    }
    
    /**
     * Simplify a set of points into more squarish shapes
     * 
     * @param points Original set of points that we wish to simplify
     * @return Simplified set of points
     */
    public ArrayList<Vec2> simplifySquare(ArrayList<Vec2> points)
    {
        ArrayList<Vec2> smoothedLine = simplify(points);
        ArrayList<Vec2> simplifiedLine = new ArrayList<>();
        
        float curvatureTotal = 0;
        float curvature = 0;
        
        //this seems good for limbs and such
        for(int i = 0; i < smoothedLine.size() - 3; i++)
        {
            curvature = curvature(smoothedLine.get(i), smoothedLine.get(i + 1), smoothedLine.get(i + 2));
            
            curvatureTotal += curvature;
            if(curvatureTotal > GUI.limit)
            {
                curvatureTotal = 0;
                simplifiedLine.add(smoothedLine.get(i));
            }
            else if((smoothedLine.get(i).x == smoothedLine.get(i + 1).x && smoothedLine.get(i).x == smoothedLine.get(i + 2).x) || (smoothedLine.get(i).y == smoothedLine.get(i + 1).y && smoothedLine.get(i).y == smoothedLine.get(i + 2).y))
            {
                simplifiedLine.add(smoothedLine.get(i));
                i+=2;
            }
        }
        return simplifiedLine;
    }
    
    /**
     * Simplify corners of shapes into more diagonal lines
     * 
     * @param points Original set of points that we wish to simplify
     * @return Simplified set of points
     */
    public ArrayList<Vec2> simplifyCorners(ArrayList<Vec2> points)
    {
        ArrayList<Vec2> smoothedLine = new ArrayList<>();
        
        smoothedLine.add(points.get(0));
                
        for(int i = 0; i < points.size(); i++)
        {
            int alphaPix = 0;            
            for(int j = -1; j < 2; j++)
            {
                for(int k = -1; k < 2; k++)
                {
                    if(!pointHasAlpha((int)points.get(i).x + j, (int)points.get(i).y + k))
                    {
                        alphaPix++;
                    }
                }
            }

            if(alphaPix > 1)
            {
                smoothedLine.add(points.get(i));
            }
            else if(alphaPix == 1)
            {
                boolean corner = false;
                if(!pointHasAlpha((int)points.get(i).x - 1, (int)points.get(i).y - 1))
                {
                    corner = true;
                }
                if(!pointHasAlpha((int)points.get(i).x + 1, (int)points.get(i).y - 1))
                {
                    corner = true;
                }
                if(!pointHasAlpha((int)points.get(i).x - 1, (int)points.get(i).y + 1))
                {
                    corner = true;
                }
                if(!pointHasAlpha((int)points.get(i).x + 1, (int)points.get(i).y + 1))
                {
                    corner = true;
                }
                
                if(!corner)
                {
                    smoothedLine.add(points.get(i));
                }
            }
        }
        
        return smoothedLine;
    }
    
    /**
     * Experimental method to simplify shapes using a combination of algorithms
     * 
     * @param points Original set of points that we wish to simplify
     * @return Simplified set of points
     */
    public ArrayList<Vec2> simplifyBoth5(ArrayList<Vec2> points)
    {
        ArrayList<Vec2> smoothedLine = simplifyCorners(points);
        ArrayList<Vec2> simplifiedLine = new ArrayList<>();

        boolean curving = false;
        
        for(int i = 0; i < smoothedLine.size(); i++)
        {
            //set next and last points
            int next = i + 1;
            int next2 = next + 1;
            int last = i - 1;
            
            if(i + 1 == smoothedLine.size())
            {
                next = 0;
            }

            if(next2 == smoothedLine.size())
            {
                next2 = 0;
            }
            else if(next2 > smoothedLine.size())
            {
                next2 = 1;
            }
            
            if(i - 1 == -1)
            {
                last = smoothedLine.size() - 1;
            }
            
            //get alpha surroundings
            int alphaPix = 0;
            for(int j = -1; j < 2; j++)
            {
                for(int k = -1; k < 2; k++)
                {
                    if(!pointHasAlpha((int)smoothedLine.get(i).x + j, (int)smoothedLine.get(i).y + k))
                    {
                        alphaPix++;
                    }
                }
            }
            
            int alphaPix1 = 0;
            for(int j = -1; j < 2; j++)
            {
                for(int k = -1; k < 2; k++)
                {
                    if(!pointHasAlpha((int)smoothedLine.get(last).x + j, (int)smoothedLine.get(last).y + k))
                    {
                        alphaPix1++;
                    }
                }
            }
            int alphaPix2 = 0;
            for(int j = -1; j < 2; j++)
            {
                for(int k = -1; k < 2; k++)
                {
                    if(!pointHasAlpha((int)smoothedLine.get(next).x + j, (int)smoothedLine.get(next).y + k))
                    {
                        alphaPix2++;
                    }
                }
            }
            
            System.out.println("ALPHA " + alphaPix + " AT " + smoothedLine.get(i).x + " " + smoothedLine.get(i).y);
                        
            //check for straights and corners
             boolean alphaAbove = (!pointHasAlpha((int)smoothedLine.get(last).x, (int)smoothedLine.get(last).y) && (smoothedLine.get(i).x == smoothedLine.get(last).x && smoothedLine.get(i).y != smoothedLine.get(last).y));
            if(alphaPix == 1)
            {
                simplifiedLine.add(smoothedLine.get(i));
            }
            else if((alphaPix == 2 && alphaPix1 == 4 && alphaPix2 == 2) || (alphaPix == 2 && alphaPix1 == 2 && alphaPix2 == 3) || (alphaPix == 2 && alphaPix1 == 3 && alphaPix2 == 3 && !alphaAbove))
            {
                simplifiedLine.add(smoothedLine.get(i));
            }
            else if((alphaPix == 3 && alphaPix1 == 2 && alphaPix2 == 2) || (alphaPix == 3 && alphaPix1 == 1))
            {
                simplifiedLine.add(smoothedLine.get(i));
            }
            else if(alphaPix == 4)
            {
                simplifiedLine.add(smoothedLine.get(i));
            }
            else if(alphaPix == 5)
            {
                simplifiedLine.add(smoothedLine.get(i));
            }
            
            //set curves
            //instead of this "no points till a threshold is past", try just averaging 3 curved points repeatedly to get a smoother curve
            //OR try all points with alpha of 4 and any with 2 when the previous had 3
            if(curving)
            {
                if(alphaPix == 4 || (alphaPix == 3 && alphaPix1 == 1 && alphaPix2 == 1) || (alphaPix == 2 && alphaPix1 == 2) || (alphaPix != 3 && alphaPix1 == 1 && alphaPix2 == 1))
                {
                    simplifiedLine.add(smoothedLine.get(i));
                }
            }
        }
        
        return simplifiedLine;
    }

    /**
     * Use the Ramer-Douglas-Peucker algorithm to simplify lines
     * Loop through the vector points and mark them accordingly as points that should be kept or removed for the new simplified image
     * 
     * @param points Original list of points that we wish to simplify
     * @return Simplified list of points
     */
    public ArrayList<Vec2> simplifyDouglas(ArrayList<Vec2> points)
    {
        int size = points.size();
        
        if(GUI.limit <= 0 || size < 3)
        {
            return points;
        }
        boolean[] marked = new boolean[size];
        for(int i = 1; i < size; i++)
        {
            marked[i] = false;
        }
        //typically the algorithm would want the first and last points of a line to remain unchanged
        //but in this case, the last point is a point in a loop (that forms a complete object) that shouldn't be kept
        marked[0] = true;                                       

        douglasReduction(points, marked, GUI.limit, 0, size);
        
        ArrayList<Vec2> newPoints = new ArrayList<>(size);
        for(int i = 0; i < size; i++)
        {
            if(marked[i])
            {
                newPoints.add(points.get(i));
            }
        }
        return newPoints;
    }
    
    /**
     * Use the tolerance limiter to determine which points to keep or remove
     * Checks whether a point exceeds the distance limit between points.
     * If it does, it is too "sharp" to remove
     * 
     * @param points Points we are trying to simplify
     * @param marked Determines which points are marked for alteration and which are not
     * @param tolerance limit to point deviation which will limit extreme outliers from getting altered
     * @param first first point in the list we are checking
     * @param last last point in the list we are checking
     */
    private static void douglasReduction(ArrayList<Vec2> points, boolean[] marked, double tolerance, int first, int last)
    {
        //ensure the set of points are valid
        if (last <= first + 1)  
        {
            return;
        }

        double maxDistance = 0.0;
        int farthestPoint = 0;

        Vec2 firstPoint = points.get(first);
        Vec2 lastPoint = points.get(last - 1);

        for (int i = first + 1; i < last; i++)
        {
            Vec2 point = points.get(i);
            double distance = orthogonalDistance(point, firstPoint, lastPoint);
            if (distance > maxDistance)
            {
                maxDistance = distance;
                farthestPoint = i;
            }
        }

        //distance too great so keep this point, but calculate possible removals for similar points
        if (maxDistance > tolerance)        
        {
            marked[farthestPoint] = true;
            douglasReduction(points, marked, tolerance, first, farthestPoint);
            douglasReduction(points, marked, tolerance, farthestPoint, last);
        }
    }

    /**
     * Get the distance of a point on the line
     * 
     * @param point Point we are checking
     * @param start Start of the line
     * @param end End of the line
     * @return Distance of point along the line
     */
    public static double orthogonalDistance(Vec2 point, Vec2 start, Vec2 end)
    {
        double area = Math.abs((1.0 * start.y * end.x + 1.0 * end.y * point.x
        + 1.0 * point.y * start.x - 1.0 * end.y * start.x - 1.0 * point.y * end.x
        - 1.0 * start.y * point.x)/2.0);

        double bottom = Math.hypot(start.y - end.y, start.x - end.x);

        return (area/bottom * 2.0);
    }

    /**
     * Original simplification algorithm
     * 
     * @param points Original list of points that we wish to simplify
     * @return Simplified list of points
     */
    public ArrayList<Vec2> simplifyold (ArrayList<Vec2> points)
    {
        ArrayList<Vec2> smoothedLine = new ArrayList<> ();
        ArrayList<Vec2> simplifiedLine = new ArrayList<> ();

        // Add the first point
        smoothedLine.add(this.points.get(0));

        ArrayList<Vec2> averageVertices = new ArrayList<Vec2>();

        // Loop over the next [average] vertices and add the result to the array of smoothed points
        for(int i = 0; i < (this.points.size() - GUI.average); i++)
        {
            averageVertices.clear();
            for(int j = 0; j < GUI.average; j++)
            {
                averageVertices.add(this.points.get(i + j));
            }
            smoothedLine.add(average(averageVertices));
        }

        float curvatureTotal = 0;
        float curvature = 0;

        for(int i=0; i<smoothedLine.size()-3; i++)
        {
            // Calculate the curvature
            curvature = curvature(smoothedLine.get(i), smoothedLine.get(i+1), smoothedLine.get(i+2));

            // Use a curvature accumulator to prevent cases where a line curves gradually
            //this would be picked up if we just used the curvature because each individual curvature may be less than our limit
            curvatureTotal += curvature;

            // If the total curvature is greater than our set limit then add the point to our simplified line
            if(curvatureTotal > GUI.limit)
            {
                curvatureTotal = 0;
                simplifiedLine.add(smoothedLine.get(i));
            }
        }
        return simplifiedLine;
    }
    
    /**
     * Parses through the original list of points extracted from an image and repositions them to determine a pixel-perfect trace
     * 
     * @param points Original list of points found in the image
     * @return Pixel-perfect list of points in the image
     */
    public ArrayList<Vec2> perfectPoints(ArrayList<Vec2> points)
    {
        ArrayList<Vec2> returns = points;
        
        for(int i = 0; i < points.size(); i++)
        {
            boolean alphaTop = false;
            boolean alphaRight = false;
            boolean alphaBottomRight = false;
            boolean alphaTopRight = false;
            boolean alphaTopLeft = false;
            
            if(!pointHasAlpha((int)points.get(i).x + 0, (int)points.get(i).y + 1))
            {
                alphaTop = true;
            }
            if(!pointHasAlpha((int)points.get(i).x + 1, (int)points.get(i).y + 0))
            {
                alphaRight = true;
            }
            if(!pointHasAlpha((int)points.get(i).x + 1, (int)points.get(i).y - 1))
            {
                int alphaPix = 0;
                for(int j = -1; j < 2; j++)
                {
                    for(int k = -1; k < 2; k++)
                    {
                        if(!pointHasAlpha((int)points.get(i).x + j, (int)points.get(i).y + k))
                        {
                            alphaPix++;
                        }
                    }
                }
                if(alphaPix == 1)
                {
                    alphaBottomRight = true;
                }
            }
            if(!pointHasAlpha((int)points.get(i).x + 1, (int)points.get(i).y + 1))
            {
                int alphaPix = 0;
                for(int j = -1; j < 2; j++)
                {
                    for(int k = -1; k < 2; k++)
                    {
                        if(!pointHasAlpha((int)points.get(i).x + j, (int)points.get(i).y + k))
                        {
                            alphaPix++;
                        }
                    }
                }
                if(alphaPix == 1)
                {
                    alphaTopRight = true;
                }
            }
            if(!pointHasAlpha((int)points.get(i).x - 1, (int)points.get(i).y + 1))
            {
                int alphaPix = 0;
                for(int j = -1; j < 2; j++)
                {
                    for(int k = -1; k < 2; k++)
                    {
                        if(!pointHasAlpha((int)points.get(i).x + j, (int)points.get(i).y + k))
                        {
                            alphaPix++;
                        }
                    }
                }
                if(alphaPix == 1)
                {
                    alphaTopLeft = true;
                }
            }
            
            if(alphaTop == true && alphaRight == true)
            {
                GUI.txtOutput.setText("Repositioning point " + returns.get(i));
                returns.set(i, new Vec2(points.get(i).x + 1, points.get(i).y + 1));
            }
            else if(alphaBottomRight == true)
            {
                GUI.txtOutput.setText("Repositioning point " + returns.get(i));
                returns.set(i, new Vec2(points.get(i).x + 1, points.get(i).y));
            }
            else if(alphaTopRight == true)
            {
                GUI.txtOutput.setText("Repositioning point " + returns.get(i));
                returns.set(i, new Vec2(points.get(i).x + 1, points.get(i).y + 1));
            }
            else if(alphaTopLeft == true)
            {
                GUI.txtOutput.setText("Repositioning point " + returns.get(i));
                returns.set(i, new Vec2(points.get(i).x, points.get(i).y + 1));
            }
            else
            {
                GUI.txtOutput.setText("Repositioning point " + returns.get(i));
                if(alphaTop == true)
                {
                    returns.set(i, new Vec2(points.get(i).x, points.get(i).y + 1));
                }
                if(alphaRight == true)
                {
                    returns.set(i, new Vec2(points.get(i).x + 1, points.get(i).y));
                }
            }
        }
        return returns;
    }
    
    /**
     * Takes the original point list of any hole in the image and determines pixel-perfect traces for them
     * UNFINISHED METHOD. Doesn't work in all situations
     * 
     * @param points Original list of points taken from the image
     * @return Pixel-perfect list of points in the hole
     */
    public ArrayList<Vec2> perfectHoles(ArrayList<Vec2> points)
    {
        //NOTE: SOMETHING WRONG IN HERE, GLITCHES OUT THE BOWLING BALL IMAGE
        //Duplicates are being generated! They must be removed BEFORE simplifying!!
        ArrayList<Vec2> returns = points;
        
        for(int i = 0; i < points.size(); i++)
        {
            boolean alphaTop = false;
            boolean alphaRight = false;
            boolean alphaBottomRight = false;
            boolean alphaTopRight = false;
            boolean alphaTopLeft = false;
            
            if(!pointHasAlpha((int)points.get(i).x + 0, (int)points.get(i).y + 1))
            {
                alphaTop = true;
            }
            if(!pointHasAlpha((int)points.get(i).x + 1, (int)points.get(i).y + 0))
            {
                alphaRight = true;
            }
            if(!pointHasAlpha((int)points.get(i).x + 1, (int)points.get(i).y - 1))
            {
                int alphaPix = 0;
                for(int j = -1; j < 2; j++)
                {
                    for(int k = -1; k < 2; k++)
                    {
                        if(!pointHasAlpha((int)points.get(i).x + j, (int)points.get(i).y + k))
                        {
                            alphaPix++;
                        }
                    }
                }
                if(alphaPix == 1)
                {
                    alphaBottomRight = true;
                }
            }
            if(!pointHasAlpha((int)points.get(i).x + 1, (int)points.get(i).y + 1))
            {
                int alphaPix = 0;
                for(int j = -1; j < 2; j++)
                {
                    for(int k = -1; k < 2; k++)
                    {
                        if(!pointHasAlpha((int)points.get(i).x + j, (int)points.get(i).y + k))
                        {
                            alphaPix++;
                        }
                    }
                }
                if(alphaPix == 1)
                {
                    alphaTopRight = true;
                }
            }
            if(!pointHasAlpha((int)points.get(i).x - 1, (int)points.get(i).y + 1))
            {
                int alphaPix = 0;
                for(int j = -1; j < 2; j++)
                {
                    for(int k = -1; k < 2; k++)
                    {
                        if(!pointHasAlpha((int)points.get(i).x + j, (int)points.get(i).y + k))
                        {
                            alphaPix++;
                        }
                    }
                }
                if(alphaPix == 1)
                {
                    alphaTopLeft = true;
                }
            }
            
            if(alphaTop == true && alphaRight == true)
            {
                GUI.txtOutput.setText("Repositioning point " + returns.get(i));
                returns.set(i, new Vec2(points.get(i).x + 1, points.get(i).y + 1));
            }
            else if(alphaBottomRight == true)
            {
                GUI.txtOutput.setText("Repositioning point " + returns.get(i));
                returns.set(i, new Vec2(points.get(i).x + 1, points.get(i).y));
            }
            else if(alphaTopRight == true)
            {
                GUI.txtOutput.setText("Repositioning point " + returns.get(i));
                returns.set(i, new Vec2(points.get(i).x + 1, points.get(i).y + 1));
            }
            else if(alphaTopLeft == true)
            {
                GUI.txtOutput.setText("Repositioning point " + returns.get(i));
                returns.set(i, new Vec2(points.get(i).x, points.get(i).y + 1));
            }
            else
            {
                GUI.txtOutput.setText("Repositioning point " + returns.get(i));
                if(alphaTop == true)
                {
                    returns.set(i, new Vec2(points.get(i).x, points.get(i).y + 1));
                }
                if(alphaRight == true)
                {
                    returns.set(i, new Vec2(points.get(i).x + 1, points.get(i).y));
                }
            }
        }
        return returns;
    }
}