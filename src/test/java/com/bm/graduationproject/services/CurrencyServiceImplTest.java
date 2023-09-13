package com.bm.graduationproject.services;

import com.bm.graduationproject.config.CachingConfig;
import com.bm.graduationproject.web.response.CompareResponse;
import com.bm.graduationproject.web.response.ConversionResponse;
import com.bm.graduationproject.models.entities.CurrencyDetails;
import com.bm.graduationproject.dtos.ExchangeRateOpenApiResponseDto;
import com.bm.graduationproject.web.response.FavoritesResponse;
import com.bm.graduationproject.models.enums.Currency;
import com.bm.graduationproject.repositories.CurrencyRepository;
import com.bm.graduationproject.dtos.ConversionOpenApiDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@EnableCaching
@Import(CachingConfig.class)
@ActiveProfiles("test")
class CurrencyServiceImplTest {


    @Value("${expire_after_duration}")
    private long expireAfterDuration;

    @Value("${expire_after_time_unit}")
    private String expireAfterTimeUnit;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private ExchangeRateAdapter adapter;

    @MockBean
    private CurrencyRepository currencyRepository;

    @Autowired
    private CacheManager cacheManager;

    private Cache cache;

    @Test
    public void testConvert() throws TimeoutException {
        CurrencyRepository repository = Mockito.mock(CurrencyRepository.class);
        CurrencyServiceImpl currencyService = new CurrencyServiceImpl(repository,adapter);

        ConversionOpenApiDto apiResponse = new ConversionOpenApiDto();

        apiResponse.setConversion_rate(0.93);
        apiResponse.setConversion_result(46.5);

        when(repository.getCurrencyPair(Mockito.anyString(), Mockito.anyString(), Mockito.anyDouble()))
                .thenReturn(apiResponse);

        ConversionResponse result = currencyService.convert("USD", "EUR", 50.0);

        Assertions.assertEquals("USD", result.getSource());
        Assertions.assertEquals("EUR", result.getDestination());
        Assertions.assertEquals(46.5, result.getAmount());

    }

    @Test
    public void testcompare() {
        CurrencyRepository repository = Mockito.mock(CurrencyRepository.class);
        CurrencyServiceImpl currencyService = new CurrencyServiceImpl(repository,adapter);

        ConversionOpenApiDto apiResponse1 = new ConversionOpenApiDto();
        ConversionOpenApiDto apiResponse2 = new ConversionOpenApiDto();


        apiResponse1.setConversion_result(46.5);
        apiResponse1.setTarget_code("EUR");
        apiResponse2.setTarget_code("KWD");
        apiResponse2.setConversion_result(15.5);

        when(repository.getCurrencyPair(Mockito.anyString(), Mockito.anyString(), Mockito.anyDouble()))
                .thenReturn(apiResponse1, apiResponse2);

        CompareResponse result = currencyService.compare("USD", "EUR", "KWD", 50.0);

        Assertions.assertEquals("USD", result.getSource());
        Assertions.assertEquals(15.5, result.getAmount2());
        Assertions.assertEquals(46.5, result.getAmount1());
        Assertions.assertEquals("EUR", result.getDestination1());
        Assertions.assertEquals("KWD", result.getDestination2());
    }

    @Test
    public void testGetExchangeRate() {
        //Arrange
        Currency baseCurrency = Currency.SAR;
        List<Currency> favourites = new ArrayList<>();
        favourites.add(Currency.KWD);
        favourites.add(Currency.AED);
        favourites.add(Currency.EUR);
        Map<String, Double> currencies_rates = new HashMap<>();
        List<CurrencyDetails> favoriteCurrenciesList = new ArrayList<>();
        favourites.forEach(f -> {
            currencies_rates.put(f.name(), 0.082);
            CurrencyDetails currencyDetails =
                    new CurrencyDetails(f.name(), f.getCountry(),f.getFlagImageUrl(), 0.082);
            favoriteCurrenciesList.add(currencyDetails);
        });
        ExchangeRateOpenApiResponseDto exchangeRateOpenApiResponseDto =
                ExchangeRateOpenApiResponseDto.builder().result("success")
                        .base_code(baseCurrency.name()).conversion_rates(currencies_rates).build();
        //Act
        when(currencyRepository.getExchangeRate(Mockito.anyString())).thenReturn(exchangeRateOpenApiResponseDto);
        FavoritesResponse favoritesResponse = currencyService.getExchangeRate(baseCurrency, favourites);
        //Assert
        assertNotEquals(favoritesResponse, null);
        assertEquals(favoritesResponse.getCurrencies().size(), 3);
        assertEquals(favoritesResponse.getBase(), baseCurrency.name());
        favoritesResponse.getCurrencies().forEach(f -> assertEquals(f.rate(), 0.082));
        assertEquals(favoritesResponse.getCurrencies(), favoriteCurrenciesList);
    }


    @Test
    public void convert_testCacheBehavior() throws TimeoutException {

        // Arrange
        cache = cacheManager.getCache("conversionCache");
        when(currencyRepository.getCurrencyPair(any(), any(), any())).thenReturn(ConversionOpenApiDto.builder()
                .base_code("KWD")
                .target_code("USD")
                .conversion_rate(105.0)
                .build());

        // Act
        currencyService.convert("KWD", "USD", 10.0);
        currencyService.convert("KWD", "USD", 10.0);

        // Assert
        //  that the repository method has only ONE call

        verify(currencyRepository, times(1)).getCurrencyPair(any(), any(), any());

    }

    @Test
    public void compare_testCacheBehavior() {

        // Arrange
        cache = cacheManager.getCache("compareCache");
        when(currencyRepository.getCurrencyPair(any(), any(), any())).thenReturn(ConversionOpenApiDto.builder()
                .base_code("KWD")
                .target_code("USD")
                .conversion_rate(105.0)
                .build());

        // Act
        currencyService.compare("KWD", "USD", "EUR", 10.0);
        currencyService.compare("KWD", "USD", "EUR", 10.0);
        currencyService.compare("KWD", "USD", "EUR", 10.0);
        currencyService.compare("KWD", "USD", "EUR", 10.0);


        // Assert
        //  that the repository method has only TWO calls ,
        //  because each compare needs two calls for two currencies.

        verify(currencyRepository, times(2)).getCurrencyPair(any(), any(), any());

    }


    @Test
    public void testgetAllCurrencies() {

        //Arrange
        List<Currency> currencies = List.of(Currency.values());
        List<CurrencyDetails> actualResponse = new ArrayList<>();
        currencies.forEach(r -> {
            CurrencyDetails currencyDetails = new CurrencyDetails(r.name(), r.getCountry(), r.getFlagImageUrl(), null);
            actualResponse.add(currencyDetails);

        });

        //Act
        List<CurrencyDetails> expectedResponse = currencyService.getAllCurrencies();

        //Assert

        Assertions.assertEquals(expectedResponse.size(), actualResponse.size());
        Assertions.assertEquals(actualResponse, expectedResponse);
    }

    @Test
    public void convert_testCacheExpiration() throws TimeoutException,
            InterruptedException {
        // Arrange
        String from = "KWD";
        String to = "USD";
        double amount = 10.5;
        cache = cacheManager.getCache("conversionCache");
        when(currencyRepository.getCurrencyPair(any(), any(), any())).thenReturn(ConversionOpenApiDto.builder()
                .base_code(from)
                .target_code(to)
                .conversion_rate(amount)
                .build());


        // Act
        currencyService.convert(from, to, amount);

        // Assert with the first call:before expiration
        // #from-#to-#amount
        String cacheKey = from + '-' + to + '-' + amount;
        assertNotNull(cache.get(cacheKey));

        // Wait for cache expiration
        TimeUnit.valueOf(expireAfterTimeUnit).sleep(expireAfterDuration);

        // Assert second time after the expiration
        assertNull(cache.get(cacheKey));
    }


    @Test
    public void compare_testCacheExpiration() throws InterruptedException {
        // Arrange
        String src = "KWD";
        String des1 = "USD";
        String des2 = "EUR";
        Double amount = 10.5;
        cache = cacheManager.getCache("compareCache");
        when(currencyRepository.getCurrencyPair(any(), any(), any())).thenReturn(ConversionOpenApiDto.builder()
                .base_code("KWD")
                .target_code("USD")
                .conversion_rate(10.5)
                .build());


        // Act
        currencyService.compare(src, des1, des2, amount);

        // Assert with the first call:before expiration
        // #from-#to-#amount
        String cacheKey = src + '-' + des1 + '-' + des2 + '-' + amount;
        assertNotNull(cache.get(cacheKey));

        // Wait for cache expiration
        TimeUnit.valueOf(expireAfterTimeUnit).sleep(expireAfterDuration);

        // Assert second time after the expiration
        assertNull(cache.get(cacheKey));
    }


}
