package com.fiix.Agamotto.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AssetReading {
  private String name;
  private String value;
}
