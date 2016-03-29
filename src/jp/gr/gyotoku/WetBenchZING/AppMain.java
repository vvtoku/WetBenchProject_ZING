
package jp.gr.gyotoku.WetBenchZING;

import java.sql.SQLException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import jp.gr.gyotoku.util.AccessDb;
import jp.gr.gyotoku.util.QueDialog;
import jp.gr.gyotoku.util.QueDialogTask;
/**
 *
 * @author tokusan
 */
public class AppMain extends Application {
    static String[] aArgs;
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));
        
        Scene scene = new Scene(root);
        QueDialogTask.start(stage);
        
        stage.setScene(scene);
        stage.show();
        if (aArgs != null) {
            for(String a : aArgs) {
                QueDialog.showError("*" + a);

            } 
        }
        QueDialog.showError("3");
        QueDialog.showError("4");
        // ダイアログ表示タスクを起動
        try {
            AccessDb.open("test");
        } catch (ClassNotFoundException ex) {
            QueDialog.showError(String.format("Fail to connect SQLite3 driver."));
        } catch (SQLException ex) {
            QueDialog.showError(String.format("Fail to access database file '%s'.", "test"));
        }
        // stage.hide();
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
        aArgs = args;
    }
}
