package com.opal.rest;

import javax.servlet.http.HttpServletRequest;

public abstract class Authenticator<A> {

	public Authenticator() {
		super();
	}

	public abstract A getCredential(HttpServletRequest argRequest) throws RestResultException;
}
