/*
 * @(#)QueDialog.java 1.01 2013/08/06
 *
 * Copyright(c) Yutaka Gyotoku. All rights reserved.
 * 
 * ダイアログを表示する。ダイアログはQueへ入れられ、表示は別タスクから行われる
 * 
 * 使用例：
 *   QueDialog.showConfirm(
 *       "アプリケーションを終了しますか？",
 *       new String[] {"Cancel","OK"},
 *       new EventHandler() {
 *           @Override
 *           public void handle(Event t) {
 *               Button n = (Button)t.getSource();
 *               switch(n.getText()) {
 *                   case "OK":
 *                       queThread.interrupt();
 *                       mainStage.close();
 *                       break;
 *               }
 *           }
 *       }
 *   );
 *   QueDialog.showInfo("GPIB通信", "REN信号をアクティブにしました。");
 *
 * コンストラクタを一つにまとめたいな
 * 
 */
package jp.gr.gyotoku.util;

import java.util.concurrent.ArrayBlockingQueue;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.Window;

public class QueDialog {
    protected static ArrayBlockingQueue<QueDialog> aDialogQueue = new ArrayBlockingQueue<>(20);
    protected static boolean hasActiveDialog = false;
    protected enum DialogType {SHOW_INFO, SHOW_WARNING, SHOW_ERROR, SHOW_THROWABLE, SHOW_CONFIRM}
    DialogType aType;
    String aTitle;
    String aMessage;
    String[] buttons;
    Window aOwner;
    EventHandler aHandler;
    Throwable aThrow;
    
    protected QueDialog(DialogType y, String t, String m, Window w, EventHandler h, Throwable e) {
        aType    = y;
        aTitle   = t;
        aMessage = m;
        aOwner   = w;
        aHandler = h;
        aThrow   = e;
    }
    protected QueDialog(DialogType y, String m) {
        aType    = y;
        aTitle   = appName;
        aMessage = m;
        aOwner   = ownerWin;
    }
    protected QueDialog(DialogType y, String m, Throwable e) {
        aType    = y;
        aTitle   = appName;
        aMessage = m;
        aOwner   = ownerWin;
        aThrow   = e;
    }
    protected QueDialog(DialogType y, String m, String[] b, EventHandler h) {
        aType    = y;
        aTitle   = appName;
        aMessage = m;
        aHandler = h;
        buttons  = b;
        aOwner   = ownerWin;
    }
    /**
     * Add the Dialog to the Queue
     * 
     * @return
     */
    protected void queue() {
        aDialogQueue.offer(this);
    }
    /**
     * 
     * 
     */
    protected static String appName = "";
    public static Window ownerWin;
    public static void setTitle(String aName) {
        appName  = aName;
    }
    public static void setOwner(Stage aOwn) {
        ownerWin = aOwn;
    }
    /**
     * Show information dialog box as parentStage child
     * 
     * @param message dialog message
     */
    public static void showInfo(String message) {
        new QueDialog(
                DialogType.SHOW_INFO,
                message
        ).queue();
    }
    /**
     * Show information dialog box with OK buttons
     * 
     * @param message dialog message
     * @param handler event handler 
    public static void showInfo(String message, String[] buttons, EventHandler handler) {
        new QueDialog(
                DialogType.SHOW_INFO,
                message,
                buttons,
                handler
        ).queue();
    }    /**
     * Show information dialog box with OK buttons
     * 
     * @param message dialog message
     * @param handler event handler 
    public static void showInfo(String message, String[] buttons, EventHandler handler) {
        new QueDialog(
                DialogType.SHOW_INFO,
                message,
                buttons,
                handler
        ).queue();
    }
     */

    /**
     * Show warning dialog box
     * 
     * @param message dialog message
     */
    public static void showWarning(String message) {
        new QueDialog(
                DialogType.SHOW_WARNING,
                message
        ).queue();
    }
    /**
     * Show warning dialog box with some buttons
     * 
     * @param title dialog title
     * @param message dialog message
     * @param button name list of buttons
     * @param handler event handler 
     *
    public static void showWarning(String message, String[] buttons,EventHandler handler) {
        new QueDialog(DialogType.SHOW_WARNING,
                appName,message,buttons,handler).queue();    /**
     * Show warning dialog box with some buttons
     * 
     * @param title dialog title
     * @param message dialog message
     * @param button name list of buttons
     * @param handler event handler 
     *
    public static void showWarning(String message, String[] buttons,EventHandler handler) {
        new QueDialog(DialogType.SHOW_WARNING,
                appName,message,buttons,handler).queue();
    } */

    /**
     * Show error dialog box
     * 
     * @param message dialog message
     */
    public static void showError(String message) {
        new QueDialog(
                DialogType.SHOW_ERROR,
                message
        ).queue();
    }
    
    
    /**
     * Show error dialog box with stacktrace
     * 
     * @param message dialog message
     * @param ex throwable
     */
    public static void showThrowable(String message, Throwable ex) {
        new QueDialog(
                DialogType.SHOW_THROWABLE,
                message,
                ex
        ).queue();
    }

    /**
     * Show confirm dialog box with some buttons
     * 
     * @param message dialog message
     * @param buttons name list of buttons
     * @param handler event handler 
     */
    public static void showConfirm(String message, String[] buttons, EventHandler handler) {
        new QueDialog(
                DialogType.SHOW_CONFIRM,
                message,
                buttons,
                handler
        ).queue();
    }
    
    /**
     * Dequeue and show the Dialog
     * 
     * @return
     */
    public static void showNextDialog() {
        QueDialog d=aDialogQueue.poll();
        if(d==null) {
            hasActiveDialog = false;
            return;
        }
        switch(d.aType) {
            case SHOW_INFO:
                Dialog.showInfo(d.aTitle,d.aMessage,d.aOwner);
                break;
            case SHOW_WARNING:
                Dialog.showWarning(d.aTitle,d.aMessage,d.aOwner);
                break;
            case SHOW_ERROR:
                Dialog.showError(d.aTitle,d.aMessage,d.aOwner);
                break;
            case SHOW_THROWABLE:
                Dialog.showThrowable(d.aTitle,d.aMessage,d.aThrow,d.aOwner);
                break;
            case SHOW_CONFIRM:
                Dialog.showConfirmation(d.aTitle,d.aMessage,d.buttons,d.aHandler,d.aOwner);
                break;
        }
    }
    
//    public static boolean hasDialog() {
//        return hasActiveDialog || !aDialogQueue.isEmpty();
//    }
    
    protected static void deactive() {
        hasActiveDialog = false;
    }

    /**
     * 次のダイアログがあるか調べ、あれば表示する様に指示を出す
     * 
     * @return true 新しく表示されたダイアログがあった、または表示中
     */
    public static boolean hasNextDialog() {
        if(hasActiveDialog) {
            return true;
        }
        if(aDialogQueue.isEmpty()) {
            return false;
        }
        hasActiveDialog = true;
        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                QueDialog.showNextDialog();
            }
        });
        return true;
    }
}
