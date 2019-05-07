package com.fiix.Agamotto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.fiix.Agamotto.services.AssetService;

@SpringBootApplication
public class AgamottoApplication
{

	public static void main(String[] args)
	{
		ConfigurableApplicationContext context = SpringApplication.run(AgamottoApplication.class, args);
		context.getBean(AssetService.class).getTapMeterReadingIDs();
	}
}
