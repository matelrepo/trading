//package io.matel.app;
//
//import org.hibernate.annotations.CreationTimestamp;
//import org.hibernate.annotations.UpdateTimestamp;
//
//import javax.persistence.Column;
//import javax.persistence.Entity;
//import javax.persistence.Id;
//import javax.persistence.IdClass;
//import java.time.LocalDate;
//import java.time.ZonedDateTime;
//
//@Entity
//public class MacroData {
//    @Id
//    private String code;
//
//    private double value;
//
//    @CreationTimestamp
//    @Column(nullable = false, updatable = false, columnDefinition= "TIMESTAMP WITH TIME ZONE")
//    private ZonedDateTime createdOn;
//
//    @UpdateTimestamp
//    @Column(nullable = false, columnDefinition= "TIMESTAMP WITH TIME ZONE")
//    private ZonedDateTime updatedOn;
//
//    public MacroData(){};
//
//    public MacroData(String code, String date, String value){
//        this.code = code;
//        this.value = Double.valueOf(value);
//    };
//
//
//
//    public double getValue() {
//        return value;
//    }
//
//    public void setValue(double value) {
//        this.value = value;
//    }
//
//    @Override
//    public String toString() {
//        return "MacroData{" +
//                "code='" + code + '\'' +
//                ", value=" + value +
//                '}';
//    }
//
//    public String getCode() {
//        return code;
//    }
//
//    public void setCode(String code) {
//        this.code = code;
//    }
//
//
//}