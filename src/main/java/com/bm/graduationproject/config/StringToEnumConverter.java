package com.bm.graduationproject.config;

import com.bm.graduationproject.models.enums.Currency;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToEnumConverter implements Converter<String, Currency> {

    @Override
    public Currency convert(String source) {
        return Currency.valueOf(source.toUpperCase()); // KW
    }
}

