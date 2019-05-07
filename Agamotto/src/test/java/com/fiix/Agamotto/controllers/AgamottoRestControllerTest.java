package com.fiix.Agamotto.controllers;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

public class AgamottoRestControllerTest
{

	private AgamottoRestController restController = new AgamottoRestController();

	private String bigOlMachineID = "9,643,149";

	@Test
	public void testAssetsReturnSomething()
	{
		try
		{
			String testString = restController.getDetails(bigOlMachineID);
			assertNotEquals("", testString);
		}
		catch (JsonProcessingException e)
		{
			fail("JsonProcessingException Occurred");
			e.printStackTrace();
		}

	}

}
