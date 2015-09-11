//package rasdaman.jetty.embedded;

/*
* This file is part of rasdaman community.
*
* Rasdaman community is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Rasdaman community is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
*
* Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
rasdaman GmbH.
*
* For more information please see <http://www.rasdaman.org>
* or contact Peter Baumann via <baumann@rasdaman.com>.
*/
/*************************************************************

*/


import java.io.File;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import java.lang.management.ManagementFactory;

//import java.nio.file.Path;
//import java.nio.file.Paths; // get Current Directory

import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

import org.eclipse.jetty.server.handler.HandlerCollection; // for multi handle .war files

public class start_petascope
{

    /**
     * Return petascope Port from $RMANHOME/etc/petascope.properties (jetty_port=8080), default
     *
     * @param String RMANHOME: Path to $RMANHOME
     * @return int value (portJetty)
     */
    public static int getJettyPort(String RMANHOME)
    {
	String petascopePropertiesFile = RMANHOME + "/etc/petascope.properties";

	// List Command to execute

	String jettyPort = ""; // Port of Jetty, Default 8080 from $RMANHOME/etc/petascope.properties

	try {

		String[] getJettyPort = {
			"/bin/sh",
			"-c",
			"cat '" + RMANHOME + "/etc/petascope.properties' | grep 'jetty_port' 						     | awk -F\"=\" '{print $2}'"
		};

		System.out.println("cat '" + RMANHOME + "/etc/petascope.properties' | grep 'jetty_port' | awk -F\"=\" '{print $2}'");

		Process p = Runtime.getRuntime().exec(getJettyPort);
		BufferedReader in =
		    new BufferedReader(new InputStreamReader(p.getInputStream()));

		String currentLine = "";


		while (( currentLine = in.readLine()) != null) {
			jettyPort = jettyPort.trim() + currentLine.trim();
		}

	} catch (Exception ex) {
	   ex.printStackTrace();
        }

	//System.out.println("Port: " + jettyPort);
	// Get the Jetty Port user want to start petascope when run with jetty
	return Integer.parseInt(jettyPort);
    }


    /**
     * Return Jetty Extracted Folder from $RMANHOME/share/rasdaman/war/jetty_tmp (default)
     *
     * @param String RMANHOME: Path to $RMANHOME
     * @return String value (User Defined Path - If it is not NULL)
     */

    public static String getJettyExtractedPath(String RMANHOME)
    {
	String petascopePropertiesFile = RMANHOME + "etc/petascope.properties";

	// List Command to execute

	String jettyExtractedPath = ""; // Port of Jetty, Default 8080 from $RMANHOME/etc/petascope.properties

	try {

		String[] getJettyExtractedPath = {
			"/bin/sh",
			"-c",
			"cat '" + RMANHOME + "etc/petascope.properties' | grep 'jetty_extracted_path' 						     | awk -F\"=\" '{print $2}'"
		};

		System.out.println("cat '" + RMANHOME + "etc/petascope.properties' | grep 'jetty_extracted_path' | awk -F\"=\" '{print $2}'");

		Process p = Runtime.getRuntime().exec(getJettyExtractedPath);
		BufferedReader in =
		    new BufferedReader(new InputStreamReader(p.getInputStream()));

		String currentLine = "";


		while (( currentLine = in.readLine()) != null) {
			jettyExtractedPath = jettyExtractedPath.trim() + currentLine.trim(); // get Extracted Path user want to extract Petascope (rasdaman.war and def.war)
		}

	} catch (Exception ex) {
	   ex.printStackTrace();
        }

	//System.out.println("Jetty Extracted Path: " + jettyExtractedPath);

	if(jettyExtractedPath == null || jettyExtractedPath.isEmpty()) // If Path is NULL then use default extracted Folder $RMANHOME/share/rasdaman/war/jetty_tmp
	{
	   jettyExtractedPath = RMANHOME + "share/rasdaman/war/jetty_tmp";
	   System.out.println("jetty_extracted_path for .war file is default folder $RMANHOME/share/rasdaman/war/jetty_tmp");
	}

	// create folder for extracted jetty file before return the temporary path
	File theDir = new File(jettyExtractedPath);

	// if the directory does not exist, create it
	if (!theDir.exists()) {
	   System.out.println("Temporary folder " + jettyExtractedPath + " is creating..........");
	   theDir.mkdir();
	}


	//System.out.println("Port: " + jettyPort);
	// Get the Jetty Port user want to start petascope when run with jetty
	return jettyExtractedPath;
    }

    /*
    *  Print how to start_petascope.java usage
       @binDir: Installation folder of Rasdaman
       @return no value
    */
    public static void printUsage(String binDir)
    {
       System.out.println("Petascope start with Jetty Usage: java -cp " + binDir + "jetty.jar: start_petascope PATH_TO_YOUR_RASDAMAN_INSTALL_DIRECTORY");
       System.out.println("Please check the script start_rasdaman.sh for more detail!");
       System.out.println("Petascope with embedded Jetty is starting....................");
    }

    /**
     * Return petascope Port from $RMANHOME/etc/petascope.properties (jetty_port=8080), default
     *
     * @param String RMANHOME: Path to $RMANHOME
     * @return int value (portJetty)
     */
    public static void main( String[] args ) throws Exception
    {
        // Create a basic jetty server object that will listen on port 8080.
        // Note that if you set this to port 0 then a randomly available port
        // will be assigned that you can either look in the logs for the port,
        // or programmatically obtain it for use in test cases.

	// Note: normally this will never be 0 but in some case some one delete the argument in start_rasdaman.sh so it could not run
        if(args.length == 0)
	{
	    System.out.println("We need the @binDir where user installed Rasdaman folder with ./configure --prefix=PATH_TO_YOUR_RASDAMAN_INSTALL_DIRECTORY");
	    System.out.println("Without this, we could not start Petascope with Jetty. Please view the start_rasdaman.sh file to add this folder after start_petascope");
	    System.out.println("NOTE: Petascope with Jetty could not start.....");
	    System.exit(0);
	}

	// Now, could start run the Jetty with argument ./configure --prefix
	String RMANHOME = args[0];//System.getenv("RMANHOME");

	printUsage(RMANHOME);

        Server server = new Server(getJettyPort(RMANHOME));

        // Setup JMX
        MBeanContainer mbContainer = new MBeanContainer(
                ManagementFactory.getPlatformMBeanServer());
        server.addBean(mbContainer);

        // The WebAppContext is the entity that controls the environment in
        // which a web application lives and breathes. In this example the
        // context path is being set to "/" so it is suitable for serving root
        // context requests and then we see it setting the location of the war.
        // A whole host of other configurations are available, ranging from
        // configuring to support annotation scanning in the webapp (through
        // PlusConfiguration) to choosing where the webapp will unpack itself.

	// extract file to folder temp (NOTE: without this, Jetty will extract .war files to new folders in /tmp each time it starts)
	File tmpDirectoryRasdaman = new File(getJettyExtractedPath(RMANHOME) + "/rasdaman_war");

	//String currentDirectory = Paths.get(".").toAbsolutePath().normalize().toString(); // Depend on $RMANHOME when install

	// GET JETTY WITH PETASCOPE INSTALLED FOLDER
	String JETTY_PETASCOPE_PATH = RMANHOME; // same with Rasdaman install folder
	String currentDirectory = JETTY_PETASCOPE_PATH + "/share/rasdaman/war";

	//System.out.println(currentDirectory);

	// 1. Rasdaman.war
        WebAppContext webappRasdaman = new WebAppContext();
        webappRasdaman.setContextPath("/rasdaman");
        File warFileRasdaman = new File(
                currentDirectory + "/rasdaman.war");

	//System.out.println(warFileRasdaman.getAbsolutePath());

	if (!warFileRasdaman.exists())
        {
            throw new RuntimeException( "Unable to find WAR File: "
                    + warFileRasdaman.getAbsolutePath() );
        }
        webappRasdaman.setWar(warFileRasdaman.getAbsolutePath());

	webappRasdaman.setTempDirectory(tmpDirectoryRasdaman);


	// 2. Def.war

	File tmpDirectoryDef = new File(getJettyExtractedPath(RMANHOME) + "/def_war");

        WebAppContext webappDef = new WebAppContext();
        webappDef.setContextPath("/def");
        File warFileDef = new File(
              currentDirectory + "/def.war");

	if (!warFileDef.exists())
        {
            throw new RuntimeException( "Unable to find WAR File: "
                    + warFileDef.getAbsolutePath() );
        }
        webappDef.setWar(warFileDef.getAbsolutePath());

	webappDef.setTempDirectory(tmpDirectoryDef);




        /*Configuration.ClassList classlist = Configuration.ClassList
                .setServerDefault( server );
	*/
        /*
	NOTE_19/05/2015: Add it then error Exception in thread "main" java.lang.NoClassDefFoundError: org/objectweb/asm/ClassVisitor, but test is ok so don't need it.

	classlist.addBefore(
                "org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
                "org.eclipse.jetty.annotations.AnnotationConfiguration" );
	*/

	// 3. Set Server Handler for Rasdaman.war and Def.war
	HandlerCollection handlers = new HandlerCollection();
	handlers.addHandler( webappRasdaman );
	handlers.addHandler( webappDef );

	// Add handlers to server
        server.setHandler( handlers );

        // Start things up!
        server.start();

        // The use of server.join() the will make the current thread join and
        // wait until the server is done executing.
        // See http://docs.oracle.com/javase/7/docs/api/java/lang/Thread.html#join()
        //server.join();
    }
}
