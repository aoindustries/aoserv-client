/*
 * Copyright 2006-2009, 2016 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests the aoserv-client object indexing algorithms for accuracy.
 *
 * @author  AO Industries, Inc.
 */
public class GetIndexedRowTest extends TestCase {

	private List<AOServConnector> conns;

	public GetIndexedRowTest(String testName) {
		super(testName);
	}

	@Override
	protected void setUp() throws Exception {
		conns = AOServConnectorTODO.getTestConnectors();
	}

	@Override
	protected void tearDown() throws Exception {
		conns = null;
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(GetIndexedRowTest.class);

		return suite;
	}

	/**
	 * Test the size() method of each AOServTable.
	 */
	public void testGetIndexedRows() throws Exception {
		System.out.println("Testing all indexed rows:");
		System.out.println("+ means supported");
		System.out.println("- means unsupported");
		for(AOServConnector conn : conns) {
			String username = conn.getThisBusinessAdministrator().pkey;
			System.out.println("    "+username);
			int numTables = SchemaTable.TableID.values().length;
			for(int c=0;c<numTables;c++) {
				// Excluded for testing speed
				if(
					c==SchemaTable.TableID.DISTRO_FILES.ordinal()
					|| c==SchemaTable.TableID.TRANSACTIONS.ordinal()
					|| c==SchemaTable.TableID.WHOIS_HISTORY.ordinal()
				) continue;
				AOServTable table=conn.getTable(c);
				String tableName=table.getTableName();
				System.out.print("        "+tableName+": ");
				List<AOServObject> rows=table.getRows();
				if(rows.isEmpty()) System.out.println("Empty table, cannot test");
				else {
					List<SchemaColumn> columns=table.getTableSchema().getSchemaColumns(conn);
					Map<Object,List<AOServObject>> expectedLists=new HashMap<>();
					for(SchemaColumn column : columns) {
						boolean supported=true;
						String columnName=column.getColumnName();
						try {
							int colIndex=column.getIndex();
							// Build our list of the expected objects by iterating through the entire list
							expectedLists.clear();
							for(AOServObject row : rows) {
								Object value=row.getColumn(colIndex);
								// null values are not indexed
								if(value!=null) {
									List<AOServObject> list=expectedLists.get(value);
									if(list==null) expectedLists.put(value, list=new ArrayList<>());
									list.add(row);
								}
							}
							// Compare to the lists using the index routines
							for(Object value : expectedLists.keySet()) {
								List<AOServObject> expectedList=expectedLists.get(value);
								List<AOServObject> indexedRows=table.getIndexedRows(colIndex, value);
								assertEquals(tableName+"."+columnName+"="+value+": Mismatch in list size: ", expectedList.size(), indexedRows.size());
								if(!expectedList.containsAll(indexedRows)) fail(tableName+"."+columnName+"="+value+": expectedList does not contain all the rows of indexedRows");
								if(!indexedRows.containsAll(expectedList)) fail(tableName+"."+columnName+"="+value+": indexedRows does not contain all the rows of expectedList");
							}
						} catch(UnsupportedOperationException err) {
							supported=false;
						} catch(RuntimeException err) {
							System.out.println("RuntimeException tableName="+tableName+", columnName="+columnName);
							throw err;
						}
						System.out.print(supported?'+':'-');
					}
					System.out.println();
				}
			}
		}
	}
}
