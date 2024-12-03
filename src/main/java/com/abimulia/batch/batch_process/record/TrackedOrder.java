package com.abimulia.batch.batch_process.record;

public class TrackedOrder{
    private final Order order;  // Komposisi: Order disimpan sebagai atribut
    private String trackingNumber;
    private boolean freeShipping;

    public TrackedOrder(Order order) {
        this.order = order;
    }

    // Getter untuk Order
    public Order getOrder() {
        return order;
    }

    // Getter dan setter untuk trackingNumber
    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    // Getter dan setter untuk freeShipping
    public boolean isFreeShipping() {
        return freeShipping;
    }

    public void setFreeShipping(boolean freeShipping) {
        this.freeShipping = freeShipping;
    }

    // Metode tambahan untuk mengakses data Order
    public Long getOrderId() {
        return order.orderId();
    }

    public String getFirstName() {
        return order.firstName();
    }

}
