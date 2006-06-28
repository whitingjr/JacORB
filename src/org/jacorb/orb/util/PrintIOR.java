/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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

package org.jacorb.orb.util;

import org.apache.avalon.framework.logger.Logger;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.jacorb.orb.CDRInputStream;
import org.jacorb.orb.ORBConstants;
import org.jacorb.orb.ParsedIOR;
import org.jacorb.orb.TaggedComponentList;
import org.jacorb.orb.giop.CodeSet;
import org.jacorb.orb.iiop.IIOPProfile;
import org.jacorb.orb.iiop.IIOPAddress;

import org.omg.CONV_FRAME.CodeSetComponentInfoHelper;
import org.omg.CSIIOP.CompoundSecMechList;
import org.omg.CSIIOP.CompoundSecMechListHelper;
import org.omg.CSIIOP.TAG_CSI_SEC_MECH_LIST;
import org.omg.CSIIOP.TAG_NULL_TAG;
import org.omg.CSIIOP.TAG_SECIOP_SEC_TRANS;
import org.omg.CSIIOP.TAG_TLS_SEC_TRANS;
import org.omg.CSIIOP.TLS_SEC_TRANS;
import org.omg.CSIIOP.TLS_SEC_TRANSHelper;
import org.omg.IOP.TAG_ALTERNATE_IIOP_ADDRESS;
import org.omg.IOP.TAG_CODE_SETS;
import org.omg.IOP.TAG_JAVA_CODEBASE;
import org.omg.IOP.TAG_ORB_TYPE;
import org.omg.IOP.TaggedComponent;
import org.omg.SSLIOP.TAG_SSL_SEC_TRANS;

/**
 * @author Gerald Brose
 * @version $Id$
 */

public class PrintIOR
{
    /**
     * entry point from the command line
     */

    public static void main(String args[])
    {
        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args,null);
        Logger logger =
            ((org.jacorb.orb.ORB)orb).getConfiguration().getNamedLogger("jacorb.print_ior");
        String line, iorString = null;

        if( args.length != 2)
        {
            System.err.println("Usage: java PrintIOR [ -i ior_str | -f filename ]"); // NOPMD
            System.exit( 1 );
        }

        if( args[0].equals("-f"))
        {
            try
            {
                BufferedReader br = new BufferedReader ( new FileReader( args[1] ), 2048 );
                line = br.readLine();
                if ( line != null )
                {
                    iorString = line;
                    while ( line != null )
                    {
                        line = br.readLine();
                        if ( line != null )
                        {
                            iorString = iorString + line;
                        }
                    }
                }
            }
            catch ( IOException ioe )
            {
                ioe.printStackTrace();
                System.exit(1);
            }
        }
        else if ( args[0].equals("-i"))
        {
            iorString = args[1];
        }
        else
        {
            System.err.println("Usage: java PrintIOR [ -i ior_str | -f filename ]"); // NOPMD
            System.exit( 1 );
        }

        if( logger.isDebugEnabled() )
        {
            logger.debug
            (
                "Under " +
                System.getProperty ("os.name") +
                " the encoding name is " +
                System.getProperty( "file.encoding" ) +
                " and the canonical encoding name is " +
                ( new java.io.OutputStreamWriter( new ByteArrayOutputStream () ) ).getEncoding()
            );
        }

        if( iorString.startsWith( "IOR:" ))
        {
            ParsedIOR pior = new ParsedIOR( iorString, orb, logger );
            printIOR(pior, orb);
        }
        else
        {
            println("Sorry, we only unparse IORs in the standard IOR URL scheme");
        }

        orb.shutdown(true);
    }

    private static void print(String mesg)
    {
        System.out.print(mesg); // NOPMD
    }

    private static void println(String mesg)
    {
        System.out.println(mesg); // NOPMD
    }

    private static void println()
    {
        System.out.println(); // NOPMD
    }

    /**
     * top-level
     */

    public static void printIOR( ParsedIOR pior, org.omg.CORBA.ORB orb)
    {
        org.omg.IOP.IOR ior = pior.getIOR();

        println("------IOR components-----");
        println("TypeId\t:\t" + ior.type_id );

        List profiles = pior.getProfiles();

        println("TAG_INTERNET_IOP Profiles:");
        for( int i = 0; i < profiles.size(); i++ )
        {
            print("\tProfile Id:  ");

            IIOPProfile profile = (IIOPProfile)profiles.get(i);
            println("\tIIOP Version :  " +
                               (int)profile.version().major + "." +
                               (int)profile.version().minor);

            println("\tHost\t:\t" +
                               ((IIOPAddress)profile.getAddress()).getOriginalHost());
            int port = ((IIOPAddress)profile.getAddress()).getPort();
            if( port < 0 )
            {
                port += 65536;
            }

            println("\tPort\t:\t" + port );
            try
            {
                println("\tObject key (URL):      " +
                                   CorbaLoc.parseKey( pior.get_object_key()));
            }
            catch( Exception e )
            {
                // ignore, object key not in url format
            }
            print("\tObject key (hex):    0x" );
            dumpHex( pior.get_object_key() );
            println();

            if ( profile.version().minor >= ( char ) 1 )
            {
                if( profile.getComponents().size() > 0 )
                {
                    println("\t-- Found " +
                                       profile.getComponents().size() +
                                       " Tagged Components--" );
                }

                printTaggedComponents( profile.getComponents().asArray() );
            }
            print("\n");
        }

        TaggedComponentList multiple_components = pior.getMultipleComponents();

        if( multiple_components.size() > 0 )
        {
            println("Components in MULTIPLE_COMPONENTS profile: " +
                               multiple_components.size() );

            printTaggedComponents( multiple_components.asArray() );
        }

    }

    /**
     * Iterates over a tagged IOP components and prints those that are
     * recognized.
     */

    private static void printTaggedComponents( TaggedComponent[] taggedComponents )
    {
        for( int i = 0; i < taggedComponents.length; i++ )
        {
            switch( taggedComponents[i].tag )
            {
                case TAG_SSL_SEC_TRANS.value:
                println("\t#"+ i + ": TAG_SSL_SEC_TRANS");
                printSSLTaggedComponent( taggedComponents[i] );
                break;
                case TAG_CSI_SEC_MECH_LIST.value:
                println("\t#"+ i + ": TAG_CSI_SEC_MECH_LIST");
                printCSIMechComponent( taggedComponents[i] );
                break;
                case TAG_SECIOP_SEC_TRANS.value:
                println("\t#"+ i + ": TAG_SECIOP_SEC_TRANS");
                break;
                case TAG_ALTERNATE_IIOP_ADDRESS.value:
                println("\t#"+ i + ": TAG_ALTERNATE_IIOP_ADDRESS");
                printAlternateAddress(taggedComponents[i]);
                break;
                case TAG_CODE_SETS.value:
                println("\t#"+ i + ": TAG_CODE_SETS");
                printCodeSetComponent( taggedComponents[i] );
                break;
                case TAG_JAVA_CODEBASE.value:
                println("\t#"+ i + ": TAG_JAVA_CODEBASE");
                printJavaCodebaseComponent( taggedComponents[i] );
                break;
                case TAG_ORB_TYPE.value:
                println("\t#"+ i + ": TAG_ORB_TYPE");
                printOrbTypeComponent( taggedComponents[i] );
                break;
                case TAG_NULL_TAG.value:
                println("\t#"+ i + ": TAG_NULL_TAG");
                break;
                default:
                println("\tUnknown tag : " +
                                   taggedComponents[i].tag);
            }
        }
    }

    private static void printCSIMechComponent( TaggedComponent taggedComponent )
    {
        CDRInputStream is =
        new CDRInputStream( (org.omg.CORBA.ORB)null,
                            taggedComponent.component_data);

        is.openEncapsulatedArray();
        CompoundSecMechList csmList = CompoundSecMechListHelper.read( is );

        if( csmList!= null )
        {
            println("\t\tis stateful: " + csmList.stateful );
            for( int i = 0; i < csmList.mechanism_list.length; i++ )
            {
                println("\t\tCompoundSecMech #" + i);
                println("\t\t\ttarget_requires: " +
                                   csmList.mechanism_list[i].target_requires );
                print("\t\t\ttransport mechanism tag: ");
                switch( csmList.mechanism_list[i].transport_mech.tag )
                {
                    case TAG_TLS_SEC_TRANS.value:
                    println("TAG_TLS_SEC_TRANS");
                    printTlsSecTrans(csmList.mechanism_list[i].transport_mech.component_data);
                    break;
                    case TAG_NULL_TAG.value:
                    println("TAG_NULL_TAG");
                    break;
                    default:
                    println("Unknown tag : " +
                                       csmList.mechanism_list[i].transport_mech.tag );
                }
                println("\t\t\tAS_ContextSec target_supports: " + csmList.mechanism_list[i].as_context_mech.target_supports );
                println("\t\t\tAS_ContextSec target_requires: " + csmList.mechanism_list[i].as_context_mech.target_requires );
                print("\t\t\tAS_ContextSec mech: " );
                dumpHex(csmList.mechanism_list[i].as_context_mech.client_authentication_mech);
                println();
                print("\t\t\tAS_ContextSec target_name: " );
                printNTExportedName(csmList.mechanism_list[i].as_context_mech.target_name);
                //}
                println("\t\t\tSAS_ContextSec target_supports: " + csmList.mechanism_list[i].sas_context_mech.target_supports );
                println("\t\t\tSAS_ContextSec target_requires: " + csmList.mechanism_list[i].sas_context_mech.target_requires );

                for (int j = 0; j < csmList.mechanism_list[i].sas_context_mech.supported_naming_mechanisms.length; j++) {
                    print("\t\t\tSAS_ContextSec Naming mech: " );
                    dumpHex(csmList.mechanism_list[i].sas_context_mech.supported_naming_mechanisms[j]);
                    println();
                }
                println("\t\t\tSAS_ContextSec Naming types: " + csmList.mechanism_list[i].sas_context_mech.supported_identity_types);
                println();
            }
        }
    }

    private static void printNTExportedName(byte[] nameData) {
        // check for token identifier
        if (nameData.length < 2 || nameData[0] != 0x04 || nameData[1] != 0x01) {
            dumpHex(nameData);
            println();
            return;
        }

        // get mech length
        int mechLen = (nameData[2] << 8) + nameData[3];
        if (mechLen > (nameData.length - 8)) {
            dumpHex(nameData);
            println();
            return;
        }

        // get name length
        int nameLen = (nameData[mechLen + 4] << 24) +
                      (nameData[mechLen + 5] << 16) +
                      (nameData[mechLen + 6] << 8) +
                      (nameData[mechLen + 7]);
        if ((mechLen + nameLen) > (nameData.length - 8)) {
            dumpHex(nameData);
            println();
            return;
        }
        byte[] name = new byte[nameLen];
        System.arraycopy(nameData, mechLen + 8, name, 0, nameLen);
        println(new String(name));
    }

    private static void printTlsSecTrans(byte[] tagData) {
        CDRInputStream in = new CDRInputStream( (org.omg.CORBA.ORB)null, tagData );
        try
        {
            in.openEncapsulatedArray();
            TLS_SEC_TRANS tls = TLS_SEC_TRANSHelper.read( in );
            println("\t\t\tTLS SEC TRANS target requires: " + tls.target_requires);
            println("\t\t\tTLS SEC TRANS target supports: " + tls.target_supports);
            for (int i = 0; i < tls.addresses.length; i++) {
                int ssl_port = tls.addresses[i].port;
                if( ssl_port < 0 )
                {
                    ssl_port += 65536;
                }
                println("\t\t\tTLS SEC TRANS address: " + tls.addresses[i].host_name+":"+ssl_port);
            }
        }
        catch ( Exception ex )
        {
            print("\t\t\tTLS SEC TRANS: " );
            dumpHex(tagData);
            println();
        }
    }

    private static void printCodeSetComponent( TaggedComponent taggedComponent )
    {
        CDRInputStream is =
        new CDRInputStream( (org.omg.CORBA.ORB)null,
                            taggedComponent.component_data);

        is.openEncapsulatedArray();

        org.omg.CONV_FRAME.CodeSetComponentInfo codeSet =
        CodeSetComponentInfoHelper.read( is );

        if( codeSet != null )
        {
            println("\t\tForChar native code set Id: " +
                               CodeSet.csName(codeSet.ForCharData.native_code_set ));
            print("\t\tChar Conversion Code Sets: ");
            for( int ji = 0; ji < codeSet.ForCharData.conversion_code_sets.length; ji++ )
            {
                println( CodeSet.csName( codeSet.ForCharData.conversion_code_sets[ji] ) );

                if( ji < (codeSet.ForCharData.conversion_code_sets.length - 1) )
                {
                    print( ", " );
                }
            }
            if (codeSet.ForCharData.conversion_code_sets.length == 0 )
            {
                print("\n");
            }

            println("\t\tForWChar native code set Id: " +
                               CodeSet.csName(codeSet.ForWcharData.native_code_set ));
            print("\t\tWChar Conversion Code Sets: ");
            for( int ji = 0; ji < codeSet.ForWcharData.conversion_code_sets.length; ji++ )
            {
                println( CodeSet.csName( codeSet.ForWcharData.conversion_code_sets[ji] ));

                if( ji < (codeSet.ForWcharData.conversion_code_sets.length - 1) )
                {
                    print( ", " );
                }
            }
            if (codeSet.ForCharData.conversion_code_sets.length == 0 )
            {
                print("\n");
            }
        }
    }

    private static void printSSLTaggedComponent( TaggedComponent taggedComponent )
    {
        org.omg.SSLIOP.SSL  ssl = null;
        if( taggedComponent.tag == 20 )
        {
            CDRInputStream in =
            new CDRInputStream( (org.omg.CORBA.ORB)null,
                                taggedComponent.component_data );
            try
            {
                in.openEncapsulatedArray();
                ssl =  org.omg.SSLIOP.SSLHelper.read( in );
            }
            catch ( Exception ex )
            {
                return;
            }
            int ssl_port = ssl.port;
            if( ssl_port < 0 )
            {
                ssl_port += 65536;
            }

            print( "\t\ttarget_supports\t:\t" );
            //dump               ( ssl.target_supports );
            decodeAssociationOption( ssl.target_supports );
            println();
            print( "\t\ttarget_requires\t:\t" );
            //dump               ( ssl.target_requires );
            decodeAssociationOption( ssl.target_requires );
            println();
            println( "\t\tSSL Port\t:\t" + ssl_port );

        }
    }
    private static void decodeAssociationOption( int option )
    {
        boolean first = true;

        if( (option & org.omg.Security.NoProtection.value) != 0 )
        {
            if( ! first )
            {
                print( ", " );
            }

            print( "NoProtection" );

            first = false;
        }

        if( (option & org.omg.Security.Integrity.value) != 0 )
        {
            if( ! first )
            {
                print( ", " );
            }

            print( "Integrity" );

            first = false;
        }

        if( (option & org.omg.Security.Confidentiality.value) != 0 )
        {
            if( ! first )
            {
                print( ", " );
            }

            print( "Confidentiality" );

            first = false;
        }

        if( (option & org.omg.Security.DetectReplay.value) != 0 )
        {
            if( ! first )
            {
                print( ", " );
            }

            print( "DetectReplay" );

            first = false;
        }

        if( (option & org.omg.Security.DetectMisordering.value) != 0 )
        {
            if( ! first )
            {
                print( ", " );
            }

            print( "DetectMisordering" );

            first = false;
        }

        if( (option & org.omg.Security.EstablishTrustInTarget.value) != 0 )
        {
            if( ! first )
            {
                print( ", " );
            }

            print( "EstablishTrustInTarget" );

            first = false;
        }

        if( (option & org.omg.Security.EstablishTrustInClient.value) != 0 )
        {
            if( ! first )
            {
                print( ", " );
            }

            print( "EstablishTrustInClient" );

            first = false;
        }

        if( (option & org.omg.Security.NoDelegation.value) != 0 )
        {
            if( ! first )
            {
                print( ", " );
            }

            print( "NoDelegation" );

            first = false;
        }

        if( (option & org.omg.Security.SimpleDelegation.value) != 0 )
        {
            if( ! first )
            {
                print( ", " );
            }

            print( "SimpleDelegation" );

            first = false;
        }

        if( (option & org.omg.Security.CompositeDelegation.value) != 0 )
        {
            if( ! first )
            {
                print( ", " );
            }

            print( "CompositeDelegation" );

            first = false;
        }
    }


    private static void printJavaCodebaseComponent( TaggedComponent taggedComponent )
    {
        final CDRInputStream in = new CDRInputStream( (org.omg.CORBA.ORB)null,
                            taggedComponent.component_data );

        try
        {
            in.openEncapsulatedArray();
            String codebase = in.read_string();

            println( "\t\tCodebase: " + codebase );
        }
        finally
        {
            in.close();
        }
    }

    private static void printOrbTypeComponent (TaggedComponent taggedComponent)
    {
        final CDRInputStream is = new CDRInputStream ((org.omg.CORBA.ORB)null, taggedComponent.component_data );

        try
        {
            is.openEncapsulatedArray ();
            int type = is.read_long ();

            print ( "\t\tType: " + type);
            if (type == ORBConstants.JACORB_ORB_ID)
            {
                println (" (JacORB)");
            }
            else
            {
                println (" (Foreign)");
            }
        }
        finally
        {
            is.close();
        }
    }

    private static void printAlternateAddress(TaggedComponent taggedComponent)
    {
        final CDRInputStream is = new CDRInputStream((org.omg.CORBA.ORB)null, taggedComponent.component_data);

        try
        {
            is.openEncapsulatedArray();
            println("\t\tAddress: " + IIOPAddress.read(is));
        }
        finally
        {
            is.close();
        }
    }

    public static void dumpHex(byte values[])
    {
        for (int i=0; i<values.length; i++)
        {
            int n1 = (values[i] & 0xff) / 16;
            int n2 = (values[i] & 0xff) % 16;
            char c1 = (char)(n1>9 ? ('A'+(n1-10)) : ('0'+n1));
            char c2 = (char)(n2>9 ? ('A'+(n2-10)) : ('0'+n2));
            print( c1 + (c2 + " "));
        }
    }

    private static final char hexDigit[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                               'A', 'B', 'C', 'D', 'E', 'F'
    };

    public static void dump ( byte values[] ) {
        for ( int i = 0; i < values.length; i++ )
        {
            dump ( values[ i ] );
            print( " " );
        }
    }

    public static void dump ( int values[] ) {
        for ( int i = 0; i < values.length; i++ )
        {
            dump ( values[ i ] );
            print( " " );
        }
    }

    public static void dump ( byte value ) {
        print( ""
                                    + hexDigit[ ( value >>  4 ) & 0x0f ]
                                    + hexDigit[ ( value       ) & 0x0f ]
                                  );
    }

    public static void dump ( short value ) {
        print( ""
                                    + hexDigit[ ( value >> 12 ) & 0x0f ]
                                    + hexDigit[ ( value >>  9 ) & 0x0f ]
                                    + hexDigit[ ( value >>  4 ) & 0x0f ]
                                    + hexDigit[ ( value       ) & 0x0f ]
                                  );
    }

    public static void dump ( int value ) {
        print( ""
                                    + hexDigit[ ( value >> 28 ) & 0x0f ]
                                    + hexDigit[ ( value >> 24 ) & 0x0f ]
                                    + hexDigit[ ( value >> 20 ) & 0x0f ]
                                    + hexDigit[ ( value >> 16 ) & 0x0f ]
                                    + hexDigit[ ( value >> 12 ) & 0x0f ]
                                    + hexDigit[ ( value >>  8 ) & 0x0f ]
                                    + hexDigit[ ( value >>  4 ) & 0x0f ]
                                    + hexDigit[ ( value       ) & 0x0f ]
                                  );
    }

    public static void dump ( byte values[], boolean withChar )
    {
        char c;
        int len = values.length;
        for ( int i = 0; i < len; i++ ) {
            if ( 0 == i % 16 )
            {
                println();
            }
            if ( values[ i ] > ( byte ) 31 && values[ i ] < ( byte ) 127 )
            {
                c = ( char ) values[ i ];
            }
            else {
                c = ' ';
            }
            print( ":"
                                        + hexDigit[ ( values [ i ] >> 4 ) & 0x0f ]
                                        + hexDigit[ values [ i ] & 0x0f ]
                                        + " " + c
                                      );
        }
    }
}
