package com.aoindustries.aoserv.client;

/*
 * Copyright 2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */

/**
 * Encapsulates a reason and resource.
 *
 * @author  AO Industries, Inc.
 */
public final class Reason {

    private final String reason;
    private final Resource resource;

    public Reason(String reason, Resource resource) {
        if(reason==null) throw new IllegalArgumentException("reason==null");
        if(resource==null) throw new IllegalArgumentException("resource==null");
        this.reason = reason;
        this.resource = resource;
    }
    
    public String getReason() {
        return reason;
    }

    public Resource getResource() {
        return resource;
    }
}