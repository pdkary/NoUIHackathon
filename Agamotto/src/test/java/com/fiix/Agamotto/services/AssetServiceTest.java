package com.fiix.Agamotto.services;

import com.fiix.Agamotto.models.TapDto;
import com.ma.cmms.api.client.FiixCmmsClient;
import com.ma.cmms.api.client.dto.MeterReading;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AssetServiceTest
{
	private static Long GENERIC_ID = -0L;
	private static Long TAP_IN_ID = -1L;
	private static Long TAP_OUT_ID = -2L;
	private static Long ASSET_ID = -3L;
	private static Long USER_ID = -4L;

	private static String TEST_STR = "Test";

	@Mock
	FiixCmmsClient fiixCmmsClient;

	MeterReading mockTapIn = new MeterReading();
	MeterReading mockTapOut = new MeterReading();

	@InjectMocks
	public AssetService sut;

	@Before
	public void setUp()
	{
		mockTapIn.setId(GENERIC_ID);
		mockTapIn.setDblMeterReading(0.0);
		mockTapIn.setDtmDateSubmitted(new Date());
		mockTapIn.setIntAssetID(ASSET_ID);
		mockTapIn.setIntMeterReadingUnitsID(TAP_IN_ID);
		Map<String,Object> extraFields = new LinkedHashMap<>();
		extraFields.put("dv_intMeterReadingUnitsID","Tap in (ti)");
		extraFields.put("dv_intAssetID",TEST_STR);
		mockTapIn.setExtraFields(extraFields);

		mockTapOut.setId(GENERIC_ID);
		mockTapOut.setDblMeterReading(0.0);
		mockTapOut.setDtmDateSubmitted(new Date());
		mockTapOut.setIntAssetID(ASSET_ID);
		mockTapOut.setIntMeterReadingUnitsID(TAP_OUT_ID);
		Map<String,Object> extraFields2 = new LinkedHashMap<>();
		extraFields.put("dv_intMeterReadingUnitsID","Tap Out (to)");
		mockTapOut.setExtraFields(extraFields);

		List<MeterReading> readings = new ArrayList<>();
		readings.add(mockTapIn);

		when(sut.getMeterReadingsByUser("18")).thenReturn(readings);
	}

	@Test
	public void testTapSubmitsATapOutWhenOpenAssetIsTapped()
	{
		TapDto tapDto = sut.Tap("1","18");
		assert(tapDto.getOutTap().equals(TEST_STR));
	}
}