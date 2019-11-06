package payments;

import dbUtil.dbConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.util.Callback;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    private ComboBox<String> comboBoxSearch;
    @FXML
    private Button addPaymentButton;
    @FXML
    private Button editPaymentButton;
    @FXML
    private TextField textFieldForSearchByComboBoxSearch;
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
    private TextField paymentTypeTextField;
    @FXML
    private Label sumOfAmountRecognized;
    @FXML
    private Label sumOfAmountSpent;


    private dbConnection dc;
    private ObservableList<PaymentsData> data;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dc = new dbConnection();
        //paymentsTable.setEditable(true);
        paymentsTable.setTableMenuButtonVisible(true);
        this.nameColumn.setCellFactory(tc -> {
            TableCell<PaymentsData, String> cell = new TableCell<>();
            Text text = new Text();
            cell.setGraphic(text);
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(this.nameColumn.widthProperty());
            text.textProperty().bind(cell.itemProperty());
            return cell ;
        });

        this.invoiceColumn.setCellFactory(tc -> {
            TableCell<PaymentsData, String> cell = new TableCell<>();
            Text text = new Text();
            cell.setGraphic(text);
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(this.invoiceColumn.widthProperty());
            text.textProperty().bind(cell.itemProperty());
            return cell ;
        });
        loadPaymentData("SELECT * FROM payments");
        loadPaymentTypes();
        loadComboxBoxSearch();

        this.paymentsTable.setRowFactory( tv -> {
            TableRow<PaymentsData> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    PaymentsData rowData = row.getItem();
                    fillDataRowToFormForEdit(rowData);
                }
            });
            return row ;
        });

    }

    private void fillDataRowToFormForEdit(PaymentsData rowData) {
        this.editPaymentButton.setVisible(!this.editPaymentButton.isVisible());
        this.addPaymentButton.setVisible(!this.addPaymentButton.isVisible());
        if(this.editPaymentButton.isVisible()){
            this.name.setText(rowData.getName());
            this.invoice.setText(rowData.getInvoice());
            this.amountRecognized.setText(String.valueOf(rowData.getAmountRecognized()));
            this.amountSpent.setText(String.valueOf(rowData.getAmountSpent()));
            this.paymentsType.setValue(rowData.getPaymentsType());
            this.date.setValue(rowData.getDate());
        }
        else clearFields();

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
            Double sumOfAmountRecognized = 0.00;
            Double sumOfAmountSpent = 0.00;
            for(PaymentsData paymentsData : data) {
                sumOfAmountRecognized += paymentsData.getAmountRecognized();
                sumOfAmountSpent += paymentsData.getAmountSpent();
            }
            this.sumOfAmountRecognized.setText(sumOfAmountRecognized.toString());
            this.sumOfAmountSpent.setText(sumOfAmountSpent.toString());
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
            ResultSet rs = conn.createStatement().executeQuery("SELECT name FROM paymentsType");
            while(rs.next()){
                data.add(rs.getString(1));
            }

            this.paymentsType.setItems(data);
            this.paymentTypeListView.setItems(data);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadComboxBoxSearch() {
        this.comboBoxSearch.getItems().addAll(
                "Nazwa",
                "Numer faktury",
                "Rodzaj"
        );
        this.comboBoxSearch.setValue("Rodzaj");
    }

    @FXML
    private void showPaymentsByComboBoxSearchAndTextField(ActionEvent event){
        String sql;
        String column = null;
        switch (comboBoxSearch.getValue()){
            case "Nazwa": column = "name"; break;
            case "Numer faktury": column = "invoice"; break;
            case "Rodzaj": column = "paymentsType"; break;
        }
        if(!textFieldForSearchByComboBoxSearch.getText().equals(""))
            sql = "SELECT * FROM  payments WHERE "+column+" LIKE '"+textFieldForSearchByComboBoxSearch.getText()+"'";
        else sql = "SELECT * FROM payments";
        loadPaymentData(sql);

    }


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
            loadPaymentData("SELECT * FROM payments");

            clearFields();

        }catch (SQLException ex){
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
            this.paymentTypeTextField.setText("SELECT * FROM payments");

        }catch (SQLException ex){
            ex.printStackTrace();
        }

    }

    private void clearFields(){
        this.name.setText("");
        this.invoice.setText("");
        this.amountRecognized.setText("");
        this.amountSpent.setText("");
        this.paymentsType.setValue(null);
        this.date.setValue(null);
    }



    @FXML
    private void showPaymentTypePane(ActionEvent event) {
        this.paymentTypeSettingPane.setVisible(!this.paymentTypeSettingPane.isVisible());
    }

    @FXML
    public void editPayment(ActionEvent event) {
    }
}
