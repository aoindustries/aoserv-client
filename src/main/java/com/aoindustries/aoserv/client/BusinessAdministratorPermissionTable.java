/*
 * Copyright 2007-2009, 2016 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.client;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @see  BusinessAdministratorPermission
 *
 * @author  AO Industries, Inc.
 */
final public class BusinessAdministratorPermissionTable extends CachedTableIntegerKey<BusinessAdministratorPermission> {

	BusinessAdministratorPermissionTable(AOServConnector connector) {
		super(connector, BusinessAdministratorPermission.class);
	}

	private static final OrderBy[] defaultOrderBy = {
		new OrderBy(BusinessAdministratorPermission.COLUMN_USERNAME_name, ASCENDING),
		new OrderBy(BusinessAdministratorPermission.COLUMN_PERMISSION_name+'.'+AOServPermission.COLUMN_SORT_ORDER_name, ASCENDING)
	};
	@Override
	OrderBy[] getDefaultOrderBy() {
		return defaultOrderBy;
	}

	@Override
	public BusinessAdministratorPermission get(int pkey) throws IOException, SQLException {
		return getUniqueRow(BusinessAdministratorPermission.COLUMN_PKEY, pkey);
	}

	@Override
	public SchemaTable.TableID getTableID() {
		return SchemaTable.TableID.BUSINESS_ADMINISTRATOR_PERMISSIONS;
	}

	List<BusinessAdministratorPermission> getPermissions(BusinessAdministrator ba) throws IOException, SQLException {
		return getIndexedRows(BusinessAdministratorPermission.COLUMN_USERNAME, ba.pkey);
	}

	/**
	 * Caches the permission lookups for speed.
	 */
	private Map<String,SortedSet<String>> cachedPermissions;

	@Override
	public void clearCache() {
		super.clearCache();
		synchronized(this) {
			cachedPermissions = null;
		}
	}

	boolean hasPermission(BusinessAdministrator ba, String permission) throws IOException, SQLException {
		synchronized(this) {
			if(cachedPermissions==null) {
				Map<String,SortedSet<String>> newCachedPermissions = new HashMap<>();
				List<BusinessAdministratorPermission> baps = getRows();
				for(BusinessAdministratorPermission bap : baps) {
					String bapUsername = bap.username;
					String bapPermission = bap.permission;
					SortedSet<String> perms = newCachedPermissions.get(bapUsername);
					if(perms==null) newCachedPermissions.put(bapUsername, perms = new TreeSet<>());
					perms.add(bapPermission);
				}
				cachedPermissions = newCachedPermissions;
			}
			SortedSet<String> perms = cachedPermissions.get(ba.pkey);
			return perms!=null && perms.contains(permission);
		}
	}
}