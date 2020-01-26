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
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;

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
    private TableColumn<PaymentsData, String> fileColumn;
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


    private String pathForUploadFiles;
    private Connection conn;
    private ObservableList<PaymentsData> data;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        pathForUploadFiles = "C:/Users/"+System.getProperty("user.name")+"/AppData/Local/FinanceOrganizerUploads";

        try {
            conn = dbConnection.getConnection();
            paymentsTable.setTableMenuButtonVisible(true);
            paymentsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
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
            this.paymentsTypeColumn.setCellFactory(tc -> {
                TableCell<PaymentsData, String> cell = new TableCell<>();
                Text text = new Text();
                cell.setGraphic(text);
                cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
                text.wrappingWidthProperty().bind(this.paymentsTypeColumn.widthProperty());
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
            MenuItem itemUploadFileToPayment = new MenuItem("Dołąćż plik");
            MenuItem itemDownloadPayment = new MenuItem("Pobierz plik");

            MenuItem itemDeletePaymentsType = new MenuItem("Usuń rodzaj płatnośi");

            Alert alertConfirm = new Alert(Alert.AlertType.CONFIRMATION);
            Alert alertWarning = new Alert(Alert.AlertType.WARNING);
            contextMenuForPayments.getItems().addAll(itemEditPayment, itemDeletePayment, itemUploadFileToPayment, itemDownloadPayment);
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
                            alertConfirm.setTitle("Confirmation Dialog");
                            alertConfirm.setHeaderText("");
                            alertConfirm.setContentText("Na pewno chcesz usunąć ten rodzaj płatności?");
                            Optional<ButtonType> result = alertConfirm.showAndWait();
                            if (result.get() == ButtonType.OK) {
                                deletePaymentsType(cell.getItem());
                            }
                        }
                    });
                });
                return cell;
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
                                    alertConfirm.setTitle("Confirmation Dialog");
                                    alertConfirm.setHeaderText("");
                                    alertConfirm.setContentText("Na pewno chcesz zedytować tą płatność?");
                                    Optional<ButtonType> result = alertConfirm.showAndWait();
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
                            alertConfirm.setTitle("Confirmation Dialog");
                            alertConfirm.setHeaderText("");
                            alertConfirm.setContentText("Na pewno chcesz usunąć tą płatność?");
                            Optional<ButtonType> result = alertConfirm.showAndWait();
                            if (result.get() == ButtonType.OK) {
                                deletePayment(row.getItem());
                            }
                        }
                    });
                    itemUploadFileToPayment.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            FileChooser fileChooser = new FileChooser();
                            fileChooser.setTitle("Select file");
                            File selectedFile = fileChooser.showOpenDialog(null);
                            if (selectedFile != null) {
                                Integer id_payment = row.getItem().getId();
                                if (!row.getItem().getFileName().equals("")) {
                                    alertConfirm.setTitle("Confirmation Dialog");
                                    alertConfirm.setHeaderText("");
                                    alertConfirm.setContentText("Ta płatność już posiada dołączony plik. Nowy plik zastąpi poprzedni. Czy chcesz kontynuować?");
                                    Optional<ButtonType> result = alertConfirm.showAndWait();
                                    if (result.get() == ButtonType.OK) {
                                        editFile(id_payment, selectedFile);
                                    }
                                } else {
                                    addFile(id_payment, selectedFile);
                                }
                            }
                        }
                    });
                    itemDownloadPayment.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            if (row.getItem().getFileName().equals("")) {
                                alertWarning.setTitle("Warning Dialog");
                                alertWarning.setHeaderText("");
                                alertWarning.setContentText("Do tej płatności nie jest dołączony plik");
                                alertWarning.showAndWait();
                            } else {
                                DirectoryChooser directoryChooser = new DirectoryChooser();
                                File selectedDirectory = directoryChooser.showDialog(null);
                                if (selectedDirectory != null) {
                                    downloadFile(row.getItem().getId(), selectedDirectory);
                                }
                            }
                        }
                    });
                });
                return row;
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }

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
            this.data = FXCollections.observableArrayList();

            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                LocalDate date;
                String fileName;

                if (rs.getDate(7) != null) date = rs.getDate(7).toLocalDate();
                else date = null;

                if (rs.getInt(8) > 0) {
                    ResultSet rs2 = conn.createStatement().executeQuery("SELECT originalName FROM files WHERE id =" + rs.getInt(8));
                    fileName = rs2.getString(1);
                } else fileName = "";

                this.data.add(new PaymentsData(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getDouble(4),
                        rs.getDouble(5), rs.getString(6), date, fileName));
            }
            Double sumOfAmountRecognized = 0.00;
            Double sumOfAmountSpent = 0.00;
            for (PaymentsData paymentsData : data) {
                sumOfAmountRecognized += paymentsData.getAmountRecognized();
                sumOfAmountSpent += paymentsData.getAmountSpent();
            }
            this.sumOfAmountRecognized.setText(String.valueOf(Math.round(sumOfAmountRecognized * 100.0) / 100.0));
            this.sumOfAmountSpent.setText(String.valueOf(Math.round(sumOfAmountSpent * 100.0) / 100.0));
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
        this.fileColumn.setCellValueFactory(new PropertyValueFactory<PaymentsData, String>("fileName"));


        this.paymentsTable.setItems(null);
        this.paymentsTable.setItems(this.data);
    }

    private void deletePayment(PaymentsData payment) {
        try {
            PreparedStatement stmt;
            ResultSet rs;

            if (!payment.getFileName().equals("")) {
                rs = conn.createStatement().executeQuery("SELECT file FROM payments WHERE id =" + payment.getId());
                Integer id_file = rs.getInt(1);
                rs = conn.createStatement().executeQuery("SELECT generatedName FROM files WHERE id =" + id_file);
                String fileGeneratedName = rs.getString(1);
                stmt = conn.prepareStatement("DELETE FROM files WHERE id = ?");
                stmt.setInt(1, id_file);
                stmt.executeUpdate();
                deleteFile(new File(pathForUploadFiles +"/"+fileGeneratedName));

            }
            stmt = conn.prepareStatement("DELETE FROM payments WHERE id = ?");
            stmt.setInt(1, payment.getId());
            stmt.executeUpdate();

            loadPaymentData("SELECT * FROM payments");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deletePaymentsType(String paymentType) {
        try {
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
    private void showPaymentsBySearchOptions(ActionEvent event) {
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
            sql = "SELECT * FROM  payments WHERE " + column + " LIKE '%" + textFieldForSearchByComboBoxSearch.getText() + "%'";
        else sql = "SELECT * FROM payments";
        loadPaymentData(sql);

    }


    @FXML
    private void addPayment(ActionEvent event) {
        if (validation()) {
            String sqlInsert = "INSERT INTO payments(name, invoice,amountRecognized, amountSpent,paymentsType,date) VALUES (?,?,?,?,?,?)";
            try {
                PreparedStatement stmt = conn.prepareStatement(sqlInsert);

                stmt.setString(1, this.name.getText());
                stmt.setString(2, this.invoice.getText());
                if (this.amountRecognized.getText().equals("")) stmt.setDouble(3, 0.0);
                else stmt.setDouble(3, Double.valueOf(this.amountRecognized.getText()));
                if (this.amountSpent.getText().equals("")) stmt.setDouble(4, 0.0);
                else stmt.setDouble(4, Double.valueOf(this.amountSpent.getText()));
                stmt.setString(5, this.paymentsType.getValue());
                if (this.date.getValue() == null) stmt.setDate(6, null);
                else stmt.setDate(6, Date.valueOf(this.date.getValue()));

                stmt.execute();
                loadPaymentData("SELECT * FROM payments");

                clearFields();

            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void addFile(Integer id_payment, File selectedFile) {
        File uploadDir = new File(pathForUploadFiles);
        if (!uploadDir.exists()) {
            uploadDir.mkdir();
        }
        String sqlInsertFile = "INSERT INTO files(originalName, generatedName) VALUES (?,?)";
        String sqlUpdatePayment = "UPDATE payments SET file=? WHERE id =" + id_payment;

        String originalName = selectedFile.getName();
        String generatedName = UUID.randomUUID() + originalName;
        Path copied = Paths.get(pathForUploadFiles +"/"+ generatedName);
        Path originalPath = selectedFile.toPath();
        try {
            Files.copy(originalPath, copied, StandardCopyOption.REPLACE_EXISTING);
            try {
                PreparedStatement stmt;

                stmt = conn.prepareStatement(sqlInsertFile);
                stmt.setString(1, originalName);
                stmt.setString(2, generatedName);
                stmt.execute();

                ResultSet rs = conn.createStatement().executeQuery("SELECT id FROM files WHERE generatedName LIKE '" + generatedName + "'");
                Integer id_addedFile = rs.getInt(1);

                stmt = conn.prepareStatement(sqlUpdatePayment);
                stmt.setInt(1, id_addedFile);
                stmt.execute();

                loadPaymentData("SELECT * FROM payments");

            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Dialog");
            alert.setHeaderText("");
            alert.setContentText("Coś poszło nie tak. Nie udało się zapisać plik");

            alert.showAndWait();
        }
    }


    private void deleteFile(File file) {
        file.delete();
    }


    private void editFile(Integer id_payment, File selectedFile) {
        String originalName = selectedFile.getName();
        String generatedName = UUID.randomUUID() + originalName;
        Path copied = Paths.get(pathForUploadFiles + "/" + generatedName);
        Path originalPath = selectedFile.toPath();
        try {
            Files.copy(originalPath, copied, StandardCopyOption.REPLACE_EXISTING);
            try {
                PreparedStatement stmt;
                ResultSet rs;
                rs = conn.createStatement().executeQuery("SELECT file FROM payments WHERE id =" + id_payment);
                Integer id_file = rs.getInt(1);
                rs = conn.createStatement().executeQuery("SELECT generatedName FROM files WHERE id =" + id_file);
                String fileGeneratedName = rs.getString(1);
                stmt = conn.prepareStatement("UPDATE files SET originalName=?, generatedName=? WHERE id =" + id_file);
                stmt.setString(1, originalName);
                stmt.setString(2, generatedName);
                stmt.execute();
                deleteFile(new File(pathForUploadFiles +"/"+fileGeneratedName));
                loadPaymentData("SELECT * FROM payments");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Dialog");
            alert.setHeaderText("");
            alert.setContentText("Coś poszło nie tak. Nie udało się zapisać plik");

            alert.showAndWait();
        }
    }

    private void downloadFile(Integer id_payment, File selectedDirectory) {
        try {
            PreparedStatement stmt;
            ResultSet rs;
            rs = conn.createStatement().executeQuery("SELECT file FROM payments WHERE id =" + id_payment);
            Integer id_file = rs.getInt(1);
            rs = conn.createStatement().executeQuery("SELECT originalName, generatedName FROM files WHERE id =" + id_file);
            String originalName = rs.getString(1);
            String generatedName = rs.getString(2);


            File file = new File(selectedDirectory.getAbsolutePath()+"/"+originalName);
            int index = 1;
            String copyOriginalName= originalName;
            while (file.exists()) {
                index++;
                originalName = "(" + index + ")" + copyOriginalName;
                file = new File(selectedDirectory.getAbsolutePath()+"/"+originalName);
            }
            Path copied = Paths.get(selectedDirectory.getAbsolutePath()+"/"+originalName);
            Path originalPath = Paths.get(pathForUploadFiles +"/" + generatedName);

            try {
                Files.copy(originalPath, copied, StandardCopyOption.REPLACE_EXISTING);
            }catch (IOException e){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Dialog");
                alert.setHeaderText("");
                alert.setContentText("Coś jest nie tak. Nie udało się odnaleźć plik w tym programie");

                alert.showAndWait();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void addPaymentType(ActionEvent event) {
        if (!this.paymentTypeTextField.getText().equals("")) {
            String sqlInsert = "INSERT INTO paymentsType(name) VALUES (?)";
            try {
                PreparedStatement stmt = conn.prepareStatement(sqlInsert);
                stmt.setString(1, this.paymentTypeTextField.getText());
                stmt.execute();
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
        if (validation()) {
            String sqlUpdate = "UPDATE payments SET name=?, invoice=?,amountRecognized=?, amountSpent=?,paymentsType=?,date=? WHERE id =" + payment.getId();
            try {
                PreparedStatement stmt = conn.prepareStatement(sqlUpdate);

                stmt.setString(1, this.name.getText());
                stmt.setString(2, this.invoice.getText());
                if (this.amountRecognized.getText().equals("")) stmt.setDouble(3, 0.0);
                else stmt.setDouble(3, Double.valueOf(this.amountRecognized.getText()));
                if (this.amountSpent.getText().equals("")) stmt.setDouble(4, 0.0);
                else stmt.setDouble(4, Double.valueOf(this.amountSpent.getText()));
                stmt.setString(5, this.paymentsType.getValue());
                if (this.date.getValue() == null) stmt.setDate(6, null);
                else stmt.setDate(6, Date.valueOf(this.date.getValue()));

                stmt.execute();
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

    private boolean validation() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        if (this.name.getText().equals("") && this.invoice.getText().equals("") &&
                this.amountRecognized.getText().equals("") && this.amountSpent.getText().equals("") &&
                this.paymentsType.getValue() == null && this.date.getValue() == null) {
            alert.setTitle("Warning Dialog");
            alert.setHeaderText("");
            alert.setContentText("Co najmniej jedno pole powinno być wprowadzone!");
            alert.showAndWait();
            return false;
        } else {
            try {
                if (!this.amountRecognized.getText().equals("")) Double.valueOf(this.amountRecognized.getText());
            } catch (NumberFormatException ex) {
                alert.setTitle("Warning Dialog");
                alert.setHeaderText("");
                alert.setContentText("Pole kwoty przyznanej powinno być liczbą!");
                alert.showAndWait();
                return false;
            }
            try {
                if (!this.amountSpent.getText().equals("")) Double.valueOf(this.amountSpent.getText());
            } catch (NumberFormatException ex) {
                alert.setTitle("Warning Dialog");
                alert.setHeaderText("");
                alert.setContentText("Pole kwoty wydatkowanej powinno być liczbą!");
                alert.showAndWait();
                return false;
            }
            return true;
        }

    }
}
