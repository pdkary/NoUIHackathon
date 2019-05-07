package com.fiix.Agamotto.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ma.cmms.api.client.Credentials;
import com.ma.cmms.api.client.FiixCmmsClient;

@Configuration
public class AgamottoConfig
{
	@Value("${Agamotto.cube.subdomain}")
	private String subdomain;

	@Bean
	public Credentials agamottoCredentials()
	{
		return new AgamottoCredentials();
	}

	@Bean
	public FiixCmmsClient fiixCmmsClient(Credentials agamottoCredentials)
	{
		return new FiixCmmsClient(agamottoCredentials, subdomain);
	}

}
