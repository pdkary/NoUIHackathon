package com.fiix.Agamotto.controllers;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AgamottoRestController
{
	public static String DETAIL_FIELDS = "id, strName, strCode, strDescription, strCity, strAddress, strNotes, intSiteID, intCategoryID, strSerialNumber, intAssetLocationID, bolIsOnline, intAssetParentID, strStockLocation, intUpdated, strBarcode";

	@RequestMapping("/details/{assetID}")
	public void getDetails(@PathVariable(value = "assetID") String id)
	{
		//make bean to autowire an authenticated fiixCmmsClient 
	}
}
