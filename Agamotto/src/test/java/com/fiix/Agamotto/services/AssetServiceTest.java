package com.fiix.Agamotto.services;

import com.ma.cmms.api.client.FiixCmmsClient;
import com.ma.cmms.api.client.dto.MeterReading;
import com.ma.cmms.api.crud.FindRequest;
import com.ma.cmms.api.crud.FindResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.internal.matchers.Find;

import static org.mockito.Mockito.*;

import java.util.*;

public class AssetServiceTest extends AssetService
{
	private static Long GENERIC_ID = -0L;
	private static Long TAP_IN_ID = -1L;
	private static Long TAP_OUT_ID = -2L;
	private static Long ASSET_ID = -3L;
	private static Long USER_ID = -4L;

	@Mock
	FiixCmmsClient fiixCmmsClient;

	MeterReading mockTapIn = new MeterReading();
	MeterReading mockTapOut = new MeterReading();

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
		mockTapIn.setExtraFields(extraFields);

		mockTapOut.setId(GENERIC_ID);
		mockTapOut.setDblMeterReading(0.0);
		mockTapOut.setDtmDateSubmitted(new Date());
		mockTapOut.setIntAssetID(ASSET_ID);
		mockTapOut.setIntMeterReadingUnitsID(TAP_OUT_ID);
		Map<String,Object> extraFields2 = new LinkedHashMap<>();
		extraFields.put("dv_intMeterReadingUnitsID","Tap Out (to)");
		mockTapOut.setExtraFields(extraFields);
	}

	@Test
	public void testTapSubmitsATapOutWhenOpenAssetIsTapped()
	{
		List<MeterReading> readings = new ArrayList<>();
		readings.add(mockTapIn);

		FindResponse<MeterReading> findResponse = new FindResponse<MeterReading>();
		findResponse.setObjects(readings);

		when(fiixCmmsClient.find(any(FindRequest.class))).thenReturn(findResponse);

	}

}
