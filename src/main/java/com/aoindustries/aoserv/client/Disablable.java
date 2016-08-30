/*
 * Copyright 2002-2009, 2016 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.client;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Classes that are <code>Disablable</code> can be disable and enabled.
 *
 * @author  AO Industries, Inc.
 */
public interface Disablable {

	/**
	 * Checks if this object is disabled.  This should execute very quickly (not
	 * incur any round-trip to any database) and thus does not throw any checked
	 * exceptions.
	 */
	boolean isDisabled();

	DisableLog getDisableLog() throws IOException, SQLException;

	boolean canDisable() throws IOException, SQLException;

	boolean canEnable() throws IOException, SQLException;

	void disable(DisableLog dl) throws IOException, SQLException;

	void enable() throws IOException, SQLException;
}