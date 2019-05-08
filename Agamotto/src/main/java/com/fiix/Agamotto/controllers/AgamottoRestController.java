package com.fiix.Agamotto.controllers;

import static java.util.Arrays.asList;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiix.Agamotto.services.AssetService;
import com.ma.cmms.api.client.FiixCmmsClient;
import com.ma.cmms.api.client.dto.Asset;
import com.ma.cmms.api.client.dto.MeterReading;

@RestController
public class AgamottoRestController
{
	@Autowired
	public FiixCmmsClient fiixCmmsClient;

	@Autowired
	public AssetService assetService;

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
		Asset asset = assetService.getAsset(id);
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		return mapper.writeValueAsString(asset);
	}

	@RequestMapping("/history/asset/{assetID}")
	@ResponseBody
	public String getAssetHistory(@PathVariable(value = "assetID") String id) throws JsonProcessingException
	{
		List<MeterReading> readings = assetService.getMeterReadingsByAsset(id);
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		return mapper.writeValueAsString(readings);
	}

	@RequestMapping("/history/user/{userId}")
	@ResponseBody
	public String getUserHistory(@PathVariable(value = "userId") String id) throws JsonProcessingException
	{
		List<MeterReading> readings = assetService.getMeterReadingsByUser(id);
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		return mapper.writeValueAsString(readings);
	}

	@GetMapping("/neighbors/{assetID}")
	@ResponseBody
	public String getNeighbors(@PathVariable(value = "assetID") String id) throws JsonProcessingException
	{
		final List<Asset> nearbyAssets = assetService.getNearbyAssets(id);
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		return mapper.writeValueAsString(asList(nearbyAssets));
	}

	@GetMapping("/tap")
	@ResponseBody
	public String testTap()
	{
		return assetService.Tap("5904069", "18");
	}
}
