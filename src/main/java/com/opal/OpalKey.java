package com.opal;

/* This SuppressWarnings annotation is for the type variable O, which I can't seem to get rid without causing
 * compilation errors.
 */
public abstract class OpalKey<O extends Opal<? extends UserFacing>> {

//	private static final org.apache.log4j.Logger ourLogger = org.apache.log4j.Logger.getLogger(OpalKey.class.getName());
	
	protected OpalKey() {
		super();
	}
	
}
