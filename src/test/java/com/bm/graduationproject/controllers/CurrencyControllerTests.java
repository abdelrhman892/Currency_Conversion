package com.bm.graduationproject.controllers;

import com.bm.graduationproject.web.response.CompareResponse;
import com.bm.graduationproject.web.response.ConversionResponse;
import com.bm.graduationproject.models.entities.CurrencyDetails;
import com.bm.graduationproject.web.response.FavoritesResponse;
import com.bm.graduationproject.models.enums.Currency;
import com.bm.graduationproject.services.CurrencyServiceImpl;
import com.bm.graduationproject.web.controllers.CurrencyController;
import com.bm.graduationproject.web.response.ApiCustomResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CurrencyController.class)
public class CurrencyControllerTests {

    @MockBean
    private CurrencyServiceImpl service;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @Test
    public void getAllCurrencies_returnAllCurrenciesEnums() throws Exception {
        // Arrange
        String uri = "/api/v1/currency";
        List<Currency> currencies = Arrays.stream(Currency.values()).toList();
        List<CurrencyDetails> responseDtos = currencies.stream()
                .map(c -> new CurrencyDetails(c.name(), c.getCountry(), c.getFlagImageUrl(), null))
                .toList();
        ApiCustomResponse<?> response = ApiCustomResponse.builder()
                .data(responseDtos)
                .isSuccess(true)
                .message(null)
                .statusCode(HttpStatus.OK.value())
                .build();
        when(service.getAllCurrencies()).thenReturn(responseDtos);

        // Act
        String expectedResponse = mapper.writeValueAsString(response).replace(",\"message\":null", "");

        // Assert
        mockMvc.perform(MockMvcRequestBuilders.get(uri))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse));

    }

    @Test
    public void convertOrCompare_requestWithInvalidCurrencyNames_return_BAD_REQUEST_response() throws Exception {
        // Arrange
        String uri = "/api/v1/currency/conversion";
        ApiCustomResponse<?> responseBody = ApiCustomResponse.builder()
                .isSuccess(false)
                .message("Please Enter valid currency name.")
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .build();
        String expectedResponse = mapper.writeValueAsString(responseBody).replace(",\"data\":null", "");

        // Act - Assert
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .param("from", "Invalid From")
                        .param("to1", "Invalid To")
                        .param("amount", String.valueOf(20.0)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().json(expectedResponse));
    }

    @Test
    public void convertOrCompare_sendTo2ParameterOrNot_selectBetweenConvertOrCompare() throws Exception {
        // Arrange
        String uri = "/api/v1/currency/conversion";
        ConversionResponse conversionDto = Mockito.mock(ConversionResponse.class);
        CompareResponse compareDto = Mockito.mock(CompareResponse.class);
        when(service.convert(Mockito.anyString(), Mockito.anyString(), Mockito.anyDouble())).thenReturn(conversionDto);
        when(service.compare(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyDouble())).thenReturn(compareDto);


        // Act
        ApiCustomResponse<?> conversionResponseBody = ApiCustomResponse.builder()
                .statusCode(200)
                .message(null)
                .isSuccess(true)
                .data(conversionDto)
                .build();
        String conversionExpectedResponse = mapper.writeValueAsString(conversionResponseBody).replace(",\"message\":null", "");

        ApiCustomResponse<?> compareResponseBody = ApiCustomResponse.builder()
                .statusCode(200)
                .message(null)
                .isSuccess(true)
                .data(compareDto)
                .build();
        String compareExpectedResponse = mapper.writeValueAsString(compareResponseBody).replace(",\"message\":null", "");

        // Assert
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .param("from", "KWD")
                        .param("to1", "USD")
                        .param("amount", String.valueOf(20.0)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(conversionExpectedResponse));

        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .param("from", "KWD")
                        .param("to1", "USD")
                        .param("to2", "EUR")
                        .param("amount", String.valueOf(20.0)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(compareExpectedResponse));
    }

    @Test
    public void convert_timeoutException_returnGATEWAY_TIMEOUT_Response() throws Exception {
        // Arrange
        given(
                service.convert("KWD", "USD", 20.0)
        ).willAnswer(i -> {
            throw new TimeoutException();
        });

        // Act
        String uri = "/api/v1/currency/conversion";
        ApiCustomResponse<?> response = ApiCustomResponse.builder()
                .isSuccess(false)
                .message("The request timed out. Please try again later.")
                .statusCode(HttpStatus.GATEWAY_TIMEOUT.value())
                .data(null)
                .build();
        ObjectMapper mapper = new ObjectMapper();
        String expectedResponse = mapper.writeValueAsString(response).replace(",\"data\":null", "");


        // Assert
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .param("from", "KWD")
                        .param("to1", "USD")
                        .param("amount", String.valueOf(20.0)))
                .andDo(print())
                .andExpect(status().isGatewayTimeout())
                .andExpect(content().json(expectedResponse));
    }

    @Test
    public void convertOrCompare_negativeAmount_return_BAD_REQUEST_Response() throws Exception {
        // Arrange
        String uri = "/api/v1/currency/conversion";
        ApiCustomResponse<?> response = ApiCustomResponse.builder()
                .isSuccess(false)
                .message("Amount must be greater than Zero")
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .build();


        // Act
        ObjectMapper mapper = new ObjectMapper();
        String expectedResponse = mapper.writeValueAsString(response).replace(",\"data\":null", "");


        // Assert
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .param("from", "KWD")
                        .param("to1", "USD")
                        .param("amount", String.valueOf(-2.0)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().json(expectedResponse));
    }


    @Test
    public void compare_timeoutException_returnGATEWAY_TIMEOUT_Response() throws Exception {
        // Arrange
        given(
                service.compare("KWD", "USD", "EUR", 20.0)
        ).willAnswer(i -> {
            throw new TimeoutException();
        });

        // Act
        String uri = "/api/v1/currency/conversion";
        ApiCustomResponse<?> response = ApiCustomResponse.builder()
                .isSuccess(false)
                .message("The request timed out. Please try again later.")
                .statusCode(HttpStatus.GATEWAY_TIMEOUT.value())
                .data(null)
                .build();
        ObjectMapper mapper = new ObjectMapper();
        String expectedResponse = mapper.writeValueAsString(response).replace(",\"data\":null", "");


        // Assert
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .param("from", "KWD")
                        .param("to1", "USD")
                        .param("to2", "EUR")
                        .param("amount", String.valueOf(20.0)))
                .andDo(print())
                .andExpect(status().isGatewayTimeout())
                .andExpect(content().json(expectedResponse));
    }

    @Test
    public void getExchangeRate_timeoutException_returnGATEWAY_TIMEOUT_Response() throws Exception {
        // Arrange
        List<Currency> favs = new ArrayList<>();
        favs.add(Currency.USD);
        favs.add(Currency.QAR);
        given(
                service.getExchangeRate(Currency.KWD, favs)
        ).willAnswer(i -> {
            throw new TimeoutException();
        });

        // Act
        String uri = "/api/v1/currency";
        ApiCustomResponse<?> response = ApiCustomResponse.builder()
                .isSuccess(false)
                .message("The request timed out. Please try again later.")
                .statusCode(HttpStatus.GATEWAY_TIMEOUT.value())
                .data(null)
                .build();
        ObjectMapper mapper = new ObjectMapper();
        String expectedResponse = mapper.writeValueAsString(response).replace(",\"data\":null", "");


        // Assert
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .content(mapper.writeValueAsString(favs))
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("base", "KWD"))
                .andDo(print())
                .andExpect(status().isGatewayTimeout())
                .andExpect(content().json(expectedResponse));
    }


    @Test
    public void getExchangeRate_invalidBaseCurrencyValue_return_BAD_REQUEST_response() throws Exception {
        // Arrange
        String notValidCurrency = "NOT_VALID";
        String uri = "/api/v1/currency";
        List<Currency> favs = new ArrayList<>();
        favs.add(Currency.USD);
        favs.add(Currency.QAR);

        // Act
        ApiCustomResponse<?> expectedApiResponse = ApiCustomResponse.builder()
                .isSuccess(false)
                .message("Please Enter valid currency name.")
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .build();
        ObjectMapper mapper = new ObjectMapper();
        String expectedEndpointResponse = mapper.writeValueAsString(expectedApiResponse).replace(",\"data\":null", "");

        // Assert
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .content(mapper.writeValueAsString(favs))
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("base", notValidCurrency))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().json(expectedEndpointResponse));

    }

    @Test
    public void getExchangeRate_invalidFavoritesListCurrencyValue_return_BAD_REQUEST_response() throws Exception {
        // Arrange
        String validCurrencyName = "KWD";
        String uri = "/api/v1/currency";
        List<String> favs = new ArrayList<>();
        favs.add("NOT_VALID"); // not valid currency name -not included in Currency enum-
        favs.add("USD");

        // Act
        ApiCustomResponse<?> expectedApiResponse = ApiCustomResponse.builder()
                .isSuccess(false)
                .message("Please Enter valid currency name.")
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .build();
        ObjectMapper mapper = new ObjectMapper();
        String expectedEndpointResponse = mapper.writeValueAsString(expectedApiResponse).replace(",\"data\":null", "");

        // Assert
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .content(mapper.writeValueAsString(favs))
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("base", validCurrencyName))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().json(expectedEndpointResponse));

    }

    @Test
    public void getExchangeRate_EmptyFavoritesList_returnEmptyDataObjectWithMessage() throws Exception {
        // Arrange
        String validCurrencyName = "KWD";
        String uri = "/api/v1/currency";
        List<String> favs = new ArrayList<>();

        // Act
        ApiCustomResponse<?> expectedApiResponse = ApiCustomResponse.builder()
                .isSuccess(true)
                .message("There is no Favorites !!")
                .statusCode(HttpStatus.OK.value())
                .data(new FavoritesResponse())
                .build();
        ObjectMapper mapper = new ObjectMapper();
        String expectedEndpointResponse = mapper.writeValueAsString(expectedApiResponse)
                .replace("\"base\":null", "")
                .replace(",\"currencies\":null", "");

        // Assert
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .content(mapper.writeValueAsString(favs))
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("base", validCurrencyName))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(expectedEndpointResponse));

    }


    @Test
    public void getExchangeRate_validFavoritesList_return_OK_response() throws Exception {
        // Arrange
        String validCurrencyName = "KWD";
        String uri = "/api/v1/currency";
        List<Currency> favs = new ArrayList<>();
        favs.add(Currency.JPY);
        favs.add(Currency.USD);

        List<CurrencyDetails> favsRates = new ArrayList<>();

        favsRates.add(new CurrencyDetails(Currency.JPY.name(), Currency.JPY.getCountry(), Currency.JPY.getFlagImageUrl(), 22.5));
        favsRates.add(new CurrencyDetails(Currency.USD.name(), Currency.USD.getCountry(), Currency.USD.getFlagImageUrl(), 22.5));

        FavoritesResponse serviceResponse = FavoritesResponse.builder()
                .currencies(favsRates)
                .base(validCurrencyName)
                .build();

        when(service.getExchangeRate(Currency.valueOf(validCurrencyName), favs)).thenReturn(serviceResponse);

        // Act
        ApiCustomResponse<?> expectedApiResponse = ApiCustomResponse.builder()
                .isSuccess(true)
                .statusCode(HttpStatus.OK.value())
                .data(serviceResponse)
                .build();
        ObjectMapper mapper = new ObjectMapper();
        String expectedEndpointResponse = mapper.writeValueAsString(expectedApiResponse)
                .replace(",\"message\":null", "");

        // Assert
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .content(mapper.writeValueAsString(favs))
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("base", validCurrencyName))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(expectedEndpointResponse));

    }
}

