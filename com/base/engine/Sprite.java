package com.base.engine;

import com.base.engine.polydecomposition.triangulation.delaunay.DelaunayTriangle;
import com.base.simulation.GUI;
import com.base.simulation.Simulation;
import java.io.IOException;
import java.util.List;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import static org.lwjgl.opengl.GL11.*;

/**
 * Stores data related to any sprite that is rendered in OpenGL
 * 
 * @author Jordan
 */
public class Sprite
{
    private float red, green, blue, sizeX, sizeY; 
    private Texture texture;
    
    /**
     * Set up the sprite data related to this object
     * 
     * @param red Intensity of the red in the RGB colouring
     * @param green Intensity of the green in the RGB colouring
     * @param blue Intensity of the blue in the RGB colouring
     * @param sizeX Width of the sprite
     * @param sizeY Height of the sprite
     * @param reference Filepath which the texture is loaded from
     */
    public Sprite(float red, float green, float blue, float sizeX, float sizeY, String reference)
    {
        this.red = red;                                                         
        this.green = green;
        this.blue = blue;
        this.sizeX = sizeX;                                                     
        this.sizeY = sizeY;
        if(!reference.equals(""))                                               
        {
            try  
            {
                texture = Simulation.getTextureLoader().getTexture(reference);
                sizeX = texture.getWidth();                                
                sizeY = texture.getHeight();                               
            }
            catch(IOException ex)
            {
                ex.printStackTrace();
                System.exit(-1);                                                
            }
        }
        else              
        {
            texture = null;                                                     
        }        
    }
    
    /**
     * Set up the sprite data related to this object, guaranteed to be without a texture
     * 
     * @param red Intensity of the red in the RGB colouring
     * @param green Intensity of the green in the RGB colouring
     * @param blue Intensity of the blue in the RGB colouring
     * @param sizeX Width of the sprite
     * @param sizeY Height of the sprite
     */
    public Sprite(float red, float green, float blue, float sizeX, float sizeY)
    {
        this.red = red;                                                         
        this.green = green;
        this.blue = blue;
        this.sizeX = sizeX;                                                     
        this.sizeY = sizeY;
        texture = null;
    }
    
    /**
     * Draw the sprite to the OpenGL world. Default render method
     */
    public void render()                                                        
    {
        glColor3f(red, green, blue);             
        
        //if the object has an associated texture, we will need to draw that
        if(texture != null)                                                                                      
        {
            glEnable(GL_TEXTURE_2D);                            
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);  
            texture.bind();                                    
            glBegin(GL_QUADS);                                  
            {
                glTexCoord2f(0, texture.getNormalizedHeight());                           
                glVertex2f(0, 0);                                               
                glTexCoord2f(texture.getNormalizedWidth(), texture.getNormalizedHeight());
                glVertex2f(sizeX, 0);
                glTexCoord2f(texture.getNormalizedWidth(), 0);
                glVertex2f(sizeX, sizeY);
                glTexCoord2f(0, 0);
                glVertex2f(0, sizeY);                                         
            }
            glEnd();                                                            
            glDisable(GL_TEXTURE_2D);                           
        }
        else                                                                    
        {
            glBegin(GL_QUADS);                                                  
            {
                glVertex2f(-sizeX/2, -sizeY/2);                                              
                glVertex2f(-sizeX/2, sizeY/2);
                glVertex2f(sizeX/2, sizeY/2);
                glVertex2f(sizeX/2, -sizeY/2);
            }
            glEnd(); 
        }   
    }
    
    /**
     * Get width of the sprite
     * 
     * @return Width of the sprite
     */
    public float getSizeX()
    {
        return sizeX;
    }
    
    /**
     * Get height of the sprite
     * 
     * @return Height of the sprite
     */
    public float getSizeY()
    {
        return sizeY;
    }
    
    /**
     * Set width of the sprite
     * 
     * @param sizeX New width of the sprite 
     */
    public void setSizeX(float sizeX)
    {
        this.sizeX = sizeX;
    }

    /**
     * Set height of the sprite
     * 
     * @param sizeY New height of the sprite 
     */
    public void setSizeY(float sizeY)
    {
        this.sizeY = sizeY;
    }
    
    /**
     * Render method for when textures are disabled by the user. Draws each fixture within the Box2d body
     *  
     * 
     * @param body Box2D body of the object we are trying to draw
     */
    public void render(Body body)                                                        
    {
        Fixture f = body.getFixtureList();  
        
        //loop through all the Box2D fixtures of the body
        while(f != null)                                        
        {
            glColor3f(0, 1, 0);                                
            glLineWidth(1);                                    
            PolygonShape poly = (PolygonShape)f.getShape();    
            Vec2[] v = poly.getVertices();
            glBegin(GL_LINE_LOOP);                              
            {
                for(int i = 0; i < poly.getVertexCount(); i++)
                {
                    glVertex2f(v[i].x*WorldScale.RATIO, v[i].y*WorldScale.RATIO);
                }
            }
            glEnd();

            //Draw a small blue square around each vertice. An unecessary little feature, but I like it.
            if(GUI.showPoints)
            {
                for(int i = 0; i < poly.getVertexCount(); i++)
                {
                    glColor3f(0.2f, 0.2f, 1);                      
                    glLineWidth(2);                                 
                    glBegin(GL_LINE_LOOP);                            
                    {                                              
                        //scale each line to camera zoom, so no matter the zoom, the square sizes stay the same
                        glVertex2f(v[i].x * WorldScale.RATIO - (2/CameraController.controller.translationZ), v[i].y * WorldScale.RATIO - (2/CameraController.controller.translationZ));
                        glVertex2f(v[i].x * WorldScale.RATIO + (2/CameraController.controller.translationZ), v[i].y * WorldScale.RATIO - (2/CameraController.controller.translationZ));
                        glVertex2f(v[i].x * WorldScale.RATIO + (2/CameraController.controller.translationZ), v[i].y * WorldScale.RATIO + (2/CameraController.controller.translationZ));
                        glVertex2f(v[i].x * WorldScale.RATIO - (2/CameraController.controller.translationZ), v[i].y * WorldScale.RATIO + (2/CameraController.controller.translationZ));
                    }
                    glEnd();
                }
            }
            f = f.getNext();
        }
        
        //reset line thickness
        glLineWidth(1);                                         
    }
    
    //secondary render method for objects constructed out of many fixtures.
    //NOTE: INCOMPLETE
    public void renderComplex(Vec2 upperBound, Vec2 lowerBound, List<DelaunayTriangle> triangles)                                                        
    {
        glColor3f(red, green, blue);                                            
        int index = 0;
        Vec2 boundingBoxSize = new Vec2(upperBound.x - lowerBound.x, upperBound.y - lowerBound.y);
        
        if(texture != null)                                                     
        {
            glEnable(GL_TEXTURE_2D);
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            texture.bind();                                                    
            glBegin(GL_TRIANGLES);                                                  
            {
                for(DelaunayTriangle t : triangles)
                {
                    //NOTE: Try calculating this out on paper in the case of bbox.png
                    //If calculations are correct and algorithm still fails, maybe
                    //the texture class is storing image sizes wrong or textureloader
                    //is initializing them incorrectly
                    Vec2 p1 = new Vec2(t.vectors[0].getXf(), t.vectors[0].getYf());
                    Vec2 p2 = new Vec2(t.vectors[1].getXf(), t.vectors[1].getYf());
                    Vec2 p3 = new Vec2(t.vectors[2].getXf(), t.vectors[2].getYf());
                    Vec2 relativeP1 = new Vec2(p1.x - lowerBound.x, p1.y - lowerBound.y);
                    Vec2 relativeP2 = new Vec2(p2.x - lowerBound.x, p2.y - lowerBound.y);
                    Vec2 relativeP3 = new Vec2(p3.x - lowerBound.x, p3.y - lowerBound.y);
                    //Vec2 relativeP1 = new Vec2(upperBound.x/p1.x, upperBound.y/p1.y);
                    //Vec2 relativeP2 = new Vec2(upperBound.x/p2.x, upperBound.y/p2.y);
                    //Vec2 relativeP3 = new Vec2(upperBound.x/p3.x, upperBound.y/p3.y);

                    //Vec2 textP1 = new Vec2(1/relativeP1.x, 1/relativeP1.y);
                    //Vec2 textP2 = new Vec2(1/relativeP2.x, 1/relativeP2.y);
                    //Vec2 textP3 = new Vec2(1/relativeP3.x, 1/relativeP3.y);
                    Vec2 boundarySize = new Vec2(upperBound.x - lowerBound.x, upperBound.y - lowerBound.y);
                    Vec2 textP1 = new Vec2(relativeP1.x/boundarySize.x, relativeP1.y/boundarySize.y);
                    Vec2 textP2 = new Vec2(relativeP2.x/boundarySize.x, relativeP2.y/boundarySize.y);
                    Vec2 textP3 = new Vec2(relativeP3.x/boundarySize.x, relativeP3.y/boundarySize.y);

                    /*glTexCoord2f(textP1.x, textP1.y);                           
                    glVertex2f(p1.x, p1.y);                                               
                    glTexCoord2f(textP2.x, textP2.y);                           
                    glVertex2f(p2.x, p2.y);  
                    glTexCoord2f(textP3.x, textP3.y);                           
                    glVertex2f(p3.x, p3.y);*/
                    
                    /*                                            
                    glTexCoord2f(-sizeX, -sizeY);
                    glVertex2f(0, sizeY);
                    glTexCoord2f(texture.getWidth() + -sizeX, -sizeY);
                    glVertex2f(sizeX, sizeY);
                    glTexCoord2f(texture.getWidth() + -sizeX, texture.getHeight() + -sizeY);
                    glVertex2f(sizeX, 0);*/

                    glTexCoord2f(p1.x/upperBound.x, p1.y/upperBound.y);
                    glVertex2f(p1.x, p1.y);
                    glTexCoord2f(p2.x/upperBound.x, p2.y/upperBound.y);
                    glVertex2f(p2.x, p2.y);
                    glTexCoord2f(p3.x/upperBound.x, p3.y/upperBound.y);
                    glVertex2f(p3.x, p3.y);
                    System.out.println(p1.x + " " + p1.y + " " + upperBound.x + " " + upperBound.y);
                }
                System.out.println(sizeY + " " + sizeX + " " + texture.getWidth());
            }
            glEnd();         
            glDisable(GL_TEXTURE_2D); 
        }
    }
}
