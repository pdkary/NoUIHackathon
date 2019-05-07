package com.fiix.Agamotto.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ma.cmms.api.client.Credentials;
import com.ma.cmms.api.client.FiixCmmsClient;

import services.AssetService;

@Configuration
public class AgamottoConfig
{
	@Value("${Agamotto.cube.subdomain}")
	private String subdomain;

	@Value("${Agamotto.cube.appkey}")
	private String appkey;

	@Value("${Agamotto.cube.accesskey}")
	private String accesskey;

	@Value("${Agamotto.cube.secret}")
	private String secret;

	@Bean
	public Credentials agamottoCredentials()
	{
		return new AgamottoCredentials(accesskey, appkey, secret);
	}

	@Bean
	public FiixCmmsClient fiixCmmsClient(Credentials agamottoCredentials)
	{
		return new FiixCmmsClient(agamottoCredentials, subdomain);
	}

	@Bean
	public AssetService assetService()
	{
		return new AssetService();
	}

}
