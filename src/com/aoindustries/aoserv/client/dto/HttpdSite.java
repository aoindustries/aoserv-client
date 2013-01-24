/*
 * Copyright 2010-2011 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.client.dto;

/**
 * @author  AO Industries, Inc.
 */
public class HttpdSite extends AOServerResource {

    private DomainLabels siteName;
    private boolean listFirst;
    private int linuxAccountGroup;
    private Email serverAdmin;
    private boolean manualConfig;
    private String awstatsSkipFiles;

    public HttpdSite() {
    }

    public HttpdSite(
        int pkey,
        String resourceType,
        AccountingCode accounting,
        long created,
        UserId createdBy,
        Integer disableLog,
        long lastEnabled,
        int aoServer,
        int businessServer,
        DomainLabels siteName,
        boolean listFirst,
        int linuxAccountGroup,
        Email serverAdmin,
        boolean manualConfig,
        String awstatsSkipFiles
    ) {
        super(pkey, resourceType, accounting, created, createdBy, disableLog, lastEnabled, aoServer, businessServer);
        this.siteName = siteName;
        this.listFirst = listFirst;
        this.linuxAccountGroup = linuxAccountGroup;
        this.serverAdmin = serverAdmin;
        this.manualConfig = manualConfig;
        this.awstatsSkipFiles = awstatsSkipFiles;
    }

    public DomainLabels getSiteName() {
        return siteName;
    }

    public void setSiteName(DomainLabels siteName) {
        this.siteName = siteName;
    }

    public boolean isListFirst() {
        return listFirst;
    }

    public void setListFirst(boolean listFirst) {
        this.listFirst = listFirst;
    }

    public int getLinuxAccountGroup() {
        return linuxAccountGroup;
    }

    public void setLinuxAccountGroup(int linuxAccountGroup) {
        this.linuxAccountGroup = linuxAccountGroup;
    }

    public Email getServerAdmin() {
        return serverAdmin;
    }

    public void setServerAdmin(Email serverAdmin) {
        this.serverAdmin = serverAdmin;
    }

    public boolean isManualConfig() {
        return manualConfig;
    }

    public void setManualConfig(boolean manualConfig) {
        this.manualConfig = manualConfig;
    }

    public String getAwstatsSkipFiles() {
        return awstatsSkipFiles;
    }

    public void setAwstatsSkipFiles(String awstatsSkipFiles) {
        this.awstatsSkipFiles = awstatsSkipFiles;
    }
}