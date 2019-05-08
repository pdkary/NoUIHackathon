package com.fiix.Agamotto.helpers;

import com.fiix.Agamotto.models.AssetDto;
import com.fiix.Agamotto.models.AssetReading;
import com.fiix.Agamotto.models.Manual;
import com.fiix.Agamotto.models.NeighbourAsset;
import com.ma.cmms.api.client.dto.Asset;
import com.ma.cmms.api.client.dto.MeterReading;

import java.util.ArrayList;
import java.util.List;

public class ConsolidatedHelper {
  private static final Long BOOLEAN_TRUE = 1L;
  private static final Long TENANT_ID = 45620L;

  public static void copyAssetData(Asset asset, AssetDto.AssetDtoBuilder builder) {
    builder
        .assetCode(asset.getStrCode())
        .assetName(asset.getStrName())
        .serialNumber(asset.getStrSerialNumber())
        .assetCategory(String.valueOf(asset.getIntCategoryID())) // TODO??
        // .assetManufacturer(asset)
        .assetModel(asset.getStrModel())
        .assetStatus(BOOLEAN_TRUE.equals(asset.getBolIsOnline()) ? "online" : "offline")
        .assetDescription(asset.getStrDescription())
        .assetLocation(asset.getStrStockLocation())
        // .assetCreatedDate()//TODO??
        .assetLastInspectedDate(String.valueOf(asset.getIntUpdated()))
        .assetBarCode(asset.getStrBarcode())
        .tenantId(String.valueOf(TENANT_ID));
  }

  public static List<Manual> getManuals(Asset asset) {
    List<Manual> manuals = new ArrayList<>();
    manuals.add(new Manual("http://www.africau.edu/images/default/sample.pdf"));
    manuals.add(new Manual("http://www.africau.edu/images/default/sample.pdf"));
    return manuals;
  }

  public static List<AssetReading> getReadings(List<MeterReading> meterReadingsByAsset) {
    List<AssetReading> readings = new ArrayList<>();
    if (meterReadingsByAsset != null) {
      meterReadingsByAsset.stream()
          .forEach(
              reading ->
                  readings.add(
                      new AssetReading(
                          String.valueOf(reading.getIntMeterReadingUnitsID()),
                          String.valueOf(reading.getIntUpdated()))));
    }
    return readings;
  }

  public static List<NeighbourAsset> getNeighbourAssets(List<Asset> nearbyAssets) {
    List<NeighbourAsset> neighbourAssets = new ArrayList<>();
    if (nearbyAssets != null) {
      nearbyAssets.stream()
          .forEach(
              asset ->
                  neighbourAssets.add(
                      new NeighbourAsset(
                          String.valueOf(asset.getId()),
                          BOOLEAN_TRUE.equals(asset.getBolIsOnline()) ? "online" : "offline",
                          String.valueOf(asset.getIntUpdated()))));
    }
    return neighbourAssets;
  }
}
