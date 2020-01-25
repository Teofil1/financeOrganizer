package loginapp;

import dbUtil.dbConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

public class ChangePasswordController implements Initializable {

    @FXML
    private Label changePasswordStatus;
    @FXML
    private TextField oldPassword;
    @FXML
    private TextField newPassword;
    @FXML
    private TextField repeatedNewPassword;

    private Connection conn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            this.conn = dbConnection.getConnection();
        }catch (SQLException ex){
            ex.printStackTrace();
        }
    }

    @FXML
    public void saveNewPassword(ActionEvent event) {
        if(correctInputOfOldPassword()){
            if (correctRepetitionOfPassword()){
                Alert alertConfirm = new Alert(Alert.AlertType.CONFIRMATION);
                alertConfirm.setTitle("Confirmation Dialog");
                alertConfirm.setHeaderText("");
                alertConfirm.setContentText("Na pewno chcesz zmienić hasło?");
                Optional<ButtonType> result = alertConfirm.showAndWait();
                if (result.get() == ButtonType.OK) {
                    try {
                        PreparedStatement stmt = conn.prepareStatement("UPDATE login SET password=?");
                        stmt.setString(1, this.newPassword.getText());
                        stmt.execute();
                        changePasswordStatus.setText("Hasło zostało zmienione");
                        clearFields();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
            else{
                changePasswordStatus.setText("Nie poprawne powtórzenie hasła");
            }
        }
        else{
            changePasswordStatus.setText("Nie poprawne stare hasło");
        }
    }

    private boolean correctInputOfOldPassword(){
        try {
            ResultSet rs = conn.createStatement().executeQuery("SELECT password FROM login");
            if(oldPassword.getText().equals(rs.getString(1))) return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean correctRepetitionOfPassword(){
        if(newPassword.getText().equals(repeatedNewPassword.getText())) return true;
        else return false;
    }

    private void clearFields(){
        oldPassword.setText("");
        newPassword.setText("");
        repeatedNewPassword.setText("");
    }
}
