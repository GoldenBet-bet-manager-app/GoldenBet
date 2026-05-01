package Models;

public class Manager {
    private String name;
    private String surname;
    private String username;
    private String password;
    private String managerId;

    public Manager(String name, String surname, String username, String password, String managerId) {
        this.name = name;
        this.surname = surname;
        this.username = username;
        this.password = password;
        this.managerId = managerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }
}
