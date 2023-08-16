package htwb.ai.authservice.model;

import jakarta.persistence.*;

import java.util.Set;

/**
 * -- auto-generated definition
 * create table usertable
 * (
 *     id        integer default nextval('usertable_id_seq1'::regclass) not null
 *         constraint usertable_pkey1
 *             primary key,
 *     userid    varchar(20)                                            not null,
 *     password  varchar(20)                                            not null,
 *     firstname varchar(50)                                            not null,
 *     lastname  varchar(50)                                            not null
 * );
 *
 * alter table usertable
 *     owner to dhumsa_songservlet_kbeai;
 */
@Entity
@Table(name = "usertable")
public class User {

    @Id
    @Column(name="userid")
    private String userId;
    @Column(name="password")
    private String password;
    @Column(name="firstName")
    private String firstName;
    @Column(name="lastName")
    private String lastName;


    public User() {
    }

    public User(String userId, String password, String firstName, String lastName) {
        this.userId = userId;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public User(Builder builder) {
        this.userId = builder.userId;
        this.password = builder.password;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
    }

    public String getUserId() {
        return userId;
    }

    public String setUserId(String userId) {
        return this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String setFirstName(String firstName) {
        return this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "User [userId=" + userId + ", firstName=" + firstName + ", lastName=" + lastName
                + ", password=" + password + "]";
    }

    public static class Builder {
        private String userId;
        private String password;
        private String firstName;
        private String lastName;

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }
}
