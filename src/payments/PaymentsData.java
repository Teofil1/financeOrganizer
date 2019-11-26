package payments;

import javafx.beans.property.*;

import java.time.LocalDate;

public class PaymentsData {

    private final IntegerProperty id;
    private final StringProperty name;
    private final StringProperty invoice;
    private final DoubleProperty amountRecognized;
    private final DoubleProperty amountSpent;
    private final StringProperty paymentsType;
    private final ObjectProperty<LocalDate> date;


    public PaymentsData(Integer id, String name, String invoice, Double amountRecognized, Double amountSpent, String paymentsType, LocalDate date) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.invoice = new SimpleStringProperty(invoice);
        this.amountRecognized = new SimpleDoubleProperty(amountRecognized);
        this.amountSpent = new SimpleDoubleProperty(amountSpent);
        this.paymentsType = new SimpleStringProperty(paymentsType);
        this.date = new SimpleObjectProperty<>(date);
    }

    public int getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getInvoice() {
        return invoice.get();
    }

    public StringProperty invoiceProperty() {
        return invoice;
    }

    public void setInvoice(String invoice) {
        this.invoice.set(invoice);
    }

    public double getAmountRecognized() {
        return amountRecognized.get();
    }

    public DoubleProperty amountRecognizedProperty() {
        return amountRecognized;
    }

    public void setAmountRecognized(double amountRecognized) {
        this.amountRecognized.set(amountRecognized);
    }

    public double getAmountSpent() {
        return amountSpent.get();
    }

    public DoubleProperty amountSpentProperty() {
        return amountSpent;
    }

    public void setAmountSpent(double amountSpent) {
        this.amountSpent.set(amountSpent);
    }

    public String getPaymentsType() {
        return paymentsType.get();
    }

    public StringProperty paymentsTypeProperty() {
        return paymentsType;
    }

    public void setPaymentsType(String paymentsType) {
        this.paymentsType.set(paymentsType);
    }

    public LocalDate getDate() {
        return date.get();
    }

    public ObjectProperty<LocalDate> dateProperty() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date.set(date);
    }
}
