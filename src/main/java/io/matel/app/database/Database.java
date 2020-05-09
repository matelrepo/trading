package io.matel.app.database;

import io.matel.app.AppController;
import io.matel.app.config.Global;
import io.matel.app.domain.Candle;
import io.matel.app.domain.EventType;
import io.matel.app.domain.Tick;
import io.matel.app.state.ProcessorState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
//        LOGGER.info("Creation database " + databaseName);
        connect(databaseName, port, username);
        saverController = new SaverController(this);
    }

    public SaverController getSaverController() {
        return saverController;
    }


    public void close() {
//        LOGGER.info("Closing database " + databaseName);
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
        //    System.out.println(sql);
            ResultSet rs = connection.createStatement().executeQuery(sql);
            while (rs.next()) {
                idTicks.put(rs.getLong(1), rs.getLong(2));
            }
        } catch (SQLException e) {
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
            String sql = "select timestamp, open, high, low, close, idcontract, freq, id_candle, color from public.processor_state WHERE id_tick IN (select id_tick from public.processor_state where id_tick IN " +
                    "(select max(id_tick) from public.processor_state where id_tick <=" + idTick + " and idcontract =" + idContract+ ") limit 1 ) order by freq";
            ResultSet rs = connection.createStatement().executeQuery(sql);
   //         System.out.println(sql);
            while (rs.next()) {
                ZonedDateTime time = rs.getTimestamp(1) ==null ? null : rs.getTimestamp(1).toLocalDateTime().atZone(Global.ZONE_ID);
                 Candle candle = new Candle(time,
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

    public Map<Integer, ProcessorState > getProcessorStateTable(Long idTick, long idContract) {
        Map<Integer, ProcessorState> processorStates = new ConcurrentHashMap<>();
        if(idTick==null)
            idTick=Long.MAX_VALUE;
        try {
            String sql = "select * from public.processor_state WHERE id_tick IN (select id_tick from public.processor_state where id_tick IN " +
                    "(select max(id_tick) from public.processor_state where id_tick <=" + idTick + " and idcontract =" + idContract+ ") limit 1 ) order by freq";
            ResultSet rs = connection.createStatement().executeQuery(sql);
            System.out.println(sql);
            while (rs.next()) {
                ZonedDateTime time = rs.getTimestamp(25) ==null ? null : rs.getTimestamp(25).toLocalDateTime().atZone(Global.ZONE_ID);
                ProcessorState state = new ProcessorState(rs.getLong(11), rs.getInt(7));
                state.setId(rs.getLong(1));
                state.setCheckpoint(rs.getBoolean(2));
                state.setClose(rs.getDouble(3));
                state.setColor(rs.getInt(4));
                state.setEvent(EventType.valueOf(rs.getString(5)));
                state.setActiveEvents(rs.getString(6));
                state.setHigh(rs.getDouble(8));
                state.setIdCandle(rs.getLong(9));
                state.setIdTick(rs.getLong(10));
                state.setTradable(rs.getBoolean(12));
                if(rs.getObject(13)==null){
                    state.setLastDayOfQuarter(null);
                }else{
                    state.setLastDayOfQuarter( rs.getDate(13).toLocalDate());
                }
                state.setLow(rs.getDouble(14));
                state.setMax(rs.getDouble(15));
                state.setMaxTrend(rs.getBoolean(16));
                state.setMaxValid(rs.getDouble(17));
                state.setMaxValue(rs.getDouble(18));
                state.setMin(rs.getDouble(19));
                state.setMinTrend(rs.getBoolean(20));
                state.setMinValid(rs.getDouble(21));
                state.setMinValue(rs.getDouble(22));
                state.setOpen(rs.getDouble(23));
                state.setTarget(rs.getDouble(24));
                state.setTimestamp(time);
                state.setValue(rs.getDouble(26));

                processorStates.put(rs.getInt(7),state);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return processorStates;
    }

    public List<Candle> getHistoricalCandles(Long idcontract, int freq, Long maxIdCandle, boolean clone) {
        List<Candle> candles = new ArrayList<>();
        if(maxIdCandle == null)
            maxIdCandle = Long.MAX_VALUE;
        long id = clone ? idcontract -1000 : idcontract;
        try {
            String sql = "SELECT \n" +
                    "id, idcontract, freq, idtick, timestamp,\n" +
                    "open, high, low, close, \n" +
                    "color, new_candle, progress,\n" +
                    "trigger_down, trigger_up,\n" +
                    "abnormal_height_level, big_candle, close_average,\n" +
                    "created_on, updated_on, volume \n" +
                    "FROM public.candle WHERE idcontract =" + id + " and freq =" + freq + " and id < " + maxIdCandle + " order by timestamp desc limit " + Global.MAX_LENGTH_CANDLE;
    //      System.out.println(sql);
            ResultSet rs = connection.createStatement().executeQuery(sql);
            while (rs.next()) {
                candles.add(new Candle(rs.getLong(1), appController.getGenerators().get( rs.getLong(2)).getContract(), rs.getInt(3), rs.getLong(4),
                        rs.getTimestamp(5).toLocalDateTime().atZone(Global.ZONE_ID),
                        rs.getDouble(6), rs.getDouble(7), rs.getDouble(8), rs.getDouble(9),
                        rs.getInt(10), rs.getBoolean(11), rs.getInt(12),
                        rs.getInt(13), rs.getInt(14), rs.getDouble(15), rs.getBoolean(16), rs.getDouble(17),
                        rs.getTimestamp(18).toLocalDateTime().atZone(Global.ZONE_ID),
                        rs.getTimestamp(19).toLocalDateTime().atZone(Global.ZONE_ID),
                        rs.getInt(20)));
            }
        } catch (SQLException e) {
        }
        return candles;
    }

    public long getTicksByTable(long idcontract, boolean saveTick, String table, long idTick, boolean computing, boolean clone) {
        int count =0;
        long maxIdTick =0;
        ZonedDateTime previousDate = null;
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
                    Tick tick = new Tick(rs.getLong(1), idcon,
                            ZonedDateTime.ofInstant(rs.getTimestamp(5).toInstant(), Global.ZONE_ID), rs.getDouble(2));
                    if(tick.getId() > maxIdTick) maxIdTick = tick.getId();
                    appController.getGenerators().get(tick.getIdcontract()).processPrice(tick, false, saveTick, computing);
                    if(Global.HISTO && Global.hasCompletedLoading) {
                        if(previousDate != null) {
                            try {
                                long speed = (long) (Math.min(Math.max(Duration.between( previousDate, tick.getTimestamp() ).toMillis(),1),5000)
                                        / appController.getGenerators().get(tick.getIdcontract()).getGeneratorState().getSpeedMultiplier());
                                System.out.println(previousDate + " " + tick.getTimestamp() + " " + speed + " " + appController.getGenerators().get(tick.getIdcontract()).getGeneratorState().getSpeedMultiplier());
                                Thread.sleep(speed);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        previousDate = tick.getTimestamp();
                    }
                    count++;
                } catch (NullPointerException e) {
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

    public synchronized int saveProcessorStates(List<ProcessorState> states) {
        int count = 0;
        try (Statement statement = this.connection.createStatement()) {
            String sql = "INSERT INTO public.processor_state " +
                    "(close, color, event, events, freq, high, id_candle, id_tick, idcontract,is_tradable,last_day_of_quarter, low, max, max_trend, max_valid," +
                    "max_value, min, min_trend, min_valid, min_value, open, target, timestamp,value, checkpoint)" +
                    "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            for (ProcessorState state : states) {
                preparedStatement.setDouble(1, state.getClose());
                preparedStatement.setInt(2, state.getColor());
               // System.out.println(state.getEvent());
                if(state.getEvent()==null){
                    preparedStatement.setString(3,null);
                }else{
                    preparedStatement.setString(3,state.getEvent().toString());
                }
                preparedStatement.setString(4,state.getEvents());
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
                if(state.getTimestamp()!=null) {
                    preparedStatement.setTimestamp(23, new Timestamp(state.getTimestamp().toEpochSecond() * 1000));
                }else{
                    preparedStatement.setTimestamp(23,null);
                }
                preparedStatement.setDouble(24, state.getValue());
                preparedStatement.setBoolean(25, state.isCheckpoint());

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
                preparedStatement.setTimestamp(2, new Timestamp(tick.getTimestamp().toEpochSecond()*1000));
                preparedStatement.setDouble(3, tick.getClose());
                preparedStatement.setLong(4, tick.getIdcontract());
                preparedStatement.setInt(5, tick.getTriggerDown());
                preparedStatement.setInt(6, tick.getTriggerUp());
                preparedStatement.setInt(7, tick.getVolume());
                preparedStatement.setTimestamp(8, new Timestamp(ZonedDateTime.now().toEpochSecond()*1000));
                preparedStatement.setTimestamp(9, new Timestamp(ZonedDateTime.now().toEpochSecond()*1000));

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
                preparedStatement.setTimestamp(5, new Timestamp(ZonedDateTime.now().toEpochSecond()*1000));
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
                    "(id, idcontract, freq, idtick, timestamp,\n" +
                    "open, high, low, close,\n" +
                    "color, new_candle, progress,\n" +
                    "trigger_down, trigger_up,\n" +
                    "abnormal_height_level, big_candle, close_average,\n" +
                    "volume, created_on, updated_on, checkpoint)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?, ? ,?,?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            for (Candle candle : candles) {
                preparedStatement.setLong(1, candle.getId());
                preparedStatement.setLong(2, candle.getIdcontract());
                preparedStatement.setInt(3, candle.getFreq());
                preparedStatement.setLong(4, candle.getIdtick());
                preparedStatement.setTimestamp(5, new Timestamp(candle.getTimestamp().toEpochSecond()*1000));
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
                preparedStatement.setTimestamp(19, new Timestamp(ZonedDateTime.now().toEpochSecond()*1000));
                preparedStatement.setTimestamp(20, new Timestamp(ZonedDateTime.now().toEpochSecond()*1000));
                preparedStatement.setBoolean(21, candle.isCheckpoint());


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

    public int updateCandles(long idcontract, double adjustment){
        try (Statement statement = this.connection.createStatement()) {
            String sql = "UPDATE public.tick SET close = close + ? WHERE idcontract =?";
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

}

