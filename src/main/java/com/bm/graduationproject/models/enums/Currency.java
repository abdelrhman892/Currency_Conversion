package com.bm.graduationproject.models.enums;

import lombok.Getter;

@Getter
public enum Currency {

    USD("US Dollar","https://flagcdn.com/w40/us.png"),
    EUR("EURO","https://api.exchangerate-api.com/flag-images/EU.gif"),
    GBP("Sterling Pound","https://flagcdn.com/w40/gb.png"),
    AED("UAE Dirham","https://flagcdn.com/w40/ae.png"),
    BHD("Bahrain Dinar","https://flagcdn.com/w40/bh.png"),
    JPY("Japan Yen","https://flagcdn.com/w40/jp.png"),
    KWD("Kuwaiti Dinar","https://flagcdn.com/w40/kw.png"),
    OMR("Oman Riyal","https://flagcdn.com/w40/om.png"),
    QAR("QATARI Riyal","https://flagcdn.com/w40/qa.png"),
    SAR("Saudi Riyal","https://flagcdn.com/w40/sa.png"),

    EGP("Egyption Pound","https://flagcdn.com/w40/eg.png") ;

    private final String country;
    private String flagImageUrl;

    Currency(String country, String flagImageUrl) {
        this.country = country;
        this.flagImageUrl = flagImageUrl;
    }
}
