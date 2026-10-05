package com.expense.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class CurrencyConverterService {
    
    // Using a free API for exchange rates. For production, consider caching or an API key.
    // E.g., open.er-api.com or frankfurter.app
    private static final String API_URL = "https://api.frankfurter.app/latest?from=%s&to=TRY";
    private final HttpClient httpClient;

    public CurrencyConverterService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public BigDecimal convertToTRY(BigDecimal amount, String fromCurrency) {
        if ("TRY".equalsIgnoreCase(fromCurrency)) {
            return amount;
        }

        try {
            String url = String.format(API_URL, fromCurrency.toUpperCase());
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
                BigDecimal rate = jsonObject.getAsJsonObject("rates").get("TRY").getAsBigDecimal();
                return amount.multiply(rate);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // indicates failure
    }
}
