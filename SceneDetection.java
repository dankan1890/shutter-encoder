/*******************************************************************************************
* Copyright (C) 2020 PACIFICO PAUL
*
* This program is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation; either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License along
* with this program; if not, write to the Free Software Foundation, Inc.,
* 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
* 
********************************************************************************************/

package application;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.awt.event.*;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import java.awt.Image;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JSpinner;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import library.FFMPEG;
import library.FFPROBE;

import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.JScrollPane;

	public class SceneDetection {
	public static JFrame frame;
	public static JDialog shadow;;
	
	/*
	 * Composants
	 */
	private static JPanel panelHaut;
	private boolean drag;
	private JLabel quit;
	private JLabel reduce;
	private JLabel topImage;
	private JLabel bottomImage;
	private static JLabel lblFlecheBas;
	private static JButton btnEDL;
	private static JButton btnExport;
	public static JLabel lblEdit;
	public static JTable table;
	public static DefaultTableModel tableRow;
	private static JSpinner tolerance;
	public static JButton btnAnalyse;
	public static JScrollPane scrollPane;
	
	public static File sortieDossier;
	public static File sortieFichier;
	
	public static boolean isRunning = false;
	private static StringBuilder errorList = new StringBuilder();
	private static int complete;
	
	/**
	 * @wbp.parser.entryPoint
	 */
	public SceneDetection(boolean runAnalyse) {
		frame = new JFrame();
		shadow = new JDialog();
		frame.getContentPane().setBackground(new Color(50,50,50));
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setTitle(Shutter.language.getProperty("frameDetectionCoupe"));
		frame.setForeground(Color.WHITE);
		frame.getContentPane().setLayout(null);
		frame.setSize(400, 600);
		frame.setResizable(false);
		frame.setAlwaysOnTop(true);	
		frame.getRootPane().putClientProperty( "Window.shadow", Boolean.FALSE );
		
		if (frame.isUndecorated() == false) //Evite un bug lors de la seconde ouverture
		{
			frame.setUndecorated(true);
			frame.setShape(new RoundRectangle2D.Double(0, 0, frame.getWidth(), frame.getHeight() + 18, 15, 15));
			frame.getRootPane().setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, new Color(100,100,100)));
			frame.setIconImage(new ImageIcon((getClass().getClassLoader().getResource("contents/icon.png"))).getImage());
			frame.setLocation(Shutter.frame.getLocation().x - frame.getSize().width -20, Shutter.frame.getLocation().y);
	    	setShadow();
		}
				
		panelHaut();
		contenu();
		
		drag = false;
				
		frame.addMouseMotionListener (new MouseMotionListener(){
 			
			@Override
			public void mouseDragged(MouseEvent e) {
				if (drag && frame.getSize().height > 90)
		       	{	
			        frame.setSize(frame.getSize().width, e.getY() + 10);	
			        scrollPane.setSize(scrollPane.getSize().width, frame.getSize().height - 160);
			    	lblFlecheBas.setLocation(0, frame.getSize().height - lblFlecheBas.getSize().height);		
					btnEDL.setBounds(7, 89 + scrollPane.getHeight() + 2, 190, 25);
					btnExport.setBounds(202, 89 + scrollPane.getHeight() + 2, 190, 25);
					lblEdit.setBounds(frame.getWidth() / 2 - 119, 89 + scrollPane.getHeight() + 32, 245, 15);
		       	}	
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				if ((MouseInfo.getPointerInfo().getLocation().y - frame.getLocation().y) > frame.getSize().height - 20)
					 frame.setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
				 else 
				{
					if (drag == false)
					 frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			}				
			
		});
		
		frame.addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent e) {				
			}

			@Override
			public void mousePressed(MouseEvent e) {		
				if (frame.getCursor().getType() == Cursor.S_RESIZE_CURSOR)
					drag = true;
			}

			@Override
			public void mouseReleased(MouseEvent e) {		
				drag = false;
				frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));	
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				if (frame.getSize().height <= 90)
				{
					frame.setSize(frame.getSize().width, 100);
		    		lblFlecheBas.setLocation(0, frame.getSize().height - lblFlecheBas.getSize().height);
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {		
				if (frame.getCursor().getType() == Cursor.S_RESIZE_CURSOR)
					frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
			
		});		

		frame.addWindowListener(new WindowAdapter(){			
			public void windowDeiconified(WindowEvent we)
		    {
		       shadow.setVisible(true);
			   frame.toFront();
		    }
		});
		
    	frame.addComponentListener(new ComponentAdapter() {
		    public void componentResized(ComponentEvent e2)
		    {
		    	frame.setShape(new RoundRectangle2D.Double(0, 0, frame.getWidth(), frame.getHeight() + 18, 15, 15));
		    }
 		});
		    	
		Utils.changeFrameVisibility(frame, shadow, false);
		
		if (runAnalyse)
			btnAnalyse.doClick();
	}
		
	private static class MousePosition {
		static int mouseX;
		static int mouseY;
	}
	
	private void panelHaut() {	
		panelHaut	= new JPanel();
		panelHaut.setLayout(null);
		panelHaut.setBounds(0, 0, frame.getSize().width, 51);
		
		quit = new JLabel(new ImageIcon(getClass().getClassLoader().getResource("contents/quit2.png")));
		quit.setBounds(frame.getSize().width - 24,0,21, 21);
				
		ImageIcon image = new ImageIcon(getClass().getClassLoader().getResource("contents/header.png"));
		Image scaledImage = image.getImage().getScaledInstance(panelHaut.getSize().width, panelHaut.getSize().height, Image.SCALE_SMOOTH);
		ImageIcon header = new ImageIcon(scaledImage);
		bottomImage = new JLabel(header);
		bottomImage.setBounds(0 ,0, frame.getSize().width, 51);
			
		JLabel title = new JLabel(Shutter.language.getProperty("frameDetectionCoupe"));
		title.setHorizontalAlignment(JLabel.CENTER);
		title.setBounds(0, 0, frame.getWidth(), 52);
		title.setFont(new Font("Magneto", Font.PLAIN, 26));
		panelHaut.add(title);
		
		topImage = new JLabel();
		ImageIcon imageIcon = new ImageIcon(header.getImage().getScaledInstance(panelHaut.getSize().width, panelHaut.getSize().height, Image.SCALE_DEFAULT));
		topImage.setIcon(imageIcon);		
		topImage.setBounds(title.getBounds());
		
		reduce = new JLabel(new ImageIcon(getClass().getClassLoader().getResource("contents/reduce2.png")));
		reduce.setHorizontalAlignment(SwingConstants.CENTER);
		reduce.setBounds(quit.getLocation().x - 21,0,21, 21);
			
		reduce.addMouseListener(new MouseListener(){
			
			private boolean accept = false;

			@Override
			public void mouseClicked(MouseEvent e) {			
			}

			@Override
			public void mousePressed(MouseEvent e) {		
				reduce.setIcon(new ImageIcon((getClass().getClassLoader().getResource("contents/reduce3.png"))));
				accept = true;
			}

			@SuppressWarnings("static-access")
			@Override
			public void mouseReleased(MouseEvent e) {		
				
				if (accept)
				{							
					shadow.setVisible(false);
					frame.setState(frame.ICONIFIED);	
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {			
				reduce.setIcon(new ImageIcon((getClass().getClassLoader().getResource("contents/reduce.png"))));
			}

			@Override
			public void mouseExited(MouseEvent e) {		
				reduce.setIcon(new ImageIcon((getClass().getClassLoader().getResource("contents/reduce2.png"))));
				accept = false;
			}
			
			
		});
				
		panelHaut.add(quit);	
		panelHaut.add(reduce);
		panelHaut.add(topImage);
		panelHaut.add(bottomImage);
		
		quit.addMouseListener(new MouseListener(){

			private boolean accept = false;

			@Override
			public void mouseClicked(MouseEvent e) {			
			}

			@Override
			public void mousePressed(MouseEvent e) {		
				quit.setIcon(new ImageIcon((getClass().getClassLoader().getResource("contents/quit3.png"))));
				accept = true;
			}

			@Override
			public void mouseReleased(MouseEvent e) {	
				if (accept)		
				{		  
					if (FFMPEG.runProcess.isAlive())
						Shutter.btnAnnuler.doClick();
					
					if (Shutter.btnAnnuler.isEnabled() == false)
					{
						if (sortieDossier.exists())
							deleteDirectory(sortieDossier);
						
						Utils.changeFrameVisibility(frame, shadow, true);	
						}
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {			
				quit.setIcon(new ImageIcon((getClass().getClassLoader().getResource("contents/quit.png"))));
			}

			@Override
			public void mouseExited(MouseEvent e) {		
				quit.setIcon(new ImageIcon((getClass().getClassLoader().getResource("contents/quit2.png"))));
				accept = false;
			}

						
		});
		
		panelHaut.setBounds(0, 0, frame.getSize().width, 51);
		frame.getContentPane().add(panelHaut);						

		bottomImage.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent down) {
			}

			@Override
			public void mousePressed(MouseEvent down) {
				shadow.toFront();
				MousePosition.mouseX = down.getPoint().x;
				MousePosition.mouseY = down.getPoint().y;					
			}

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {					
			}

			@Override
			public void mouseExited(MouseEvent e) {				
			}		

		 });
		 		
		bottomImage.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseDragged(MouseEvent e) {
					frame.setLocation(MouseInfo.getPointerInfo().getLocation().x - MousePosition.mouseX, MouseInfo.getPointerInfo().getLocation().y - MousePosition.mouseY);	
			}

			@Override
			public void mouseMoved(MouseEvent e) {
			}
			
		});
		
	}

	private void contenu() {
		scrollPane = new JScrollPane();
		scrollPane.setBounds(9, 89, 380, frame.getSize().height - 160);
		frame.getContentPane().add(scrollPane);
		
		JLabel lblPourcentage = new JLabel("%");
		lblPourcentage.setFont(new Font("FreeSans", Font.PLAIN, 12));
		lblPourcentage.setBounds(132, 63, 11, 15);
		frame.getContentPane().add(lblPourcentage);
			
		btnAnalyse = new JButton(Shutter.language.getProperty("btnAnalyse"));
		btnAnalyse.setFont(new Font("Montserrat", Font.PLAIN, 12));
		btnAnalyse.setBounds(151, 57, 238, 25);
		frame.getContentPane().add(btnAnalyse);
		
		btnAnalyse.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				tolerance.setEnabled(false);
				btnAnalyse.setEnabled(false);
				btnEDL.setEnabled(false);
				btnExport.setEnabled(false);
				lblEdit.setVisible(false);
				if (sortieDossier != null && sortieDossier.exists())
					deleteDirectory(sortieDossier);
				

				if (tableRow != null)
					tableRow.setRowCount(0);
								
				runAnalyse();
			}
			
		});
		
		JLabel lblSensibilit = new JLabel(Shutter.language.getProperty("lblSensibility"));
		lblSensibilit.setFont(new Font("FreeSans", Font.PLAIN, 12));
		lblSensibilit.setBounds(10, 62, 64, 15);
		frame.getContentPane().add(lblSensibilit);
		
		tolerance = new JSpinner(new SpinnerNumberModel(80, 0, 100, 10));
		tolerance.setBounds(81, 56, 49, 27);
		frame.getContentPane().add(tolerance);	
		
		lblFlecheBas = new JLabel("▲▼");
		lblFlecheBas.setHorizontalAlignment(SwingConstants.CENTER);
		lblFlecheBas.setFont(new Font("FreeSans", Font.PLAIN, 20));
		lblFlecheBas.setSize(new Dimension(frame.getSize().width, 20));
		lblFlecheBas.setLocation(0, frame.getSize().height - lblFlecheBas.getSize().height);
		lblFlecheBas.setVisible(true);		
		frame.getContentPane().add(lblFlecheBas);
		
		btnEDL = new JButton(Shutter.language.getProperty("btnEDL"));
		btnEDL.setFont(new Font("Montserrat", Font.PLAIN, 12));
		btnEDL.setEnabled(false);
		btnEDL.setBounds(7, 89 + scrollPane.getHeight() + 2, 190, 25);
		frame.getContentPane().add(btnEDL);
		
		btnEDL.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
    		
				frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				frame.setVisible(false);
				shadow.setVisible(false);
				final FileDialog dialog = new FileDialog(frame, Shutter.language.getProperty("saveEDL"), FileDialog.SAVE);
				if (System.getProperty("os.name").contains("Mac") || System.getProperty("os.name").contains("Linux"))
					dialog.setDirectory(System.getProperty("user.home") + "/Desktop");
				else
					dialog.setDirectory(System.getProperty("user.home") + "\\Desktop");
				dialog.setLocation(frame.getLocation().x - 50, frame.getLocation().y + 50);
				dialog.setAlwaysOnTop(true);
				dialog.setVisible(true);
			    				
			    frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));		
				frame.setVisible(true);
				shadow.setVisible(true);
				frame.toFront();
			    
			    if (dialog.getFile() != null)
				{ 							
			    	Thread runProcess = new Thread(new Runnable()  {
					@Override
					public void run() {
						
					PrintWriter writer = null;
				    try {
						writer = new PrintWriter(dialog.getDirectory() + dialog.getFile().replace(".edl", "") + ".edl", "UTF-8");
						NumberFormat formatEDL = new DecimalFormat("000000");
						writer.println("TITLE : " + dialog.getFile());
						
						int countItemsEDL = 0;
				    	for (int i = 0 ; i < tableRow.getRowCount(); i++)
						{   							
							String timecodeStart = String.valueOf(tableRow.getValueAt(i, 2));
							String tcStart[] = timecodeStart.split(":");
							int tcStartToMs = (int) (Integer.valueOf(tcStart[0]) * 3600000 + Integer.valueOf(tcStart[1]) * 60000 + Integer.valueOf(tcStart[2]) * 1000 + Integer.valueOf(tcStart[3]) * (1000 / FFPROBE.currentFPS));
							
			    			NumberFormat formatter = new DecimalFormat("00");
			    			String tcInTimeLine = (formatter.format(tcStartToMs / 3600000)) 
			    					+ ":" + (formatter.format((tcStartToMs / 60000) % 60))
			    					+ ":" + (formatter.format((tcStartToMs / 1000) % 60)) 		
			    					+ ":" + (formatter.format((int) (tcStartToMs / (1000 / FFPROBE.currentFPS) % FFPROBE.currentFPS)));
			    			
			    			//Timecode Initial de la vidéo
			    			int preTcMs; 
			    			if (FFPROBE.timecode1 != "" && FFPROBE.timecode2 != "" && FFPROBE.timecode3 != "" && FFPROBE.timecode4 != "" )
			    			{    			
		    					preTcMs = (int) (Integer.parseInt(FFPROBE.timecode1) * 3600000 
		    					+ Integer.parseInt(FFPROBE.timecode2) * 60000 
		    					+ Integer.parseInt(FFPROBE.timecode3) * 1000 
		    					+ (Integer.parseInt(FFPROBE.timecode4) * (1000 / FFPROBE.currentFPS)));
			    			}
			    			else
			    				preTcMs = 0;
			    			
			    			//Timecode de la coupe
			    			int preTcInVideo = (int) (Integer.parseInt(tcStart[0]) * 3600000 
			    					+ Integer.parseInt(tcStart[1]) * 60000 
			    					+ Integer.parseInt(tcStart[2]) * 1000 
			    					+ (Integer.parseInt(tcStart[3]) * (1000 / FFPROBE.currentFPS))); 	
			    			
			    			//Total
			    			int totalTcIn = (preTcMs + preTcInVideo);
			    			
			    			//Mise en forme
			    			String tcInVideo = (formatter.format(totalTcIn / 3600000)) 
			    					+ ":" + (formatter.format((totalTcIn / 60000) % 60))
			    					+ ":" + (formatter.format((totalTcIn / 1000) % 60)) 		
			    					+ ":" + (formatter.format((int) (totalTcIn / (1000 / FFPROBE.currentFPS) % FFPROBE.currentFPS)));							
			    			
			    			int tcEndToMS;
			    			int preTcOutVideo;
			    			String tcOutVideo;
			    			if (i < tableRow.getRowCount() - 1)
			    			{
								String timecodeEnd = String.valueOf(tableRow.getValueAt(i + 1, 2));
								String tcEnd[] = timecodeEnd.split(":");
								tcEndToMS = (int) (Integer.valueOf(tcEnd[0]) * 3600000 + Integer.valueOf(tcEnd[1]) * 60000 + Integer.valueOf(tcEnd[2]) * 1000 + Integer.valueOf(tcEnd[3]) * (1000 / FFPROBE.currentFPS));
	
				    			//Timecode de la coupe
				    			 preTcOutVideo = (int) (Integer.parseInt(tcEnd[0]) * 3600000 
				    					+ Integer.parseInt(tcEnd[1]) * 60000 
				    					+ Integer.parseInt(tcEnd[2]) * 1000 
				    					+ (Integer.parseInt(tcEnd[3]) * (1000 / FFPROBE.currentFPS))); 	
			    			}
			    			else //Si c'est le dernier fichier on récupère la durée de la vidéo
							{								
								String tc[] = FFPROBE.getVideoLengthTC.split(":");
								int h = (Integer.valueOf(tc[0]) * 3600000);
								int m = (Integer.valueOf(tc[1]) * 60000);
								int s = (Integer.valueOf(tc[2]) * 1000);
								int f = (int) (Integer.valueOf(tc[3]) * 10);
										
								tcEndToMS = (h+m+s+f);
															
				    			//Timecode de la coupe
				    			preTcOutVideo = tcEndToMS;			
							}							
							
			    			//Total
			    			int totalTcOut = (preTcMs + preTcOutVideo);
			    			
			    			//Mise en forme
			    			tcOutVideo = (formatter.format(totalTcOut / 3600000)) 
			    					+ ":" + (formatter.format((totalTcOut / 60000) % 60))
			    					+ ":" + (formatter.format((totalTcOut / 1000) % 60)) 		
			    					+ ":" + (formatter.format((int) (totalTcOut / (1000 / FFPROBE.currentFPS) % FFPROBE.currentFPS)));	
			    			
			    			
			    			String tcOutTimeLine = (formatter.format(tcEndToMS / 3600000)) 
			    					+ ":" + (formatter.format((tcEndToMS / 60000) % 60))
			    					+ ":" + (formatter.format((tcEndToMS / 1000) % 60)) 		
			    					+ ":" + (formatter.format((int) (tcEndToMS / (1000 / FFPROBE.currentFPS) % FFPROBE.currentFPS)));
			    			
							String cutName;
							String ext = sortieFichier.toString().substring(sortieFichier.toString().lastIndexOf("."));
							if (i % 2 == 0)
								cutName = sortieFichier.toString().replace(" ", "_");
							else
								cutName = sortieFichier.toString().replace(" ", "_").replace(ext, "_" + Shutter.language.getProperty("cut") + ext);
			    																	
							writer.println(formatEDL.format(countItemsEDL + 1) + "  " + cutName + " V     C        " + tcInVideo + " " + tcOutVideo + " " + tcInTimeLine + " " + tcOutTimeLine);
							writer.println(formatEDL.format(countItemsEDL + 2) + "  " + cutName + " A     C        " + tcInVideo + " " + tcOutVideo + " " + tcInTimeLine + " " + tcOutTimeLine);
							writer.println(formatEDL.format(countItemsEDL + 3) + "  " + cutName + " A2    C        " + tcInVideo + " " + tcOutVideo + " " + tcInTimeLine + " " + tcOutTimeLine);
							
							countItemsEDL += 3;	
						} //End for
				    	
				    } catch (Exception e){
				    	System.out.println(e);
				    }
				    				    
					writer.close();	
					JOptionPane.showMessageDialog(frame, Shutter.language.getProperty("fileCreated"), "EDL", JOptionPane.INFORMATION_MESSAGE);
						
					}//End run
					
			    }); runProcess.start();					
				}
			}			
		});
		
		btnExport = new JButton(Shutter.language.getProperty("btnExport"));
		btnExport.setFont(new Font("Montserrat", Font.PLAIN, 12));
		btnExport.setBounds(202, 89 + scrollPane.getHeight() + 2, 190, 25);
		btnExport.setEnabled(false);
		frame.getContentPane().add(btnExport);
		
		btnExport.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));			    
		    	frame.setVisible(false);
		    	shadow.setVisible(false);
				final FileDialog dialog = new FileDialog(frame, Shutter.language.getProperty("chooseFileName"), FileDialog.SAVE);
				if (System.getProperty("os.name").contains("Mac") || System.getProperty("os.name").contains("Linux"))
					dialog.setDirectory(System.getProperty("user.home") + "/Desktop");
				else
					dialog.setDirectory(System.getProperty("user.home") + "\\Desktop");
				dialog.setLocation(frame.getLocation().x - 50, frame.getLocation().y + 50);
				dialog.setAlwaysOnTop(true);
				dialog.setVisible(true);
			    				
			    frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));			    
		    	frame.setVisible(true);
		    	shadow.setVisible(true);
				frame.toFront();
			    
			    if (dialog.getFile() != null)
			    {
			    	final File sortieFolder = new File(dialog.getDirectory() + dialog.getFile());
			    	sortieFolder.mkdir();
												
			    	Thread runProcess = new Thread(new Runnable()  {
					@Override
					public void run() {
						
						isRunning = true;
						
				    	for (int i = 0 ; i < tableRow.getRowCount(); i++)
						{
			    		try {
			    			
			    		//Si c'est le premier lancement
			    		if (i == 0)
			    		{
			    			//On cache la fenêtre 
			    			Utils.changeFrameVisibility(frame, shadow, true);
			    			
			    			//On choisit la fonction
	    			    	Shutter.comboFonctions.setSelectedItem(Shutter.language.getProperty("functionCut"));

							Shutter.caseChangeFolder1.setSelected(true);
							Shutter.caseOpenFolderAtEnd1.setSelected(false);
							Shutter.lblDestination1.setText(sortieFolder.toString());
							Shutter.caseInAndOut.doClick();								
							
							do {
								Thread.sleep(100);
							} while (VideoPlayer.frame.isVisible() == false);
							VideoPlayer.frame.setVisible(false);
							VideoPlayer.shadow.setVisible(false);
							
							//On démarre le lecteur puis on fait pause
							do {
								Thread.sleep(100);
							} while (VideoPlayer.leftPlay.getText().equals(Shutter.language.getProperty("btnPause")) == false);
							
							VideoPlayer.leftPlay.doClick();	
			    		}
							
							String timecodeStart = String.valueOf(tableRow.getValueAt(i, 2));
							String tcStart[] = timecodeStart.split(":");
							int tcStartToMS = (int) (Integer.valueOf(tcStart[0]) * 3600000 + Integer.valueOf(tcStart[1]) * 60000 + Integer.valueOf(tcStart[2]) * 1000 + Integer.valueOf(tcStart[3]) * (1000 / FFPROBE.currentFPS));
							
							//On définit le point d'entrée
							VideoPlayer.sliderIn.setValue(tcStartToMS);
							VideoPlayer.caseInH.setText(tcStart[0]);
							VideoPlayer.caseInM.setText(tcStart[1]);
							VideoPlayer.caseInS.setText(tcStart[2]);
							VideoPlayer.caseInF.setText(tcStart[3]);

							//Si c'est le dernier fichier pas de tc de fin
							if (i < tableRow.getRowCount() - 1)
							{
								String timecodeEnd = String.valueOf(tableRow.getValueAt(i + 1, 2));
								String tcEnd[] = timecodeEnd.split(":");								
								int tcEndToMS = (int) (Integer.valueOf(tcEnd[0]) * 3600000 + Integer.valueOf(tcEnd[1]) * 60000 + Integer.valueOf(tcEnd[2]) * 1000 + Integer.valueOf(tcEnd[3]) * (1000 / FFPROBE.currentFPS));
								
								//On définit le point sortie
								VideoPlayer.sliderOut.setValue(tcEndToMS);								
								VideoPlayer.caseOutH.setText(tcEnd[0]);
								VideoPlayer.caseOutM.setText(tcEnd[1]);
								VideoPlayer.caseOutS.setText(tcEnd[2]);
								VideoPlayer.caseOutF.setText(tcEnd[3]);
		
								VideoPlayer.dureeTotale();
							}
							else
								VideoPlayer.sliderOut.setValue(VideoPlayer.sliderOut.getMaximum());		
							
							
							//On démarre la fonction
							Shutter.btnStart.doClick();
							
							//On attend que le processus se lance
							do {
								Thread.sleep(100);
							} while (Shutter.btnStart.getText().equals(Shutter.language.getProperty("btnStartFunction")));		
							
							//On attend que le fichier soit terminé
							do {
								Thread.sleep(100);
							} while (Shutter.btnStart.getText().equals(Shutter.language.getProperty("btnStartFunction")) == false);							
														
			    			} catch (Exception e){}
			    		
						} //End for
				    	
				    	isRunning = false;

				    	//On remet la fonction Détection de coupe
    	                for(int comboItem = 0 ; comboItem < Shutter.comboFonctions.getModel().getSize() ; comboItem ++) {
    	                    Object element = Shutter.comboFonctions.getModel().getElementAt(comboItem);
    	                    if (element.toString().equals(Shutter.language.getProperty("functionSceneDetection")))
    	                    {
    			    			Shutter.comboFonctions.setSelectedIndex(comboItem);
    	                    	break;
    	                    }
    	                }
    	                
		    			//On réouvre la fenêtre 
		    			Utils.changeFrameVisibility(frame, shadow, false);
				    	
				    	//On ferme le lecteur une fois terminé
				    	Shutter.caseInAndOut.setSelected(false);
						if (VideoPlayer.mediaPlayerComponentLeft != null)
							VideoPlayer.mediaPlayerComponentLeft.getMediaPlayer().stop();
						if (VideoPlayer.mediaPlayerComponentRight != null)
							VideoPlayer.mediaPlayerComponentRight.getMediaPlayer().stop();
						VideoPlayer.frame.getContentPane().removeAll();			
					}
			    	}); runProcess.start();					
				}
			}
			
		});
		
		lblEdit = new JLabel(Shutter.language.getProperty("lblEdit"));
		lblEdit.setForeground(new Color(71,163,236));
		lblEdit.setHorizontalAlignment(SwingConstants.CENTER);
		lblEdit.setFont(new Font("Montserrat", Font.PLAIN, 13));
		lblEdit.setBounds(frame.getWidth() / 2 - 119, 89 + scrollPane.getHeight() + 32, 245, 15);
		lblEdit.setVisible(false);
		frame.getContentPane().add(lblEdit);
									
	}
	
	@SuppressWarnings("serial")
	private static void newTable() {
		ImageIcon imageIcon = new ImageIcon(SceneDetection.sortieDossier.toString() + "/0.png");
		ImageIcon icon = new ImageIcon(imageIcon.getImage().getScaledInstance(142, 80, Image.SCALE_DEFAULT));			
		Object[][] firstImage = {{"1", icon, "00:00:00:00"}};

		tableRow = new DefaultTableModel(firstImage, new String[] {"N\u00B0", "Plans", "Timecode"});
		DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
		dtcr.setHorizontalAlignment(SwingConstants.CENTER);
        table = new JTable(tableRow)
        {
            @SuppressWarnings({ "unchecked", "rawtypes" })
			public Class getColumnClass(int column)
            {
                return getValueAt(0, column).getClass();
            }
            
            @Override
            public boolean isCellEditable(int row, int column) {
               return false;
            }
        };
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment( JLabel.CENTER );
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
		table.setShowVerticalLines(false);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setRowHeight(80);
		table.setForeground(Color.BLACK);
		table.getColumnModel().getColumn(0).setPreferredWidth(18);
		table.getColumnModel().getColumn(1).setPreferredWidth(table.getColumnModel().getColumn(1).getPreferredWidth());
		table.setBounds(9, 89, 380, frame.getHeight() - 134);
		scrollPane.setViewportView(table);
		
		JTableHeader header = table.getTableHeader();
	    header.setForeground(Color.BLACK);
		
		table.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
				if ((e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE) && FFMPEG.isRunning == false && table.getSelectedRowCount() > 0)
				{	
					File imageToDelete = new File(sortieDossier.toString() + "/" + (table.getSelectedRow()) + ".png");
					imageToDelete.delete();
					
					int i = 0; //On renomme toutes les images
					for (File file : sortieDossier.listFiles())
					{
						if (file.toString().substring(file.toString().lastIndexOf(".")).equals(".png"))
							file.renameTo(new File(sortieDossier.toString() + "/" + i + ".png"));
						
						i++;			
					}
					
					tableRow.removeRow(table.getSelectedRow());	
					
					//On remet les chiffres dans l'ordre
					for (int n = 0 ; n < tableRow.getRowCount() ; n++)
					{	
						table.getModel().setValueAt(String.valueOf(n + 1), n, 0);	
					}					

					table.repaint();
				}
				
			}

			@Override
			public void keyReleased(KeyEvent arg0) {	
			}

			@Override
			public void keyTyped(KeyEvent arg0) {
			}
			
		});
				
		final JPopupMenu popupListe = new JPopupMenu();
		JMenuItem visualiser = new JMenuItem(Shutter.language.getProperty("menuItemVisualiser"));
		JMenuItem ouvrirDossier = new JMenuItem(Shutter.language.getProperty("menuItemOuvrirDossier"));
		JMenuItem copieTimeCode = new JMenuItem(Shutter.language.getProperty("menuItemCopyTimecode"));
		
		visualiser.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
	            try {
					Desktop.getDesktop().open(new File(sortieDossier + "/" + table.getSelectedRow() + ".png"));
				} catch (IOException e1) {}			
			}		
		});

		ouvrirDossier.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
	            try {
					Desktop.getDesktop().open(sortieDossier);
				} catch (IOException e1) {}
			}		
		});
		
		copieTimeCode.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				String timecode = tableRow.getValueAt(table.getSelectedRow(), 2).toString();
				StringSelection stringSelection = new StringSelection(timecode);
				Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
				clpbrd.setContents(stringSelection, null);
			}		
		});
		
		popupListe.add(visualiser);
		popupListe.add(ouvrirDossier);
		popupListe.add(copieTimeCode);
		
		table.addMouseListener(new MouseAdapter(){
		    @Override
		    public void mouseClicked(MouseEvent e){
		    	//Double clic
		        if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1 && table.getSelectedRowCount() == 1)
		        {
		            try {
						Desktop.getDesktop().open(new File(sortieDossier + "/" + table.getSelectedRow() + ".png"));
					} catch (IOException e1) {}
		        }
		        
		        //Clic droit
				if (e.getButton() == MouseEvent.BUTTON3 || (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) == ActionEvent.CTRL_MASK && e.getButton() == MouseEvent.BUTTON1)
				{
					popupListe.show(table, e.getX() - 30, e.getY());
				}					
		    }
		});

	}
	
	private void setShadow() {
		shadow.setSize(frame.getSize().width + 14, frame.getSize().height + 7);
    	shadow.setLocation(frame.getLocation().x - 7, frame.getLocation().y - 7);
    	shadow.setUndecorated(true);
    	shadow.setContentPane(new DetectionCoupeShadow());
    	shadow.setBackground(new Color(255,255,255,0));
		
		shadow.setFocusableWindowState(false);
		
		shadow.addMouseListener(new MouseAdapter() {

			public void mousePressed(MouseEvent down) {
				frame.toFront();
			}
    		
    	});
   		
    	frame.addComponentListener(new ComponentAdapter() {
		    public void componentMoved(ComponentEvent e) {
		        shadow.setLocation(frame.getLocation().x - 7, frame.getLocation().y - 7);
		    }
		    public void componentResized(ComponentEvent e2)
		    {
		    	shadow.setSize(frame.getSize().width + 14, frame.getSize().height + 7);
		    }
 		});
	}
	
	public static void runAnalyse() {
		
		Thread thread = new Thread(new Runnable(){			
			@Override
			public void run() {				
				complete = 0;
			
				Shutter.lblTermine.setText(Utils.fichiersTermines(complete));

					
				for (int i = 0 ; i < Shutter.liste.getSize() ; i++)
				{
					File file = new File(Shutter.liste.getElementAt(i));
					
					if (i > 0)
					{
						new SceneDetection(false);
					}
			            
					try {
						// Analyse des données					 
						 FFPROBE.Data(file.toString());						 
						 do
							Thread.sleep(100);	
						 while (FFPROBE.isRunning);
							 																	
						String fichier = file.getName();
						Shutter.lblEncodageEnCours.setText(fichier);
						
						String sortie = file.getParent();					
						final String extension =  fichier.substring(fichier.lastIndexOf("."));
						sortieDossier =  new File(sortie + "/" + fichier.replace(extension, ""));		
						sortieDossier.mkdir();
						
						sortieFichier =  new File(file.getName());
						
						//Envoi de la commande
						String cmd;
						cmd = " -f image2 -vframes 1 ";
						FFMPEG.run(" -i " + '"' + file.toString() + '"' + cmd + "-y " + '"'  + sortieDossier.toString() + "/0.png" + '"');
						
						//Attente de la fin de FFMPEG
						do
							Thread.sleep(100);
						while(FFMPEG.runProcess.isAlive());										
						
						//On créer le tableau ici après la première image
						newTable();
					        		
						//Envoi de la commande
						String tol = String.valueOf((float) (100 - Integer.valueOf(application.SceneDetection.tolerance.getValue().toString())) / 100);
						cmd = " -vf select=" + '"' + "gt(scene\\," + tol  + ")" + '"' + ",showinfo -vsync 2 -f image2 ";
						FFMPEG.run(" -i " + '"' + file.toString() + '"' + cmd + "-y " + '"'  + sortieDossier.toString() + "/%01d.png" + '"');		
						
						//Attente de la fin de FFMPEG
						do
							Thread.sleep(100);
						while(FFMPEG.runProcess.isAlive());						
					
						actionsDeFin();
						
					} catch (InterruptedException e) {
						FFMPEG.error  = true;
					}//End Try
				}//End for
				
				//Affichage des erreurs
				if (errorList.length() != 0)
					JOptionPane.showMessageDialog(Shutter.frame, Shutter.language.getProperty("notProcessedFiles") + " " + '\n' + '\n' + errorList.toString() ,Shutter.language.getProperty("encodingError"), JOptionPane.ERROR_MESSAGE);
				errorList.setLength(0);
				
				FFMPEG.FinDeFonction();
			}//run
			
		});
		thread.start();
		
    }//main

	private static void actionsDeFin() {
		//Erreurs
		if (FFMPEG.error)
		{
		    errorList.append(System.lineSeparator());
		}

		//Fichiers terminés
		if (FFMPEG.cancelled == false && FFMPEG.error == false)
		{
			complete++;
			Shutter.lblTermine.setText(Utils.fichiersTermines(complete));
		}
		
		tolerance.setEnabled(true);
		btnAnalyse.setEnabled(true);
		btnEDL.setEnabled(true);
		btnExport.setEnabled(true);
		lblEdit.setVisible(true);

	}
	
	public static boolean deleteDirectory(File dir) {
	    if(! dir.exists() || !dir.isDirectory())    {
	        return false;
	    }

	    String[] files = dir.list();
	    for(int i = 0, len = files.length; i < len; i++)    {
	        File f = new File(dir, files[i]);
	        if(f.isDirectory()) {
	            deleteDirectory(f);
	        }else   {
	            f.delete();
	        }
	    }
	    return dir.delete();
	}
	
}
	
//Ombre
@SuppressWarnings("serial")
class DetectionCoupeShadow extends JPanel {
    public void paintComponent(Graphics g){
  	  RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
  	  qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY );
  	  Graphics2D g1 = (Graphics2D)g.create();
  	  g1.setComposite(AlphaComposite.SrcIn.derive(0.0f));
  	  g1.setRenderingHints(qualityHints);
  	  g1.setColor(new Color(0,0,0));
  	  g1.fillRect(0,0,SceneDetection.frame.getWidth() + 14, SceneDetection.frame.getHeight() + 7);
  	  
 	  for (int i = 0 ; i < 7; i++) 
 	  {
 		  Graphics2D g2 = (Graphics2D)g.create();
 		  g2.setRenderingHints(qualityHints);
 		  g2.setColor(new Color(0,0,0, i * 10));
 		  g2.drawRoundRect(i, i, SceneDetection.frame.getWidth() + 13 - i * 2, SceneDetection.frame.getHeight() + 7, 20, 20);
 	  }
     }
 }