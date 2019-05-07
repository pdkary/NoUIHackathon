package com.fiix.Agamotto.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ma.cmms.api.client.FiixCmmsClient;
import com.ma.cmms.api.client.dto.Asset;
import com.ma.cmms.api.crud.FindRequest;
import com.ma.cmms.api.crud.FindResponse;

@RestController
public class AgamottoRestController
{
	public static String DETAIL_FIELDS = "id, strName, strCode, strDescription, strCity, strAddress, strNotes, intSiteID, intCategoryID, strSerialNumber, intAssetLocationID, bolIsOnline, intAssetParentID, strStockLocation, intUpdated, strBarcode";

	@Autowired
	public FiixCmmsClient fiixCmmsClient;

	@RequestMapping("/")
	@ResponseBody
	public String home()
	{
		return "Peer into the Eye of Agamotto";
	}

	@RequestMapping("/details/{assetID}")
	@ResponseBody
	public String getDetails(@PathVariable(value = "assetID") String id) throws JsonProcessingException
	{
		FindRequest<Asset> findRequest = fiixCmmsClient.prepareFind(Asset.class);
		findRequest.setFields(DETAIL_FIELDS);
		FindResponse<Asset> findResponse = fiixCmmsClient.find(findRequest);
		List<Asset> assets = findResponse.getObjects();
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(assets);
	}
}
