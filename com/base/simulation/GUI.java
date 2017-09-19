package com.base.simulation;

import com.base.simulation.worldobjects.VectorObject;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.filechooser.*;

/**
 * Java Swing and AWT GUI
 * 
 * @author Jordan
 */
public class GUI implements ActionListener, ItemListener
{    
    public static int RAMER_SIMPLIFIER = 0;
    public static int CIRCLE_SIMPLIFIER = 1;
    public static int FLEXIBLE_SIMPLIFIER = 2;
    public static int ORIG_SIMPLIFIER = 3;
    public static int NO_SIMPLIFIER = 4;
            
    public static boolean showPoints, checkHoles, doubleBuffer, showSprite;
    public static int selectedSimplifier, average;
    public static float limit;
    
    private String filePath;
    private JPanel addPanel, cmbPanel, paraPanel, btnPanel, outPanel, renderPanel;
    private JButton btnAdd, btnTest, btnSave, btnRender;
    private JTextField txtAdd, txtLimit, txtAverage;
    public static JTextField txtOutput;
    private JLabel lblSimplify, lblLimit, lblAverage, lblSpace, lblHoleSpace, lblBufferSpace;
    private JComboBox cmbSimplify;
    private JCheckBox chkHoles, chkBuffer, chkPoints;
    
    /**
     * Initialise the GUI
     * 
     * @param panel Panel to contain the GUI
     */
    public GUI(JPanel panel)
    {
        showPoints = false;
        checkHoles = true;
        doubleBuffer = false;
        showSprite = true;
        selectedSimplifier = 0;
        average = 2;
        limit = 0.2f;
        
        filePath = "";
        
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        //setup add image panel
        addPanel = new JPanel();
        addPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
        
        txtAdd = new JTextField();
        txtAdd.setPreferredSize(new Dimension(280, 20));
        txtAdd.setText(filePath);
        
        btnAdd = new JButton("Browse...");
        btnAdd.addActionListener(this);
        
        //setup a combobox for selecting the simplify algorithm
        cmbPanel = new JPanel();
        cmbPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
                
        chkHoles = new JCheckBox("Check holes");
        chkHoles.setHorizontalTextPosition(SwingConstants.LEFT);
        chkHoles.setSelected(true);
        chkHoles.addActionListener(this);
        
        lblHoleSpace = new JLabel("   ");
        
        lblSimplify = new JLabel("Simplifier: ");

        cmbSimplify = new JComboBox();
        cmbSimplify.addItem("Ramer-Douglas-Peucker");
        cmbSimplify.addItem("Circle Algorithm");
        cmbSimplify.addItem("Flexible Algorithm");
        cmbSimplify.addItem("Original Algorithm");
        cmbSimplify.addItem("None");
        cmbSimplify.setMaximumSize(new Dimension(200, 100));
        cmbSimplify.addItemListener(this);
        
        //setup the textfields for specifying the simplifier limiter and average
        paraPanel = new JPanel();
        paraPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
        
        chkBuffer = new JCheckBox("Double Buffer");
        chkBuffer.setHorizontalTextPosition(SwingConstants.LEFT);
        chkBuffer.addActionListener(this);
        
        lblBufferSpace = new JLabel("           ");
        
        lblLimit = new JLabel("Limit:");
        txtLimit = new JTextField();
        txtLimit.setPreferredSize(new Dimension(50, 20));
        txtLimit.setText("0.2");
        
        //setup an empty jlabel as a gap between two components
        lblSpace = new JLabel("   ");                               
        
        //setup the textfield for specifying the simplifier average
        lblAverage = new JLabel("Average:");
        txtAverage = new JTextField();
        txtAverage.setPreferredSize(new Dimension(50, 20));
        txtAverage.setText("2");
        txtAverage.setEditable(false);
        txtAverage.setEnabled(false);
        
        //setup testing and saving buttons
        btnPanel = new JPanel();
        btnPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
        
        btnTest = new JButton("Test");
        btnTest.addActionListener(this);
        
        btnSave = new JButton("Save");
        btnSave.addActionListener(this);
        
        //setup output notes
        outPanel = new JPanel();
        outPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
        txtOutput = new JTextField();
        txtOutput.setEditable(false);
        txtOutput.setPreferredSize(new Dimension(380, 20));

        //setup button for hiding textures in the OpenGL world
        renderPanel = new JPanel();
        renderPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
                
        chkPoints = new JCheckBox("Highlight Vertices");
        chkPoints.setHorizontalTextPosition(SwingConstants.LEFT);
        chkPoints.setSelected(false);
        chkPoints.addActionListener(this);
        
        btnRender = new JButton("Toggle Renderer");
        btnRender.addActionListener(this);
        
        //add all the components to their respective jpanels
        addPanel.add(txtAdd);
        addPanel.add(btnAdd);
        panel.add(addPanel);
        
        cmbPanel.add(chkHoles);
        cmbPanel.add(lblHoleSpace);
        cmbPanel.add(lblSimplify);
        cmbPanel.add(cmbSimplify);
        panel.add(cmbPanel);
        
        paraPanel.add(chkBuffer);
        paraPanel.add(lblBufferSpace);
        paraPanel.add(lblLimit);
        paraPanel.add(txtLimit);
        paraPanel.add(lblSpace);
        paraPanel.add(lblAverage);
        paraPanel.add(txtAverage);
     
        panel.add(paraPanel);
        
        btnPanel.add(btnTest);
        btnPanel.add(btnSave);
        panel.add(btnPanel);
        
        outPanel.add(txtOutput);
        panel.add(outPanel);
        
        renderPanel.add(chkPoints);
        renderPanel.add(btnRender);
        panel.add(renderPanel);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if(e.getSource() == btnAdd)
        {
            final JFileChooser uploader = new JFileChooser();
            FileFilter imageFilter = new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes());
            uploader.addChoosableFileFilter(imageFilter);
            int returnValue = uploader.showOpenDialog(uploader);

            if(returnValue == JFileChooser.APPROVE_OPTION)
            {
                File file = uploader.getSelectedFile();     

                filePath = file.getAbsolutePath();
                txtAdd.setText(filePath);              
            }
        }
        else if(e.getSource() == chkHoles)
        {
            if(chkHoles.isSelected())
            {
                checkHoles = true;
            }
            else
            {
                checkHoles = false;
            }
        }
        else if(e.getSource() == chkBuffer)
        {
            if(chkBuffer.isSelected())
            {
                doubleBuffer = true;
            }
            else
            {
                doubleBuffer = false;
            }
        }
        else if(e.getSource() == btnTest)
        {
            filePath = txtAdd.getText();
            if(!filePath.isEmpty())
            {
                if(!txtLimit.getText().isEmpty())
                {
                    //store the limiter value
                    limit = Float.parseFloat(txtLimit.getText());       
                }
                else
                {
                    limit = 0;
                }
                
                if(!txtAverage.getText().isEmpty())
                {
                    //store the average value
                    average = Integer.parseInt(txtAverage.getText()); 
                }
                else
                {
                    average = 0;
                }
                
                Simulation.simulation.queueSpawns(filePath);
            }
            else
            {
                txtOutput.setText("FILE PATH NOT SPECIFIED");
                System.err.println("FILE PATH NOT SPECIFIED");
            }
        }
        else if(e.getSource() == btnSave)
        {
            filePath = txtAdd.getText();
            //don't allow to open the save dialog unless triangulation has been processed
            if(VectorObject.json != null)                                          
            {
                final JFileChooser saver = new JFileChooser();
                if(saver.showSaveDialog(saver) == JFileChooser.APPROVE_OPTION)
                {
                    boolean doExport = true;
                    boolean overrideExistingFile = false;

                    File destinationFile = new File(saver.getSelectedFile().getAbsolutePath() + ".json");

                    while(doExport && destinationFile.exists() && !overrideExistingFile)
                    {
                        overrideExistingFile = (JOptionPane.showConfirmDialog(saver, "Replace file?", "Export Settings", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION);
                        if(!overrideExistingFile)
                        {
                            if(saver.showSaveDialog(saver) == JFileChooser.APPROVE_OPTION)
                            {
                                destinationFile = new File(saver.getSelectedFile().getAbsolutePath());
                            }
                            else
                            {
                                doExport = false;
                            }
                        }
                    }

                    if(doExport)
                    {
                        try
                        {
                            if(!destinationFile.exists())
                            {
                                destinationFile.createNewFile();
                            }
                            FileWriter writer = new FileWriter(destinationFile.getAbsoluteFile());
                            BufferedWriter buffer = new BufferedWriter(writer);
                            buffer.write(VectorObject.json);
                            buffer.close();
                        }
                        catch(IOException ex)
                        {
                            ex.printStackTrace();
                        }
                    }
                }
            }
            else
            {
                txtOutput.setText("FILE PATH NOT SPECIFIED");
                System.err.println("FILE PATH NOT SPECIFIED");
            }
        }
        else if(e.getSource() == chkPoints)
        {
            if(chkPoints.isSelected())
            {
                showPoints = true;
            }
            else
            {
                showPoints = false;
            }
        }
        else if(e.getSource() == btnRender)
        {
            if(showSprite)
            {
                showSprite = false;
            }
            else
            {
                showSprite = true;
            }
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e)
    {
        if(e.getItem() == "Ramer-Douglas-Peucker")
        {
            txtLimit.setEditable(true);
            txtLimit.setEnabled(true);
            txtAverage.setEditable(false);
            txtAverage.setEnabled(false);
            selectedSimplifier = RAMER_SIMPLIFIER;
        }
        else if(e.getItem() == "Circle Algorithm")
        {
            txtLimit.setEditable(true);
            txtLimit.setEnabled(true);
            txtAverage.setEditable(true);
            txtAverage.setEnabled(true);
            selectedSimplifier = CIRCLE_SIMPLIFIER;
        }
        else if(e.getItem() == "Flexible Algorithm")
        {
            txtLimit.setEditable(true);
            txtLimit.setEnabled(true);
            txtAverage.setEditable(true);
            txtAverage.setEnabled(true);
            
            selectedSimplifier = FLEXIBLE_SIMPLIFIER;
        }
        else if(e.getItem() == "Original Algorithm")
        {
            txtLimit.setEditable(true);
            txtLimit.setEnabled(true);
            txtAverage.setEditable(true);
            txtAverage.setEnabled(true);
            selectedSimplifier = ORIG_SIMPLIFIER;
        }
        else if(e.getItem() == "None")
        {
            txtLimit.setEditable(false);
            txtLimit.setEnabled(false);
            txtAverage.setEditable(false);
            txtAverage.setEnabled(false);
            selectedSimplifier = NO_SIMPLIFIER;
        }
    }
}