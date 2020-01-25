package loginapp;

import payments.PaymentsController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    LoginModel loginModel = new LoginModel();

    @FXML
    private Label dbstatus;
    @FXML
    private PasswordField password;
    @FXML
    private Button loginButton;
    @FXML
    private Label loginStatus;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (!this.loginModel.isDatabaseConnected()) {
            this.dbstatus.setText("Nie udało się polączyć z bazą danych");
        }
    }

    @FXML
    public void login(ActionEvent event) {
        try {
            if(this.loginModel.isLogin(this.password.getText())){
                Stage stage = (Stage)this.loginButton.getScene().getWindow();
                stage.close();

                Stage paymentStage = new Stage();
                FXMLLoader loader = new FXMLLoader();
                Pane root = (Pane) loader.load(getClass().getResource("/payments/paymentsFXML.fxml").openStream());

                PaymentsController paymentsController = (PaymentsController) loader.getController();

                Scene scene = new Scene(root);
                scene.getStylesheets().add("/payments/style.css");
                paymentStage.setScene(scene);
                paymentStage.setTitle("Finance Organizer");
                paymentStage.setMinWidth(1200);
                paymentStage.setMinHeight(720);

                paymentStage.show();

            }else{
                this.loginStatus.setText("Nie poprawne hasło");
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void changePassword(ActionEvent event) {
        try {
            Stage changePasswordStage = new Stage();
            FXMLLoader loader = new FXMLLoader();
            Pane root = (Pane) loader.load(getClass().getResource("/loginapp/changePassword.fxml").openStream());
            ChangePasswordController changePasswordController = (ChangePasswordController) loader.getController();
            Scene scene = new Scene(root);
            scene.getStylesheets().add("/loginapp/style.css");
            changePasswordStage.setResizable(false);
            changePasswordStage.setScene(scene);
            changePasswordStage.setTitle("Finance Organizer");
            changePasswordStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }



}
