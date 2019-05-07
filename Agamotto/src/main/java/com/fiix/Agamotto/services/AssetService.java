package com.fiix.Agamotto.services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ma.cmms.api.client.FiixCmmsClient;
import com.ma.cmms.api.client.dto.Asset;
import com.ma.cmms.api.client.dto.MeterReading;
import com.ma.cmms.api.client.dto.MeterReadingUnit;
import com.ma.cmms.api.crud.FindFilter;
import com.ma.cmms.api.crud.FindRequest;
import com.ma.cmms.api.crud.FindResponse;

import static java.util.Arrays.asList;

@Service
public class AssetService
{
	private static String DETAIL_FIELDS = "id, strName, strCode, strDescription, strCity, strAddress, strNotes, intSiteID, intCategoryID, strSerialNumber, intAssetLocationID, bolIsOnline, intAssetParentID, strStockLocation, intUpdated, strBarcode";
	private static String HISTORY_FIELDS = "id,dblMeterReading,dtmDateSubmitted,dv_intMeterReadingUnitsID,intSubmittedByUserID";

	private static String ID = "id";
	private static String ASSET_ID = "intAssetID";
	private static String USER_ID = "intSubmittedByUserID";

	public List<Long> tapMRIDs;

	@Autowired
	private FiixCmmsClient fiixCmmsClient;

	public Asset getAsset(String assetId)
	{
		List<FindFilter> filterList = getFilter(ID, "=", assetId);

		FindRequest<Asset> findRequest = fiixCmmsClient.prepareFind(Asset.class);
		findRequest.setFilters(filterList);
		findRequest.setFields(DETAIL_FIELDS);

		FindResponse<Asset> findResponse = fiixCmmsClient.find(findRequest);
		return findResponse.getObjects().get(0);
	}

	public List<MeterReading> getMeterReadingsByAsset(String assetId)
	{
		List<FindFilter> filterList = getFilter(ASSET_ID, "=", assetId);
		FindRequest<MeterReading> findRequest = fiixCmmsClient.prepareFind(MeterReading.class);
		findRequest.setFilters(filterList);
		findRequest.setFields(HISTORY_FIELDS);

		FindResponse<MeterReading> findResponse = fiixCmmsClient.find(findRequest);
		return findResponse.getObjects();
	}

	public List<MeterReading> getMeterReadingsByUser(String userId)
	{
		List<FindFilter> filterList = getFilter(USER_ID, "=", userId);
		FindRequest<MeterReading> findRequest = fiixCmmsClient.prepareFind(MeterReading.class);
		findRequest.setFilters(filterList);
		findRequest.setFields(HISTORY_FIELDS);

		FindResponse<MeterReading> findResponse = fiixCmmsClient.find(findRequest);
		return findResponse.getObjects();
	}

	public void Tap(String assetId, Long userId)
	{

	}

	private List<FindFilter> getFilter(String field, String operator, String value)
	{
		FindFilter findFilter = new FindFilter();
		findFilter.setQl(field.concat(operator).concat(value));

		List<FindFilter> filterList = new ArrayList<FindFilter>();
		filterList.add(findFilter);
		return filterList;
	}

	public void getTapMeterReadingIDs()
	{
		FindRequest<MeterReadingUnit> findRequest = fiixCmmsClient.prepareFind(MeterReadingUnit.class);
		findRequest.setFields("id,strName");
		FindResponse<MeterReadingUnit> findResponse = fiixCmmsClient.find(findRequest);
		tapMRIDs = findResponse.getObjects().stream().filter(mr -> mr.getStrName().contains("Tap")).map(mr -> mr.getId()).collect(Collectors.toList());
	}

	public List<Asset> getNearbyAssets(String asset)
	{
		return getNearbyAssets(getAsset(asset));
	}

	public List<Asset> getNearbyAssets(Asset asset)
	{
		List<FindFilter> filterList = getNearbyFilter(asset);

		FindRequest<Asset> findRequest = fiixCmmsClient.prepareFind(Asset.class);
		findRequest.setFilters(filterList);
		findRequest.setFields(DETAIL_FIELDS);

		FindResponse<Asset> findResponse = fiixCmmsClient.find(findRequest);
		return findResponse.getObjects();
	}

	public List<FindFilter> getNearbyFilter(Asset asset)
	{
		boolean hasAisleAndRow, hasBinNumber, hasParentId, hasLocation;
		hasAisleAndRow = asset.getStrRow() != null && asset.getStrAisle() != null;
		hasBinNumber = asset.getStrBinNumber() != null;
		hasParentId = asset.getIntAssetParentID() != null;
		hasLocation = asset.getIntAssetLocationID() != null;
		List<Object> parameters = new ArrayList<>();
		StringBuilder sb = new StringBuilder();

		sb.append("(id != ?)");
		parameters.add(asset.getId());
		if (hasParentId || hasLocation || hasAisleAndRow || hasBinNumber)
		{
			sb.append(" AND (");
			if (hasParentId)
			{
				sb.append("intAssetParentID=?");
				parameters.add(asset.getIntAssetParentID());
			}
			else
			{
				if (hasLocation)
				{
					sb.append("(");
					sb.append("intAssetLocationID=?");
					parameters.add(asset.getIntAssetLocationID());
					sb.append(")");
				}
				if (hasAisleAndRow)
				{
					if (hasLocation)
					{
						sb.append(" OR ");
					}
					sb.append("(");
					sb.append("(strAisle=?)");
					parameters.add(asset.getStrAisle());
					sb.append(" AND ");
					sb.append("(strRow=?)");
					parameters.add(asset.getStrRow());
					sb.append(")");
				}
				if (hasBinNumber)
				{
					if (hasLocation || hasAisleAndRow)
					{
						sb.append(" OR ");
					}
					sb.append("(");
					sb.append("(strBinNumber=?)");
					parameters.add(asset.getStrBinNumber());
					sb.append(")");
				}
			}
			sb.append(")");
		}
		else
		{
			return null;
		}

		FindFilter filter = new FindFilter();
		filter.setFields(DETAIL_FIELDS);
		filter.setQl(sb.toString());
		filter.setParameters(parameters);

		return asList(filter);
	}
}
