package com.expense.model;

public class User {
    private int id;
    private String username;
    private String passwordHash;
    private String firstName;
    private String lastName;
    private String profilePicture;
    private String themeColor;
    private java.math.BigDecimal monthlyIncome;
    private java.math.BigDecimal monthlyBudget;

    public User() {}

    public User(int id, String username, String passwordHash, String firstName, String lastName, String profilePicture, String themeColor) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.profilePicture = profilePicture;
        this.themeColor = themeColor != null ? themeColor : "#5944d1";
        this.monthlyIncome = new java.math.BigDecimal("50000.00");
        this.monthlyBudget = new java.math.BigDecimal("35000.00");
    }

    public User(int id, String username, String passwordHash, String firstName, String lastName, String profilePicture, String themeColor, java.math.BigDecimal monthlyIncome, java.math.BigDecimal monthlyBudget) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.profilePicture = profilePicture;
        this.themeColor = themeColor != null ? themeColor : "#5944d1";
        this.monthlyIncome = monthlyIncome;
        this.monthlyBudget = monthlyBudget;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    public String getThemeColor() { return themeColor; }
    public void setThemeColor(String themeColor) { this.themeColor = themeColor; }
    
    public java.math.BigDecimal getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(java.math.BigDecimal monthlyIncome) { this.monthlyIncome = monthlyIncome; }
    
    public java.math.BigDecimal getMonthlyBudget() { return monthlyBudget; }
    public void setMonthlyBudget(java.math.BigDecimal monthlyBudget) { this.monthlyBudget = monthlyBudget; }
    
    public String getFullName() {
        if (firstName != null && !firstName.isEmpty() && lastName != null && !lastName.isEmpty()) {
            return firstName + " " + lastName;
        } else if (firstName != null && !firstName.isEmpty()) {
            return firstName;
        } else {
            return username;
        }
    }
}
