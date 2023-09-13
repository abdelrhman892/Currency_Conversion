package com.bm.graduationproject.repositories;

import com.bm.graduationproject.dtos.ExchangeRateOpenApiResponseDto;
import com.bm.graduationproject.dtos.ConversionOpenApiDto;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "currency", url = "${base_url}")
public interface CurrencyRepository {

    @GetMapping("/pair/{fromCurrency}/{toCurrency}/{amount}")
    ConversionOpenApiDto getCurrencyPair(@PathVariable("fromCurrency") String fromCurrency,
                                         @PathVariable("toCurrency") String toCurrency,
                                         @PathVariable("amount") Double amount);

    @GetMapping("/latest/{base}")
    @Cacheable(value = "latestCurrenciesCache",key = "#base")
    ExchangeRateOpenApiResponseDto getExchangeRate(@PathVariable("base") String base);

}
