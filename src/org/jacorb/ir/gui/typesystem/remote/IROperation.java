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
package org.jacorb.ir.gui.typesystem.remote;

/**
 * This class was generated by a SmartGuide.
 * 
 */
 
import java.util.*;
import org.omg.CORBA.*;
import javax.swing.tree.*;
import org.jacorb.ir.gui.typesystem.*;
 
public class IROperation extends IRNodeWithType implements AbstractContainer
{
    protected IRParameter[] parameters;
    protected IRException[] exceptions;
    private String instanceNodeTypeName;
    private OperationDef operationDef;


    /**
     * Default-Konstruktor: wird von TypeSystem.createNode(...) benutzt
     */
    public IROperation ( ) {
	super();
    }
    /**
     * This method was created by a SmartGuide.
     * @param irObject org.omg.CORBA.IRObject
     */
    public IROperation ( IRObject irObject) {
	super(irObject);
	this.operationDef = OperationDefHelper.narrow((org.omg.CORBA.Object)irObject);
	setAssociatedTypeSystemNode(RemoteTypeSystem.createTypeSystemNode(operationDef.result_def()));	
	String dummy = super.getInstanceNodeTypeName();
	if (operationDef.mode().value() == OperationMode._OP_ONEWAY) {
            dummy = "oneway" + " " + dummy;
	}	
	this.instanceNodeTypeName = dummy;
    }
    /**
     * contents method comment.
     */
    public org.jacorb.ir.gui.typesystem.ModelParticipant[] contents() {
        //	if (!getAbsoluteName().equals("::org::omg::CORBA::DynamicImplementation::invoke")) {
        OperationDef operationDef= OperationDefHelper.narrow((org.omg.CORBA.Object)this.irObject);
        ParameterDescription[] contents = operationDef.params();	
        org.jacorb.ir.gui.typesystem.TypeSystemNode[] result = new org.jacorb.ir.gui.typesystem.TypeSystemNode[contents.length];
        for (int i=0; i<contents.length; i++) {
            result[i] = RemoteTypeSystem.createTypeSystemNode(contents[i]);
        } // for
        return result;	
        //	}
        //	return new org.jacorb.ir.gui.typesystem.TypeSystemNode[0];
    }
    /**
     * This method was created by a SmartGuide.
     * @return java.lang.String
     */
    public String getInstanceNodeTypeName ( ) {
	return instanceNodeTypeName;
    }
    /**
     * This method was created by a SmartGuide.
     * @return java.lang.String
     */
    public static String nodeTypeName() {
	return "operation";	
    }

    public String description() {
	String result = super.description();
	if (exceptions==null) {
            ExceptionDef[] exceptionDefs = operationDef.exceptions();	
            exceptions = new IRException[exceptionDefs.length];
            for (int i=0; i<exceptionDefs.length; i++) {
                exceptions[i] = (IRException)RemoteTypeSystem.createTypeSystemNode(exceptionDefs[i]);
            } // for
	}
	if (exceptions.length>0) {
            result = result + "\nExceptions:\t ";
            for (int i = 0; i<exceptions.length; i++) {
                result = result + ((TypeSystemNode)exceptions[i]).getAbsoluteName();
                if (!(i==exceptions.length-1)) {
                    result = result + ", ";
                }	
            }	
	}
	else {
            result = result	+ "\nExceptions:\t:none";
	}	
	return result;	
    }
}











