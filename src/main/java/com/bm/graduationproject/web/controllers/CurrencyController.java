package com.bm.graduationproject.web.controllers;

import com.bm.graduationproject.models.entities.CurrencyDetails;
import com.bm.graduationproject.exceptions.NotValidAmountException;
import com.bm.graduationproject.models.enums.Currency;
import com.bm.graduationproject.web.response.ApiCustomResponse;
import com.bm.graduationproject.web.response.FavoritesResponse;
import com.bm.graduationproject.services.CurrencyService;
import com.bm.graduationproject.services.CurrencyServiceImpl;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeoutException;

@RestController
@Validated
@CrossOrigin(
        origins = "*",
        maxAge = 3600,
        allowedHeaders = "*")
@RequestMapping("/api/v1/currency")
public class CurrencyController {
    private CurrencyService service;

    @Autowired
    public CurrencyController(CurrencyServiceImpl service) {
        this.service = service;
    }

    @GetMapping("/conversion")
    public ResponseEntity<ApiCustomResponse<?>> convertOrCompare(@RequestParam("from") Currency from
            , @RequestParam("to1") Currency to1
            , @RequestParam(value = "to2", required = false) Currency to2
            , @RequestParam("amount") @Min(1) Double amount) throws TimeoutException {
        if (amount <= 0)
            throw new NotValidAmountException("Amount must be greater than Zero");
        return ResponseEntity.ok(ApiCustomResponse.builder()
                .statusCode(200)
                .isSuccess(true)
                .data(to2 == null
                        ? this.service.convert(from.name(), to1.name(), amount)
                        : this.service.compare(from.name(), to1.name(), to2.name(), amount))
                .build());
    }

    @GetMapping()
    public ResponseEntity<ApiCustomResponse<List<CurrencyDetails>>> getAllCurrencies() {
        return ResponseEntity.ok(ApiCustomResponse.<List<CurrencyDetails>>builder()
                .data(service.getAllCurrencies())
                .statusCode(200)
                .isSuccess(true)
                .build());
    }


    @PostMapping()
    public ResponseEntity<ApiCustomResponse<FavoritesResponse>> getExchangeRate(@RequestParam(name = "base") Currency baseCurrency, @RequestBody List<Currency> favorites) {
        return ResponseEntity.ok(ApiCustomResponse.<FavoritesResponse>builder()
                .data(favorites.isEmpty()
                        ? new FavoritesResponse()
                        : service.getExchangeRate(baseCurrency, favorites))
                .isSuccess(true)
                .message(favorites.isEmpty()
                        ? "There is no Favorites !!"
                        : null)
                .statusCode(200)
                .build());
    }
}
