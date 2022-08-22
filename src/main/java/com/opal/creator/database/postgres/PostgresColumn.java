package com.opal.creator.database.postgres;

import com.opal.creator.database.DatabaseColumn;
import com.opal.creator.database.DefaultValue;

public class PostgresColumn extends DatabaseColumn {
	private final String mySequenceName;
	
	public PostgresColumn(PostgresTableName argTableName, String argName, String argDataType, int argLength, int argPrecision, int argScale, String argDomainName, boolean argNullable, DefaultValue argDefault, String argSequenceName) {
		super(argTableName, argName, argDataType, argLength, argPrecision, argScale, argDomainName, argNullable, argDefault);
		
		mySequenceName = argSequenceName;
	}
		
	@Override
	public String getSequenceName() {
		return mySequenceName;
	}
	
	@Override
	public boolean isSequenced() {
		return getSequenceName() != null;
	}
}
