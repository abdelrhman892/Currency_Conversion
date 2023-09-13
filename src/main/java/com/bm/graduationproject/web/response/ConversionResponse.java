package com.bm.graduationproject.web.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConversionResponse {
    String source;
    String destination;
    double amount;
}

