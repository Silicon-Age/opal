package com.opal.creator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.Validate;
import org.w3c.dom.Document;

import com.opal.creator.database.RelationalDatabaseAdapter;

public class XMLCreator {
	
	private final String myFilename;
	private final String mySourceDirectory;
	
	private final String myDatabaseUsername; // may be null
	private final String myDatabasePassword; // may be null
	
	public XMLCreator(String argFilename, String argSourceDirectory, String argDatabaseUsername, String argDatabasePassword) {
		super();
		
		myFilename = Validate.notNull(argFilename);
		mySourceDirectory = Validate.notNull(argSourceDirectory);
		myDatabaseUsername = argDatabaseUsername; // may be null
		myDatabasePassword = argDatabasePassword; // may be null
	}
	
	public XMLCreator(String argFilename, String argSourceDirectory) {
		this(argFilename, argSourceDirectory, null, null);
	}
	
	protected String getFilename() {
		return myFilename;
	}
	
	protected String getSourceDirectory() {
		return mySourceDirectory;
	}
	
	protected String getDatabaseUsername() {
		return myDatabaseUsername;
	}
	
	protected String getDatabasePassword() {
		return myDatabasePassword;
	}
	
	public static void main(String[] argS) {
		Validate.isTrue(argS.length >= 2, "XMLCreator requires two command line arguments: the configuration file and the source directory. After that, you may optionally give the database username, and the database password.");
		
		String lclFilename = argS[0];
		Validate.notNull(lclFilename, "No configuration file name was provided; it should be the first command line argument");
		
		String lclSourceDirectory = argS[1];
		Validate.notNull(lclSourceDirectory, "No source directory was provided; it should be the second command line argument");
		
		String lclDatabaseUsername = argS.length > 2 ? argS[2] : null;
		String lclDatabasePassword = argS.length > 3 ? argS[3] : null;
		
		try {
			XMLCreator lclXMLCreator = new XMLCreator(lclFilename, lclSourceDirectory, lclDatabaseUsername, lclDatabasePassword);
			
			lclXMLCreator.process();
		} catch (Throwable lclT) {
			lclT.printStackTrace(System.err);
		}
		
	}
	
	public void process() throws IOException {
		File lclFile = new File(getFilename());
		if (!lclFile.exists()) {
			throw new IOException("Configuration file " + getFilename() + " not found.");
		}
		
		try (FileInputStream lclFIS = new FileInputStream(lclFile)) {
			
			DocumentBuilderFactory lclFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder lclBuilder = lclFactory.newDocumentBuilder();
			
			Document lclD = lclBuilder.parse(lclFIS);
			
			OpalParseContext lclOPC = new OpalParseContext(getDatabaseUsername(), getDatabasePassword());
			lclOPC.setElementPackageName("com.opal.creator");
			lclOPC.setSourceDirectory(getSourceDirectory());
			
			lclOPC.parse(lclD);
			
			RelationalDatabaseAdapter lclRDA = lclOPC.getRelationalDatabaseAdapter();
			
			/* Now we have all the MappedClasses */
			
			lclRDA.generateClasses(lclOPC);
		} catch (Exception lclE) {
			lclE.printStackTrace(System.err);
		}
	}
	
	protected static void complain(MessageLevel argML, String argMessage) {
		complain(argML, null, argMessage);
	}
	
	protected static void complain(MessageLevel argML, MappedClass argMC, String argMessage) {
		if (argML == null) {
			complain(MessageLevel.Error, argMC, "Message generated with no MessageLevel (\"" + argMessage + "\")"); 
		} else {
			MessageLevel argThreshold = MessageLevel.Warning;
			if (argMC != null) {
				argThreshold = argMC.getMessageLevel();
			} else {
				argThreshold = MessageLevel.Warning;
			}
			if (argML.compareTo(argThreshold) >= 0) {
				System.out.println(argML.name() + ": [" + argMC + "] " + argMessage);
			}
		}
	}
	
}
