/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package org.jacorb.naming.namemanager;

import java.awt.*;
import javax.swing.*;
import org.omg.CosNaming.*;
import org.jacorb.naming.*;

/**
 * A graphical user interface for the Naming Service.
 * If invoked with a file name argument, the NameManager
 * will create and use its own root context and print its
 * IOR to the given file such that it complies with the 
 * JacORB mechanism for locating the root naming context
 *
 * @authors Gerald Brose, Wei-Ju Wu, Volker Siegel
 * @date September 1998
 */

public class NameManager 
{
    public static void main(String args[])
    {
	org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args,null);

	JFrame frame = new JFrame("JacORB NameManager");

	// set up menu bars and menu
	JMenuBar menubar = new JMenuBar();

	JMenu fileMenu = new JMenu("File");
	JMenu editMenu = new JMenu("Edit");
	JMenu helpMenu = new JMenu("Help");

	JMenuItem quit = new JMenuItem("Quit");
	fileMenu.add(quit);

	JMenuItem options = new JMenuItem("Options");
	JMenuItem create =  new JMenuItem("New context");
	JMenuItem unbind =  new JMenuItem("Unbind name");

	editMenu.add(options);		
	editMenu.add(create);
	editMenu.add(unbind);
						
	JMenuItem about = new JMenuItem("About...");
	helpMenu.add(about);
		
	menubar.add(fileMenu);
	menubar.add(editMenu);
	menubar.add(helpMenu);

	NamingContextExt rootContext = null;
	try 
	{
	    rootContext = 
                NamingContextExtHelper.narrow( orb.resolve_initial_references("NameService"));	   
	}
	catch (Exception e) 
	{ 
	    JOptionPane.showMessageDialog(frame,
                                          "Could not find name service",
					  "Initialization error",
                                          JOptionPane.ERROR_MESSAGE);
	    System.exit(1);
	}

	if( rootContext == null )
	{
	    System.err.println("Narrow for name service failed, exiting...");
	    System.exit(1);
	}

	// set up tree and table
		
	NSTable nstable = new NSTable();
	JScrollPane tableScrollPane=new JScrollPane(nstable);
	nstable.setPreferredScrollableViewportSize(new Dimension(300,250));
		
	NSTree tree=new NSTree(300,200,nstable, rootContext);
	JScrollPane treeScrollPane=new JScrollPane(tree);
		
	JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	splitPane.setLeftComponent( treeScrollPane );
	splitPane.setRightComponent( tableScrollPane );
	splitPane.setDividerLocation(200);
	splitPane.setDividerSize(2);
	frame.getContentPane().setBackground(Color.white);
	frame.getContentPane().add(splitPane);
	tree.update();

	Handler handler=new Handler(frame,tree);
	TableHandler tableHandler = new TableHandler(frame, nstable);
	quit.addActionListener(handler);
	options.addActionListener(handler);
	create.addActionListener(handler);
	unbind.addActionListener(handler);
	about.addActionListener(handler);

	tree.addMouseListener(handler);
	tree.addKeyListener(handler);

	frame.addWindowListener(handler);
	nstable.addMouseListener(tableHandler);
	nstable.addKeyListener(tableHandler);

	frame.setJMenuBar(menubar);
	frame.pack();
	frame.show();

	orb.run();
    }

    public static void usage() 
    {
	System.out.println("NameManager [ <filename> ]");
    }
}

