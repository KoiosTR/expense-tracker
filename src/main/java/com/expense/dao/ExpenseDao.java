package com.expense.dao;

import com.expense.model.Expense;
import com.expense.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExpenseDao {

    public List<Expense> getExpensesByUserId(int userId) {
        List<Expense> expenses = new ArrayList<>();
        String sql = "SELECT e.*, c.name as category_name FROM expenses e JOIN categories c ON e.category_id = c.id WHERE e.user_id = ? ORDER BY e.date DESC";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Expense exp = new Expense();
                exp.setId(rs.getInt("id"));
                exp.setUserId(rs.getInt("user_id"));
                exp.setCategoryId(rs.getInt("category_id"));
                exp.setCategoryName(rs.getString("category_name"));
                exp.setDate(rs.getDate("date").toLocalDate());
                exp.setDescription(rs.getString("description"));
                exp.setAmount(rs.getBigDecimal("amount"));
                exp.setCurrency(rs.getString("currency"));
                exp.setAmountTl(rs.getBigDecimal("amount_tl"));
                expenses.add(exp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return expenses;
    }

    public boolean createExpense(Expense expense) {
        String sql = "INSERT INTO expenses (user_id, category_id, date, description, amount, currency, amount_tl) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, expense.getUserId());
            pstmt.setInt(2, expense.getCategoryId());
            pstmt.setDate(3, Date.valueOf(expense.getDate()));
            pstmt.setString(4, expense.getDescription());
            pstmt.setBigDecimal(5, expense.getAmount());
            pstmt.setString(6, expense.getCurrency());
            pstmt.setBigDecimal(7, expense.getAmountTl());
            
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateExpense(Expense expense) {
        String sql = "UPDATE expenses SET category_id = ?, date = ?, description = ?, amount = ?, currency = ?, amount_tl = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, expense.getCategoryId());
            pstmt.setDate(2, Date.valueOf(expense.getDate()));
            pstmt.setString(3, expense.getDescription());
            pstmt.setBigDecimal(4, expense.getAmount());
            pstmt.setString(5, expense.getCurrency());
            pstmt.setBigDecimal(6, expense.getAmountTl());
            pstmt.setInt(7, expense.getId());
            pstmt.setInt(8, expense.getUserId());
            
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteExpense(int id, int userId) {
        String sql = "DELETE FROM expenses WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            pstmt.setInt(2, userId);
            
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
