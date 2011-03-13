/*
 * Copyright 2003-2011 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.client;

/**
 * @see FailoverFileReplication
 *
 * @author  AO Industries, Inc.
 */
@ServiceAnnotation(ServiceName.failover_file_replications)
public interface FailoverFileReplicationService extends AOServService<Integer,FailoverFileReplication> {
}
