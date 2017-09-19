package com.base.engine;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Hashtable;
import javax.swing.ImageIcon;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;

/**
 * Class used for loading textures into the application
 * 
 * @author Jordan
 */
public class TextureLoader
{
    public static TextureLoader textureLoader;
    private HashMap<String, Texture> table = new HashMap<String, Texture>();
    private ColorModel glAlphaColorModel;
    private ColorModel glColorModel;
    private IntBuffer textureIDBuffer = BufferUtils.createIntBuffer(1);
    
    /**
     * Initialise the textureloader class with transparency enabled
     */
    public TextureLoader()
    {
        glAlphaColorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] {8, 8, 8, 8}, true, false, ComponentColorModel.TRANSLUCENT, DataBuffer.TYPE_BYTE);
        glColorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] {8, 8, 8, 0}, true, false, ComponentColorModel.OPAQUE, DataBuffer.TYPE_BYTE);
    }
    
    /**
     * Create an ID for a texture in OpenGL
     * 
     * @return ID of the texture loaded
     */
    private int createTextureID()
    {
        glGenTextures(textureIDBuffer);
        return textureIDBuffer.get(0);
    }
    
    /**
     * Get a texture from a resource
     * 
     * @param resourceName name of the texture we wish to fetch
     * @return Texture we are returning
     * @throws IOException
     */
    public Texture getTexture(String resourceName) throws IOException
    {
        Texture texture = table.get(resourceName);
        if(texture != null)
        {
            return texture;
        }
        
        //GL_NEAREST provides a crisp to-the-pixel look (that might be better done with mipmapping)
        //GL_LINEAR provides a slightly blurred take, meaning things don't look blocky when zoomed in
        texture = getTexture(resourceName, GL_TEXTURE_2D, GL_RGBA, GL_NEAREST, GL_NEAREST);
        table.put(resourceName, texture);
        return texture;
    }
    
   /**
    * Get a texture from a resource, a more thorough texture setup
    * 
    * @param resourceName name of the texture we wish to fetch
    * @param target ID of the target object we will bind the texture to
    * @param dstPixelFormat format of the pixels in the texture (OpenGL variable)
    * @param minFilter render minimum filter applied to the texture
    * @param magFilter render magnitude filter applied to the texture
    * @return Texture we are returning
    * @throws IOException 
    */
    public Texture getTexture(String resourceName, int target, int dstPixelFormat, int minFilter, int magFilter) throws IOException
    {
        int srcPixelFormat;
        int textureID = createTextureID();
        Texture texture = new Texture(target, textureID);
        
        glBindTexture(target, textureID);
        
        BufferedImage bufferedImage = loadImage(resourceName);
        texture.setWidth(bufferedImage.getWidth());
        texture.setHeight(bufferedImage.getHeight());
        texture.setNormalizedWidth(powerOfTwo(bufferedImage.getWidth()));
        texture.setNormalizedHeight(powerOfTwo(bufferedImage.getHeight()));
        
        if(bufferedImage.getColorModel().hasAlpha())
        {
            srcPixelFormat = GL_RGBA;
        }
        else
        {
            srcPixelFormat = GL_RGB;
        }
        
        ByteBuffer textureBuffer = convertImageData(bufferedImage, texture);

        if(target == GL_TEXTURE_2D)
        {
            glTexParameteri(target, GL_TEXTURE_MIN_FILTER, minFilter);
            glTexParameteri(target, GL_TEXTURE_MAG_FILTER, magFilter);
        }
        
        glTexImage2D(target, 0, dstPixelFormat, get2Fold(bufferedImage.getWidth()), get2Fold(bufferedImage.getHeight()), 0, srcPixelFormat, GL_UNSIGNED_BYTE, textureBuffer);
        return texture;
    }

    /**
     * Bitwise operation to convert an NPOT texture to a POT texture
     * 
     * @param value Value we wish to change
     * @return 
     */
    private int powerOfTwo(int value)
    {
        if(value != 0)
        {
            value--;
            value |= (value >> 1);
            value |= (value >> 2);
            value |= (value >> 4);
            value |= (value >> 8);
            value |= (value >> 16);
            value++;
        }
        return value;
    }
    
    /**
     * Operation to 2fold a value
     * 
     * @param fold value we wish to 2fold
     * @return 2folded value
     */
    private static int get2Fold(int fold)
    {
        int ret = 2;
        while(ret < fold)
        {
            ret *= 2;
        }
        return ret;
    }
    
    /**
     * Convert the image resource into a bytebuffer for rasterization
     * 
     * @param bufferedImage Image we wish to convert
     * @param texture Texture the image is related to
     * @return Bytebuffer of the source image
     */
    private ByteBuffer convertImageData(BufferedImage bufferedImage, Texture texture)
    {
        ByteBuffer imageBuffer;
        WritableRaster raster;
        BufferedImage textureImage;
        
        int textureWidth = 2;
        int textureHeight = 2;
        
        while(textureWidth < bufferedImage.getWidth())
        {
            textureWidth *= 2;
        }
        while(textureHeight < bufferedImage.getHeight())
        {
            textureHeight *= 2;
        }
        
        if(bufferedImage.getColorModel().hasAlpha())
        {
            raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, textureWidth, textureHeight, 4, null);
            textureImage = new BufferedImage(glAlphaColorModel, raster, false, new Hashtable());
        }
        else
        {
            raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, textureWidth, textureHeight, 3, null);
            textureImage = new BufferedImage(glColorModel, raster, false, new Hashtable());
        }
        
        Graphics g = textureImage.getGraphics();
        g.setColor(new Color(0f, 0f, 0f, 0f));
        g.fillRect(0, 0, textureWidth, textureHeight);
        g.drawImage(bufferedImage, 0, 0, null);
        
        byte[] data = ((DataBufferByte) textureImage.getRaster().getDataBuffer()).getData();
        
        imageBuffer = ByteBuffer.allocateDirect(data.length);
        imageBuffer.order(ByteOrder.nativeOrder());
        imageBuffer.put(data, 0, data.length);
        imageBuffer.flip();
        
        return imageBuffer;
    }
    
    /**
     * Load the image from file
     * Publicly accessible for situations that need to load the image directly and not the texture
     * 
     * @param reference Filepath of the image we are loading
     * @return Image we are loading, if found
     * @throws IOException 
     */
    public BufferedImage loadImage(String reference) throws IOException
    {
        File file = new File(reference);

        if(file == null)
        {
            throw new IOException("Cannot find: " + reference);
        }
        
        Image image = new ImageIcon(file.getAbsolutePath()).getImage();
        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics g = bufferedImage.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        
        return bufferedImage;
    }
}
