package io.matel.app.config;

import io.matel.app.Generator;
import io.matel.app.domain.ContractBasic;
import io.matel.app.macro.domain.MacroDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class DatabaseJDBC {
    protected Connection connection;
    private String port;
    private String url;

    @Autowired
    Global global;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMyy");


    public DatabaseJDBC() { }

    public void init(String databaseName, String port){
        this.port = port;
        connect(databaseName);
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Connection connect(String databaseName) {
        try {
            url = "jdbc:postgresql://127.0.0.1:" + port + "/" + databaseName;
            String login = "matel";
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

    public List<ContractBasic> loadContracts() {
        List<ContractBasic> contracts = new ArrayList<>();
        try {
            String sql = "SELECT idcontract, title, sectype, exchange,currency, symbol, ticksize, rounding, multiplier, expiration, firstnotice, isactive, flowtype, fusion"
                    + " FROM trading.contracts WHERE isactive = true ORDER BY idcontract;";
            ResultSet rs = connection.createStatement().executeQuery(sql);
            while (rs.next()) {
//			LocalDate expiration = rs.getDate(13) == null ? null : rs.getDate(13).toLocalDate();
//			LocalDate firstnotice = rs.getDate(14) == null ? null : rs.getDate(14).toLocalDate();
                String expiration = null;
                if (rs.getDate(10) != null) {
                    expiration = rs.getDate(10).toString().replace("-", "");
                }

                String firstNotice = null;
                if (rs.getDate(11) != null) {
                    firstNotice = rs.getDate(11).toString().replace("-", "");
                }
                ContractBasic contract = new ContractBasic(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6),
                        rs.getDouble(7), rs.getInt(8), rs.getString(9), expiration, firstNotice, rs.getBoolean(12), rs.getString(13), rs.getInt(14));
                contracts.add(contract);
            }
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contracts;
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
                "LEFT JOIN public.macro_update u on f.code = u.code WHERE country LIKE ('" + country_ + "') " +
                "and f.date > NOW() - INTERVAL '7 days' and f.date < NOW() + INTERVAL '15 days'\n" +
                "ORDER BY date";

//        String tickerList="";
        List<MacroDAO> list = new ArrayList<>();
        try {
            ResultSet rs = connection.createStatement().executeQuery(sql);
            while (rs.next()) {
                MacroDAO item = new MacroDAO(rs.getString(1), rs.getDate(2).toLocalDate(),
                        rs.getDouble(3), rs.getDouble(4), rs.getString(5),rs.getString(6),
                        rs.getTimestamp(7).toLocalDateTime().atZone(global.getZoneId()), rs.getTimestamp(8).toLocalDateTime().atZone(global.getZoneId()));
//                System.out.println(item.toString());
//                tickerList = tickerList + " (" + item.getCode() + ")  " + item.getDataName() + " (" + formatter.format(item.getDate()) + ") >>> " + item.getCurrent() +" vs " + item.getPrevious() + "     ";
            list.add(item);
            }
            global.setTickerCrawl(list);
            connection.commit();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void getTicks2018ByContract(ContractBasic contract, Generator gen) {
        try {
            String sql = "SELECT id, contract, date, close, trigger_Down, trigger_up" + " FROM trading.data18 WHERE contract ="
                    + contract.getIdcontract() + " ORDER BY date;";
            ResultSet rs = connection.createStatement().executeQuery(sql);
            while (rs.next()) {
//                Candle candle = new Candle(rs.getTimestamp(3).getTime(), 0, Utils.round(rs.getDouble(4), contract.getRounding()), contract, 0,
//                        rs.getTimestamp(3).getTime());
//                candle.setIdtick(rs.getInt(1));
//                Global.getInstance().setIdTick(rs.getInt(1));
//                candle.setTriggerDown(rs.getInt(5));
//                candle.setTriggerDown(rs.getInt(6));
//                gen.process(candle);
            }
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



//    public Event reqEventState(int idcontract, int freq) {
//        Event event = new Event();
//        String sql = "SELECT date, contract, freq, color, maxtrend, maxvalue, maxvalid, mintrend, minvalue, minvalid, tags, idtick FROM trading.event WHERE contract ="
//                + idcontract + " AND freq=" + freq;
//        try {
//            ResultSet rs = connection.createStatement().executeQuery(sql);
//
//            while (rs.next()) {
//                Map<EventType, Boolean> tags = new HashMap<>();
//                event = new Event(rs.getInt(2), rs.getInt(3));
//                event.setColor(rs.getInt(4));
//                event.setMaxTrend(rs.getBoolean(5));
//                event.setMaxValue(rs.getDouble(6));
//                event.setMaxValid(rs.getDouble(7));
//                event.setMinTrend(rs.getBoolean(8));
//                event.setMinValue(rs.getDouble(9));
//                event.setMinValid(rs.getDouble(10));
//                event.setIdtick(rs.getLong(12));
////				System.out.println(event.toString() + " coucou ");
//            }
//
//            connection.commit();
//            if (event.getIdcontract() != -1)
//                return event;
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

}

