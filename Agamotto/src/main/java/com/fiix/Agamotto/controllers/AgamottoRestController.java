package com.fiix.Agamotto.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ma.cmms.api.client.FiixCmmsClient;
import com.ma.cmms.api.client.dto.Asset;

import services.AssetService;

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
		Optional<Asset> asset = assetService.getAsset(id);
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		return asset.isPresent() ? mapper.writeValueAsString(asset.get()) : "";
	}
}
