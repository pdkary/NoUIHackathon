package com.fiix.Agamotto.config;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class AgamottoCredentialsTest
{
	@Autowired
	private AgamottoCredentials agamottoCredentials;

	@Test
	public void testCredentialsGetPulledFromProperties()
	{
		assertNotNull(agamottoCredentials.getAccessKey());
		assertNotNull(agamottoCredentials.getAppKey());
		assertNotNull(agamottoCredentials.getSecretKey());
	}

}
