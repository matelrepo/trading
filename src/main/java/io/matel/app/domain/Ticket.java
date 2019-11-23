package io.matel.app.domain;

public class Ticket {

    public Ticket(){}

    public Ticket(String message, String status){
        this.message = message;
        this.status = status;
    }

    private String message;
    private String status;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "message='" + message + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
