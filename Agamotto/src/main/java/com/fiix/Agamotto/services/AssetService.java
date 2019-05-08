package com.fiix.Agamotto.services;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import com.fiix.Agamotto.helpers.ConsolidatedHelper;
import com.fiix.Agamotto.models.AssetDto;
import com.fiix.Agamotto.models.AssetReading;
import com.fiix.Agamotto.models.Manual;
import com.fiix.Agamotto.models.NeighbourAsset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ma.cmms.api.client.FiixCmmsClient;
import com.ma.cmms.api.client.dto.Asset;
import com.ma.cmms.api.client.dto.MeterReading;
import com.ma.cmms.api.client.dto.MeterReadingUnit;
import com.ma.cmms.api.crud.FindFilter;
import com.ma.cmms.api.crud.FindRequest;
import com.ma.cmms.api.crud.FindResponse;

@Service
public class AssetService
{
	private static String DETAIL_FIELDS = "id, strName, strCode, strDescription, strCity, strAddress, strNotes, intSiteID, intCategoryID, strSerialNumber, intAssetLocationID, bolIsOnline, intAssetParentID, strStockLocation, intUpdated, strBarcode";
	private static String HISTORY_FIELDS = "id,intAssetId,dblMeterReading,dtmDateSubmitted,dv_intMeterReadingUnitsID,intSubmittedByUserID";

	private static String ID = "id";
	private static String ASSET_ID = "intAssetID";
	private static String USER_ID = "intSubmittedByUserID";

	public List<Long> tapMRIDs;
	public List<FindFilter> tapFilter;

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
		findRequest.setOrderBy("dtmDateSubmitted");

		FindResponse<MeterReading> findResponse = fiixCmmsClient.find(findRequest);
		return findResponse.getObjects();
	}

	public List<MeterReading> getMeterReadingsByUser(String userId)
	{
		List<FindFilter> filterList = getFilter(USER_ID, "=", userId);
		FindRequest<MeterReading> findRequest = fiixCmmsClient.prepareFind(MeterReading.class);
		findRequest.setFilters(filterList);
		findRequest.setFields(HISTORY_FIELDS);
		findRequest.setOrderBy("dtmDateSubmitted");

		FindResponse<MeterReading> findResponse = fiixCmmsClient.find(findRequest);
		return findResponse.getObjects();
	}

	public String Tap(String assetId, String userId)
	{
		HashMap<Long, Stack<MeterReading>> history = new HashMap<>();

		List<MeterReading> allTaps = getMeterReadingsByUser(userId).stream().filter(mr -> mr.getExtraFields().get("dv_intMeterReadingUnitsID").toString().contains("Tap")).collect(Collectors.toList());

		allTaps.forEach(mr -> {
			Long mrAssetID = mr.getIntAssetID();
			if (!history.containsKey(mrAssetID))
			{
				history.put(mrAssetID, new Stack<MeterReading>());
			}
			Stack<MeterReading> assetStack = history.get(mrAssetID);
			if (mr.getExtraFields().get("dv_intMeterReadingUnitsID").toString().contains("In"))
			{
				assetStack.push(mr);
			}
			else
			{
				assetStack.pop();
			}
			history.put(mrAssetID, assetStack);
		});
		//		List<MeterReading> tapIn = allMeterReadings.stream().filter(mr -> mr.getExtraFields().get("dv_intMeterReadingUnitsID").toString().contains("Tap In")).map(mr -> {
		//			if (!inHistory.containsKey(mr.getIntAssetID()))
		//			{
		//				inHistory.put(mr.getIntAssetID(), new ArrayList<Date>());
		//			}
		//			List<Date> assetInHistory = inHistory.get(mr.getIntAssetID());
		//			assetInHistory.add(mr.getDtmDateSubmitted());
		//			inHistory.put(mr.getIntAssetID(), assetInHistory);
		//			return mr;
		//		}).collect(Collectors.toList());
		//
		//		List<MeterReading> tapOut = allMeterReadings.stream().filter(mr -> mr.getExtraFields().get("dv_intMeterReadingUnitsID").toString().contains("Tap Out")).map(mr -> {
		//			if (!outHistory.containsKey(mr.getIntAssetID()))
		//			{
		//				outHistory.put(mr.getIntAssetID(), new ArrayList<Date>());
		//			}
		//			List<Date> assetOutHistory = outHistory.get(mr.getIntAssetID());
		//			assetOutHistory.add(mr.getDtmDateSubmitted());
		//			outHistory.put(mr.getIntAssetID(), assetOutHistory);
		//			return mr;
		//		}).collect(Collectors.toList());
		return history.toString();

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
		String ql = tapMRIDs.toString();
		ql = "(".concat(ql.substring(1, ql.length() - 1)).concat(")");
		tapFilter = getFilter(ID, " IN ", ql);
	}

	public List<Asset> getNearbyAssets(String asset)
	{
		return getNearbyAssets(getAsset(asset));
	}

	public List<Asset> getNearbyAssets(Asset asset)
	{
		List<FindFilter> filterList = getNearbyFilter(asset);

		if(filterList!=null){
			FindRequest<Asset> findRequest = fiixCmmsClient.prepareFind(Asset.class);
			findRequest.setFilters(filterList);
			findRequest.setFields(DETAIL_FIELDS);

			FindResponse<Asset> findResponse = fiixCmmsClient.find(findRequest);
			return findResponse.getObjects();
		}
		return null;
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

	public AssetDto getConsolidatedAsset(String id)
	{
		final AssetDto.AssetDtoBuilder builder = AssetDto.builder();
		final Asset asset = getAsset(id);

		ConsolidatedHelper.copyAssetData(asset, builder);

		if(asset!=null)
		{
			List<NeighbourAsset> neighbourAssets = ConsolidatedHelper.getNeighbourAssets(getNearbyAssets(asset));
			List<Manual> manuals = ConsolidatedHelper.getManuals(asset);
			List<AssetReading> readings = ConsolidatedHelper.getReadings(getMeterReadingsByAsset(String.valueOf(asset.getId())));

			builder.neighbouringAssets(neighbourAssets)
			.assetManuals(manuals)
			.assetReadings(readings);

			return builder.build();
		}
		return null;
	}
}
