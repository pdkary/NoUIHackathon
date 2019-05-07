package com.fiix.Agamotto.config;

import org.springframework.beans.factory.annotation.Value;

import com.ma.cmms.api.client.Credentials;

public class AgamottoCredentials implements Credentials
{
	@Value("${Agamotto.cube.appkey}")
	private String appkey;

	@Value("${Agamotto.cube.apikey}")
	private String apikey;

	@Value("${Agamotto.cube.secret}")
	private String secret;

	@Override
	public String getAccessKey()
	{
		return apikey;
	}

	@Override
	public String getAppKey()
	{
		return appkey;
	}

	@Override
	public String getSecretKey()
	{
		return secret;
	}

}
