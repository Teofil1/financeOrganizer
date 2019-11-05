package payments;

import dbUtil.dbConnection;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

public class PaymentsController implements Initializable {

    @FXML
    private TextField name;
    @FXML
    private TextField invoice;
    @FXML
    private TextField amountRecognized;
    @FXML
    private TextField amountSpent;
    @FXML
    private ComboBox<String> paymentsType;
    @FXML
    private ComboBox<String> paymentsTypeSearch;
    /*@FXML
    private ComboBox<String> testCombo;*/
    @FXML
    private DatePicker date;
    @FXML
    private TableView<PaymentsData> paymentsTable;
    @FXML
    private TableColumn<PaymentsData, Integer> idColumn;
    @FXML
    private TableColumn<PaymentsData, String> nameColumn;
    @FXML
    private TableColumn<PaymentsData, String> invoiceColumn;
    @FXML
    private TableColumn<PaymentsData, Double> amountRecognizedColumn;
    @FXML
    private TableColumn<PaymentsData, Double> amountSpentColumn;
    @FXML
    private TableColumn<PaymentsData, String> paymentsTypeColumn;
    @FXML
    private TableColumn<PaymentsData, LocalDate> dateColumn;
    @FXML
    private Pane paymentTypeSettingPane;
    @FXML
    private ListView<String> paymentTypeListView;
    @FXML
    TextField paymentTypeTextField;

    private dbConnection dc;
    private ObservableList<PaymentsData> data;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.dc = new dbConnection();
        this.paymentsTable.setEditable(true);
        this.paymentsTable.setTableMenuButtonVisible(true);


        loadPaymentData("SELECT * FROM payments");
        loadPaymentTypes();
        this.paymentsTypeSearch.valueProperty().addListener(new ChangeListener<String>() {
            @Override public void changed(ObservableValue ov, String oldVal, String newVal) {
                if(oldVal != newVal){
                    if(newVal == "Wszystkie") loadPaymentData("SELECT * FROM payments");
                    else loadPaymentData("Select * FROM payments WHERE paymentsType = '"+ newVal +"'");
                }
            }
        });
    }

    private void loadPaymentData(String sql) {
        try {
            Connection conn = dbConnection.getConnection();
            this.data = FXCollections.observableArrayList();

            ResultSet rs = conn.createStatement().executeQuery(sql);
            while(rs.next()){
                LocalDate date;
                if(!rs.getString(7).equals("")) date = LocalDate.parse(rs.getString(7),formatter);
                else date = null;
                this.data.add(new PaymentsData(rs.getInt(1),rs.getString(2),rs.getString(3),rs.getDouble(4),
                        rs.getDouble(5),rs.getString(6), date));
            }

        }catch (SQLException ex){
            ex.printStackTrace();
        }

        this.idColumn.setCellValueFactory(new PropertyValueFactory<PaymentsData, Integer>("id"));
        this.nameColumn.setCellValueFactory(new PropertyValueFactory<PaymentsData, String>("name"));
        this.invoiceColumn.setCellValueFactory(new PropertyValueFactory<PaymentsData, String>("invoice"));
        this.amountRecognizedColumn.setCellValueFactory(new PropertyValueFactory<PaymentsData, Double>("amountRecognized"));
        this.amountSpentColumn.setCellValueFactory(new PropertyValueFactory<PaymentsData, Double>("amountSpent"));
        this.paymentsTypeColumn.setCellValueFactory(new PropertyValueFactory<PaymentsData, String>("paymentsType"));
        this.dateColumn.setCellValueFactory(new PropertyValueFactory<PaymentsData, LocalDate>("date"));

        this.paymentsTable.setItems(null);
        this.paymentsTable.setItems(this.data);
    }

    private void loadPaymentTypes() {
        try {
            Connection conn = dbConnection.getConnection();
            ObservableList<String>  data = FXCollections.observableArrayList();
            ObservableList<String>  dataForPaymentsTypeSearch = FXCollections.observableArrayList();
            ResultSet rs = conn.createStatement().executeQuery("SELECT name FROM paymentsType");
            while(rs.next()){
                data.add(rs.getString(1));
                dataForPaymentsTypeSearch.add(rs.getString(1));

            }
            dataForPaymentsTypeSearch.add("Wszystkie");
            this.paymentsType.setItems(data);
            this.paymentsTypeSearch.setItems(dataForPaymentsTypeSearch);
            this.paymentTypeListView.setItems(data);
            //this.paymentTypeScrollPane.setContent(listPaymentType);


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*private void loadTest() {
        try {
            Connection conn = dbConnection.getConnection();
            ObservableList<String>  data = FXCollections.observableArrayList();
            data.add("")
            dataForPaymentsTypeSearch.add("Wszystkie");
            this.paymentsType.setItems(data);
            this.paymentsTypeSearch.setItems(dataForPaymentsTypeSearch);
            this.paymentTypeListView.setItems(data);
            //this.paymentTypeScrollPane.setContent(listPaymentType);


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }*/


    @FXML
    private void addPayment(ActionEvent event){
        String sqlInsert = "INSERT INTO payments(name, invoice,amountRecognized, amountSpent,paymentsType,date) VALUES (?,?,?,?,?,?)";

        try {
            Connection conn = dbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sqlInsert);

            stmt.setString(1 ,this.name.getText());
            stmt.setString(2,this.invoice.getText());
            stmt.setDouble(3, Double.valueOf(this.amountRecognized.getText()));
            stmt.setDouble(4, Double.valueOf(this.amountSpent.getText()));
            stmt.setString(5,this.paymentsType.getValue());
            stmt.setString(6, this.date.getEditor().getText());

            stmt.execute();
            conn.close();

            if(this.paymentsTypeSearch.getValue() == "Wszystkie"){
                loadPaymentData("SELECT * FROM payments");
            }
            else loadPaymentData("Select * FROM payments WHERE paymentsType = '"+ this.paymentsTypeSearch.getValue() +"'");

            clearFields();

        }catch (SQLException ex){
            /*String error = ex.getMessage();
            if(error.equals("[SQLITE_CONSTRAINT]  Abort due to constraint violation (UNIQUE constraint failed: payments.number)"))
                error = "płatność o takim numerze już istnieje";
            labelError.setText("Błąd: "+error);*/
            ex.printStackTrace();
        }

    }

    @FXML
    private void addPaymentType(ActionEvent event){
        String sqlInsert = "INSERT INTO paymentsType(name) VALUES (?)";
        try {
            Connection conn = dbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sqlInsert);

            stmt.setString(1 ,this.paymentTypeTextField.getText());

            stmt.execute();
            conn.close();

            loadPaymentTypes();
            this.paymentTypeTextField.setText("");

        }catch (SQLException ex){
            /*String error = ex.getMessage();
            if(error.equals("[SQLITE_CONSTRAINT]  Abort due to constraint violation (UNIQUE constraint failed: payments.number)"))
                error = "płatność o takim numerze już istnieje";
            labelError.setText("Błąd: "+error);*/
            ex.printStackTrace();
        }

    }

    @FXML
    private void updatePayment(ActionEvent event){

        /*String sqlInsert = "UPDATE payments SET number = ? ,name=? ,amount=?,paymentsType=?,date=? WHERE number = 1";

        try {
            Connection conn = dbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sqlInsert);

            stmt.setString(1 ,this.number.getText());
            stmt.setString(2,this.name.getText());
            stmt.setString(3,this.amount.getText());
            stmt.setString(4,this.paymentsType.getText());
            stmt.setString(5,this.date.getEditor().getText());

            stmt.execute();
            conn.close();

            loadPaymentData();
            clearFields();

        }catch (SQLException ex){
            ex.printStackTrace();
        }*/
    }

    private void clearFields(){
        this.name.setText("");
        this.invoice.setText("");
        this.amountRecognized.setText("");
        this.amountSpent.setText("");
        this.paymentsType.setPromptText("Rodzaj płatności");
        this.date.setValue(null);
    }

    @FXML
    private void showPaymentTypePane(ActionEvent event) {
        this.paymentTypeSettingPane.setVisible(!this.paymentTypeSettingPane.isVisible());
    }




}
