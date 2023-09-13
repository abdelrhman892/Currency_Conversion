package com.bm.graduationproject.services;


import com.bm.graduationproject.web.response.CompareResponse;
import com.bm.graduationproject.web.response.ConversionResponse;
import com.bm.graduationproject.models.entities.CurrencyDetails;
import com.bm.graduationproject.models.enums.Currency;
import com.bm.graduationproject.web.response.FavoritesResponse;

import java.util.List;
import java.util.concurrent.TimeoutException;

public interface CurrencyService {

    ConversionResponse convert(String from, String to, double amount) throws TimeoutException;

    List<CurrencyDetails> getAllCurrencies();

    CompareResponse compare(String src, String des1 , String des2 , Double amount);

    FavoritesResponse getExchangeRate(Currency baseCurrency, List<Currency> favourites);

}
