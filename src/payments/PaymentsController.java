package payments;

import dbUtil.dbConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
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
    private Button cancelEditPaymentButton;
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
            return cell;
        });

        this.invoiceColumn.setCellFactory(tc -> {
            TableCell<PaymentsData, String> cell = new TableCell<>();
            Text text = new Text();
            cell.setGraphic(text);
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(this.invoiceColumn.widthProperty());
            text.textProperty().bind(cell.itemProperty());
            return cell;
        });


        loadPaymentData("SELECT * FROM payments");
        loadPaymentTypes();
        loadComboxBoxSearch();

        ContextMenu contextMenuForPayments = new ContextMenu();
        ContextMenu contextMenuForPaymentsTypes = new ContextMenu();
        MenuItem itemDeletePayment = new MenuItem("Usuń płatność");
        MenuItem itemEditPayment = new MenuItem("Edytuj płatność");
        MenuItem itemDeletePaymentsType = new MenuItem("Usuń rodzaj płatnośi");
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        contextMenuForPayments.getItems().addAll(itemEditPayment, itemDeletePayment);
        contextMenuForPaymentsTypes.getItems().add(itemDeletePaymentsType);

        this.paymentTypeListView.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<>();
            cell.textProperty().bind(cell.itemProperty());
            cell.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.SECONDARY) {
                    contextMenuForPaymentsTypes.show(cell, event.getScreenX(), event.getScreenY());
                }
                itemDeletePaymentsType.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        alert.setTitle("Confirmation Dialog");
                        alert.setHeaderText("Na pewno chcesz usunąć ten rodzaj płatności?");
                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.get() == ButtonType.OK) {
                            deletePaymentsType(cell.getItem());
                        }
                    }
                });
            });
            return cell ;
        });

        this.paymentsTable.setRowFactory(tv -> {
            TableRow<PaymentsData> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.SECONDARY) {
                    contextMenuForPayments.show(row, event.getScreenX(), event.getScreenY());
                }
                itemEditPayment.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        fillDataRowToFormForEdit(row.getItem());
                        editPaymentButton.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent e) {
                                alert.setTitle("Confirmation Dialog");
                                alert.setHeaderText("Na pewno chcesz zedytować tą płatność?");
                                Optional<ButtonType> result = alert.showAndWait();
                                if (result.get() == ButtonType.OK) {
                                    editPayment(row.getItem());
                                }
                            }
                        });
                    }
                });
                itemDeletePayment.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        alert.setTitle("Confirmation Dialog");
                        alert.setHeaderText("Na pewno chcesz usunąć tą płatność?");
                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.get() == ButtonType.OK) {
                            deletePayment(row.getItem());
                        }
                    }
                });
            });
            return row;
        });


    }

    private void fillDataRowToFormForEdit(PaymentsData rowData) {
        this.editPaymentButton.setVisible(true);
        this.cancelEditPaymentButton.setVisible(true);
        this.addPaymentButton.setVisible(false);
        this.name.setText(rowData.getName());
        this.invoice.setText(rowData.getInvoice());
        this.amountRecognized.setText(String.valueOf(rowData.getAmountRecognized()));
        this.amountSpent.setText(String.valueOf(rowData.getAmountSpent()));
        this.paymentsType.setValue(rowData.getPaymentsType());
        this.date.setValue(rowData.getDate());

    }

    private void loadPaymentData(String sql) {
        try {
            Connection conn = dbConnection.getConnection();
            this.data = FXCollections.observableArrayList();

            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                LocalDate date;
                if (!rs.getString(7).equals("")) date = LocalDate.parse(rs.getString(7), formatter);
                else date = null;
                this.data.add(new PaymentsData(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getDouble(4),
                        rs.getDouble(5), rs.getString(6), date));
            }
            Double sumOfAmountRecognized = 0.00;
            Double sumOfAmountSpent = 0.00;
            for (PaymentsData paymentsData : data) {
                sumOfAmountRecognized += paymentsData.getAmountRecognized();
                sumOfAmountSpent += paymentsData.getAmountSpent();
            }
            this.sumOfAmountRecognized.setText(sumOfAmountRecognized.toString());
            this.sumOfAmountSpent.setText(sumOfAmountSpent.toString());
        } catch (SQLException ex) {
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

    private void deletePayment(PaymentsData payment) {
        try {
            Connection conn = dbConnection.getConnection();
            PreparedStatement statement = conn.prepareStatement("DELETE FROM payments WHERE id = ?");
            statement.setInt(1, payment.getId());
            statement.executeUpdate();
            loadPaymentData("SELECT * FROM payments");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deletePaymentsType(String paymentType) {
        try {
            Connection conn = dbConnection.getConnection();
            PreparedStatement statement = conn.prepareStatement("DELETE FROM paymentsType WHERE  name= ?");
            statement.setString(1, paymentType);
            statement.executeUpdate();
            loadPaymentTypes();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadPaymentTypes() {
        try {
            Connection conn = dbConnection.getConnection();
            ObservableList<String> data = FXCollections.observableArrayList();
            ResultSet rs = conn.createStatement().executeQuery("SELECT name FROM paymentsType");
            while (rs.next()) {
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
    private void showPaymentsByComboBoxSearchAndTextField(ActionEvent event) {
        String sql;
        String column = null;
        switch (comboBoxSearch.getValue()) {
            case "Nazwa":
                column = "name";
                break;
            case "Numer faktury":
                column = "invoice";
                break;
            case "Rodzaj":
                column = "paymentsType";
                break;
        }
        if (!textFieldForSearchByComboBoxSearch.getText().equals(""))
            sql = "SELECT * FROM  payments WHERE " + column + " LIKE '" + textFieldForSearchByComboBoxSearch.getText() + "'";
        else sql = "SELECT * FROM payments";
        loadPaymentData(sql);

    }


    @FXML
    private void addPayment(ActionEvent event) {
        String sqlInsert = "INSERT INTO payments(name, invoice,amountRecognized, amountSpent,paymentsType,date) VALUES (?,?,?,?,?,?)";

        try {
            Connection conn = dbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sqlInsert);

            stmt.setString(1, this.name.getText());
            stmt.setString(2, this.invoice.getText());
            stmt.setDouble(3, Double.valueOf(this.amountRecognized.getText()));
            stmt.setDouble(4, Double.valueOf(this.amountSpent.getText()));
            stmt.setString(5, this.paymentsType.getValue());
            stmt.setString(6, this.date.getEditor().getText());

            stmt.execute();
            conn.close();
            loadPaymentData("SELECT * FROM payments");

            clearFields();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }

    @FXML
    private void addPaymentType(ActionEvent event) {
        if (!this.paymentTypeTextField.getText().equals("")) {
            String sqlInsert = "INSERT INTO paymentsType(name) VALUES (?)";
            try {
                Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sqlInsert);
                stmt.setString(1, this.paymentTypeTextField.getText());
                stmt.execute();
                conn.close();
                loadPaymentTypes();
                this.paymentTypeTextField.setText("");

            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void clearFields() {
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
    private void cancelEditPayment(ActionEvent event) {
        this.addPaymentButton.setVisible(true);
        this.editPaymentButton.setVisible(false);
        this.cancelEditPaymentButton.setVisible(false);
        clearFields();
    }

    public void editPayment(PaymentsData payment) {
        String sqlUpdate = "UPDATE payments SET name=?, invoice=?,amountRecognized=?, amountSpent=?,paymentsType=?,date=? WHERE id =" + payment.getId();
        try {
            Connection conn = dbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sqlUpdate);

            stmt.setString(1, this.name.getText());
            stmt.setString(2, this.invoice.getText());
            stmt.setDouble(3, Double.valueOf(this.amountRecognized.getText()));
            stmt.setDouble(4, Double.valueOf(this.amountSpent.getText()));
            stmt.setString(5, this.paymentsType.getValue());
            stmt.setString(6, this.date.getEditor().getText());

            stmt.execute();
            conn.close();
            loadPaymentData("SELECT * FROM payments");

            this.addPaymentButton.setVisible(true);
            this.editPaymentButton.setVisible(false);
            this.cancelEditPaymentButton.setVisible(false);
            clearFields();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
