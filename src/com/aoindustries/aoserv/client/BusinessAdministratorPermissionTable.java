package com.aoindustries.aoserv.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/*
 * Copyright 2007 by AO Industries, Inc.,
 * 816 Azalea Rd, Mobile, Alabama, 36693, U.S.A.
 * All rights reserved.
 */

/**
 * @see  BusinessAdministratorPermission
 *
 * @version  1.0
 *
 * @author  AO Industries, Inc.
 */
final public class BusinessAdministratorPermissionTable extends CachedTableIntegerKey<BusinessAdministratorPermission> {

    BusinessAdministratorPermissionTable(AOServConnector connector) {
	super(connector, BusinessAdministratorPermission.class);
    }

    public BusinessAdministratorPermission get(Object pkey) {
	return getUniqueRow(BusinessAdministratorPermission.COLUMN_PKEY, pkey);
    }

    int getTableID() {
	return SchemaTable.BUSINESS_ADMINISTRATOR_PERMISSIONS;
    }

    List<BusinessAdministratorPermission> getPermissions(BusinessAdministrator ba) {
        return getIndexedRows(BusinessAdministratorPermission.COLUMN_USERNAME, ba.pkey);
    }
    
    /**
     * Caches the permission lookups for speed.
     */
    private Map<String,SortedSet<String>> cachedPermissions;

    public void clearCache() {
        synchronized(this) {
            cachedPermissions = null;
        }
    }
    
    boolean hasPermission(BusinessAdministrator ba, String permission) {
        synchronized(this) {
            if(cachedPermissions==null) {
                Map<String,SortedSet<String>> newCachedPermissions = new HashMap<String,SortedSet<String>>();
                List<BusinessAdministratorPermission> baps = getRows();
                for(BusinessAdministratorPermission bap : baps) {
                    String bapUsername = bap.username;
                    String bapPermission = bap.permission;
                    SortedSet<String> perms = newCachedPermissions.get(bapUsername);
                    if(perms==null) newCachedPermissions.put(bapUsername, perms = new TreeSet<String>());
                    perms.add(bapPermission);
                }
                cachedPermissions = newCachedPermissions;
            }
            SortedSet<String> perms = cachedPermissions.get(ba.pkey);
            return perms!=null && perms.contains(permission);
        }
    }
}
