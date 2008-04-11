/*
    This Module is a Maven plugin to convert an xml to a java resource bundle
    Copyright (C) 2008  Shams Mahmood

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.escenic.maven.plugin.xml.parser;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.smartitengineering.xml2props.util.LogUtil;
import com.smartitengineering.xml2props.xml.parser.InputParser;
import com.smartitengineering.xml2props.xml.parser.XmlXomInputParser;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:shams.mahmood@gmail.com">Shams Mahmood</a>
 * @created Apr 5, 2008 10:42:27 AM
 */

public class XmlXomInputParserTest
    extends TestCase
{
    private InputParser mInputParser;

    private File mInputFile;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp()
        throws Exception
    {
        super.setUp();

        if ( mInputParser == null )
        {
            mInputParser = new XmlXomInputParser();
            LogUtil.debug( null, "Using Parser: " + mInputParser );
        }
        if ( mInputFile == null )
        {
            mInputFile = new File( "target/test-classes", "input-1.xml" );
            LogUtil.debug( null, "Test File: " + mInputFile.getAbsolutePath() );
        }
    }

    public void testFileParsing()
        throws IOException
    {
        LogUtil.debug( null, "XmlXomInputParserTest.testFileParsing() starts..." );

        final Map<Locale, Properties> eParsedInput = mInputParser.parseInput( mInputFile );
        assertNotNull( eParsedInput );
        LogUtil.debug( null, "Parsed Input: " );

        for ( Iterator i = eParsedInput.keySet().iterator(); i.hasNext(); )
        {
            Locale eLoopLocale = (Locale) i.next();
            Properties eLoopProperties = (Properties) eParsedInput.get( eLoopLocale );

            LogUtil.debug( null, "  Locale: " + eLoopLocale );
            LogUtil.debug( null, "     " + eLoopProperties );
        }

        LogUtil.debug( null, "XmlXomInputParserTest.testFileParsing() ends." );
    }

    public void testParsedContentLocales()
        throws IOException
    {
        LogUtil.debug( null, "XmlXomInputParserTest.testParsedContentLocales() starts..." );

        final Map<Locale, Properties> eParsedInput = mInputParser.parseInput( mInputFile );
        assertNotNull( eParsedInput );

        // Test valid Locale's were parsed
        assertEquals( eParsedInput.keySet().size(), 7 );
        final Set<Locale> eKeySet = eParsedInput.keySet();
        assertTrue( eKeySet.contains( null ) );
        assertTrue( eKeySet.contains( new Locale( "bn" ) ) );
        assertTrue( eKeySet.contains( new Locale( "bn", "BD" ) ) );
        assertTrue( eKeySet.contains( new Locale( "en" ) ) );
        assertTrue( eKeySet.contains( new Locale( "en", "US" ) ) );
        assertTrue( eKeySet.contains( new Locale( "", "US" ) ) );
        assertTrue( eKeySet.contains( new Locale( "no" ) ) );

        // Test invalid Locales were not parsed
        assertTrue( !eKeySet.contains( new Locale( "bn", "IN" ) ) );
        assertTrue( !eKeySet.contains( new Locale( "en", "UK" ) ) );
        assertTrue( !eKeySet.contains( new Locale( "no", "NO" ) ) );

        // Test correct number of entries were parsed
        int ePropertiesSizeSum = 0;
        for ( Iterator i = eParsedInput.keySet().iterator(); i.hasNext(); )
        {
            Locale eLoopLocale = (Locale) i.next();
            Properties eLoopProperties = (Properties) eParsedInput.get( eLoopLocale );
            assertNotNull( eLoopProperties );
            ePropertiesSizeSum += eLoopProperties.size();
        }
        LogUtil.debug( null, "Total Properties created: " + ePropertiesSizeSum );
        assertEquals( ePropertiesSizeSum, 14 );

        LogUtil.debug( null, "XmlXomInputParserTest.testParsedContentLocales() ends." );
    }

    public void testParsedContentProperties()
        throws IOException
    {
        LogUtil.debug( null, "XmlXomInputParserTest.testParsedContentProperties() starts..." );

        final Map<Locale, Properties> eParsedInput = mInputParser.parseInput( mInputFile );
        assertNotNull( eParsedInput );

        LogUtil.debug( null, "Checking default properties" );
        final Properties eDefaultProperties = (Properties) eParsedInput.get( null );
        assertNotNull( eDefaultProperties );
        assertEquals( eDefaultProperties.size(), 3 );
        assertTrue( eDefaultProperties.containsKey( "name" ) );
        assertTrue( eDefaultProperties.containsKey( "type" ) );
        assertTrue( eDefaultProperties.containsKey( "title" ) );

        LogUtil.debug( null, "Checking bn_BD properties" );
        final Properties eBnBdProperties = (Properties) eParsedInput.get( new Locale( "bn", "BD" ) );
        assertNotNull( eBnBdProperties );
        assertEquals( eBnBdProperties.size(), 2 );
        assertTrue( eBnBdProperties.containsKey( "article" ) );
        assertTrue( eBnBdProperties.containsKey( "title" ) );

        LogUtil.debug( null, "Checking bn properties" );
        final Properties eBnProperties = (Properties) eParsedInput.get( new Locale( "bn" ) );
        assertNotNull( eBnProperties );
        assertEquals( eBnProperties.size(), 2 );
        assertTrue( eBnProperties.containsKey( "article" ) );
        assertTrue( eBnProperties.containsKey( "type" ) );

        LogUtil.debug( null, "Checking en properties" );
        final Properties eEnProperties = (Properties) eParsedInput.get( new Locale( "en" ) );
        assertNotNull( eEnProperties );
        assertEquals( eEnProperties.size(), 1 );
        assertTrue( eEnProperties.containsKey( "article" ) );

        LogUtil.debug( null, "Checking en_US properties" );
        final Properties eEnUSProperties = (Properties) eParsedInput.get( new Locale( "en", "US" ) );
        LogUtil.debug( null, "Size: " + eEnUSProperties.size() + " : " + eEnUSProperties );
        assertNotNull( eEnUSProperties );
        assertEquals( eEnUSProperties.size(), 2 );
        assertTrue( eEnUSProperties.containsKey( "article" ) );
        assertTrue( eEnUSProperties.containsKey( "name" ) );

        LogUtil.debug( null, "XmlXomInputParserTest.testParsedContentProperties() ends." );
    }

}
