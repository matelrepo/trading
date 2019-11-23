package io.matel.app.macro.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class CountryPref {

    public CountryPref(){}

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private long id;
    private String idUser;
    private String country;
    private String type;

    public CountryPref(String idUser, String country, String type) {
        this.idUser = idUser;
        this.country = country;
        this.type = type;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "CountryPref{" +
                "id=" + id +
                ", idUser='" + idUser + '\'' +
                ", country='" + country + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
