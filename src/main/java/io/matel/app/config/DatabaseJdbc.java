package io.matel.app.config;

import io.matel.app.AppController;
import io.matel.app.domain.Candle;
import io.matel.app.domain.ContractBasic;
import io.matel.app.domain.Tick;
import io.matel.app.macro.domain.MacroDAO;
import io.matel.app.tools.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.sql.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class DatabaseJdbc {
    protected Connection connection;
    private String port;
    private String url;

    @Autowired
    Global global;

    @Autowired
    AppController appController;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMyy");


    public DatabaseJdbc() { }

    public void init(String databaseName, String port, String username){
        this.port = port;
        connect(databaseName, username);
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Connection connect(String databaseName, String username) {
        try {
            url = "jdbc:postgresql://127.0.0.1:" + port + "/" + databaseName;
            String login = username;
            String password = "Mq1rrill";
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(url, login, password);
            connection.setTransactionIsolation(java.sql.Connection.TRANSACTION_READ_COMMITTED);
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            statement.execute("set search_path='" + databaseName + "';");
            statement.close();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }


    public void getMacroItemsByCountry(String country_) {
        String sql = "SELECT f.code, f.date, f.current, f.previous, u.country, u.data_name, u.created_on, u.updated_on FROM (\n" +
                "SELECT a.code, a.date, a.value as current, b.value as previous FROM (\n" +
                "SELECT * from (\n" +
                "SELECT\n" +
                "   code, date, value,\n" +
                "   DENSE_RANK () OVER ( \n" +
                "      PARTITION BY code\n" +
                "      ORDER BY date DESC\n" +
                "   ) date_rank \n" +
                "FROM\n" +
                "   public.macro_data order by date desc\n" +
                "\t) t1 WHERE date_rank =1\n" +
                "\t) a\n" +
                "\tLEFT JOIN(\n" +
                "\tSELECT * FROM (\n" +
                "\tSELECT * from (\n" +
                "SELECT\n" +
                "   code, date, value,\n" +
                "   DENSE_RANK () OVER ( \n" +
                "      PARTITION BY code\n" +
                "      ORDER BY date DESC\n" +
                "   ) date_rank \n" +
                "FROM\n" +
                "   public.macro_data order by date desc\n" +
                "\t) t1 WHERE date_rank =2\n" +
                "\t\t\n" +
                "\t\t) c) b ON a.code = b.code order by a.date desc ) f\n" +
                "LEFT JOIN public.macro_update u on f.code = u.code WHERE " +
                "f.date > NOW() - INTERVAL '7 days' and f.date < NOW() + INTERVAL '15 days'\n" +
                "ORDER BY date, created_on desc";

//        String tickerList="";
        List<MacroDAO> list = new ArrayList<>();
        try {
            ResultSet rs = connection.createStatement().executeQuery(sql);
            while (rs.next()) {
                MacroDAO item = new MacroDAO(rs.getString(1), rs.getDate(2).toLocalDate(),
                        rs.getDouble(3), rs.getDouble(4), rs.getString(5),rs.getString(6),
                        rs.getTimestamp(7).toLocalDateTime().atZone(global.getZoneId()), rs.getTimestamp(8).toLocalDateTime().atZone(global.getZoneId()));
         list.add(item);
            }
            global.setTickerCrawl(list);
            connection.commit();

        } catch (SQLException e) {
        }
    }

//    public long getMaxIdDailyCandle() throws SQLException {
//        String sql = "SELECT date, open, high, low, close, symbol, volume FROM public.daily_candle WHERE id > " + idTick + " order by date;";
//        ResultSet rs = connection.createStatement().executeQuery(sql);
//    }

    public void getDailyConHisto(long idTick) {

        try {
            String sql = "SELECT date, open, high, low, close, symbol, volume FROM public.daily_candle WHERE id > " + idTick + " order by date;";
            ResultSet rs = connection.createStatement().executeQuery(sql);
            while (rs.next()) {
                ContractBasic contract = appController.getContractsBySymbol().get(rs.getString(6));
                ZonedDateTime date = rs.getTimestamp(1).toLocalDateTime().atZone(global.getZoneId());
                try {
                    Candle candle = new Candle(date, Utils.round(rs.getDouble(2), contract.getRounding()), Utils.round(rs.getDouble(3), contract.getRounding()),
                            Utils.round(rs.getDouble(4), contract.getRounding()), Utils.round(rs.getDouble(5),
                            contract.getRounding()), contract.getIdcontract(), 1380);
//                candle.setIdtick(rs.getInt(1));
//                Global.getInstance().setIdTick(rs.getInt(1));
//                candle.setTriggerDown(rs.getInt(5));
//                candle.setTriggerDown(rs.getInt(6));
                    appController.getGenerators().get(contract.getIdcontract()).process(candle);
                }catch(NullPointerException e){
                    e.getMessage();
                }
            }
            connection.commit();
            rs.close();
        } catch (SQLException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void getTicks2018(){
        try {
            String sql = "SELECT id, close, created, contract, date, trigger_down, trigger_up, updated FROM trading.data18 where contract =5 order by date LIMIT 100";
            System.out.println(sql);
            ResultSet rs = connection.createStatement().executeQuery(sql);
            while (rs.next()) {
                try {
                    Tick tick = new Tick(rs.getLong(4), rs.getTimestamp(5).toLocalDateTime().atZone(global.getZoneId()), rs.getDouble(2));
                    System.out.println(tick.toString());
                    appController.getGenerators().get(tick.getIdcontract()).processPrice(tick, false);
                }catch(NullPointerException e){

                }
            }

            System.out.println("finish");
            connection.commit();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}

