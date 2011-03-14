/*
 * Copyright 2000-2011 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.client;

import com.aoindustries.table.IndexType;
import java.rmi.RemoteException;

/**
 * A <code>ResourceType</code> is a measurable hardware resource.  A <code>PackageDefinition</code>
 * comes with a set of resources, and when those <code>PackageDefinitionLimit</code>s are exceeded,
 * an additional amount is charged to the <code>Business</code>.
 *
 * @see  PackageDefinition
 *
 * @author  AO Industries, Inc.
 */
final public class ResourceType extends AOServObjectStringKey implements Comparable<ResourceType>, DtoFactory<com.aoindustries.aoserv.client.dto.ResourceType> {

    // <editor-fold defaultstate="collapsed" desc="Constants">
    public static final String
        // linux_account types
        SHELL_ACCOUNT="shell_account",
        EMAIL_INBOX="email_inbox",
        FTPONLY_ACCOUNT="ftponly_account",
        SYSTEM_ACCOUNT="system_account",
        // linux_group types
        SHELL_GROUP="shell_group",
        SYSTEM_GROUP="system_group",
        // httpd_site types
        HTTPD_JBOSS_SITE="httpd_jboss_site",
        HTTPD_STATIC_SITE="httpd_static_site",
        HTTPD_TOMCAT_SHARED_SITE="httpd_tomcat_shared_site",
        HTTPD_TOMCAT_STD_SITE="httpd_tomcat_std_site",
        // others
        CVS_REPOSITORY = "cvs_repository",
        DISTRIBUTION_SCAN="distribution_scan",
        DNS_RECORD = "dns_record",
        DNS_ZONE = "dns_zone",
        HTTPD_SERVER = "httpd_server",
        IP_ADDRESS="ip_address",
        MYSQL_DATABASE="mysql_database",
        MYSQL_SERVER="mysql_server",
        MYSQL_USER="mysql_user",
        POSTGRESQL_DATABASE="postgresql_database",
        POSTGRESQL_SERVER="postgresql_server",
        POSTGRESQL_USER="postgresql_user",
        PRIVATE_FTP_SERVER="private_ftp_server"
    ;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Fields">
    private static final long serialVersionUID = 1920780117817958169L;

    public ResourceType(AOServConnector connector, String name) {
        super(connector, name);
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Ordering">
    @Override
    public int compareTo(ResourceType other) {
        return compareIgnoreCaseConsistentWithEquals(getKey(), other.getKey());
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Columns">
    /**
     * Gets the unique name of this resource type.
     */
    @SchemaColumn(order=0, index=IndexType.PRIMARY_KEY, description="the name of the resource type")
    public String getName() {
        return getKey();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="DTO">
    public ResourceType(AOServConnector connector, com.aoindustries.aoserv.client.dto.ResourceType dto) {
        this(connector, dto.getName());
    }

    @Override
    public com.aoindustries.aoserv.client.dto.ResourceType getDto() {
        return new com.aoindustries.aoserv.client.dto.ResourceType(getKey());
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Relations">
    @DependentObjectSet
    public IndexedSet<Resource> getResources() throws RemoteException {
        return getConnector().getResources().filterIndexed(Resource.COLUMN_RESOURCE_TYPE, this);
    }

    @DependentObjectSingleton
    public LinuxAccountType getLinuxAccountType() throws RemoteException {
        return getConnector().getLinuxAccountTypes().filterUnique(LinuxAccountType.COLUMN_RESOURCE_TYPE, this);
    }

    @DependentObjectSingleton
    public LinuxGroupType getLinuxGroupType() throws RemoteException {
        return getConnector().getLinuxGroupTypes().filterUnique(LinuxGroupType.COLUMN_RESOURCE_TYPE, this);
    }

    @DependentObjectSet
    public IndexedSet<PackageDefinitionLimit> getPackageDefinitionLimits() throws RemoteException {
        return getConnector().getPackageDefinitionLimits().filterIndexed(PackageDefinitionLimit.COLUMN_RESOURCE_TYPE, this);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="i18n">
    public String getDisplayUnit(int quantity) {
        if(quantity==1) return ApplicationResources.accessor.getMessage("ResourceType."+getKey()+".singularDisplayUnit", quantity);
        else return ApplicationResources.accessor.getMessage("ResourceType."+getKey()+".pluralDisplayUnit", quantity);
    }

    public String getPerUnit(Object amount) {
        return ApplicationResources.accessor.getMessage("ResourceType."+getKey()+".perUnit", amount);
    }

    @Override
    String toStringImpl() {
        return ApplicationResources.accessor.getMessage("ResourceType."+getKey()+".toString");
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="TODO">
    /* TODO
    public List<Resource> getResources(AOServConnector connector) throws IOException, SQLException {
        return connector.getResources().getIndexedRows(Resource.COLUMN_RESOURCE_TYPE, pkey);
    }
     */
    // </editor-fold>
}
