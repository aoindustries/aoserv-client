/*
 * Copyright 2010-2011 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.client.dto;

/**
 * @author  AO Industries, Inc.
 */
public class LinuxAccountGroup extends AOServObject {

    private int pkey;
    private int linuxAccount;
    private int linuxGroup;
    private boolean primary;

    public LinuxAccountGroup() {
    }

    public LinuxAccountGroup(int pkey, int linuxAccount, int linuxGroup, boolean primary) {
        this.pkey = pkey;
        this.linuxAccount = linuxAccount;
        this.linuxGroup = linuxGroup;
        this.primary = primary;
    }

    public int getPkey() {
        return pkey;
    }

    public void setPkey(int pkey) {
        this.pkey = pkey;
    }

    public int getLinuxAccount() {
        return linuxAccount;
    }

    public void setLinuxAccount(int linuxAccount) {
        this.linuxAccount = linuxAccount;
    }

    public int getLinuxGroup() {
        return linuxGroup;
    }

    public void setLinuxGroup(int linuxGroup) {
        this.linuxGroup = linuxGroup;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }
}