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
final public class SetTicketContactEmailsCommand extends RemoteCommand<Void> {

    private static final long serialVersionUID = 1L;

    final private int ticketId;
    final private String contactEmails;

    public SetTicketContactEmailsCommand(
        @Param(name="ticketId") int ticketId,
        @Param(name="contactEmails") String contactEmails
    ) {
        this.ticketId = ticketId;
        this.contactEmails = contactEmails;
    }

    public int getTicketId() {
        return ticketId;
    }

    public String getContactEmails() {
        return contactEmails;
    }

    @Override
    public boolean isReadOnlyCommand() {
        return false;
    }

    public Map<String, List<String>> validate(BusinessAdministrator connectedUser) throws RemoteException {
        // TODO
        return Collections.emptyMap();
    }
}