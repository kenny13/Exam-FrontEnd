package com.bla.laa.client;

import com.bla.laa.shared.Model.TCaseModel;
import com.bla.laa.shared.Model.TCaseTypeModel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import java.util.List;
import java.util.SortedMap;


@RemoteServiceRelativePath("RPC")
public interface RPC extends RemoteService {

    SortedMap<Integer, String > getParagraph(Integer paragId) throws RpcCustException;

    SortedMap<Integer, String > getParagraphMore(Integer paragId) throws RpcCustException;

    TCaseModel getTC(Integer str) throws RpcCustException;

    List<TCaseTypeModel> getTCaseTypes() throws RpcCustException;

    public static class App {
        private static final RPCAsync ourInstance = (RPCAsync) GWT.create(RPC.class);
        public static RPCAsync getInstance() {
            return ourInstance;
        }
    }
}
