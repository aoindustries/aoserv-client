/*
 * Copyright 2001-2010 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.client;

/**
 * @see  HttpdSite
 *
 * @author  AO Industries, Inc.
 */
@ServiceAnnotation(ServiceName.httpd_sites)
public interface HttpdSiteService<C extends AOServConnector<C,F>, F extends AOServConnectorFactory<C,F>> extends AOServServiceIntegerKey<C,F,HttpdSite> {

    /* TODO
    public String generateSiteName(String template) throws IOException, SQLException {
    	return connector.requestStringQuery(true, AOServProtocol.CommandID.GENERATE_SITE_NAME, template);
    }

    HttpdSite getHttpdSite(String siteName, AOServer ao) throws IOException, SQLException {
        int aoPKey=ao.pkey;

        List<HttpdSite> cached=getRows();
        int size=cached.size();
        for(int c=0;c<size;c++) {
            HttpdSite site=cached.get(c);
            if(
                site.ao_server==aoPKey
                && site.site_name.equals(siteName)
            ) return site;
        }
        return null;
    }

    List<HttpdSite> getHttpdSites(HttpdServer server) throws IOException, SQLException {
        int serverPKey=server.pkey;

        List<HttpdSite> cached=getRows();
    	int size=cached.size();
        List<HttpdSite> matches=new ArrayList<HttpdSite>(size);
    	for(int c=0;c<size;c++) {
            HttpdSite site=cached.get(c);
            for(HttpdSiteBind bind : site.getHttpdSiteBinds()) {
                if(bind.getHttpdBind().httpd_server==serverPKey) {
                    matches.add(site);
                    break;
                }
            }
        }
        return matches;
    }

    List<HttpdSite> getHttpdSites(AOServer ao) throws IOException, SQLException {
        return getIndexedRows(HttpdSite.COLUMN_AO_SERVER, ao.pkey);
    }

    List<HttpdSite> getHttpdSites(Business bu) throws IOException, SQLException {
        return getIndexedRows(HttpdSite.COLUMN_ACCOUNTING, bu.pkey);
    }

    @Override
    boolean handleCommand(String[] args, InputStream in, TerminalWriter out, TerminalWriter err, boolean isInteractive) throws IllegalArgumentException, SQLException, IOException {
        String command=args[0];
        if(command.equalsIgnoreCase(AOSHCommand.CHECK_SITE_NAME)) {
            if(AOSH.checkParamCount(AOSHCommand.CHECK_SITE_NAME, args, 1, err)) {
                try {
                    SimpleAOClient.checkSiteName(args[1]);
                    out.println("true");
                } catch(IllegalArgumentException iae) {
                    out.print("aosh: "+AOSHCommand.CHECK_SITE_NAME+": ");
                    out.println(iae.getMessage());
                }
                out.flush();
            }
            return true;
    	} else if(command.equalsIgnoreCase(AOSHCommand.DISABLE_HTTPD_SITE)) {
            if(AOSH.checkParamCount(AOSHCommand.DISABLE_HTTPD_SITE, args, 3, err)) {
                out.println(
                    connector.getSimpleAOClient().disableHttpdSite(
                        args[1],
                        args[2],
                        args[3]
                    )
                );
                out.flush();
            }
            return true;
    	} else if(command.equalsIgnoreCase(AOSHCommand.ENABLE_HTTPD_SITE)) {
            if(AOSH.checkParamCount(AOSHCommand.ENABLE_HTTPD_SITE, args, 2, err)) {
                connector.getSimpleAOClient().enableHttpdSite(args[1], args[2]);
            }
            return true;
        } else if(command.equalsIgnoreCase(AOSHCommand.GENERATE_SITE_NAME)) {
            if(AOSH.checkParamCount(AOSHCommand.GENERATE_SITE_NAME, args, 1, err)) {
                out.println(connector.getSimpleAOClient().generateSiteName(args[1]));
                out.flush();
            }
            return true;
        } else if(command.equalsIgnoreCase(AOSHCommand.GET_AWSTATS_FILE)) {
            if(AOSH.checkParamCount(AOSHCommand.GET_AWSTATS_FILE, args, 4, err)) {
                connector.getSimpleAOClient().getAWStatsFile(
                    args[1],
                    args[2],
                    args[3],
                    args[4],
                    new WriterOutputStream(out)
                );
                out.flush();
            }
            return true;
//        } else if(command.equalsIgnoreCase(AOSHCommand.INITIALIZE_HTTPD_SITE_PASSWD_FILE)) {
//            if(AOSH.checkParamCount(AOSHCommand.INITIALIZE_HTTPD_SITE_PASSWD_FILE, args, 4, err)) {
//                connector.getSimpleAOClient().initializeHttpdSitePasswdFile(
//                    args[1],
//                    args[2],
//                    args[3],
//                    args[4]
//                );
//            }
//            return true;
        } else if(command.equalsIgnoreCase(AOSHCommand.IS_SITE_NAME_AVAILABLE)) {
            if(AOSH.checkParamCount(AOSHCommand.IS_SITE_NAME_AVAILABLE, args, 1, err)) {
                out.println(connector.getSimpleAOClient().isSiteNameAvailable(args[1]));
                out.flush();
            }
            return true;
        } else if(command.equalsIgnoreCase(AOSHCommand.REMOVE_HTTPD_SITE)) {
            if(AOSH.checkParamCount(AOSHCommand.REMOVE_HTTPD_SITE, args, 2, err)) {
                connector.getSimpleAOClient().removeHttpdSite(args[1], args[2]);
            }
            return true;
        } else if(command.equalsIgnoreCase(AOSHCommand.SET_HTTPD_SITE_SERVER_ADMIN)) {
            if(AOSH.checkParamCount(AOSHCommand.SET_HTTPD_SITE_SERVER_ADMIN, args, 3, err)) {
                connector.getSimpleAOClient().setHttpdSiteServerAdmin(args[1], args[2], args[3]);
            }
            return true;
        } else if(command.equalsIgnoreCase(AOSHCommand.SET_HTTPD_SITE_IS_MANUAL)) {
            if(AOSH.checkParamCount(AOSHCommand.SET_HTTPD_SITE_IS_MANUAL, args, 3, err)) {
                connector.getSimpleAOClient().setHttpdSiteIsManual(
                    args[1],
                    args[2],
                    AOSH.parseBoolean(args[3], "is_manual")
                );
            }
            return true;
        } else if(command.equalsIgnoreCase(AOSHCommand.WAIT_FOR_HTTPD_SITE_REBUILD)) {
            if(AOSH.checkParamCount(AOSHCommand.WAIT_FOR_HTTPD_SITE_REBUILD, args, 1, err)) {
                connector.getSimpleAOClient().waitForHttpdSiteRebuild(args[1]);
            }
            return true;
        } else return false;
    }

    public boolean isSiteNameAvailable(String sitename) throws IOException, SQLException {
        return connector.requestBooleanQuery(true, AOServProtocol.CommandID.IS_SITE_NAME_AVAILABLE, sitename);
    }

    void waitForRebuild(AOServer aoServer) throws IOException, SQLException {
    	connector.requestUpdate(
            true,
            AOServProtocol.CommandID.WAIT_FOR_REBUILD,
            SchemaTable.TableID.HTTPD_SITES,
            aoServer.pkey
        );
    }
     */
}