package com.fiix.Agamotto.config;

import com.ma.cmms.api.client.Credentials;

public class AgamottoCredentials implements Credentials
{
	private String accesskey;
	private String appkey;
	private String secret;

	public AgamottoCredentials(String accesskey, String appkey, String secret)
	{
		this.accesskey = accesskey;
		this.appkey = appkey;
		this.secret = secret;
	}

	@Override
	public String getAccessKey()
	{
		return accesskey;
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
