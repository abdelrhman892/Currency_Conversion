package com.bm.graduationproject.dtos;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConversionOpenApiDto {
    String result;
    String base_code;
    String target_code;
    Double conversion_rate;
    Double conversion_result;
}
