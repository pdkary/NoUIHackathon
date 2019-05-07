package services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ma.cmms.api.client.FiixCmmsClient;
import com.ma.cmms.api.client.dto.Asset;
import com.ma.cmms.api.client.dto.AssetEvent;
import com.ma.cmms.api.crud.FindFilter;
import com.ma.cmms.api.crud.FindRequest;
import com.ma.cmms.api.crud.FindResponse;

@Service
public class AssetService
{
	private static String DETAIL_FIELDS = "id, strName, strCode, strDescription, strCity, strAddress, strNotes, intSiteID, intCategoryID, strSerialNumber, intAssetLocationID, bolIsOnline, intAssetParentID, strStockLocation, intUpdated, strBarcode";
	private static String ID = "id";
	private static String ASSETID = "intAssetID";

	@Autowired
	private FiixCmmsClient fiixCmmsClient;

	public Asset getAsset(String assetId)
	{
		List<FindFilter> filterList = getFilter(ID, assetId);

		FindRequest<Asset> findRequest = fiixCmmsClient.prepareFind(Asset.class);
		findRequest.setFilters(filterList);
		findRequest.setFields(DETAIL_FIELDS);

		FindResponse<Asset> findResponse = fiixCmmsClient.find(findRequest);
		return findResponse.getObjects().get(0);
	}

	public List<AssetEvent> getAssetEvents(String assetId)
	{
		List<FindFilter> filterList = getFilter(ASSETID, assetId);
		FindRequest<AssetEvent> findRequest = fiixCmmsClient.prepareFind(AssetEvent.class);
		findRequest.setFilters(filterList);
		findRequest.setFields(DETAIL_FIELDS);

		FindResponse<AssetEvent> findResponse = fiixCmmsClient.find(findRequest);
		return findResponse.getObjects();
	}

	private List<FindFilter> getFilter(String field, String value)
	{
		FindFilter findFilter = new FindFilter();
		findFilter.setQl(field.concat("=").concat(value));

		List<FindFilter> filterList = new ArrayList<FindFilter>();
		filterList.add(findFilter);
		return filterList;
	}
}
