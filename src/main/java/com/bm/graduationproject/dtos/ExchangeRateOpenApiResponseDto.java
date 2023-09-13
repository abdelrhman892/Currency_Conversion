package com.bm.graduationproject.dtos;

import lombok.*;

import java.util.Map;

@Setter
@Getter
@NoArgsConstructor
@Builder
@Data
@AllArgsConstructor
public class ExchangeRateOpenApiResponseDto {
    private String result;
    private String base_code;
    private Map<String, Double> conversion_rates;

}
