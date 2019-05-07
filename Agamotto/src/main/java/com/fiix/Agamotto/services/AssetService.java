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

@Service
public class AssetService
{
	private static String DETAIL_FIELDS = "id, strName, strCode, strDescription, strCity, strAddress, strNotes, intSiteID, intCategoryID, strSerialNumber, intAssetLocationID, bolIsOnline, intAssetParentID, strStockLocation, intUpdated, strBarcode";
	private static String HISTORY_FIELDS = "id,dblMeterReading,dtmDateSubmitted,dv_intMeterReadingUnitsID";
	private static String ID = "id";
	private static String ASSETID = "intAssetID";

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

	public List<MeterReading> getMeterReadings(String assetId)
	{
		List<FindFilter> filterList = getFilter(ASSETID, "=", assetId);
		FindRequest<MeterReading> findRequest = fiixCmmsClient.prepareFind(MeterReading.class);
		findRequest.setFilters(filterList);
		findRequest.setFields(HISTORY_FIELDS);

		FindResponse<MeterReading> findResponse = fiixCmmsClient.find(findRequest);
		return findResponse.getObjects();
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
}
