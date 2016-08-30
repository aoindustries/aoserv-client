/*
 * Copyright 2001-2012, 2016 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.client;

import com.aoindustries.io.CompressedDataInputStream;
import com.aoindustries.io.CompressedDataOutputStream;
import com.aoindustries.io.Streamable;
import com.aoindustries.io.TerminalWriter;
import com.aoindustries.sql.SQLUtility;
import com.aoindustries.table.Table;
import com.aoindustries.table.TableListener;
import com.aoindustries.util.WrappedException;
import com.aoindustries.util.sort.ComparisonSortAlgorithm;
import com.aoindustries.util.sort.JavaSort;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * An <code>AOServTable</code> provides access to one
 * set of <code>AOServObject</code>s.  The subclasses often provide additional
 * methods for manipulating the data outside the scope
 * of a single <code>AOServObject</code>.
 *
 * @author  AO Industries, Inc.
 *
 * @see  AOServObject
 */
abstract public class AOServTable<K,V extends AOServObject<K,V>> implements Iterable<V>, Table<V> {

	final AOServConnector connector;
	//final SimpleAOClient client;
	final Class<V> clazz;

	final Object tableListenersLock = new Object() {
		@Override
		public String toString() {
			return "tableListenersLock - "+getTableID();
		}
	};

	/**
	 * The list of <code>TableListener</code>s.
	 */
	List<TableListenerEntry> tableListeners;

	/**
	 * The lock used for cache event handling.
	 */
	final Object eventLock=new Object() {
		@Override
		public String toString() {
			return "EventLock - "+getTableID();
		}
	};

	/**
	 * The thread that is performing the batched updates.
	 * All access should be protected by the eventLock.
	 */
	TableEventThread thread;

	/**
	 * The list of <code>ProgressListener</code>s.
	 */
	final List<ProgressListener> progressListeners = new ArrayList<>();

	/**
	 * All of the table load listeners.
	 */
	final List<TableLoadListenerEntry> _loadListeners = new ArrayList<>();

	protected AOServTable(AOServConnector connector, Class<V> clazz) {
		this.connector=connector;
		//this.client=new SimpleAOClient(connector);
		this.clazz=clazz;
	}

	final public void addProgressListener(ProgressListener listener) {
		synchronized(progressListeners) {
			progressListeners.add(listener);
		}
	}

	/**
	 * Checks if this table has at least one listener.
	 */
	final public boolean hasAnyTableListener() {
		synchronized(tableListenersLock) {
			return tableListeners!=null && !tableListeners.isEmpty();
		}
	}

	final public boolean hasTableListener(TableListener listener) {
		synchronized(tableListenersLock) {
			if(tableListeners==null) return false;
			for(TableListenerEntry tableListenerEntry : tableListeners) {
				if(tableListenerEntry.listener==listener) return true;
			}
			return false;
		}
	}

	/**
	 * Registers a <code>TableListener</code> to be notified when
	 * the cached data for this table expires.  The default of
	 * 1000ms of batching is used.
	 *
	 * @see  #addTableListener(TableListener,long)
	 */
	@Override
	final public void addTableListener(TableListener listener) {
		addTableListener(listener, 1000);
	}

	/**
	 * Registers a <code>TableListener</code> to be notified when
	 * the cached data for this table expires.  Repitative incoming
	 * requests will be batched into fewer events, in increments
	 * provided by batchTime.  If batchTime is 0, the event is immediately
	 * and always distributed.  Batched events are performed in
	 * concurrent Threads, while immediate events are triggered by the
	 * central cache invalidation thread.  In other words, don't use
	 * a batchTime of zero unless you absolutely need your code to
	 * run immediately, because it causes serial processing of the event
	 * and may potentially slow down the responsiveness of the server.
	 */
	@Override
	final public void addTableListener(TableListener listener, long batchTime) {
		if(batchTime<0) throw new IllegalArgumentException("batchTime<0: "+batchTime);

		synchronized(tableListenersLock) {
			if(tableListeners==null) tableListeners=new ArrayList<>();
			tableListeners.add(new TableListenerEntry(listener, batchTime));
		}
		synchronized(eventLock) {
			if(batchTime>0 && thread==null) thread=new TableEventThread(this);
			// Tell the thread to recalc its stuff
			eventLock.notifyAll();
		}

		connector.addingTableListener();
	}

	final public void addTableLoadListener(TableLoadListener listener, Object param) {
		synchronized(_loadListeners) {
			_loadListeners.add(new TableLoadListenerEntry(listener, param));
		}
	}

	/**
	 * Clears the cache, freeing up memory.  The data will be reloaded upon
	 * next use.
	 */
	public void clearCache() {
	}

	final public AOServConnector getConnector() {
		return connector;
	}

	/*
	 * Commented-out because I'm not sure if this handles the references like ao_server.server.farm.name
	 *  - Dan 2008-04-18
	final public SchemaColumn[] getDefaultSortSchemaColumns() {
		OrderBy[] orderBys=getDefaultOrderBy();
		if(orderBys==null) return null;
		int len=orderBys.length;
		SchemaTable schemaTable=connector.schemaTables.get(getTableID());
		SchemaColumn[] schemaColumns=new SchemaColumn[len];
		for(int c=0;c<len;c++) {
			String columnName=orderBys[c].getExpression();
			SchemaColumn col=schemaTable.getSchemaColumn(connector, columnName);
			if(col==null) throw new SQLException("Unable to find SchemaColumn: "+columnName+" on "+schemaTable.getName());
			schemaColumns[c]=col;
		}
		return schemaColumns;
	}*/

	/**
	 * Indicates ascending sort.
	 */
	public static final boolean ASCENDING=true;

	/**
	 * Indicates descending sort.
	 */
	public static final boolean DESCENDING=false;

	static class OrderBy {
		final private String expression;
		final private boolean order;

		OrderBy(String expression, boolean order) {
			this.expression = expression;
			this.order = order;
		}

		/**
		 * Gets the column name(s) that is used for sorting, may be a complex expression (currently supports things like ao_server.server.farm.name)
		 */
		String getExpression() {
			return expression;
		}

		/**
		 * Gets the ASCENDING or DESCENDING order.
		 */
		boolean getOrder() {
			return order;
		}
	}

	/**
	 * Gets the default sorting for this table.
	 *
	 * @return  <code>null</code> if the sorting is performed by the server or the array of column names
	 */
	abstract OrderBy[] getDefaultOrderBy();

	final public SQLExpression[] getDefaultOrderBySQLExpressions() throws SQLException, IOException {
		OrderBy[] orderBys=getDefaultOrderBy();
		if(orderBys==null) return null;
		int len=orderBys.length;
		SQLExpression[] exprs=new SQLExpression[len];
		for(int c=0;c<len;c++) exprs[c]=getSQLExpression(orderBys[c].getExpression());
		return exprs;
	}

	protected V getNewObject() throws IOException {
		try {
			return clazz.newInstance();
		} catch(InstantiationException | IllegalAccessException err) {
			throw new IOException("Error loading class", err);
		}
	}

	/*
	protected int getMaxConnectionsPerThread() {
		return 1;
	}*/

	protected V getObject(boolean allowRetry, final AOServProtocol.CommandID commID, final Object ... params) throws IOException, SQLException {
		return connector.requestResult(
			allowRetry,
			new AOServConnector.ResultRequest<V>() {
				V result;

				@Override
				public void writeRequest(CompressedDataOutputStream out) throws IOException {
					out.writeCompressedInt(commID.ordinal());
					AOServConnector.writeParams(params, out);
				}

				@SuppressWarnings({"unchecked"})
				@Override
				public void readResponse(CompressedDataInputStream in) throws IOException, SQLException {
					int code=in.readByte();
					if(code==AOServProtocol.NEXT) {
						V obj=getNewObject();
						obj.read(in);
						if(obj instanceof SingleTableObject) ((SingleTableObject<K,V>)obj).setTable(AOServTable.this);
						result = obj;
					} else {
						AOServProtocol.checkResult(code, in);
						result = null;
					}
				}

				@Override
				public V afterRelease() {
					return result;
				}
			}
		);
	}

	protected List<V> getObjects(boolean allowRetry, AOServProtocol.CommandID commID, Object ... params) throws IOException, SQLException {
		List<V> list=new ArrayList<>();
		getObjects(allowRetry, list, commID, params);
		return list;
	}

	protected void getObjects(boolean allowRetry, final List<V> list, final AOServProtocol.CommandID commID, final Object ... params) throws IOException, SQLException {
		final int initialSize = list.size();
		// Get a snapshot of all listeners
		final ProgressListener[] listeners=getProgressListeners();
		final int listenerCount=listeners==null?0:listeners.length;
		int[] progressScales=null;
		int[] lastProgresses=null;
		if(listeners!=null) {
			progressScales=new int[listenerCount];
			for(int c=0;c<listenerCount;c++) progressScales[c]=listeners[c].getScale();
			lastProgresses=new int[listenerCount];
		}
		final int[] finalProgressScales = progressScales;
		final int[] finalLastProgresses = lastProgresses;
		// Get a snapshot of all load listeners
		final TableLoadListenerEntry[] loadListeners=getTableLoadListeners();
		final int loadCount=loadListeners==null?0:loadListeners.length;
		// Start the progresses at zero
		for(int c=0;c<listenerCount;c++) listeners[c].progressChanged(0, progressScales[c], this);
		// Tell each load listener that we are starting
		for(int c=0;c<loadCount;c++) {
			TableLoadListenerEntry entry=loadListeners[c];
			entry.param=entry.listener.tableLoadStarted(this, entry.param);
		}

		try {
			connector.requestUpdate(
				allowRetry,
				new AOServConnector.UpdateRequest() {

					@Override
					public void writeRequest(CompressedDataOutputStream out) throws IOException {
						out.writeCompressedInt(commID.ordinal());
						out.writeBoolean(listeners!=null);
						AOServConnector.writeParams(params, out);
					}

					@SuppressWarnings({"unchecked"})
					@Override
					public void readResponse(CompressedDataInputStream in) throws IOException, SQLException {
						// Remove anything that was added during a previous attempt
						while(list.size()>initialSize) list.remove(list.size()-1);
						// Load the data
						int code=listeners==null?AOServProtocol.NEXT:in.readByte();
						if(code==AOServProtocol.NEXT) {
							int size;
							if(listeners==null) {
								size=0;
							} else {
								size=in.readCompressedInt();
							}
							int objCount=0;
							while((code=in.readByte())==AOServProtocol.NEXT) {
								V obj=getNewObject();
								obj.read(in);
								if(obj instanceof SingleTableObject) ((SingleTableObject<K,V>)obj).setTable(AOServTable.this);

								// Sort and add
								list.add(obj);

								// Notify of progress changes
								objCount++;
								if(listeners!=null) {
									for(int c=0;c<listenerCount;c++) {
										int currentProgress=(int)(((long)objCount)*finalProgressScales[c]/size);
										if(currentProgress!=finalLastProgresses[c]) listeners[c].progressChanged(finalLastProgresses[c]=currentProgress, finalProgressScales[c], AOServTable.this);
									}
								}

								// Tell each load listener of the new object
								for(int c=0;c<loadCount;c++) {
									TableLoadListenerEntry entry=loadListeners[c];
									entry.param=entry.listener.tableRowLoaded(AOServTable.this, obj, objCount-1, entry.param);
								}
							}
							AOServProtocol.checkResult(code, in);
							if(listenerCount>0 && size!=objCount) throw new IOException("Unexpected number of objects returned: expected="+size+", returned="+objCount);
							// Show at final progress scale, just in case previous algorithm did not get the scale there.
							for(int c=0;c<listenerCount;c++) if(finalLastProgresses[c]!=finalProgressScales[c]) listeners[c].progressChanged(finalLastProgresses[c]=finalProgressScales[c], finalProgressScales[c], AOServTable.this);

							// Tell each load listener that we are done
							for(int c=0;c<loadCount;c++) {
								TableLoadListenerEntry entry=loadListeners[c];
								entry.param=entry.listener.tableLoadCompleted(AOServTable.this, entry.param);
							}
						} else {
							AOServProtocol.checkResult(code, in);
							throw new IOException("Unexpected response code: "+code);
						}
					}

					@Override
					public void afterRelease() {
						try {
							sortIfNeeded(list);
						} catch(IOException | SQLException err) {
							throw new WrappedException(err);
						}
					}
				}
			);
		} catch(WrappedException err) {
			Throwable cause = err.getCause();
			if(cause instanceof IOException) throw (IOException)cause;
			if(cause instanceof SQLException) throw (SQLException)cause;
			throw err;
		}
	}

	protected List<V> getObjects(boolean allowRetry, AOServProtocol.CommandID commID, Streamable param1) throws IOException, SQLException {
		List<V> list=new ArrayList<>();
		getObjects(allowRetry, list, commID, param1);
		return list;
	}

	protected List<V> getObjectsNoProgress(boolean allowRetry, AOServProtocol.CommandID commID, Object ... params) throws IOException, SQLException {
		List<V> list=new ArrayList<>();
		getObjectsNoProgress(allowRetry, list, commID, params);
		return list;
	}

	protected void getObjectsNoProgress(boolean allowRetry, final List<V> list, final AOServProtocol.CommandID commID, final Object ... params) throws IOException, SQLException {
		final int initialSize = list.size();
		// Get a snapshot of all load listeners
		final TableLoadListenerEntry[] loadListeners=getTableLoadListeners();
		final int loadCount=loadListeners==null?0:loadListeners.length;
		// Tell each load listener that we are starting
		for(int c=0;c<loadCount;c++) {
			TableLoadListenerEntry entry=loadListeners[c];
			entry.param=entry.listener.tableLoadStarted(this, entry.param);
		}

		try {
			connector.requestUpdate(
				allowRetry,
				new AOServConnector.UpdateRequest() {

					@Override
					public void writeRequest(CompressedDataOutputStream out) throws IOException {
						out.writeCompressedInt(commID.ordinal());
						AOServConnector.writeParams(params, out);
					}

					@SuppressWarnings({"unchecked"})
					@Override
					public void readResponse(CompressedDataInputStream in) throws IOException, SQLException {
						// Remove anything that was added during a previous attempt
						while(list.size()>initialSize) list.remove(list.size()-1);
						// Load the data
						int objCount=0;
						int code;
						while((code=in.readByte())==AOServProtocol.NEXT) {
							V obj=getNewObject();
							obj.read(in);
							if(obj instanceof SingleTableObject) ((SingleTableObject<K,V>)obj).setTable(AOServTable.this);
							list.add(obj);

							// Tell each load listener of the new object
							for(int c=0;c<loadCount;c++) {
								TableLoadListenerEntry entry=loadListeners[c];
								entry.param=entry.listener.tableRowLoaded(AOServTable.this, obj, objCount-1, entry.param);
							}
						}
						AOServProtocol.checkResult(code, in);

						// Tell each load listener that we are done
						for(int c=0;c<loadCount;c++) {
							TableLoadListenerEntry entry=loadListeners[c];
							entry.param=entry.listener.tableLoadCompleted(AOServTable.this, entry.param);
						}
					}

					@Override
					public void afterRelease() {
						try {
							sortIfNeeded(list);
						} catch(IOException | SQLException err) {
							throw new WrappedException(err);
						}
					}
				}
			);
		} catch(WrappedException err) {
			Throwable cause = err.getCause();
			if(cause instanceof IOException) throw (IOException)cause;
			if(cause instanceof SQLException) throw (SQLException)cause;
			throw err;
		}
	}

	/**
	 * Gets the ComparisonSortAlgorithm used to sort the table.
	 * Defaults to JavaSort.
	 */
	protected ComparisonSortAlgorithm<Object> getSortAlgorithm() {
		return JavaSort.getInstance();
	}

	/**
	 * Sorts the table using the default sort columns and orders.  If no defaults have been provided, then
	 * the table is not sorted.
	 *
	 * @see  #getDefaultSortSQLExpressions()
	 */
	protected void sortIfNeeded(List<V> list) throws SQLException, IOException {
		// Get the details for the sorting
		SQLExpression[] sortExpressions=getDefaultOrderBySQLExpressions();
		if(sortExpressions!=null) {
			OrderBy[] orderBys=getDefaultOrderBy();
			boolean[] sortOrders = new boolean[orderBys.length];
			for(int c=0;c<orderBys.length;c++) {
				sortOrders[c] = orderBys[c].getOrder();
			}
			connector.getSchemaTypes().sort(getSortAlgorithm(), list, sortExpressions, sortOrders);
		}
	}

	private ProgressListener[] getProgressListeners() {
		synchronized(progressListeners) {
			int size=progressListeners.size();
			if(size==0) return null;
			return progressListeners.toArray(new ProgressListener[size]);
		}
	}

	/**
	 * Gets an approximate number of accessible rows in the database.
	 *
	 * @see  #size()
	 */
	public int getCachedRowCount() throws IOException, SQLException {
		return size();
	}

	/**
	 * Gets the list of all accessible rows.
	 *
	 * @return  a <code>List</code> containing all of the rows
	 *
	 * @exception  IOException  if unable to access the server
	 * @exception  SQLException  if unable to access the database
	 */
	@Override
	abstract public List<V> getRows() throws IOException, SQLException;

	final public SQLExpression getSQLExpression(String expr) throws SQLException, IOException {
		int joinPos=expr.indexOf('.');
		if(joinPos==-1) joinPos=expr.length();
		int castPos=expr.indexOf("::");
		if(castPos==-1) castPos=expr.length();
		int columnNameEnd=Math.min(joinPos, castPos);
		String columnName=expr.substring(0, columnNameEnd);
		SchemaColumn lastColumn=getTableSchema().getSchemaColumn(connector, columnName);
		if(lastColumn==null) throw new IllegalArgumentException("Unable to find column: "+getTableName()+'.'+columnName);

		SQLExpression sql=new SQLColumnValue(connector, lastColumn);
		expr=expr.substring(columnNameEnd);

		while(expr.length()>0) {
			if(expr.charAt(0)=='.') {
				List<SchemaForeignKey> keys=lastColumn.getReferences(connector);
				if(keys.size()!=1) throw new IllegalArgumentException("Column "+lastColumn.getSchemaTable(connector).getName()+'.'+lastColumn.column_name+" should reference precisely one column, references "+keys.size());

				joinPos=expr.indexOf('.', 1);
				if(joinPos==-1) joinPos=expr.length();
				castPos=expr.indexOf("::", 1);
				if(castPos==-1) castPos=expr.length();
				int joinNameEnd=Math.min(joinPos, castPos);
				columnName=expr.substring(1, joinNameEnd);
				SchemaColumn keyColumn=keys.get(0).getForeignColumn(connector);
				SchemaTable valueTable=keyColumn.getSchemaTable(connector);
				SchemaColumn valueColumn=valueTable.getSchemaColumn(connector, columnName);
				if(valueColumn==null) throw new IllegalArgumentException("Unable to find column: "+valueTable.getName()+'.'+columnName+" referenced from "+getTableName());

				sql=new SQLColumnJoin(connector, sql, keyColumn, valueColumn);
				expr=expr.substring(joinNameEnd);

				lastColumn=valueColumn;
			} else if(expr.charAt(0)==':' && expr.length()>1 && expr.charAt(1)==':') {
				joinPos=expr.indexOf('.', 2);
				if(joinPos==-1) joinPos=expr.length();
				castPos=expr.indexOf("::", 2);
				if(castPos==-1) castPos=expr.length();
				int typeNameEnd=Math.min(joinPos, castPos);
				String typeName=expr.substring(2, typeNameEnd);
				SchemaType type=connector.getSchemaTypes().get(typeName);
				if(type==null) throw new IllegalArgumentException("Unable to find SchemaType: "+typeName);

				sql=new SQLCast(sql, type);
				expr=expr.substring(typeNameEnd);
			} else throw new IllegalArgumentException("Unable to parse: "+expr);
		}
		return sql;
	}

	/**
	 * Gets the unique identifier for this table.  Each
	 * table has a unique identifier, as defined in
	 * <code>SchemaTable.TableID</code>.
	 *
	 * @return  the identifier for this table
	 *
	 * @see  SchemaTable.TableID
	 */
	public abstract SchemaTable.TableID getTableID();

	private TableLoadListenerEntry[] getTableLoadListeners() {
		synchronized(_loadListeners) {
			int size=_loadListeners.size();
			if(size==0) return null;
			return _loadListeners.toArray(new TableLoadListenerEntry[size]);
		}
	}

	final public SchemaTable getTableSchema() throws IOException, SQLException {
		return connector.getSchemaTables().get(getTableID());
	}

	@Override
	final public String getTableName() throws IOException, SQLException {
		return getTableSchema().getName();
	}

	/**
	 * Gets the rows in a more efficient, indexed manner.
	 *
	 * @exception UnsupportedOperationException if not supported by the specific table implementation
	 */
	final public List<V> getIndexedRows(int col, int value) throws IOException, SQLException {
		return getIndexedRows(col, Integer.valueOf(value));
	}

	/**
	 * Gets the rows in a more efficient, indexed manner.  This default implementation simply throws UnsupportedOperationException.
	 *
	 * @exception UnsupportedOperationException if not supported by the specific table implementation
	 */
	public List<V> getIndexedRows(int col, Object value) throws IOException, SQLException {
		throw new UnsupportedOperationException("getIndexedRows now supported by table implementation");
	}

	final public V getUniqueRow(int col, int value) throws IOException, SQLException {
		return getUniqueRowImpl(col, value);
	}

	final public V getUniqueRow(int col, long value) throws IOException, SQLException {
		return getUniqueRowImpl(col, value);
	}

	final public V getUniqueRow(int col, Object value) throws IOException, SQLException {
		if(value==null) return null;
		return getUniqueRowImpl(col, value);
	}

	final public V getUniqueRow(int col, short value) throws IOException, SQLException {
		return getUniqueRowImpl(col, value);
	}

	protected abstract V getUniqueRowImpl(int col, Object value) throws IOException, SQLException;

	boolean handleCommand(String[] args, Reader in, TerminalWriter out, TerminalWriter err, boolean isInteractive) throws IOException, SQLException {
		return false;
	}

	/**
	 * Checks if the table is loaded.  A table is considered loaded when
	 * accessing any part of it will be done entirely locally, avoiding
	 * any network traffic.
	 */
	public boolean isLoaded() {
		return false;
	}

	/**
	 * Prints the contents of this table.
	 */
	final public void printTable(AOServConnector conn, PrintWriter out, boolean isInteractive) throws IOException, SQLException {
		SchemaTable schemaTable=getTableSchema();
		List<SchemaColumn> cols=schemaTable.getSchemaColumns(conn);
		int numCols=cols.size();
		String[] titles=new String[numCols];
		SchemaType[] types=new SchemaType[numCols];
		boolean[] alignRights=new boolean[numCols];
		for(int c=0;c<numCols;c++) {
			SchemaColumn col=cols.get(c);
			titles[c]=col.getColumnName();
			SchemaType type=types[c]=col.getSchemaType(conn);
			alignRights[c]=type.alignRight();
		}

		Object[] values;
		synchronized(this) {
			List<V> rows=getRows();
			int numRows=rows.size();
			values=new Object[numRows*numCols];
			int pos=0;
			for(int rowIndex=0;rowIndex<numRows;rowIndex++) {
				V row=rows.get(rowIndex);
				for(int colIndex=0;colIndex<numCols;colIndex++) {
					values[pos++]=types[colIndex].getString(row.getColumn(colIndex));
				}
			}
		}

		SQLUtility.printTable(titles, values, out, isInteractive, alignRights);
	}

	/**
	 * Removes a <code>ProgressListener</code> from the list of
	 * objects being notified as this table is being loaded.
	 */
	final public void removeProgressListener(ProgressListener listener) {
		synchronized(progressListeners) {
			int size=progressListeners.size();
			for(int c=0;c<size;c++) {
				Object O=progressListeners.get(c);
				if(O==listener) {
					progressListeners.remove(c);
					break;
				}
			}
		}
	}

	/**
	 * Removes a <code>TableListener</code> from the list of
	 * objects being notified when the data is updated.
	 */
	@Override
	final public void removeTableListener(TableListener listener) {
		// Get thread reference and release eventLock to avoid deadlock
		Thread myThread;
		synchronized(eventLock) {
			myThread = thread;
		}
		boolean stopThread = false;
		synchronized(tableListenersLock) {
			if(tableListeners!=null) {
				int size=tableListeners.size();
				for(int c=0;c<size;c++) {
					TableListenerEntry entry=tableListeners.get(c);
					if(entry.listener==listener) {
						tableListeners.remove(c);
						size--;
						if(entry.delay>0 && myThread!=null) {
							// If all remaining listeners are immediate (delay 0), kill the thread
							boolean foundDelayed=false;
							for(int d=0;d<size;d++) {
								TableListenerEntry tle=tableListeners.get(d);
								if(tle.delay>0) {
									foundDelayed=true;
									break;
								}
							}
							if(!foundDelayed) stopThread = true;
						}
						break;
					}
				}
			}
		}
		synchronized(eventLock) {
			if(stopThread) {
				// The thread will terminate itself once the reference to it is removed
				thread=null;
			}
			// Tell the thread to recalc its stuff
			eventLock.notifyAll();
		}
	}

	/**
	 * Removes a <code>TableLoadListener</code> from the list of
	 * objects being notified when the table is being loaded.
	 */
	final public void removeTableLoadListener(TableLoadListener listener) {
		synchronized(_loadListeners) {
			int size=_loadListeners.size();
			for(int c=0;c<size;c++) {
				TableLoadListenerEntry entry=_loadListeners.get(c);
				if(entry.listener==listener) {
					_loadListeners.remove(c);
					break;
				}
			}
		}
	}

	void tableUpdated() {
		List<TableListenerEntry> tableListenersSnapshot;
		synchronized(tableListenersLock) {
			tableListenersSnapshot = this.tableListeners==null ? null : new ArrayList<>(this.tableListeners);
		}
		if(tableListenersSnapshot!=null) {
			// Notify all immediate listeners
			Iterator<TableListenerEntry> I=tableListenersSnapshot.iterator();
			while(I.hasNext()) {
				final TableListenerEntry entry=I.next();
				if(entry.delay<=0) {
					// Run in a different thread to avoid deadlock and increase concurrency responding to table update events.
					AOServConnector.executorService.submit(
						new Runnable() {
							@Override
							public void run() {
								entry.listener.tableUpdated(AOServTable.this);
							}
						}
					);
				}
			}

			synchronized(eventLock) {
				// Notify the batching thread of the update
				int size=tableListenersSnapshot.size();
				boolean modified = false;
				for(int c=0;c<size;c++) {
					TableListenerEntry entry=tableListenersSnapshot.get(c);
					if(entry.delay>0 && entry.delayStart==-1) {
						entry.delayStart=System.currentTimeMillis();
						modified = true;
					} else {
						// System.out.println("DEBUG: "+getTableID()+": Batched event");
					}
				}
				if(modified) eventLock.notify();
			}
		}
	}

	@Override
	final public String toString() {
		try {
			return getTableSchema().display;
		} catch(IOException | SQLException err) {
			throw new WrappedException(err);
		}
	}

	// Iterable methods
	@Override
	public Iterator<V> iterator() {
		try {
			return getRows().iterator();
		} catch(IOException | SQLException err) {
			throw new WrappedException(err);
		}
	}

	/**
	 * Gets a Map-compatible view of this table.
	 */
	public Map<K,V> getMap() {
		return map;
	}

	/**
	 * Gets the value for the associated key or <code>null</code> if the data
	 * doesn't exist or is filtered.
	 */
	// TODO: Rename me
	abstract public V get(Object key) throws IOException, SQLException;

	private final Map<K,V> map = new Map<K,V>() {
		// Map methods
		@Override
		public V get(Object key) {
			try {
				return AOServTable.this.get(key);
			} catch(IOException | SQLException err) {
				throw new WrappedException(err);
			}
		}

		@Override
		public Set<Entry<K,V>> entrySet() {
			try {
				return new EntrySet<>(getRows());
			} catch(IOException | SQLException err) {
				throw new WrappedException(err);
			}
		}

		@Override
		public Collection<V> values() {
			try {
				return getRows();
			} catch(IOException | SQLException err) {
				throw new WrappedException(err);
			}
		}

		@Override
		public Set<K> keySet() {
			try {
				return new KeySet<>(getRows());
			} catch(IOException | SQLException err) {
				throw new WrappedException(err);
			}
		}

		@Override
		public void clear() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void putAll(Map<? extends K, ? extends V> t) {
			throw new UnsupportedOperationException();
		}

		@Override
		public V remove(Object key) {
			throw new UnsupportedOperationException();
		}

		@Override
		public V put(K key, V value) {
			throw new UnsupportedOperationException();
		}

		@SuppressWarnings({"unchecked"})
		@Override
		public boolean containsValue(Object value) {
			V aoObj=(V)value;
			Object key=aoObj.getKey();
			return containsKey(key);
		}

		@Override
		public boolean containsKey(Object key) {
			try {
				return AOServTable.this.get(key)!=null;
			} catch(IOException | SQLException err) {
				throw new WrappedException(err);
			}
		}

		@Override
		public boolean isEmpty() {
			try {
				return AOServTable.this.isEmpty();
			} catch(IOException | SQLException err) {
				throw new WrappedException(err);
			}
		}

		@Override
		public int size() {
			try {
				return AOServTable.this.size();
			} catch(IOException | SQLException err) {
				throw new WrappedException(err);
			}
		}
	};

	public boolean isEmpty() throws IOException, SQLException {
		return getRows().isEmpty();
	}

	public int size() throws IOException, SQLException {
		return getRows().size();
	}

	/**
	 * This is size for JavaBeans compatibility.
	 */
	final public int getSize() throws IOException, SQLException {
		return size();
	}
}