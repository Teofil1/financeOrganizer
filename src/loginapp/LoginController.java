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
    private TextField username;
    @FXML
    private PasswordField password;
    @FXML
    private Button loginButton;
    @FXML
    private Label loginStatus;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (!this.loginModel.isDatabaseConnected()) {
            this.dbstatus.setText("Not Connected to Database");
        }
    }

    @FXML
    public void login(ActionEvent event) {
        try {
            if(this.loginModel.isLogin(this.username.getText(), this.password.getText())){
                Stage stage = (Stage)this.loginButton.getScene().getWindow();
                stage.close();

                Stage paymentStage = new Stage();
                FXMLLoader loader = new FXMLLoader();
                Pane root = (Pane) loader.load(getClass().getResource("/payments/paymentsFXML.fxml").openStream());

                PaymentsController paymentsController = (PaymentsController) loader.getController();

                Scene scene = new Scene(root);
                paymentStage.setScene(scene);
                paymentStage.setTitle("Finance Organizer");
                paymentStage.setResizable(false);
                paymentStage.show();

            }else{
                this.loginStatus.setText("Wrong Credential");
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    /*public void adminLogin() {
        try {
            Stage adminStage = new Stage();
            FXMLLoader loader = new FXMLLoader();
            Pane root = (Pane) loader.load(getClass().getResource("/payments/paymentsFXML.fxml").openStream());

            PaymentsController paymentsController = (PaymentsController) loader.getController();

            Scene scene = new Scene(root);
            adminStage.setScene(scene);
            adminStage.setTitle("Finance Organizer");
            adminStage.setResizable(false);
            adminStage.show();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }*/

}
