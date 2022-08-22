package com.opal.creator;

//import java.util.Iterator;

import javax.sql.DataSource;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import com.opal.creator.database.mysql.MySQLAdapter;
import com.opal.creator.database.oracle.OracleAdapter;
import com.opal.creator.database.postgres.PostgresAdapter;
import com.opal.creator.database.sqlserver.SQLServerAdapter;
import com.opal.creator.database.sybase.SybaseAdapter;

import com.siliconage.database.DirectConnectionPoolFactory;

public class Database extends OpalXMLElement {
	public Database(OpalXMLElement argParent, Node argNode) {
		super(argParent, argNode);
	}
	
	@Override
	protected void preChildren(OpalParseContext argContext) {
		/* TODO:  We now have two ConnectionPools.  This is going to get hopelessly confused. */
		
		String lclDriverClassName = getRequiredAttributeValue("Driver");
		try {
			Class.forName(lclDriverClassName);
		} catch (ClassNotFoundException lclE) {
			throw new RuntimeException("Could not find JDBC driver \"" + lclDriverClassName + "\"", lclE);
		}
		
		DataSource lclDataSource;
		String lclUrl = getRequiredAttributeValue("Url");
		
		// Command-line arguments for the database username and password override everything else.
		String lclDatabaseUsername = argContext.getDatabaseUsername();
		if (lclDatabaseUsername == null) {
			lclDatabaseUsername = getAttributeValue("User");
		}
		
		String lclDatabasePassword = argContext.getDatabasePassword();
		if (lclDatabasePassword == null) {
			lclDatabasePassword = getAttributeValue("Password");
		}
		
		// lclDatabaseUsername and lclDatabasePassword could still be null, in which case lclUrl better have that information.
		if (lclDatabaseUsername != null && lclDatabasePassword != null) {
			lclDataSource = DirectConnectionPoolFactory.getInstance().create(lclUrl, lclDatabaseUsername, lclDatabasePassword);
		} else {
			lclDataSource = DirectConnectionPoolFactory.getInstance().create(lclUrl);
		}
		
		if (lclUrl.startsWith("jdbc:oracle:")) {
			argContext.setRelationalDatabaseAdapter(new OracleAdapter(lclDataSource));
		} else if (lclUrl.startsWith("jdbc:microsoft:")) {
			argContext.setRelationalDatabaseAdapter(new SQLServerAdapter(lclDataSource));
		} else if (lclUrl.startsWith("jdbc:jtds:")) {
			argContext.setRelationalDatabaseAdapter(new SQLServerAdapter(lclDataSource));
		} else if (lclUrl.startsWith("jdbc:sybase:")) {
			argContext.setRelationalDatabaseAdapter(new SybaseAdapter(lclDataSource));
		} else if (lclUrl.startsWith("jdbc:mysql:")) {
			argContext.setRelationalDatabaseAdapter(new MySQLAdapter(lclDataSource));
		} else if (lclUrl.startsWith("jdbc:postgresql:")) {
			argContext.setRelationalDatabaseAdapter(new PostgresAdapter(lclDataSource));
		} else {
			throw new IllegalStateException("Unable to determine correct RelationalDatabaseAdapter for the JDBC URL.");
		}

		String lclJNDIName = getAttributeValue("JNDIName");
		if (lclJNDIName != null) {
			argContext.getPoolMap().put(OpalParseContext.DEFAULT_POOL_NAME, lclJNDIName);
		}
//		argContext.getRelationalDatabaseAdapter().setDataSourceName(getRequiredAttributeValue("JNDIName"));
		
		argContext.getRelationalDatabaseAdapter().initialize((Element) getNode());
	}
}
