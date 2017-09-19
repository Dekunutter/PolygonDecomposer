package com.base.engine;

import static com.base.engine.Main.framesPassed;
import static com.base.engine.Main.initSimulation;
import com.base.simulation.GUI;
import com.base.simulation.Simulation;
import com.base.simulation.Time;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import static org.lwjgl.opengl.GL11.*;

/**
 * Main class, handles initialisation
 * 
 * @author Jordan
 */
public class Main
{      
    /**
     * States that the application's OpenGL simulation can be in
     */
    private static enum State
    {
        SIMULATE;
    }
    private static Main.State state = Main.State.SIMULATE;
    public static int framesPassed = 30;
    private static JFrame frame;
    private static JPanel glPanel, optionPanel;
    private static Canvas canvas;
    
    public static void main(String[] args)
    {
        initSimulation();
        
        initFrame();                
        
        initDisplay();             
        initGL();                   
        initTextureLoader();         
        
        simulationLoop();    //loops infinitely till killed        
        
        cleanUp();
    }
    
    /**
     * Initialize the simulation as a single static instance
     */
    public static void initSimulation()                                              
    {
        Simulation.simulation = new Simulation();
    }
    
    /**
     * Handle all user input within the simulation in here, determined by simulaton state
     */
    private static void getInput()                                             
    {
        switch(state)
        {
            case SIMULATE:
            {
                Simulation.simulation.getInput();
                break;
            }
        }
    }
    
    /**
     * Handle all the simulation logic in here, determined by simulation state
     */
    private static void update()                                               
    {
        switch(state)
        {
            case SIMULATE:
            {
                Simulation.simulation.update();
                break;
            }
        }
    }
    
    /**
     * Handle all the simulation rendering in here, determined by simulation state
     */
    private static void render()                                               
    {
        glClear(GL_COLOR_BUFFER_BIT);                                    
        glLoadIdentity();                                             
        
        switch(state)
        {
            case SIMULATE:
                Simulation.simulation.render();
                break;
        }
                
        Display.update();                   
        Display.sync(60); 
    }
    
    /**
     * One-time initialization of OpenGL for rendering
     */
    private static void initGL()                                                
    {
        glMatrixMode(GL_PROJECTION);                                    
        glLoadIdentity();                                                       
        glOrtho(0, Display.getWidth(), 0, Display.getHeight(), -1, 1);          
        
        glMatrixMode(GL_MODELVIEW);                                             
        
        glDisable(GL_DEPTH_TEST);                                               
        
        glClearColor(0.2f, 0.2f, 0.2f, 0);   //dark grey                                    
    }
    
    /**
     * Initialise the textureloader class for the use of textures later in rendering
     */
    private static void initTextureLoader()
    {
        TextureLoader.textureLoader = new TextureLoader();
    }
    
    /**
     * The main logic loop in which the simulation will run
     */
    private static void simulationLoop()                                 
    {
        Time.init();
        
        int frames = 0;                                                         
        long lastTime = System.nanoTime();                                      
        long totalTime = 0;                                                     
        
        while(!Display.isCloseRequested())                             
        {   
            long now = System.nanoTime();                                       
            long passed = now - lastTime;                                       
            lastTime = now;                                                     
            totalTime += passed;                                                
            
            //determine frame rate after a certain amount of time passes
            if(totalTime >= 1000000000)                                       
            {
                framesPassed = frames;
                System.out.println("FPS: " + frames);
                totalTime = 0;                                                  
                frames = 0;                                                     
            }
            Time.update();              
            getInput();                
            update();              
            render();                 
        
            frames++;
        }
    }
    
    /**
     * Handle the freeing of resources when the program has been closed
     */
    private static void cleanUp()                                               
    {
        Display.destroy();                                                      
        Keyboard.destroy();                                                    
    }
    
    /**
     * One-time initialisation of the Java Swing and AWT components of the program
     */
    private static void initFrame()
    {
        frame = new JFrame("POLYGON DECOMPOSITION");
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
                
        //jpanel with a canvas for displaying OpenGL renderings within a Java frame
        glPanel = new JPanel();                             
        canvas = new Canvas();
        canvas.setSize(200, 200);
        
        //jpanel for the Swing and AWT gui
        optionPanel = new JPanel();                         
        optionPanel.setPreferredSize(new Dimension(400, 100));
        GUI gui = new GUI(optionPanel);
        
        glPanel.add(canvas);
        frame.add(glPanel, BorderLayout.CENTER);
        frame.add(optionPanel, BorderLayout.EAST);
        frame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                System.exit(0);
            }
        });
        frame.setVisible(true);
        frame.pack();
    }
    
    /**
     * One-time initialization of the OpenGL display window
     */
    private static void initDisplay()                                           
    {
        try
        {
            Display.setParent(canvas);              //place the OpenGL/LWJGL display into the Java AWT canvas
            Display.create();                                
            //Display.setVSyncEnabled(true);        //synchronize the frame rate to the monitor's refresh rate
            Keyboard.create();                      
        }
        catch (LWJGLException ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * Change the state of the simulation
     * (not currently doing anything, but would allow for more states and simulations to exist within the OpenGL program)
     */
    public static void setState(String strState)
    {
        if("Game".equals(strState))
        {
            state = Main.State.SIMULATE;
        }
    }
}