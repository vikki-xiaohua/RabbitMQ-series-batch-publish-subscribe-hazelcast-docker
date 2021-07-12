package com.calibre.subscriber.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, scope = FxCurrencyRateCsvRow.class)
@Data
public class FxCurrencyRateCsvRow implements Serializable {
    private static final long serialVersionUID = 129348938L;

    private String forex;
    private Double value;
}
