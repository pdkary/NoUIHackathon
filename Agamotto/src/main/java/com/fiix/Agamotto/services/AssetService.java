package com.fiix.Agamotto.services;

import com.fiix.Agamotto.helpers.ConsolidatedHelper;
import com.fiix.Agamotto.helpers.JsonToPdfClass;
import com.fiix.Agamotto.models.*;
import com.ma.cmms.api.client.FiixCmmsClient;
import com.ma.cmms.api.client.dto.Asset;
import com.ma.cmms.api.client.dto.MeterReading;
import com.ma.cmms.api.client.dto.MeterReadingUnit;
import com.ma.cmms.api.crud.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

@Service
public class AssetService
{
	protected static String DETAIL_FIELDS = "id, strName, strCode, strDescription, strCity, strAddress, strNotes, intSiteID, intCategoryID, strSerialNumber, intAssetLocationID, bolIsOnline, intAssetParentID, strStockLocation, intUpdated, strBarcode, dblLatitude, dblLongitude";
	protected static String HISTORY_FIELDS = "id,intAssetId,dblMeterReading,dtmDateSubmitted,dv_intMeterReadingUnitsID,intSubmittedByUserID, dv_intAssetID";
	private static String TAP_FIELDS = "intSubmittedByUserID, intMeterReadingUnitsID, intAssetID, dtmDateSubmitted, dv_intAssetID";

	protected static String ID = "id";
	protected static String ASSET_ID = "intAssetID";
	protected static String USER_ID = "intSubmittedByUserID";

	protected static final Double GEO_LOCATION_RANGE = 0.0001;

	protected Long inID;
	protected Long outID;
	public List<Long> tapMRIDs;
	public List<FindFilter> tapFilter;

	@Autowired
	private FiixCmmsClient fiixCmmsClient;

	private JsonToPdfClass pdfClass = new JsonToPdfClass();

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

	public TapDto Tap(String assetId, String userId)
	{
		HashMap<Long, Stack<MeterReading>> assetHistory = new HashMap<>();
		HashMap<String, String> nameMap = new HashMap<>();
		TapDto tapDto = new TapDto();

		List<MeterReading> allTaps = getMeterReadingsByUser(userId).stream().filter(mr -> {
			boolean isTap = mr.getExtraFields().get("dv_intMeterReadingUnitsID").toString().contains("Tap");
			boolean isNewIsh = mr.getDtmDateSubmitted().after(Date.from(Instant.now().minusSeconds(3600)));
			return isTap && isNewIsh;
		}).collect(Collectors.toList());
		allTaps.forEach(mr -> {
			Long mrAssetID = mr.getIntAssetID();
			nameMap.put(mrAssetID.toString(),mr.getExtraFields().get("dv_intAssetID").toString());
			if (!assetHistory.containsKey(mrAssetID))
			{
				assetHistory.put(mrAssetID, new Stack<MeterReading>());
			}
			Stack<MeterReading> assetStack = assetHistory.get(mrAssetID);
			if (mr.getExtraFields().get("dv_intMeterReadingUnitsID").toString().contains("In"))
			{
				assetStack.push(mr);
			}
			else
			{
				if(!assetStack.isEmpty()){
					assetStack.pop();
				}
			}
			assetHistory.put(mrAssetID, assetStack);
		});
		if (assetHistory.getOrDefault(Long.valueOf(assetId),new Stack<>()).isEmpty())
		{
			logTapIn(assetId, userId);
			tapDto.setInTap(nameMap.get(assetId));
		}
		assetHistory.keySet().forEach(key -> {
			Stack<MeterReading> assetStack = assetHistory.get(key);
			if (!assetStack.isEmpty())
			{
				logTapOut(key.toString(), userId);
				tapDto.setOutTap(nameMap.get(key.toString()));
			}
		});

		return tapDto;
	}

	private AddResponse<MeterReading> logTapIn(String assetID, String userID)
	{
		return logTap(assetID, userID, inID);
	}

	private AddResponse<MeterReading> logTapOut(String assetID, String userID)
	{
		return logTap(assetID, userID, outID);
	}

	private AddResponse<MeterReading> logTap(String assetId, String userId, Long unitID)
	{
		AddRequest<MeterReading> addRequest = fiixCmmsClient.prepareAdd(MeterReading.class);
		MeterReading object = new MeterReading();
		object.setIntMeterReadingUnitsID(unitID);
		object.setIntAssetID(Long.valueOf(assetId));
		object.setIntSubmittedByUserID(Long.valueOf(userId));
		object.setDtmDateSubmitted(new Date());
		object.setDblMeterReading(0.0);
		addRequest.setObject(object);
		addRequest.setFields(TAP_FIELDS);
		return fiixCmmsClient.add(addRequest);
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
		tapMRIDs = findResponse.getObjects().stream().filter(mr -> mr.getStrName().contains("Tap")).map(mr -> {
			if (mr.getStrName().contains("Out"))
			{
				outID = mr.getId();
			}
			else
			{
				inID = mr.getId();
			}
			return mr.getId();
		}).collect(Collectors.toList());
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

		if (filterList != null)
		{
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
		boolean hasAisleAndRow, hasBinNumber, hasParentId, hasLocation, hasGeoLocation;
		hasAisleAndRow = asset.getStrRow() != null && asset.getStrAisle() != null;
		hasBinNumber = asset.getStrBinNumber() != null;
		hasParentId = asset.getIntAssetParentID() != null;
		hasLocation = asset.getIntAssetLocationID() != null;
		hasGeoLocation = asset.getDblLatitude() != null && asset.getDblLongitude() !=null;
		List<Object> parameters = new ArrayList<>();
		StringBuilder sb = new StringBuilder();

		sb.append("(id != ?)");
		parameters.add(asset.getId());
		if (hasParentId || hasLocation || hasAisleAndRow || hasBinNumber || hasGeoLocation)
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
				if (hasGeoLocation)
				{
					if (hasLocation || hasAisleAndRow || hasBinNumber)
					{
						sb.append(" OR ");
					}
					sb.append("(");

					sb.append("(dblLatitude > ? AND dblLatitude < ?)");
					parameters.add(asset.getDblLatitude() - GEO_LOCATION_RANGE);
					parameters.add(asset.getDblLatitude() + GEO_LOCATION_RANGE);

					sb.append(" AND ");

					sb.append("(dblLongitude > ? AND dblLongitude < ?)");
					parameters.add(asset.getDblLongitude() - GEO_LOCATION_RANGE);
					parameters.add(asset.getDblLongitude() + GEO_LOCATION_RANGE);
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

		if (asset != null)
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

	public byte[] getPDF(String assetID) throws Throwable
	{
		AssetDto dto =  getConsolidatedAsset(assetID);
		return pdfClass.createPDF(dto);
	}
}
