package com.base.simulation.worldobjects;

import com.base.engine.jsonobjects.GsonShape;
import com.base.engine.jsonobjects.GsonObject;
import com.base.engine.WorldObject;
import com.base.engine.imageparsing.ParseImage;
import com.base.engine.Sprite;
import com.base.engine.WorldScale;
import com.base.engine.polydecomposition.PolyDecomposition;
import com.base.engine.polydecomposition.polygon.TriangulatablePolygon;
import com.base.engine.polydecomposition.polygon.PolygonVec;
import com.base.engine.polydecomposition.triangulation.delaunay.DelaunayTriangle;
import com.base.simulation.GUI;
import com.base.simulation.Simulation;
import com.google.gson.Gson;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotated;
import static org.lwjgl.opengl.GL11.glTranslatef;

/**
 * User-created object generated from the parser and triangulation algorithms and converted into a Box2d body
 * 
 * @author Jordan
 */
public class VectorObject extends WorldObject
{
    Vec2 lowerBounds, upperBounds;
    List<DelaunayTriangle> triangles;
    //since only one object at be triangulated at a time, this result can be stored as a static string ready for retrieval when saving
    public static String json;                                        
    
    /**
     * Initialise the object and all its details
     * 
     * @param imgName  name of the image we will derive the object from
     */
    public VectorObject(String imgName)                              
    {
        BodyDef bodyDef = new BodyDef();
        //set position within the world in Box2d metres
        bodyDef.position.set(0/WorldScale.RATIO, 0/WorldScale.RATIO); 

        bodyDef.angle = (float)Math.toRadians(0);
        bodyDef.type = BodyType.DYNAMIC;
        
        PolygonShape box = new PolygonShape();
        
        //parser algorithms are called here
        ParseImage pi = new ParseImage(imgName);
        ArrayList<Vec2> vertices = pi.getPoints();
        vertices = pi.perfectPoints(vertices);
        GUI.txtOutput.setText("Points repositioned to pixel perfection");
        
        //simplify the points
        vertices = pi.simplifyMenu(vertices);
        //remove duplicates from the list of points so that the triangulation doesn't crash
        LinkedHashSet<Vec2> dupRemover = new LinkedHashSet<Vec2>(vertices);
        ArrayList<Vec2> newVerts = new ArrayList<Vec2>(dupRemover);
        GUI.txtOutput.setText("Duplicate points removed to avoid errors");
        
        ArrayList<PolygonVec> points = new ArrayList<PolygonVec>();
        triangles = null;
        for(Vec2 v : newVerts)
        {
            points.add(new PolygonVec(v.x, v.y));
        }
        if(points.size() < 3)
        {
            return;
        }
        //create a polygon object for triangulation
        TriangulatablePolygon poly = new TriangulatablePolygon(points);     
        GUI.txtOutput.setText("Overall polygon created");

        //similar to the above code, for handling holes in the images being parsed
        ArrayList<ArrayList<Vec2>> holes = pi.getHoles();
        if(holes.size() > 0)
        {
            for(int i = 0; i < holes.size(); i++)
            {
                System.out.println("i " + i);
                ArrayList<Vec2> holeVertices = holes.get(i);
                holeVertices = pi.perfectHoles(holeVertices);
                LinkedHashSet<Vec2> holeDupRemover33 = new LinkedHashSet<>(holeVertices);
                holeVertices = new ArrayList<>(holeDupRemover33);
                GUI.txtOutput.setText("Points repositioned to pixel perfection");
                
                holeVertices = pi.simplifyMenu(holeVertices);
                LinkedHashSet<Vec2> holeDupRemover = new LinkedHashSet<>(holeVertices);
                ArrayList<Vec2> newHoleVerts = new ArrayList<>(holeDupRemover);
                GUI.txtOutput.setText("Duplicate points removed to avoid errors");
                ArrayList<PolygonVec> holePoints = new ArrayList<>();
                for(Vec2 v : newHoleVerts)
                {
                    holePoints.add(new PolygonVec(v.x, v.y));
                }
                if(holePoints.size() < 3)
                {
                    break;
                }
                TriangulatablePolygon hole = new TriangulatablePolygon(holePoints);
                poly.addHole(hole);
                GUI.txtOutput.setText("Hole created in overall polygon");
            }
        }

        //Triangulate the polygon object
        PolyDecomposition.triangulate(poly);
        triangles = poly.getTriangles();

        //Take each triangle and add more triangles to it from its neighbours till the seemingly maximum possible polygon is formed (has up to 8 sides and is convex)
        if(triangles != null)
        {
            //array of polygons that will form the new object, same length as triangles
            TriangulatablePolygon[] polys = new TriangulatablePolygon[triangles.size()];    
            int polyIndex = 0;
            boolean[] marked = new boolean[triangles.size()];
            //prepare the array that will hold the marking state of each triangle (so that they are not processed indefinately)
            for(int i = 0; i < triangles.size(); i++)                                       
            {
                marked[i] = false;
            }
            
            //when this is false, the loop will be exited and the polygonization will be done
            boolean notDone = true;                                                         
            while(notDone)
            {
                int currentTri = -1;
                for(int i = 0; i < triangles.size(); i++)
                {
                    //skip any triangle that has been marked as processed
                    if(marked[i])                                                           
                    {
                        continue;
                    }
                    currentTri = i;
                    break;
                }
                //for loop is done, break out of the while loop
                if(currentTri == -1)                                                        
                {
                    notDone = false;
                }
                else
                {
                    ArrayList<PolygonVec> vectors = new ArrayList<>();
                    //for each vector of each current triangle, add to the arraylist of vectors for a potential new polygon containing this triangle
                    for(int i = 0; i < triangles.get(currentTri).vectors.length; i++)       
                    {
                        vectors.add(new PolygonVec(triangles.get(currentTri).vectors[i].getXf(), triangles.get(currentTri).vectors[i].getYf()));
                    }
                    //create a polygon out of the triangle in memory
                    TriangulatablePolygon currentPoly = new TriangulatablePolygon(vectors); 
                    //mark this triangle as processed, it will no longer exist
                    marked[currentTri] = true;                                              
                    
                    //for each triangle, add another triangle's vectors to it, and check if this new polygon is valid
                    for(int i = 0; i < triangles.size(); i++)                               
                    {
                        if(marked[i])
                        {
                            //this triangle has been marked, so skip it
                            continue;                                                       
                        }

                        //add another triangle to the polygon in memory
                        TriangulatablePolygon newPoly = currentPoly.add(triangles.get(i)); 
                        if(newPoly == null)
                        {
                            //this new polygon is null (for whatever reason) so skip it
                            continue;                                                       
                        }
                        //if polygon is convex, replace original and mark as true
                        if(newPoly.isConvex())                                              
                        {
                            currentPoly = newPoly;
                            marked[i] = true;
                        }
                    }
                    //add polygon to array of polygons at polyIndex
                    polys[polyIndex] = currentPoly;                                         
                    polyIndex++;                                                           
                    GUI.txtOutput.setText("Polygonized some triangles together!");
                }
            }
            //array of polygons of size polyIndex
            TriangulatablePolygon[] newPolys = new TriangulatablePolygon[polyIndex];        
            //Copy the first polygon array to this new array from 0 to polyIndex, cutting out the nulls
            System.arraycopy(polys, 0, newPolys, 0, polyIndex);                             

            //Turn the finished product into a JSON string with the GSON library
            Gson gson = new Gson();                                             
            Vec2[] gsonVertices = null;
            GsonShape[] shapes = new GsonShape[newPolys.length];
            for(int i = 0; i < newPolys.length; i++)
            {
                gsonVertices = new Vec2[newPolys[i].getVectors().size()];
                for(int j = 0; j < newPolys[i].getVectors().size(); j++)
                {
                    float gsonX = newPolys[i].getVectors().get(j).getXf();
                    float gsonY = newPolys[i].getVectors().get(j).getYf();
                    //used when the image-doubling technique is used to allow one-pixel points on the image
                    if(GUI.doubleBuffer)                                        
                    {
                        gsonVertices[j] = new Vec2((gsonX/2), (gsonY/2));
                    }
                    else
                    {
                        gsonVertices[j] = new Vec2(gsonX, gsonY);
                    }
                }
                //create a new GsonShape object to store all the vertices as a JSON array
                GsonShape shape = new GsonShape(BodyType.STATIC, gsonVertices); 
                //add this GsonShape object to the array which will be part of the final GsonObject object
                shapes[i] = shape;                                              
            }
            GsonObject gs = new GsonObject(imgName, imgName, new Vec2(0, 0), shapes);
            //convert the final GsonObject object to a JSON string
            json = gson.toJson(gs);                                                
            System.out.println(json);
            
            
            //TURN INTO BODIES HERE
            //create the defined body
            body = Simulation.getWorld().createBody(bodyDef);   
            for(TriangulatablePolygon p: newPolys)
            {
                Vec2[] bodyVertices = new Vec2[p.vectors.size()];
                for(int i = 0; i < p.getVectors().size(); i++)
                {
                    float bodyX = p.getVectors().get(i).getXf();
                    float bodyY = p.getVectors().get(i).getYf();
                    if(GUI.doubleBuffer)
                    {
                        //used when the image-doubling technique is used to allow one-pixel points on the image
                        bodyVertices[i] = new Vec2((bodyX/2)/WorldScale.RATIO, (bodyY/2)/WorldScale.RATIO);
                    }
                    else
                    {
                        bodyVertices[i] = new Vec2(bodyX/WorldScale.RATIO, bodyY/WorldScale.RATIO);
                    }
                    GUI.txtOutput.setText("Preparing point (" + bodyX + " " + bodyY + ") for display on-screen");
                }
                box.set(bodyVertices, bodyVertices.length);
                FixtureDef fixtureDef = new FixtureDef();
                fixtureDef.density = 1;
                fixtureDef.friction = 0.3f;
                fixtureDef.shape = box;
                body.createFixture(fixtureDef);
            }
            GUI.txtOutput.setText("Body fixtures generated!");

            //fetch the image from file so that it can be used as a texture for rendering
            BufferedImage img = null;
            try
            {
                img = Simulation.getTextureLoader().loadImage(imgName);
            }
            catch (IOException ex)
            {
            }
            Vec2 lower = new Vec2(0, 0);
            Vec2 upper = new Vec2(img.getWidth()-1, img.getHeight()-1);

            this.sizeX = img.getWidth();
            this.sizeY = img.getHeight();
            this.spr = new Sprite(1.0f, 1.0f, 1.0f, sizeX, sizeY, imgName);
        }
    }
    
    /**
     * Personal render method since the objects made from fixtures have different origins and thus need to call a slightly modified render method in the Sprite class
     */
    @Override
    public void render()
    {
        glPushMatrix();                                                         
        {
            if(body != null)
            {
                Vec2 bodyPosition = body.getPosition().mul(WorldScale.RATIO);
                glTranslatef(bodyPosition.x, bodyPosition.y, 0);
                glRotated(Math.toDegrees(body.getAngle()), 0, 0, 1);

                if(GUI.showSprite)
                {
                    spr.render();
                }
                else
                {
                    spr.render(body);
                }
            }
        }
        glPopMatrix();
    }
}
