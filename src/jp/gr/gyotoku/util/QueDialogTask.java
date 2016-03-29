/*
 * @(#)QueDialogTask.java 1.01 2013/08/06
 *
 * Copyright(c) Yutaka Gyotoku. All rights reserved.
 * 
 * Queされたダイアログを一つづつ取り出して表示する為のThread
 * アプリケーション起動時に起動させる
 *   QueDialogTask.start();
 *
 * 2013.10.29
 *   ThreadからTimerTaskへ変更
 *   start()とstop()を追加
 * 2016.03.20
 *   mainStageがhideされるとPlathome.runlater()か起動しなくなるのね
 *   なのでmainStageをこのタスクでshow & hideする様にする？
 *
 */
package jp.gr.gyotoku.util;

import java.util.Timer;
import java.util.TimerTask;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author tokusan
 */
public class QueDialogTask extends TimerTask {
    
    // このクラスに唯一のインスタンス クラスのロード時に生成される
    private static QueDialogTask instance = new QueDialogTask();
    // privateなコンストラクタでインスタンスの生成を制限する
    private QueDialogTask() {
        
    }
    
    private Stage mainStage;
    private Timer tm;
    @Override
    public void run() {
        if(!QueDialog.hasNextDialog()) {
            if(!mainStage.isShowing()) {
                tm.cancel();
            }
        }
    }
    
    private void startThis() {
        if(tm==null) {
            tm = new Timer(this.getClass().getName());
            tm.schedule(this, 500, 2500);
        }
    }
    
    static public void start(Stage aStage) {
        instance.mainStage = aStage;
        instance.startThis();
        // 主ウィンドウを閉じようとしたときにダイアログを表示する様にする
        aStage.setOnCloseRequest(
            new EventHandler<WindowEvent>() {
                @Override
                public void handle(final WindowEvent event) {
                    confirmQuit();
                    event.consume();
                }
            }
        );
    }

    static private boolean hasQuitDialog = false;
    static void confirmQuit() {
        if(hasQuitDialog) {
            return;
        } 
        hasQuitDialog = true;
        QueDialog.showConfirm(
            "Are you sure to quit '" + "appName" + "' ?",
            new String[] {"Cancel","OK"},
            new EventHandler() {
                @Override
                public void handle(Event t) {
                    Button n = (Button)t.getSource();
                    switch(n.getText()) {
                        case "OK":
                            // quitAnnounce();
                    }
                    hasQuitDialog = false;
                }
            }
        );
    }
}
