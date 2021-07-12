package com.calibre.publisher.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FxCurrencyRateCsvRow implements Serializable {
    private static final long serialVersionUID = 129348938L;
    private String forex;
    private Double value;
}
