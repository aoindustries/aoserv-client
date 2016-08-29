/*
 * Copyright 2006-2009, 2016 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.client;

import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Gets the sizes of each table.
 *
 * @author  AO Industries, Inc.
 */
public class GetDefaultOrderBySQLExpressionsTest extends TestCase {

	private List<AOServConnector> conns;

	public GetDefaultOrderBySQLExpressionsTest(String testName) {
		super(testName);
	}

	@Override
	protected void setUp() throws Exception {
		conns = AOServConnectorTest.getTestConnectors();
	}

	@Override
	protected void tearDown() throws Exception {
		conns = null;
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(GetDefaultOrderBySQLExpressionsTest.class);

		return suite;
	}

	/**
	 * Test the size() method of each AOServTable.
	 */
	public void testTableSizes() throws Exception {
		System.out.println("Testing getTable(tableID).getDefaultOrderBySQLExpressions()");
		for(AOServConnector conn : conns) {
			String username = conn.getThisBusinessAdministrator().pkey;
			System.out.print("    "+username+": ");
			int numTables = SchemaTable.TableID.values().length;
			for(int c=0;c<numTables;c++) {
				System.out.print('.');
				AOServTable<?,?> table=conn.getTable(c);
				table.getDefaultOrderBySQLExpressions();
			}
			System.out.println(" Done");
		}
	}
}
