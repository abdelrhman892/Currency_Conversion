package com.bm.graduationproject.web.response;

import com.bm.graduationproject.models.entities.CurrencyDetails;
import lombok.*;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class FavoritesResponse {
    private String base;
    private List<CurrencyDetails> currencies;
}

