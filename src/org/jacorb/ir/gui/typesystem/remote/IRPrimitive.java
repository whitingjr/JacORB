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


import org.omg.CORBA.*;

/**
 * This class was generated by a SmartGuide.
 * 
 */
public class IRPrimitive extends IRNode {



/**
 * IRPrimitive constructor comment.
 */
protected IRPrimitive() {
	super();
}
/**
 * IRPrimitive constructor comment.
 * @param irObject org.omg.CORBA.IRObject
 */
protected IRPrimitive(org.omg.CORBA.IRObject irObject) {
	super(irObject);
	PrimitiveDef primitiveDef = PrimitiveDefHelper.narrow((org.omg.CORBA.Object)irObject);
	switch (primitiveDef.kind().value()) {
		case PrimitiveKind._pk_null:
			setName("null");	// gibt's nicht laut CORBA Spez.
			break;
		case PrimitiveKind._pk_void:
			setName("void");
			break;
		case PrimitiveKind._pk_short :
			setName("short");
			break;
		case PrimitiveKind._pk_long:
			setName("long");
			break;
		case PrimitiveKind._pk_ushort:
			setName("unsigned short");
			break;
		case PrimitiveKind._pk_ulong:
			setName("usigned long");
			break;
		case PrimitiveKind._pk_float:
			setName("float");
			break;
		case PrimitiveKind._pk_double:
			setName("double");
			break;
		case PrimitiveKind._pk_boolean:
			setName("boolean");
			break;
		case PrimitiveKind._pk_char:
			setName("char");
			break;
		case PrimitiveKind._pk_octet:
			setName("octet");
			break;
		case PrimitiveKind._pk_any:
			setName("any");
			break;
		case PrimitiveKind._pk_TypeCode:
			setName("typecode");
			break;
		case PrimitiveKind._pk_Principal:
			setName("Principal");
			break;
		case PrimitiveKind._pk_string:
			setName("string");
			break;
		case PrimitiveKind._pk_objref:
			setName("objref");
			break;
		case PrimitiveKind._pk_longlong:
			setName("long long");
			break;
		case PrimitiveKind._pk_ulonglong:
			setName("unsigned long long");
			break;
		case PrimitiveKind._pk_longdouble:
			setName("long double");
			break;
		case PrimitiveKind._pk_wchar:
			setName("wchar");
			break;
		case PrimitiveKind._pk_wstring:
			setName("wstring");
			break; 
		default:
			setName("unknown Primitive??");
			break;
	}	
	setAbsoluteName(getName());
}
/**
 * This method was created by a SmartGuide.
 * @return java.lang.String
 */
public static String nodeTypeName() {
	return "primitive";
}
}











