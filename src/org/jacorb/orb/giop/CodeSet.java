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

package org.jacorb.orb.connection;

import org.omg.CONV_FRAME.*;
import org.omg.IOP.*;

import org.jacorb.orb.CDRInputStream;
import org.jacorb.orb.CDROutputStream;

public class CodeSet
{
    public static final int ISO8859_1=0x00010001;   /* standard ASCII */
    public static final int UTF16= 0x00010109;      /* extended UCS2,
                                                       2 or 4 bytes
                                                       for every char */
    public static final int UTF8 = 0x05010001;      /* 1-6 bytes for
                                                       every character */

    public static String csName(int cs)
    {
        switch(cs)
        {
            case ISO8859_1: return "ISO 8859-1";
            case UTF16: return  "UTF-16";
            case UTF8: return  "UTF-8";
        }
        return "Unknown TCS: " + Integer.toHexString(cs);
    }

    public static int getTCSDefault()
    {
        return ISO8859_1;
    }

    public static int getTCSWDefault()
    {
        return UTF16;
    }

    public static int getConversionDefault()
    {
        return UTF8;
    }

    /**
     * This method compares the codesets in the component with our
     * native codeset.  
     */
    public static int selectTCS( CodeSetComponentInfo cs_info )
    {
        int with_native = selectCodeSet( cs_info.ForCharData, 
                                         getTCSDefault() );
        
        if( with_native == -1 )
        {
            //no match with native codeset, so try with conversion
            //codeset
            
            return selectCodeSet( cs_info.ForCharData, getConversionDefault() );
        }
        else
        {
            return with_native;
        }
    }

    /**
     * This method compares the wide codesets in the component with our
     * native wide codeset.  
     */
    public static int selectTCSW( CodeSetComponentInfo cs_info )
    {
        int with_native = selectCodeSet( cs_info.ForWcharData, 
                                         getTCSWDefault() );
        
        if( with_native == -1 )
        {
            //no match with native codeset, so try with conversion
            //codeset
            
            return selectCodeSet( cs_info.ForWcharData, 
                                  getConversionDefault() );
        }
        else
        {
            return with_native;
        }
    }

    private static int selectCodeSet( CodeSetComponent cs_component,
                                      int native_cs )
    {
        // check if we support server's native sets
	if( cs_component.native_code_set == native_cs ) 
        {
	    return native_cs;
        }

	// is our native CS supported at server ?
	for( int i = 0; i < cs_component.conversion_code_sets.length; i++ )
	{
	    if( cs_component.conversion_code_sets[i] == native_cs )
            { 
		return native_cs;
            }
	}
		
	// can't find supported set ..
	return -1;
    }

    public static ServiceContext createCodesetContext( int tcs, int tcsw )
    {
        // encapsulate context
	CDROutputStream os = new CDROutputStream();
	os.beginEncapsulatedArray();
	CodeSetContextHelper.write( os, new CodeSetContext( tcs, tcsw ));

        return new ServiceContext( TAG_CODE_SETS.value,
                                   os.getBufferCopy() );
    }
    
    public static CodeSetContext getCodeSetContext( ServiceContext[] contexts )
    {
        for( int i = 0; i < contexts.length; i++ )
        {
	    if( contexts[i].context_id == TAG_CODE_SETS.value )
            {
                // TAG_CODE_SETS found, demarshall
                CDRInputStream is = 
                    new CDRInputStream( (org.omg.CORBA.ORB) null,
                                        contexts[i].context_data );
                is.openEncapsulatedArray();

                return CodeSetContextHelper.read( is );
            }
	}

	return null; 
    }
}







