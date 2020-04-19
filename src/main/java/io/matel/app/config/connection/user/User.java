package io.matel.app.config.connection.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name ="users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private boolean enabled = true;

    private String authorities = "ROLE_STUDENT";

    @JsonIgnore
    @Column(nullable = false)
    private String roles = "";

    @JsonIgnore
    private String permissions = "";

    @CreationTimestamp
    @Column(nullable = false, columnDefinition= "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime creation;

    @UpdateTimestamp
    @Column(nullable = false, columnDefinition= "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime update;

    public User(String username, String password, String roles, String permissions){
        this.username = username;
        this.password = password;
        this.roles = roles;
        this.permissions = permissions;
        this.authorities = this.roles + "," + this.permissions;
    }

    protected User(){}

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getRoles() {
        return roles;
    }

    public String getPermissions() {
        return permissions;
    }

    public List<String> getRoleList(){
        if(this.roles.length() > 0){
            return Arrays.asList(this.roles.split(","));
        }
        return new ArrayList<>();
    }

    public List<String> getPermissionList(){
        if(this.permissions.length() > 0){
            return Arrays.asList(this.permissions.split(","));
        }
        return new ArrayList<>();
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", enabled=" + enabled +
                ", authorities='" + authorities + '\'' +
                ", roles='" + roles + '\'' +
                ", permissions='" + permissions + '\'' +
                ", creation=" + creation +
                ", update=" + update +
                '}';
    }
}
