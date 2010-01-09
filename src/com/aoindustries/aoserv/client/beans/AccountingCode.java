/*
 * Copyright 2010 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.client.beans;

/**
 * @author  AO Industries, Inc.
 */
public class AccountingCode {

    private String accounting;

    public AccountingCode() {
    }

    public AccountingCode(String accounting) {
        this.accounting = accounting;
    }

    public String getAccounting() {
        return accounting;
    }

    public void setAccounting(String accounting) {
        this.accounting = accounting;
    }
}
