/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
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
 *
 */
package org.jacorb.ir.gui.typesystem;

import java.util.*;
import javax.swing.tree.*;
import javax.swing.table.*;

/**
 * This class was generated by a SmartGuide.
 * 
 */
public abstract class TypeSystem 
{
    // Abbildung von Benutzer-lesbaren nodeTypeNames auf Klassennamen
    // Unterklassen von TypeSystemNode tragen sich hier entsprechend selbst ein.
    // (da sich konkret nur Klassen entsprechend den CORBA-IR Klassen eintragen,
    // kann es nicht zu Namenskonflikten kommen, da IDL-Bezeichner eindeutig sind)
    private static Hashtable nodeTypes = new Hashtable();
	
    protected DefaultTreeModel treeModel = null;



    /**
     * Erzeugt node entsprechend dem �bergebenen nodeTypeName 
     * (z.B. "module" erzeugt ein IRModule-Objekt)
     * @return org.jacorb.ir.gui.typesystem.TypeSystemNode
     * @param nodeTypeName java.lang.String
     */
    public static TypeSystemNode createNode (String nodeTypeName) 
        throws ClassNotFoundException 
    {
	TypeSystemNode node = null;
	Class c = Class.forName( (String)nodeTypes.get(nodeTypeName) );
	try {
            node = (TypeSystemNode)c.newInstance();
	}
	catch (Exception e) {
            e.printStackTrace();
	}	
	return node;
    
}
    /**
     * Erzeugt TreeModel, das nur root enth�lt. Um Nodes zu expandieren, mu� der von getTreeExpansionListener(treeModel)
     * zur�ckgegebene TreeExpansionListener bei JTree angemeldet werden.
     * @return javax.swing.tree.DefaultTreeModel
     * @param root org.jacorb.ir.gui.typesystem.ModelParticipant
     */

    public abstract DefaultTreeModel createTreeModelRoot();

    /**
     * This method was created by a SmartGuide.
     * @return TableModel
     * @param node org.jacorb.ir.gui.typesystem.TypeSystemNode
     */

    public abstract DefaultTableModel getTableModel(DefaultMutableTreeNode treeNode);
    /**
     * This method was created by a SmartGuide.
     * @return javax.swing.event.TreeExpansionListener
     * @param treeModel javax.swing.tree.DefaultTreeModel
     */
    public abstract javax.swing.event.TreeExpansionListener getTreeExpansionListener(TreeModel treeModel);
    /**
     * This method was created by a SmartGuide.
     * @return javax.swing.tree.TreeModel
     */
    public abstract TreeModel getTreeModel ( );
    /**
     * Wird von static initializern von Unterklassen von TypeSystemNode aufgerufen, um sich f�r 
     * createNode() zu registrieren.
     * @param nodeTypeName java.lang.String
     * @param className java.lang.String
     */
    protected static void registerNodeType(String nodeTypeName, String className) {
	nodeTypes.put(nodeTypeName,className);
    }
}








