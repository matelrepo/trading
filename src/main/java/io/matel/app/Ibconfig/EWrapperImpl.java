package io.matel.app.Ibconfig;

import com.ib.client.*;
import io.matel.app.AppController;
import io.matel.app.AppLauncher;
import io.matel.app.config.Global;
import io.matel.app.controller.WsController;
import io.matel.app.domain.ContractBasic;
import io.matel.app.portfolio.Portfolio;
import io.matel.app.portfolio.Position;
import io.matel.app.repo.ContractRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

@Configuration
public class EWrapperImpl implements EWrapper {
    private static final Logger LOGGER = LogManager.getLogger(EWrapperImpl.class);
    private EReaderSignal signal;
    private EClientSocket client;

    @Autowired
    DataService dataService;

    @Autowired
    AppLauncher appLauncher;

    @Autowired
    AppController appController;

    @Autowired
    Portfolio portfolio;

    @Autowired
    WsController wsController;

    @Autowired
    ContractRepository contractRepository;

    private boolean hasConnectedAlready = false;

    public EWrapperImpl() {
        init();
    }

    public void init(){
        this.signal = new EJavaSignal();
        this.client = new EClientSocket(this, this.signal);
    }

    public void disconnect() {
        client.eDisconnect();
        connectionClosed();
    }

	public EClientSocket getClient() {
		return client;
	}


    @Override
    public void error(Exception e) {
        System.out.println(e);
    }

    @Override
    public void error(String str) {
        System.out.println(str);
    }

    @Override
    public void error(int id, int errorCode, String errorMsg) {
        if (errorCode == 2104 || errorCode == 2103 || errorCode == 2106 || errorCode == 10167 || errorCode == 300) {
        } else {
            LOGGER.warn(">>> ERROR >>> " + id + " " + errorCode + " " + errorMsg);
            if(errorCode ==200){
                System.out.println(appController.getGenerators().get(Long.valueOf(id)));
                appController.getGenerators().get(Long.valueOf(id)).getGeneratorState().setMarketDataStatus(0);
            }
        }

        if (errorCode == 507 || errorCode == 502) {
            disconnect();
        }

    }

    @Override
    public void nextValidId(int orderId) {
        System.out.println("next valid id");
        if (!hasConnectedAlready) {
            client.reqAccountUpdates(true, Global.ACCOUNT_NUMBER);
//            client.reqAccountSummary(9001, "All", "$LEDGER");
//            client.reqPositions();
            client.reqPnL(17001, Global.ACCOUNT_NUMBER, "");
            appLauncher.startLive();

        } else {
            while(!client.isConnected()){
                LOGGER.warn("Trying to reconnect market data but client is not connected");
            }
            LOGGER.info("Reconnecting market data");
            appController.connectAllMarketData();
        }
        hasConnectedAlready = true;
    }

    @Override
    public void connectionClosed() {
        try {
            Thread.sleep(60000);
            init();
            dataService.connect();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connectAck() {
        LOGGER.info(">>> IB CONNECTED");
        final EReader reader = new EReader(client, signal);
        reader.start();

        new Thread(() -> {
            while (client.isConnected()) {
                signal.waitForSignal();
                try {
                    reader.processMsgs();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void tickPrice(int tickerId, int field, double price, TickAttrib attrib) {
        dataService.getOpenConnectionsContract().get(Long.valueOf(tickerId)).tickPrice(tickerId, field, price, attrib);
    }

    @Override
    public void tickSize(int tickerId, int field, int size) {
        dataService.getOpenConnectionsContract().get(Long.valueOf(tickerId)).tickSize(tickerId, field,size);
    }

    @Override
    public void tickOptionComputation(int tickerId, int field, double impliedVol, double delta, double optPrice, double pvDividend, double gamma, double vega,
                                      double theta, double undPrice) {

    }

    @Override
    public void tickGeneric(int tickerId, int tickType, double value) {

    }

    @Override
    public void tickString(int tickerId, int tickType, String value) {

    }

    @Override
    public void tickEFP(int tickerId, int tickType, double basisPoints, String formattedBasisPoints, double impliedFuture, int holdDays, String futureLastTradeDate,
                        double dividendImpact, double dividendsToLastTradeDate) {

    }

    @Override
    public void orderStatus(int orderId, String status, double filled, double remaining, double avgFillPrice, int permId, int parentId, double lastFillPrice,
                            int clientId, String whyHeld, double mktCapPrice) {
        // TODO Auto-generated method stub

    }

    @Override
    public void openOrder(int orderId, Contract contract, Order order, OrderState orderState) {
        // TODO Auto-generated method stub

    }

    @Override
    public void openOrderEnd() {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateAccountValue(String key, String value, String currency, String accountName) {
        if(((key.equals("InitMarginReq") || key.equals("MaintMarginReq")) && currency.equals("USD")) || ((key.equals("NetLiquidationByCurrency") ||
                key.equals("UnrealizedPnL") || key.equals("RealizedPnL")) && currency.equals("BASE"))){
            double val = Double.parseDouble(value);
            portfolio.setLastUpdate(ZonedDateTime.now());
            switch(key){
                case "MaintMarginReq":
                    portfolio.setMaintMarginReq(val);
                    break;
                case "InitMarginReq":
                    portfolio.setInitMarginReq(val);
                    break;
                case "NetLiquidationByCurrency":
//                    portfolio.setNetLiquidation(val);
//                    appController.getGenerators().get(1L).tickPrice(1,4,val,null);
                    break;
                case "UnrealizedPnL":
                    portfolio.setUnrealizedPnl(val);
                    break;
                case "RealizedPnL":
                    portfolio.setRealizedPnl(val);
                    break;
            }
            portfolio.setExcessLiq(portfolio.getNetLiquidation() - portfolio.getMaintMarginReq());
            //System.out.println(portfolio.toString());
        }

    }

    @Override
    public void updatePortfolio(Contract contract, double position, double marketPrice, double marketValue,
                                double averageCost, double unrealizedPNL, double realizedPNL, String accountName) {
        if(portfolio.getPositions().get(contract.conid()) == null){
            portfolio.getPositions().put(contract.conid(), new Position(contract.symbol(), position, marketPrice, marketValue, averageCost, unrealizedPNL, realizedPNL));
        }else {
            portfolio.getPositions().get(contract.conid()).updatePosition(position, marketPrice, marketValue, averageCost, unrealizedPNL, realizedPNL);
        }
        if(!portfolio.getPositions().get(contract.conid()).isConnected()){
            client.reqPnLSingle(contract.conid(), Global.ACCOUNT_NUMBER, "", contract.conid());
        }
        wsController.sendPortoflio(portfolio);
        //System.out.println(portfolio.getPositions().get(contract.conid()).toString());

    }

    @Override
    public void updateAccountTime(String timeStamp) {
    }

    @Override
    public void accountDownloadEnd(String accountName) {
        LOGGER.info("End Download: " + accountName);

    }

    @Override
    public void accountSummary(int reqId, String account, String tag, String value, String currency) {
    }

    @Override
    public void accountSummaryEnd(int reqId) {
    }


    @Override
    public void position(String account, Contract contract, double pos, double avgCost) {
    }


    @Override
    public void positionEnd() {
    }

    @Override
    public void pnl(int reqId, double dailyPnL, double unrealizedPnL, double realizedPnL) {
        portfolio.setDailyPnl(dailyPnL);
        portfolio.setRealizedPnl(realizedPnL);
        portfolio.setUnrealizedPnl(unrealizedPnL);
        wsController.sendPortoflio(portfolio);
        //System.out.println(portfolio.toString());
    }

    @Override
    public void pnlSingle(int reqId, int pos, double dailyPnL, double unrealizedPnL, double realizedPnL, double value) {
        portfolio.getPositions().get(reqId).setConnected(true);
        portfolio.getPositions().get(reqId).setDailyPnl(dailyPnL);
        portfolio.getPositions().get(reqId).setUnrealizedPnl(unrealizedPnL);
        portfolio.getPositions().get(reqId).setRealizedPnl(realizedPnL);
        wsController.sendPortoflio(portfolio);
        //System.out.println(portfolio.getPositions().get(reqId).toString());
    }

    @Override
    public void contractDetails(int reqId, ContractDetails contractDetails) {
ContractBasic contract = appController.getGenerators().get(Long.valueOf(reqId)).getContract();
        if(contract.getConid()==null) {
            contract.setConid(contractDetails.conid());
            contractRepository.save(contract);
        }
//System.out.println(contract.toString());
    }

    @Override
    public void bondContractDetails(int reqId, ContractDetails contractDetails) {
        // TODO Auto-generated method stub

    }

    @Override
    public void contractDetailsEnd(int reqId) {
//        System.out.println("Contract details");
    }

    @Override
    public void execDetails(int reqId, Contract contract, Execution execution) {
        // TODO Auto-generated method stub

    }

    @Override
    public void execDetailsEnd(int reqId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateMktDepth(int tickerId, int position, int operation, int side, double price, int size) {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateMktDepthL2(int tickerId, int position, String marketMaker, int operation, int side, double price, int size, boolean isSmartDepth) {

    }


    @Override
    public void updateNewsBulletin(int msgId, int msgType, String message, String origExchange) {
        // TODO Auto-generated method stub

    }

    @Override
    public void managedAccounts(String accountsList) {
        // TODO Auto-generated method stub

    }

    @Override
    public void receiveFA(int faDataType, String xml) {
        // TODO Auto-generated method stub

    }

    @Override
    public void historicalData(int reqId, Bar bar) {
        // TODO Auto-generated method stub

    }

    @Override
    public void scannerParameters(String xml) {
        // TODO Auto-generated method stub

    }

    @Override
    public void scannerData(int reqId, int rank, ContractDetails contractDetails, String distance, String benchmark, String projection, String legsStr) {
        // TODO Auto-generated method stub

    }

    @Override
    public void scannerDataEnd(int reqId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void realtimeBar(int reqId, long time, double open, double high, double low, double close, long volume, double wap, int count) {
        // TODO Auto-generated method stub

    }

    @Override
    public void currentTime(long time) {
        // TODO Auto-generated method stub

    }

    @Override
    public void fundamentalData(int reqId, String data) {
        // TODO Auto-generated method stub

    }

    @Override
    public void deltaNeutralValidation(int reqId, DeltaNeutralContract deltaNeutralContract) {
        // TODO Auto-generated method stub

    }

    @Override
    public void tickSnapshotEnd(int reqId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void marketDataType(int reqId, int marketDataType) {
        // TODO Auto-generated method stub

    }

    @Override
    public void commissionReport(CommissionReport commissionReport) {
        // TODO Auto-generated method stub

    }


    @Override
    public void verifyMessageAPI(String apiData) {
        // TODO Auto-generated method stub

    }

    @Override
    public void verifyCompleted(boolean isSuccessful, String errorText) {
        // TODO Auto-generated method stub

    }

    @Override
    public void verifyAndAuthMessageAPI(String apiData, String xyzChallenge) {
        // TODO Auto-generated method stub

    }

    @Override
    public void verifyAndAuthCompleted(boolean isSuccessful, String errorText) {
        // TODO Auto-generated method stub

    }

    @Override
    public void displayGroupList(int reqId, String groups) {
        // TODO Auto-generated method stub

    }

    @Override
    public void displayGroupUpdated(int reqId, String contractInfo) {
        // TODO Auto-generated method stub

    }

    @Override
    public void positionMulti(int reqId, String account, String modelCode, Contract contract, double pos, double avgCost) {
        // TODO Auto-generated method stub

    }

    @Override
    public void positionMultiEnd(int reqId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void accountUpdateMulti(int reqId, String account, String modelCode, String key, String value, String currency) {
        // TODO Auto-generated method stub

    }

    @Override
    public void accountUpdateMultiEnd(int reqId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void securityDefinitionOptionalParameter(int reqId, String exchange, int underlyingConId, String tradingClass, String multiplier, Set<String> expirations,
                                                    Set<Double> strikes) {
        // TODO Auto-generated method stub

    }

    @Override
    public void securityDefinitionOptionalParameterEnd(int reqId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void softDollarTiers(int reqId, SoftDollarTier[] tiers) {
        // TODO Auto-generated method stub

    }

    @Override
    public void familyCodes(FamilyCode[] familyCodes) {
        // TODO Auto-generated method stub

    }

    @Override
    public void symbolSamples(int reqId, ContractDescription[] contractDescriptions) {
        // TODO Auto-generated method stub

    }

    @Override
    public void historicalDataEnd(int reqId, String startDateStr, String endDateStr) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mktDepthExchanges(DepthMktDataDescription[] depthMktDataDescriptions) {
        // TODO Auto-generated method stub

    }

    @Override
    public void tickNews(int tickerId, long timeStamp, String providerCode, String articleId, String headline, String extraData) {
        // TODO Auto-generated method stub

    }

    @Override
    public void smartComponents(int reqId, Map<Integer, Entry<String, Character>> theMap) {
        // TODO Auto-generated method stub

    }

    @Override
    public void tickReqParams(int tickerId, double minTick, String bboExchange, int snapshotPermissions) {
        // TODO Auto-generated method stub

    }

    @Override
    public void newsProviders(NewsProvider[] newsProviders) {
        // TODO Auto-generated method stub

    }

    @Override
    public void newsArticle(int requestId, int articleType, String articleText) {
        // TODO Auto-generated method stub

    }

    @Override
    public void historicalNews(int requestId, String time, String providerCode, String articleId, String headline) {
        // TODO Auto-generated method stub

    }

    @Override
    public void historicalNewsEnd(int requestId, boolean hasMore) {
        // TODO Auto-generated method stub

    }

    @Override
    public void headTimestamp(int reqId, String headTimestamp) {
        // TODO Auto-generated method stub

    }

    @Override
    public void histogramData(int reqId, List<HistogramEntry> items) {
        // TODO Auto-generated method stub

    }

    @Override
    public void historicalDataUpdate(int reqId, Bar bar) {
        // TODO Auto-generated method stub

    }

    @Override
    public void rerouteMktDataReq(int reqId, int conId, String exchange) {
        // TODO Auto-generated method stub

    }

    @Override
    public void rerouteMktDepthReq(int reqId, int conId, String exchange) {
        // TODO Auto-generated method stub

    }

    @Override
    public void marketRule(int marketRuleId, PriceIncrement[] priceIncrements) {
        // TODO Auto-generated method stub

    }

    @Override
    public void historicalTicks(int reqId, List<HistoricalTick> ticks, boolean done) {
        // TODO Auto-generated method stub

    }

    @Override
    public void historicalTicksBidAsk(int reqId, List<HistoricalTickBidAsk> ticks, boolean done) {
        // TODO Auto-generated method stub

    }

    @Override
    public void historicalTicksLast(int reqId, List<HistoricalTickLast> ticks, boolean done) {
        // TODO Auto-generated method stub

    }

    @Override
    public void tickByTickAllLast(int reqId, int tickType, long time, double price, int size, TickAttribLast tickAttribLast, String exchange, String specialConditions) {

    }

    @Override
    public void tickByTickBidAsk(int reqId, long time, double bidPrice, double askPrice, int bidSize, int askSize, TickAttribBidAsk tickAttribBidAsk) {

    }


    @Override
    public void tickByTickMidPoint(int reqId, long time, double midPoint) {
        // TODO Auto-generated method stub

    }

    @Override
    public void orderBound(long orderId, int apiClientId, int apiOrderId) {

    }

    @Override
    public void completedOrder(Contract contract, Order order, OrderState orderState) {

    }

    @Override
    public void completedOrdersEnd() {

    }

}
