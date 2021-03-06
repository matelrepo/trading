package io.matel.app.database;

import io.matel.app.AppController;
import io.matel.app.config.Global;
import io.matel.app.domain.Candle;
import io.matel.app.domain.ContractBasic;
import io.matel.app.domain.EventType;
import io.matel.app.domain.Tick;
import io.matel.app.state.ProcessorState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Database {
    protected Connection connection;

    @Autowired
    AppController appController;


    private static final Logger LOGGER = LogManager.getLogger(Database.class);
    private SaverController saverController;
    private String databaseName;


    public Database(String databaseName, String port, String username) {
        this.databaseName = databaseName;
        connect(databaseName, port, username);
//        saverController = new SaverController(this);
    }

    public void setSaverController(SaverController saverController){
        this.saverController= saverController;

    }

//    public SaverController getSaverController() {
//        return saverController;
//    }


    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Connection connect(String databaseName, String port, String username) {
        try {
            String url = "jdbc:postgresql://127.0.0.1:" + port + "/" + databaseName;
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


    public Long findTopIdTickOrderByIdDesc() {
        Long idTick = null;
        try {
            String sql = "SELECT id FROM public.tick order by id desc limit 1";
            ResultSet rs = connection.createStatement().executeQuery(sql);
            while (rs.next()) {
                idTick = rs.getLong(1);
            }
        } catch (SQLException e) {
        }
        return idTick;
    }


    public Double findTopCloseFromTickByIdContractOrderByTimeStampDesc(long idcontract) {
        Double close = null;
        try {
            String sql = "SELECT close FROM public.tick WHERE idcontract = " + idcontract + " order by timestamp desc limit 1";
            ResultSet rs = connection.createStatement().executeQuery(sql);
            while (rs.next()) {
                close = rs.getDouble(1);
            }
        } catch (SQLException e) {
        }
        return close;
    }

    public Double findTopCloseFromCandleByIdContractOrderByTimeStampDesc(long idcontract) {
        Double close = null;
        try {
            String sql = "SELECT close FROM public.candle WHERE idcontract = " + idcontract + " and freq =1 order by timestamp desc limit 1";
            ResultSet rs = connection.createStatement().executeQuery(sql);
            while (rs.next()) {
                close = rs.getDouble(1);
            }
        } catch (SQLException e) {
        }
        return close;
    }

    public Long findTopIdCandleOrderByIdDesc() {
        Long idCandle = null;
        try {
            String sql = "SELECT id FROM public.candle order by id desc limit 1;";
            ResultSet rs = connection.createStatement().executeQuery(sql);
            while (rs.next()) {
                idCandle = rs.getLong(1);
            }
        } catch (SQLException e) {
        }
        return idCandle;
    }

    public Map<Long, Long> findTopIdTickFromProcessorState() {
        Map<Long, Long> idTicks = new HashMap<>();
        try {
            String sql = "SELECT idcontract, max(id_tick) FROM public.processor_state GROUP BY idcontract";
            ResultSet rs = connection.createStatement().executeQuery(sql);
            while (rs.next()) {
                idTicks.put(rs.getLong(1), rs.getLong(2));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return idTicks;
    }

    public int countIdTickBreaks(long idcontract) {
        int breaks = 0;
        try {
            String sql = "SELECT COUNT(t2.mtick) FROM(SELECT t.mtick from (SELECT max(idtick) as mtick, freq FROM public.candle WHERE idcontract = " + idcontract + " GROUP BY freq ORDER BY freq) as t GROUP BY mtick) as t2";
            ResultSet rs = connection.createStatement().executeQuery(sql);
            while (rs.next()) {
                breaks = rs.getInt(1);
            }
        } catch (SQLException e) {
        }
        return breaks;
    }

    public long findTopIdCandleByIdcontractOrderByIdDesc(long idcontract) {
        long idCandle = 0;
        try {
            String sql = "SELECT MIN(t2.mtick) FROM(SELECT t.mtick from (SELECT max(idtick) as mtick, freq FROM public.candle WHERE idcontract =" + idcontract + " GROUP BY freq ORDER BY freq) as t GROUP BY mtick) as t2";
      //   System.out.println(sql);
            ResultSet rs = connection.createStatement().executeQuery(sql);
            while (rs.next()) {
                idCandle = rs.getLong(1);
            }
        } catch (SQLException e) {
        }
        return idCandle;
    }

    public Long getSmallestIdTickBreak(long idcontract) {
        Long idTick = null;
        try {
            String sql = "SELECT MIN(t2.mtick) FROM( SELECT t.mtick from(SELECT max(idtick) as mtick, freq FROM public.candle WHERE idcontract = " + idcontract + " GROUP BY freq ORDER BY freq) as t GROUP BY mtick) as t2";
            ResultSet rs = connection.createStatement().executeQuery(sql);
            while (rs.next()) {
                idTick = rs.getLong(1);
            }
        } catch (SQLException e) {
        }
        return idTick;
    }

    public List<ContractBasic> getExpirationReport() {
        List<ContractBasic> contracts = new ArrayList<>();
        try {
            String sql = "SELECT idcontract, symbol, title, expiration, first_notice, sec_type, exchange FROM public.contracts WHERE active order by first_notice limit 2;";
            System.out.println(sql);
            ResultSet rs = connection.createStatement().executeQuery(sql);
            while (rs.next()) {
                contracts.add(new ContractBasic(rs.getLong(1), rs.getString(2), rs.getString(3) ,
                        rs.getObject(4, LocalDate.class), rs.getObject(5, LocalDate.class),
                        rs.getString(6), rs.getString(7)));
            }
        } catch (SQLException e) {
        }

        for (ContractBasic contract : contracts) {
            System.out.println(contract.toString());
        }
        return contracts;
    }


//    public Map<Integer, Long > getIdCandlesTable(Long idTick, long idContract) {
//            Map<Integer, Long> idCandlesByFreq = new ConcurrentHashMap<>();
//            if(idTick==null)
//                idTick=Long.MAX_VALUE;
//        try {
//            String sql = "select freq, id_candle, open, high,low,close from public.processor_state WHERE id_tick IN (select id_tick from public.processor_state where id_tick IN " +
//                    "(select max(id_tick) from public.processor_state where id_tick <=" + idTick + " and idcontract =" + idContract+ ") limit 1 ) order by freq";
//           System.out.println(sql);
//            ResultSet rs = connection.createStatement().executeQuery(sql);
//            while (rs.next()) {
//                idCandlesByFreq.put(rs.getInt(1), rs.getLong(2));
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return idCandlesByFreq;
//    }

    public Map<Integer, Candle > getIdCandlesTable(Long idTick, long idContract) {
        Map<Integer, Candle> idCandlesByFreq = new ConcurrentHashMap<>();
        if(idTick==null)
            idTick=Long.MAX_VALUE;
        try {
            String sql = "select timestamp_candle, open, high, low, close, idcontract, freq, id_candle, color, timestamp_tick from public.processor_state WHERE id_tick IN (select id_tick from public.processor_state where id_tick IN " +
                    "(select max(id_tick) from public.processor_state where id_tick <=" + idTick + " and idcontract =" + idContract+ ") limit 1 ) order by freq";
            ResultSet rs = connection.createStatement().executeQuery(sql);
   //         System.out.println(sql);
            while (rs.next()) {
               // ZonedDateTime _timestampCandle = rs.getTimestamp(1) ==null ? null : rs.getTimestamp(1).toLocalDateTime().atZone(Global.ZONE_ID);
                //ZonedDateTime _timestampTick = rs.getTimestamp(8) ==null ? null : rs.getTimestamp(8).toLocalDateTime().atZone(Global.ZONE_ID);
                OffsetDateTime _timestampCandle = rs.getObject(1, OffsetDateTime.class) ==null ? null : rs.getObject(1, OffsetDateTime.class);
                OffsetDateTime _timestampTick = rs.getObject(8, OffsetDateTime.class) ==null ? null : rs.getObject(8, OffsetDateTime.class);
                Candle candle = new Candle(_timestampCandle, _timestampTick,
                        rs.getDouble(2), rs.getDouble(3),
                        rs.getDouble(4), rs.getDouble(5),
                        rs.getLong(6), rs.getInt(7));
                 candle.setId(rs.getLong(8));
                 candle.setColor(rs.getInt(9));
                idCandlesByFreq.put(rs.getInt(7),candle);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return idCandlesByFreq;
    }

//    public Map<Integer, ProcessorState > getProcessorStateTable(Long idTick, long idContract) {
//        Map<Integer, ProcessorState> processorStates = new ConcurrentHashMap<>();
//        if(idTick==null)
//            idTick=Long.MAX_VALUE;
//        try {
//            String sql = "select * from public.processor_state WHERE id_tick IN (select id_tick from public.processor_state where id_tick IN " +
//                    "(select max(id_tick) from public.processor_state where id_tick <=" + idTick + " and idcontract =" + idContract+ ") limit 1 ) order by freq";
//            ResultSet rs = connection.createStatement().executeQuery(sql);
//         //   System.out.println(sql);
//            while (rs.next()) {
//            //    ZonedDateTime time = rs.getTimestamp(25) ==null ? null : rs.getTimestamp(25).toLocalDateTime().atZone(Global.ZONE_ID);
//            //    ZonedDateTime time_candle = rs.getTimestamp(26) ==null ? null : rs.getTimestamp(26).toLocalDateTime().atZone(Global.ZONE_ID);
//                OffsetDateTime time = rs.getObject(25, OffsetDateTime.class) ==null ? null : rs.getObject(25, OffsetDateTime.class);
//                OffsetDateTime time_candle = rs.getObject(26,  OffsetDateTime.class) ==null ? null : rs.getObject(26,OffsetDateTime.class);
//                ProcessorState state = new ProcessorState(rs.getLong(10), rs.getInt(7));
//                state.setId(rs.getLong(1));
//                state.setCheckpoint(rs.getBoolean(2));
//                state.setClose(rs.getDouble(3));
//                state.setColor(rs.getInt(4));
//                state.setEventType(EventType.valueOf(rs.getString(5)));
//                state.setActiveEvents(rs.getString(6));
//                state.setHigh(rs.getDouble(8));
//                state.setIdCandle(rs.getLong(9));
//                state.setIdTick(rs.getLong(10));
//                state.setTradable(rs.getBoolean(12));
//                if(rs.getObject(13)==null){
//                    state.setLastDayOfQuarter(null);
//                }else{
//                    state.setLastDayOfQuarter( rs.getDate(13).toLocalDate());
//                }
//                state.setLow(rs.getDouble(14));
//                state.setMax(rs.getDouble(15));
//                state.setMaxTrend(rs.getBoolean(16));
//                state.setMaxValid(rs.getDouble(17));
//                state.setMaxValue(rs.getDouble(18));
//                state.setMin(rs.getDouble(19));
//                state.setMinTrend(rs.getBoolean(20));
//                state.setMinValid(rs.getDouble(21));
//                state.setMinValue(rs.getDouble(22));
//                state.setOpen(rs.getDouble(23));
//                state.setTarget(rs.getDouble(24));
//                state.setTimestampTick(time);
//                state.setValue(rs.getDouble(27));
//                state.setTimestampCandle(time_candle);
//                state.setAverageClose(rs.getDouble(28));
//                state.setAbnormalHeight(rs.getDouble(29));
//
//                processorStates.put(rs.getInt(7),state);
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return processorStates;
//    }

    public List<Candle> getHistoricalCandles(Long idcontract, int freq, Long maxIdCandle, boolean clone, int numCandles) {
        List<Candle> candles = new ArrayList<>();
        String contractType;
        if (idcontract>=10000){
            contractType = "DAILY";
        }else{
            contractType = "LIVE";
        }
//        if(maxIdCandle == null)
//            maxIdCandle = Long.MAX_VALUE;
        long id = clone ? idcontract -1000 : idcontract;
        try {
            String sql = "SELECT \n" +
                    "id, idcontract, freq, idtick, timestamp_candle,\n" +
                    "open, high, low, close, \n" +
                    "color, new_candle, progress,\n" +
                    "trigger_down, trigger_up,\n" +
                    "abnormal_height_level, big_candle, close_average,\n" +
                    "created_on, updated_on, volume, timestamp_tick, small_candle_noise_removal \n" +
                    "FROM public.candle WHERE idcontract =" + id + " and freq =" + freq + " and id < " + maxIdCandle + " order by timestamp_tick desc limit " + numCandles;
           // System.out.println(sql);
            ResultSet rs = connection.createStatement().executeQuery(sql);
            while (rs.next()) {
              Candle candle =  new Candle(rs.getLong(1), appController.getGenerators().get( rs.getLong(2)).getContract(), rs.getInt(3), rs.getLong(4),
                //      rs.getTimestamp(5).toLocalDateTime().atZone(Global.ZONE_ID),rs.getTimestamp(21).toLocalDateTime().atZone(Global.ZONE_ID),
                      rs.getObject(5, OffsetDateTime.class),rs.getObject(21,OffsetDateTime.class),
                        rs.getDouble(6), rs.getDouble(7), rs.getDouble(8), rs.getDouble(9),
                        rs.getInt(10), rs.getBoolean(11), rs.getInt(12),
                        rs.getInt(13), rs.getInt(14), rs.getDouble(15), rs.getBoolean(16), rs.getDouble(17),
                        rs.getTimestamp(18).toLocalDateTime().atZone(Global.ZONE_ID),
                        rs.getTimestamp(19).toLocalDateTime().atZone(Global.ZONE_ID),
                        rs.getInt(20));
              candle.setSmallCandleNoiseRemoval(rs.getBoolean(22));
                candles.add(candle);
            }
        } catch (SQLException  | NullPointerException e) {
            e.printStackTrace();
        }
        return candles;
    }

    public long getTicksByTable(long idcontract, String table, long idTick, boolean clone) {
        int count =0;
        long maxIdTick =0;
        OffsetDateTime previousDate = null;
        long idcon = clone ? idcontract + 1000 : idcontract;
        try {
            String sql ="";
            if(table.equals("public.tick")){
                sql = "SELECT id, close, created_on, idcontract, timestamp, trigger_down, trigger_up, updated_on FROM " + table + " WHERE idcontract =" + idcontract + " and id>" + idTick + " order by timestamp";

            }else{
                 sql = "SELECT id, close, created, contract, date, trigger_down, trigger_up, updated FROM " + table + " WHERE contract =" + idcontract + " and id>" + idTick + " order by date LIMIT 250000";
            }
            System.out.println(sql);
            ResultSet rs = connection.createStatement().executeQuery(sql);


            while (rs.next()) {
                try {
                  //  Tick tick = new Tick(rs.getLong(1), idcon,
                   //         ZonedDateTime.ofInstant(rs.getTimestamp(5).toInstant(), Global.ZONE_ID), rs.getDouble(2));
                    Tick tick = new Tick(rs.getLong(1), idcon,
                            rs.getObject(5, OffsetDateTime.class), rs.getDouble(2));
                    if(tick.getId() > maxIdTick) maxIdTick = tick.getId();
                 //   System.out.println("Tick >>> " + tick.toString());
                    appController.getGenerators().get(tick.getIdcontract()).processPrice(tick, true, true, false, false, appController.getGenerators().get(tick.getIdcontract()).getFrequencies());
                    if(Global.HISTO && Global.hasCompletedLoading) {
                        if(previousDate != null) {
                            try {
                                long speed = (long) (Math.min(Math.max(Duration.between( previousDate, tick.getTimestamp() ).toMillis(),1),5000)
                                        / appController.getGenerators().get(tick.getIdcontract()).getGeneratorState().getSpeedMultiplier());
                           //     System.out.println(previousDate + " " + tick.getTimestamp() + " " + speed + " " + appController.getGenerators().get(tick.getIdcontract()).getGeneratorState().getSpeedMultiplier());
                                Thread.sleep(speed);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        previousDate = tick.getTimestamp();
                    }
                    count++;
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    LOGGER.warn("Error null pointer in database");

                }
            }

            //LOGGER.info("Historical completed for contract " + idcontract);
            connection.commit();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(count ==0 ) {
           // System.out.println(maxIdTick);

            return -1;
        }
        else {
           // System.out.println(maxIdTick);
            return maxIdTick;
        }
    }

    public synchronized int saveEvents(List<ProcessorState> states) {
        int count = 0;
        try (Statement statement = this.connection.createStatement()) {
            String sql = "INSERT INTO public.events " +
                    "(close, color, event_type, events_list, freq, high, id_candle, id_tick, idcontract,is_tradable,last_day_of_quarter, low, max, max_trend, max_valid," +
                    "max_value, min, min_trend, min_valid, min_value, open, target, timestamp_tick,value, checkpoint, timestamp_candle, average_close, abnormal_height, " +
                    "events_tradable_list, evtype)" +
                    "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            for (ProcessorState state : states) {
                preparedStatement.setDouble(1, state.getClose());
                preparedStatement.setInt(2, state.getColor());
               // System.out.println(state.getEvent());
                if(state.getEventType()==null){
                    preparedStatement.setString(3,null);
                }else{
                    preparedStatement.setString(3,state.getEventType().toString());
                }
                preparedStatement.setString(4,state.getEventsList());
                preparedStatement.setInt(5, state.getFreq());
                preparedStatement.setDouble(6, state.getHigh());
                preparedStatement.setLong(7, state.getIdCandle());
                preparedStatement.setLong(8, state.getIdTick());
                preparedStatement.setLong(9, state.getIdcontract());
                preparedStatement.setBoolean(10,state.isTradable());
                preparedStatement.setObject(11,state.getLastDayOfQuarter());
                preparedStatement.setDouble(12, state.getLow());
                preparedStatement.setDouble(13, state.getMax());
                preparedStatement.setBoolean(14,state.isMaxTrend());
                preparedStatement.setDouble(15, state.getMaxValid());
                preparedStatement.setDouble(16, state.getMaxValue());
                preparedStatement.setDouble(17, state.getMin());
                preparedStatement.setBoolean(18,state.isMinTrend());
                preparedStatement.setDouble(19, state.getMinValid());
                preparedStatement.setDouble(20, state.getMinValue());
                preparedStatement.setDouble(21, state.getOpen());
                preparedStatement.setDouble(22, state.getTarget());
                if(state.getTimestampTick()!=null) {
                    preparedStatement.setObject(23, state.getTimestampTick());
                }else{
                    preparedStatement.setTimestamp(23,null);
                }
                preparedStatement.setDouble(24, state.getValue());
                preparedStatement.setBoolean(25, state.isCheckpoint());
                if(state.getTimestampCandle()!=null) {
                    preparedStatement.setObject(26, state.getTimestampCandle());
                }else{
                    preparedStatement.setTimestamp(26,null);
                }
                preparedStatement.setDouble(27, state.getAverageClose());
                preparedStatement.setDouble(28, state.getAbnormalHeight());
                preparedStatement.setString(29,state.getEventsTradableList());
            preparedStatement.setString(30,state.getEvtype());





            preparedStatement.addBatch();
                count++;
            }
            preparedStatement.executeBatch();
            preparedStatement.close();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    public synchronized int saveTicks(List<Tick> ticks) {
        int count = 0;
        try (Statement statement = this.connection.createStatement()) {
            String sql = "INSERT INTO public.tick " +
                    "(id, timestamp, close, idcontract, trigger_down, trigger_up, volume, created_on, updated_on)" +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            for (Tick tick : ticks) {
                preparedStatement.setLong(1, tick.getId());
                preparedStatement.setObject(2, tick.getTimestamp());
                preparedStatement.setDouble(3, tick.getClose());
                preparedStatement.setLong(4, tick.getIdcontract());
                preparedStatement.setInt(5, tick.getTriggerDown());
                preparedStatement.setInt(6, tick.getTriggerUp());
                preparedStatement.setInt(7, tick.getVolume());
                preparedStatement.setObject(8, OffsetDateTime.now());
                preparedStatement.setObject(9, OffsetDateTime.now());

                preparedStatement.addBatch();
                count++;
            }
            preparedStatement.executeBatch();
            preparedStatement.close();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    public synchronized void updateCandle(Candle candle){
            try (Statement statement = this.connection.createStatement()) {
                String sql = "UPDATE public.candle SET high =?, low = ?, close =?, color = ?, updated_on=?, idtick=? WHERE id =?";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);

                preparedStatement.setDouble(1, candle.getHigh());
                preparedStatement.setDouble(2, candle.getLow());
                preparedStatement.setDouble(3, candle.getClose());
                preparedStatement.setInt(4, candle.getColor());
                preparedStatement.setObject(5, OffsetDateTime.now());
                preparedStatement.setLong(6, candle.getIdtick());
                preparedStatement.setLong(7, candle.getId());

                preparedStatement.execute();
                preparedStatement.close();
                connection.commit();
            } catch (SQLException e) {
                e.printStackTrace();
            }
    }


    public synchronized int saveCandles(List<Candle> candles) {
        int count = 0;
        try (Statement statement = this.connection.createStatement()) {
            String sql = "INSERT INTO public.candle " +
                    "(id, idcontract, freq, idtick, timestamp_candle,\n" +
                    "open, high, low, close,\n" +
                    "color, new_candle, progress,\n" +
                    "trigger_down, trigger_up,\n" +
                    "abnormal_height_level, big_candle, close_average,\n" +
                    "volume, created_on, updated_on, checkpoint, timestamp_tick, small_candle_noise_removal)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?, ? ,?,?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            for (Candle candle : candles) {
                preparedStatement.setLong(1, candle.getId());
                preparedStatement.setLong(2, candle.getIdcontract());
                preparedStatement.setInt(3, candle.getFreq());
                preparedStatement.setLong(4, candle.getIdtick());
                preparedStatement.setObject(5,candle.getTimestampCandle());
                preparedStatement.setDouble(6, candle.getOpen());
                preparedStatement.setDouble(7, candle.getHigh());
                preparedStatement.setDouble(8, candle.getLow());
                preparedStatement.setDouble(9, candle.getClose());
                preparedStatement.setInt(10, candle.getColor());
                preparedStatement.setBoolean(11, candle.isNewCandle());
                preparedStatement.setInt(12, candle.getProgress());
                preparedStatement.setInt(13, candle.getTriggerDown());
                preparedStatement.setInt(14, candle.getTriggerUp());
                preparedStatement.setDouble(15, candle.getAbnormalHeightLevel());
                preparedStatement.setBoolean(16, candle.isBigCandle());
                preparedStatement.setDouble(17, candle.getCloseAverage());
                preparedStatement.setInt(18, candle.getVolume());
                preparedStatement.setObject(19, OffsetDateTime.now());
                preparedStatement.setObject(20, OffsetDateTime.now());
                preparedStatement.setBoolean(21, candle.isCheckpoint());
                preparedStatement.setObject(22, candle.getTimestampTick());
                preparedStatement.setBoolean(23, candle.isSmallCandleNoiseRemoval());



                preparedStatement.addBatch();
                count++;
            }
            preparedStatement.executeBatch();
            preparedStatement.close();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getNextException());
        }
        return count;
    }

    public int adjustTicks(long idcontract, double adjustment){
        try (Statement statement = this.connection.createStatement()) {
            String sql = "UPDATE public.tick SET close = close + ? WHERE idcontract =?";
           // System.out.println(sql);
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setDouble(1,adjustment);
            preparedStatement.setLong(2, idcontract);
            int num = preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.commit();
            return num;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int adjustCandles(long idcontract, double adjustment){
        try (Statement statement = this.connection.createStatement()) {
            String sql = "UPDATE public.candle SET close = close + ?,open = open + ?,high = high + ?,low = low + ?, close_average = close_average + ? WHERE idcontract =?";
            // System.out.println(sql);
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setDouble(1,adjustment);
            preparedStatement.setDouble(2,adjustment);
            preparedStatement.setDouble(3,adjustment);
            preparedStatement.setDouble(4,adjustment);
            preparedStatement.setDouble(5,adjustment);
            preparedStatement.setLong(6, idcontract);


            int num = preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.commit();
            return num;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int adjustProcessorState(long idcontract, double adjustment){
        try (Statement statement = this.connection.createStatement()) {
            String sql = "UPDATE public.processor_state SET close = close + ?, open = open + ?, high = high + ?, low = low + ?," +
                    "max = max + ?, max_valid = max_valid + ?, max_value = max_value + ?, " +
                    "min = min + ?, min_valid= min_valid + ?, min_value = min_value + ?," +
                    "value = value + ?, target = target + ?, average_close = average_close + ?  WHERE idcontract =?";
            // System.out.println(sql);
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setDouble(1,adjustment);
            preparedStatement.setDouble(2,adjustment);
            preparedStatement.setDouble(3,adjustment);
            preparedStatement.setDouble(4,adjustment);
            preparedStatement.setDouble(5,adjustment);
            preparedStatement.setDouble(6,adjustment);
            preparedStatement.setDouble(7,adjustment);
            preparedStatement.setDouble(8,adjustment);
            preparedStatement.setDouble(9,adjustment);
            preparedStatement.setDouble(10,adjustment);
            preparedStatement.setDouble(11,adjustment);
            preparedStatement.setDouble(12,adjustment);
            preparedStatement.setDouble(13,adjustment);
            preparedStatement.setLong(14, idcontract);


            int num = preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.commit();
            return num;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

}

