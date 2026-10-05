package com.expense.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Expense {
    private int id;
    private int userId;
    private int categoryId;
    private String categoryName; // For UI mapping
    private LocalDate date;
    private String description;
    private BigDecimal amount;
    private String currency;
    private BigDecimal amountTl;

    public Expense() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getAmountTl() { return amountTl; }
    public void setAmountTl(BigDecimal amountTl) { this.amountTl = amountTl; }
}
