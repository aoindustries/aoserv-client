package com.aoindustries.aoserv.client.command;

/*
 * Copyright 2010 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
import com.aoindustries.aoserv.client.AOServConnector;
import com.aoindustries.aoserv.client.BusinessAdministrator;
import com.aoindustries.aoserv.client.validator.HashedPassword;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author  AO Industries, Inc.
 */
final public class HashPasswordCommand extends AOServCommand<String> {

    private final String password;

    public HashPasswordCommand(
        @Param(name="password") String password
    ) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public Map<String, List<String>> validate(Locale locale, BusinessAdministrator connectedUser) throws RemoteException {
        // TODO
        return Collections.emptyMap();
    }

    @Override
    public String execute(AOServConnector<?,?> connector, boolean isInteractive) throws RemoteException {
        return HashedPassword.hash(password);
    }
}
