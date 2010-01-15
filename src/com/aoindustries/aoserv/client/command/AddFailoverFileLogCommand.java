package com.aoindustries.aoserv.client.command;

/*
 * Copyright 2010 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
import com.aoindustries.aoserv.client.BusinessAdministrator;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author  AO Industries, Inc.
 */
final public class AddFailoverFileLogCommand extends AOServCommand<Integer> {

    private static final long serialVersionUID = 1L;

    public static final String
        PARAM_REPLICATION = "replication",
        PARAM_START_TIME = "start_time",
        PARAM_END_TIME = "end_time",
        PARAM_SCANNED = "scanned",
        PARAM_UPDATED = "updated",
        PARAM_BYTES = "bytes",
        PARAM_IS_SUCCESSFUL = "is_successful"
    ;

    final private int replication;
    final private Timestamp startTime;
    final private Timestamp endTime;
    final private int scanned;
    final private int updated;
    final private long bytes;
    final private boolean isSuccessful;

    public AddFailoverFileLogCommand(
        @Param(name=PARAM_REPLICATION) int replication,
        @Param(name=PARAM_START_TIME) Timestamp startTime,
        @Param(name=PARAM_END_TIME) Timestamp endTime,
        @Param(name=PARAM_SCANNED) int scanned,
        @Param(name=PARAM_UPDATED) int updated,
        @Param(name=PARAM_BYTES) long bytes,
        @Param(name=PARAM_IS_SUCCESSFUL) boolean isSuccessful
    ) {
        this.replication = replication;
        this.startTime = startTime;
        this.endTime = endTime;
        this.scanned = scanned;
        this.updated = updated;
        this.bytes = bytes;
        this.isSuccessful = isSuccessful;
    }

    public Map<String, List<String>> validate(Locale locale, BusinessAdministrator connectedUser) throws RemoteException {
        // TODO
        return Collections.emptyMap();
    }
}