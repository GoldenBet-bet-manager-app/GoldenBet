package gr.goldenbet.domain;

import android.util.Log;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

import gr.goldenbet.utils.Address;

/**
 * Represents a Player in the system, including personal and contact details.
 */
public class Player implements Serializable {

    private String name;
    private BigDecimal balance;

    private String surname;
    private String username;
    private String password;
    private String email;
    private String phone;

    // Composite Object for Address Information
    private Address address;

    // System Attributes
    private String playerId;
    private boolean isLoggedIn;
    private static final AtomicInteger idCounter = new AtomicInteger(1);

    /**
     * Full constructor for Player with nested Address object.
     */
    public Player(String name, String surname, String username, String password,
                  String email, String phone, Address address) {
        this.name = name;
        this.surname = surname;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.isLoggedIn = false;

        // Generates unique Player ID for the current session
        this.playerId = "Player_" + idCounter.getAndIncrement();
    }

    // Empty address,phone and email constructor
    public Player(String name, String surname, String username, String password) {
        this.name = name;
        this.surname = surname;
        this.username = username;
        this.password = password;
        this.isLoggedIn = false;
        this.email = "-";
        this.phone = "-";
        this.balance = BigDecimal.ZERO;

        // Default Address constructor
        // sets all address fields to "-"
        this.address = new Address();

        // Generates unique Player ID for the current session
        this.playerId = "Player_" + idCounter.getAndIncrement();
    }

    // --- Getters and Setters ---

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }

    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }

    public boolean isLoggedIn() { return isLoggedIn; }
    public void setLoggedIn(boolean loggedIn) { isLoggedIn = loggedIn; }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public void addBalance(BigDecimal amount) {
        this.setBalance(this.getBalance().add(amount));
    }

}