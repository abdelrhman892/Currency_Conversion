package com.bm.graduationproject.services;

import com.bm.graduationproject.dtos.ExchangeRateOpenApiResponseDto;
import com.bm.graduationproject.web.response.FavoritesResponse;
import com.bm.graduationproject.models.enums.Currency;

import java.util.List;

public interface ExchangeRateAdapter {
    FavoritesResponse adapt(ExchangeRateOpenApiResponseDto apiResponse, Currency base, List<Currency> favorites);
}
