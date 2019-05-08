package com.fiix.Agamotto.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class NeighbourAsset {
  private String assetId;
  private String assetStatus;
  private String assetLastServicedDate;
}
