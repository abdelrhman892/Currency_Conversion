package com.bm.graduationproject.services;

import com.bm.graduationproject.models.entities.CurrencyDetails;
import com.bm.graduationproject.dtos.ExchangeRateOpenApiResponseDto;
import com.bm.graduationproject.web.response.FavoritesResponse;
import com.bm.graduationproject.models.enums.Currency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ExchangeRateAdapterImpl implements ExchangeRateAdapter  {
    private Logger logger = LoggerFactory.getLogger(ExchangeRateAdapterImpl.class);

    @Override
    public FavoritesResponse adapt(ExchangeRateOpenApiResponseDto apiResponse, Currency base, List<Currency> favorites) {
        String baseCurrency = base.name();
        List<CurrencyDetails> currencies = new ArrayList<>();
        Map<String, Double> currencies_rate = apiResponse.getConversion_rates();

        extractFavoritesCurrenciesFromApiResponse(favorites, currencies_rate, currencies);

        return buildFavoritesResponseDto(baseCurrency, currencies);
    }

    private static FavoritesResponse buildFavoritesResponseDto(String baseCurrency, List<CurrencyDetails> currencies) {
        FavoritesResponse adaptedResponse = new FavoritesResponse();
        adaptedResponse.setBase(baseCurrency);
        adaptedResponse.setCurrencies(currencies);
        return adaptedResponse;
    }

    private void extractFavoritesCurrenciesFromApiResponse(List<Currency> favorites, Map<String, Double> currencies_rate, List<CurrencyDetails> currencies) {
        favorites.forEach(f -> {
            Double currencyRate = getCurrencyValue(currencies_rate, f.name());
            Currency currency = Currency.valueOf(f.name());
            logger.info(f.name() + " exchange rate is: " + currencyRate);
            CurrencyDetails currencyInfo = new CurrencyDetails(currency.name(), currency.getCountry(),
                    currency.getFlagImageUrl(), currencyRate);
            currencies.add(currencyInfo);
        });
    }

    private Double getCurrencyValue(Map<String, Double> currencyRate, String fav) {
        return currencyRate.entrySet().stream().filter(c -> c.getKey().equals(fav.toUpperCase()))
                .map(Map.Entry::getValue).toList().get(0);
    }
}
