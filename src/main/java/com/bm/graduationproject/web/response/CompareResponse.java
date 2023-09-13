package com.bm.graduationproject.web.response;

import lombok.*;

@Data
@AllArgsConstructor
@Builder
public class CompareResponse {
    private String source;
    private String destination1;
    private String destination2;
    private Double amount1;
    private Double amount2;
}
