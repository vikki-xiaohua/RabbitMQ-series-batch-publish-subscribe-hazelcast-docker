package com.calibre.publisher.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FxCurrencyRate {
    @JsonProperty("code")
    private String code;

    @JsonProperty("timestamp")
    private long timestamp;

    @JsonProperty("gmtoffset")
    private long gmtOffset;

    @JsonProperty("open")
    private double open;

    @JsonProperty("high")
    private double high;

    @JsonProperty("low")
    private double low;

    @JsonProperty("close")
    private double close;

    @JsonProperty("volume")
    private long volume;

    @JsonProperty("previousClose")
    private double previousClose;

    @JsonProperty("change")
    private double change;

    @JsonProperty("change_p")
    private double changeP;
}