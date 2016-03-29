/*
 * @(#)AccessDb.java 1.00 08/06/2013
 *
 * Copyright(c) Yutaka Gyotoku. All rights reserved.
 *
 * データベースへのアクセス
 * 
 * 2013.08.03
 *   新規作成
 * 2013.10.30
 *   blob型が取り扱える様に修正
 *   StatementをPreparedStatementへ変更
 *
 *
 */
package jp.gr.gyotoku.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author tokusan
 */
public class AccessDb {
    static private final Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    // データベースの場所
//  private final String fnB = AppMain.userHome
//                           + java.io.File.separator
//                           + "wetbench.sqlite3.backup";
//  private final String fnM = "jdbc:sqlite:memory:";
    
    static private final java.text.SimpleDateFormat fm1 = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    static private final java.text.SimpleDateFormat fmS = new java.text.SimpleDateFormat("HH:mm:ss");
    static private final java.text.SimpleDateFormat fmM = new java.text.SimpleDateFormat("dd HH:mm");
    static private final java.text.SimpleDateFormat fmL = new java.text.SimpleDateFormat("MM/dd HH");
  
    static public String timeStr(long t) {
        if(t==0) {
            return "                   "; // 19文字の空白
        }
        return fm1.format(t*1000);
    }
    static public String timeStr8(long diff,long t) {
        if(t==0) {
            return "        ";
        }
        if(diff>=0) {
            if(diff<86400) {
                // 1日以内
                return fmS.format(t*1000);
            }
            if(diff<864000) {
                // 10日以内
                return fmM.format(t*1000);
            }
        }
        return fmL.format(t*1000);
    }
    static public String timeStr(long st,long en) {
        StringBuilder ln = new StringBuilder();
        // 開示時刻
        ln.append(timeStr(st));
        ln.append(" ~ ");
        // 終了時刻
        ln.append(timeStr8(en-st,en));
        
        return ln.toString();
    }
    static public String currentTimeStr() {
        return fm1.format(currentTimeSec());
    }
    static public Long currentTimeSec() {
        long ans = System.currentTimeMillis()/ 1000;
        if(ans==0) {
            ans=1;
        }
        return ans; 
    }
    
    static private Semaphore dbSem = new Semaphore(1);
    static private Connection con = null;
    static private ResultSet rs  = null;
    // static private Statement  st         = null;
    static private PreparedStatement ps = null;

    static public ResultSet exeQuery(String sql,int t) {
        if(con==null)return null;
        try {
            dbSem.acquire();
        } catch (InterruptedException ex) {
            return null;
        }
        log.info(sql);
        try {
            ps = con.prepareStatement(sql);
            ps.setQueryTimeout(t);
            rs = ps.executeQuery();
        } catch (SQLException ex) {
            String er = ex.getMessage();
            log.log(Level.SEVERE, "{0} \"{1}\"", new Object[]{er, sql});
            QueDialog.showError(er + "\n\"" + sql +"\"");
            dbSem.release();
            return null;
        }
        /*
        try {
            // Statementオブジェクト作成
            st = con.createStatement();
            st.setQueryTimeout(t);
        } catch (SQLException ex) {
            String er = ex.getMessage();
            log.severe(er + " \"" + sql +"\"");
            QueDialog.showError(er + "\n\"" + sql +"\"");
            dbSem.release();
            return null;
        }
        try {
            rs = st.executeQuery(sql);
        } catch (SQLException ex) {
            String er = ex.getMessage();
            log.severe(er + " \"" + sql +"\"");
            QueDialog.showError(er + "\n\"" + sql +"\"");
            dbSem.release();
            return null;
        } */
        return rs;
    }

    public static ResultSet exeQuery(String sql) {
        return exeQuery(sql,5);
    }

    public static void exeUpdate(String sql) {
        exeUpdate(sql, null);
        /*
        if(con==null)return;
        try {
            dbSem.acquire();
        } catch (InterruptedException ex) {
            return;
        }
        log.info(sql);
        try {
            // Statementオブジェクト作成
            st = con.createStatement();
        } catch (SQLException ex) {
            String er = ex.getMessage();
            log.severe(er + " \"" + sql +"\"");
            QueDialog.showError(er + "\n\"" + sql +"\"");
            dbSem.release();
            return;
        }
        try {
            st.executeUpdate(sql);
        } catch (SQLException ex) {
            String er = ex.getMessage();
            log.severe(er + " \"" + sql +"\"");
            QueDialog.showError(er + "\n\"" + sql +"\"");
        }
        release();*/
    }
    public static void exeUpdate(String sql, byte[] bin) {
        if(con==null)return;
        try {
            dbSem.acquire();
        } catch (InterruptedException ex) {
            return;
        }
        log.info(sql);
        try {
            ps = con.prepareStatement(sql);
            if(bin!=null) {
                ps.setBytes(1, bin);
            }
            ps.executeUpdate();
        } catch (SQLException ex) {
            String er = ex.getMessage();
            log.log(Level.SEVERE, "{0} \"{1}\"", new Object[]{er, sql});
            QueDialog.showError(er + "\n\"" + sql +"\"");
        } catch (Exception ex) {
            QueDialog.showThrowable(sql, ex);
        }
        release();
    }
    public static void release() {
        if(rs!=null) {
            try {
                rs.close();
            } catch (SQLException ex) {
                QueDialog.showWarning("rs.close()");
            }
            rs = null;
        }
        if(ps!=null) {
            try {
                ps.close();
            } catch (SQLException ex) {
                QueDialog.showWarning("ps.close()");
            }
            ps = null;
        }
        /*
        if(st!=null) {
            try {
                st.close();
            } catch (SQLException ex) {
                QueDialog.showWarning("st.close()");
            }
            st = null;
        }
        */
        dbSem.release();
    }
    
    
    public static void open(String trgPath) throws ClassNotFoundException,SQLException {
        // JDBCドライバーの指定
        Class.forName("org.sqlite.JDBC");
        // データベースに接続する
        con = DriverManager.getConnection("jdbc:sqlite:"+trgPath);
    }
    public static void close() {
        if(con!=null) {
            // exeUpdate("backup to " + fnB);
            try {
                con.close();
            } catch (SQLException ex) {
                log.warning(ex.getMessage());
            }
            con = null;
        }
    }

}

