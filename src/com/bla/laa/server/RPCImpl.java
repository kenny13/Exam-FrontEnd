package com.bla.laa.server;

import com.bla.laa.client.RPC;
import com.bla.laa.client.RpcCustException;
import com.bla.laa.shared.DAO.ParagraphDAO;
import com.bla.laa.shared.DAO.TCaseDAO;
import com.bla.laa.shared.Model.TCaseModel;
import com.bla.laa.shared.Model.TCaseTypeModel;
import com.bla.laa.shared.PMF;
import com.bla.laa.shared.RandomTCase;
import com.bla.laa.shared.Services.ParagraphService;
import com.bla.laa.shared.TCaseWraper;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import java.util.*;
import java.util.logging.Logger;

public class RPCImpl extends RemoteServiceServlet implements RPC {
    private static final Logger logger = Logger.getLogger(RPCImpl.class.getName());
    private static final PersistenceManager pm = PMF.get().getPersistenceManager();
    public static final Integer VALUE_NOT_SET = -1;
    public static final Integer HTML_LENGHT = 1300;

    private static SortedMap<Integer, String > NO_HTML_DATA = new TreeMap<Integer, String /*SafeHtml*/>();

    ParagraphService paragraphService = null;
    TCaseWraper tCaseWraper = null;

    public RPCImpl() {
        paragraphService = new ParagraphService();
        tCaseWraper = new TCaseWraper();
    }

    public SortedMap<Integer, String> getParagraph(Integer paragId) throws RpcCustException {
        logger.info("RPCImpl.getParagraph(" + paragId + ")");

        if ( (paragId == null) || (paragId < 0 ))
            throw new RpcCustException(" Par. number not corect !");

        SortedMap<Integer, String > sortedMap = new TreeMap<Integer, String >();
        int textLenght = 0;
        while (textLenght < HTML_LENGHT){
            ParagraphDAO paragraphDAO = paragraphService.getParagById(paragId);
            if (paragraphDAO == null) {
                throw new RpcCustException("Par. not found !");
            }

            String html = paragraphDAO.getParagText();
            if ((html == null) || (html.isEmpty())) {
                throw new RpcCustException("Par. text not found !");
            }
            sortedMap.put(paragraphDAO.getNr(), paragraphDAO.getParagText() );
            textLenght += paragraphDAO.getParagText().length();

            paragId++;
            if (!paragraphService.isParagExists(paragId))
                break;

        }


        return sortedMap;
    }

    public SortedMap<Integer, String /*SafeHtml*/> getParagraphMore(Integer paragId ) throws RpcCustException{
        logger.info("RPCImpl.getParagraphMore(" + paragId +")");
        Integer fetchId = paragId;

        if (fetchId <= 0){
            logger.warning("no data for paragr up");
            return NO_HTML_DATA;
        }
        ParagraphDAO paragraphDAO = paragraphService.getParagById(fetchId);
        if (paragraphDAO == null){
            logger.warning("no data for paragr down");
            return NO_HTML_DATA;
        }

        SortedMap<Integer, String> sortedMap = new TreeMap<Integer, String>();
        sortedMap.put(paragraphDAO.getNr(), paragraphDAO.getParagText() );

        return sortedMap;
    }
     /*
    public TCaseModel getTC() throws RpcCustException {
        return getTCMock();
    }*/

    public TCaseModel getTC(Integer tCaseType) throws RpcCustException {
        logger.info("RPCImpl.getTC("+ tCaseType +")");

        // check type exists
        Integer tCaseTypeInt = VALUE_NOT_SET;
        try {
             tCaseTypeInt = Integer.valueOf(tCaseType);
        }catch (NumberFormatException nfe){
            logger.severe(nfe.getMessage());
        }

        if ( tCaseTypeInt != VALUE_NOT_SET){
            Query query = pm.newQuery(TCaseTypeModel.class);
            query.setFilter("typeIdx == param");
            query.declareParameters("Integer param");
            List<TCaseTypeModel> tCaseTypeModels = (List<TCaseTypeModel>) query.execute(tCaseTypeInt);
            if (tCaseTypeModels.isEmpty()){
                tCaseTypeInt = VALUE_NOT_SET;
                logger.warning("TCaseType not found !!! ");
            } else{
                logger.info("TCaseType found : "+ tCaseTypeModels.get(0).getTypeName());
            }
        }

        //get actual rand case
        TCaseDAO tc = RandomTCase.getNextCase(tCaseTypeInt);
        if (tc == null) {
            logger.severe("testCase not found !");
            if (tCaseTypeInt != VALUE_NOT_SET){
                logger.severe("search for all testCasesTypes");
                tc = RandomTCase.getNextCase(VALUE_NOT_SET);
            }
        } else {
            logger.info("got tc.key == " + tc.getKey());
        }
        if (tc == null)
            throw new RpcCustException("No Test case found !");

        return tCaseWraper.getTCaseMod(tc);
    }

    public List<TCaseTypeModel> getTCaseTypes() throws RpcCustException  {
        logger.info("RPCImpl.getTCaseTypes()");
        Extent exents = (Extent) pm.getExtent(TCaseTypeModel.class, false);
        List<TCaseTypeModel> caseTypeModelList = new ArrayList<TCaseTypeModel>();
        for (Object obj : exents)
            caseTypeModelList.add((TCaseTypeModel) obj);

        Collections.sort(caseTypeModelList);
        if (caseTypeModelList.isEmpty())
            throw new RpcCustException("TestCase type  not found !");
        return caseTypeModelList;
    }

    public TCaseModel getTCMock() {
        TCaseModel tCaseModel = new TCaseModel();
        tCaseModel.setCorectAnswer("Answer1");
        tCaseModel.setQuestionText("Jautajums ");
        tCaseModel.addAnswers("Answer1");
        tCaseModel.addAnswers("Answer2");
        tCaseModel.addAnswers("Answer3");
        //tCaseModel.addParagNames("1.");
        //tCaseModel.addParagNames("1.2.");

        return tCaseModel;
    }


}