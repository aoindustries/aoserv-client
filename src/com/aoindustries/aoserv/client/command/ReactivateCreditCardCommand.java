/*
 * Copyright 2010-2011 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.client.command;

import com.aoindustries.aoserv.client.*;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author  AO Industries, Inc.
 */
final public class ReactivateCreditCardCommand extends RemoteCommand<Void> {

    private static final long serialVersionUID = 1L;

    final private int pkey;

    public ReactivateCreditCardCommand(
        @Param(name="creditCard") CreditCard creditCard
    ) {
        this.pkey = creditCard.getPkey();
    }

    public int getPkey() {
        return pkey;
    }

    @Override
    public Map<String, List<String>> validate(BusinessAdministrator connectedUser) throws RemoteException {
        // TODO
        return Collections.emptyMap();
    }
}
