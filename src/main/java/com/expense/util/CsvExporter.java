package com.expense.util;

import com.expense.model.Expense;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class CsvExporter {
    public static boolean exportToCsv(List<Expense> expenses, String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("ID,Date,Category,Description,Amount,Currency,Amount(TL)");
            for (Expense e : expenses) {
                writer.printf("%d,%s,\"%s\",\"%s\",%.2f,%s,%.2f%n",
                        e.getId(),
                        e.getDate().toString(),
                        e.getCategoryName().replace("\"", "\"\""),
                        e.getDescription().replace("\"", "\"\""),
                        e.getAmount(),
                        e.getCurrency(),
                        e.getAmountTl());
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
