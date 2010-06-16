package com.aoindustries.aoserv.client.command;

/*
 * Copyright 2010 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
import com.aoindustries.aoserv.client.BusinessAdministrator;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author  AO Industries, Inc.
 */
final public class SetTicketSummaryCommand extends RemoteCommand<Void> {

    private static final long serialVersionUID = 1L;

    final private int ticketId;
    final private String summary;

    public SetTicketSummaryCommand(
        @Param(name="ticketId") int ticketId,
        @Param(name="summary") String summary
    ) {
        this.ticketId = ticketId;
        this.summary = summary;
    }

    public int getTicketId() {
        return ticketId;
    }

    public String getSummary() {
        return summary;
    }

    @Override
    public boolean isReadOnlyCommand() {
        return false;
    }

    @Override
    public Map<String, List<String>> validate(BusinessAdministrator connectedUser) throws RemoteException {
        // TODO
        return Collections.emptyMap();
    }
}