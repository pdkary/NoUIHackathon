package com.fiix.Agamotto.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetDto {
  private String assetCode;
  private String assetName;
  private String serialNumber;
  private String assetCategory;
  private String assetManufacturer;
  private String assetModel;
  private String assetStatus;
  private String assetDescription;
  private String assetLocation;
  private String assetCreatedDate;
  private String assetLastInspectedDate;
  private String assetBarCode;
  private String tenantId;
  private List<NeighbourAsset> neighbouringAssets;
  private List<Manual> assetManuals;
  private List<AssetReading> assetReadings;


}
