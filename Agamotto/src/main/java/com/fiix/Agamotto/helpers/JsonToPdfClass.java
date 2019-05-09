package com.fiix.Agamotto.helpers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiix.Agamotto.models.AssetDto;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.CMYKColor;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

//import java.io.FileNotFoundException;
//import com.itextpdf.text.DocumentException;

public class JsonToPdfClass
{
	static BaseColor cellBgTitle = BaseColor.WHITE;
	static BaseColor cellBgValue = BaseColor.WHITE;
	static Font titleFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 14, Font.NORMAL, BaseColor.BLACK);
	static Font dataFont = FontFactory.getFont(FontFactory.TIMES, 14, Font.NORMAL, BaseColor.BLACK);

	private JsonObject jsonObject;

	public void getJson(AssetDto assetDto) throws IOException {

		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		String jsonStr = mapper.writeValueAsString(assetDto);

		JsonReader jsonReader = Json.createReader(new StringReader(jsonStr));
		jsonObject = jsonReader.readObject();
		jsonReader.close();
		//System.out.println(jsonObject.toString());
	}

	public static PdfPCell formatCell(PdfPCell pdfCellInput, BaseColor basecolor) {

		pdfCellInput.setBackgroundColor(basecolor);
		pdfCellInput.setHorizontalAlignment(Element.ALIGN_CENTER);
		pdfCellInput.setVerticalAlignment(Element.ALIGN_LEFT);
		return pdfCellInput;
	}

	public void addRowToPdfTable(String keyInJson, String title, PdfPTable pdfTable) {
		PdfPCell rowTitle = new PdfPCell(new Paragraph(title,titleFont));
		rowTitle = formatCell(rowTitle, cellBgTitle);
		rowTitle.setHorizontalAlignment(Element.ALIGN_LEFT);
		String rowValueInJson=null;

		try {
			rowValueInJson = jsonObject.getString(keyInJson);
			if((keyInJson == "assetCreatedDate")||(keyInJson == "assetLastInspectedDate")) {
				@SuppressWarnings("deprecation")
				Long dateLong = Long.parseLong(rowValueInJson);
				Date date = new Date(dateLong);
				DateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss z");
				format.setTimeZone(TimeZone.getTimeZone("EST"));
				rowValueInJson = format.format(date);
			}
		}
		catch(Exception e) {
			rowValueInJson = "Not Available";
		}
		PdfPCell rowValue = new PdfPCell(new Paragraph(rowValueInJson,dataFont));
		rowValue = formatCell(rowValue,cellBgValue);
		rowValue.setHorizontalAlignment(Element.ALIGN_LEFT);
		pdfTable.addCell(rowTitle);
		pdfTable.addCell(rowValue);
	}

	public void addTableToPdfTable(int childTableCols, String keyInJsonForMainArray, String[] childArrayTitles, String[] childArrayKeys, PdfPTable mainTable) {
		PdfPTable childTable = new PdfPTable(childTableCols);
		childTable.setWidthPercentage(100);
		JsonArray childArray = jsonObject.getJsonArray(keyInJsonForMainArray);

		if(childTableCols == 1) {
			for(int j=0;j<childArray.size();j++) {
				JsonObject childObject = childArray.getJsonObject(j);
				String childInJson = childObject.getString(childArrayTitles[0]);
				PdfPCell childValue = new PdfPCell(new Paragraph(childInJson,dataFont));
				childValue = formatCell(childValue, cellBgValue);
				childValue.setHorizontalAlignment(Element.ALIGN_LEFT);
				childTable.addCell(childValue);
			}
//			for(JsonValue arrayVal: childArray) {
//				String childJson = arrayVal.toString();
//				PdfPCell childValue = new PdfPCell(new Paragraph(childJson,dataFont));
//				childTable.addCell(childValue);
//			}


			PdfPCell childTableTitle = new PdfPCell(new Paragraph("Asset Manuals",titleFont));
			mainTable.addCell(childTableTitle);
			mainTable.addCell(childTable);
		}

		else if(childTableCols > 1) {
			for(int j=0;j<childArray.size();j++) {
				JsonObject childObject = childArray.getJsonObject(j);
				for(int k = 0; k< childArrayTitles.length;k++) {
					PdfPCell childTitle = new PdfPCell(new Paragraph(childArrayTitles[k],titleFont));
					childTitle = formatCell(childTitle, cellBgTitle);
					childTitle.setHorizontalAlignment(Element.ALIGN_LEFT);
					String childInJson = childObject.getString(childArrayKeys[k]);
					if(childArrayKeys[k] == "assetLastServicedDate") {
						Long dateLong = Long.parseLong(childInJson);
						Date date = new Date(dateLong);
						DateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss z");
						format.setTimeZone(TimeZone.getTimeZone("EST"));
						childInJson = format.format(date);
					}
					PdfPCell childValue = new PdfPCell(new Paragraph(childInJson,dataFont));
					childValue = formatCell(childValue, cellBgValue);
					childValue.setHorizontalAlignment(Element.ALIGN_LEFT);
					childTable.addCell(childTitle);
					childTable.addCell(childValue);
				}
			}
			PdfPCell childTableTitle = new PdfPCell(new Paragraph("Neighbouring Assets",titleFont));
			mainTable.addCell(childTableTitle);
			mainTable.addCell(childTable);
		}
	}

	public byte[] writePdf(PdfPTable pdfTable) throws FileNotFoundException, DocumentException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Document document = new Document();
		PdfWriter.getInstance(document, baos);
		document.open();
		document.add(pdfTable);
		document.close();
		return baos.toByteArray();
	}

	public byte[] createPDF(AssetDto assetDto) throws Exception
	{
		getJson(assetDto);

		try {
			//Format the PDF
			PdfPTable pdfTable = new PdfPTable(2);
			pdfTable.setWidthPercentage(100); //Width 100%
			pdfTable.setSpacingBefore(10f); //Space before table
			pdfTable.setSpacingAfter(10f); //Space after table

			float[] columnWidths = {1f,1f};
			pdfTable.setWidths(columnWidths);

			String assetTitleString = "Information for the asset - ";
			PdfPCell assetTitle = new PdfPCell(new Paragraph(assetTitleString+jsonObject.getString("assetName")));
			assetTitle.setColspan(2);
			assetTitle.setHorizontalAlignment(Element.ALIGN_CENTER);
			pdfTable.addCell(assetTitle);

			addRowToPdfTable("assetStatus","Asset Status",pdfTable);
			addRowToPdfTable("assetCode","Asset ID",pdfTable);
			addRowToPdfTable("serialNumber","Serial Number",pdfTable);
			addRowToPdfTable("assetCategory","Asset Category",pdfTable);
			addRowToPdfTable("assetManufacturer","Asset Manufacturor",pdfTable);
			addRowToPdfTable("assetModel","Asset Model",pdfTable);
			addRowToPdfTable("assetStatus","Asset Status",pdfTable);
			addRowToPdfTable("assetDescription","Asset Description",pdfTable);
			//addRowToPdfTable("assetLocation","Asset Location",pdfTable);
			//addRowToPdfTable("assetCreatedDate","Asset Created Date",pdfTable);
			addRowToPdfTable("assetLastInspectedDate","Asset Last Inspected",pdfTable);
			addRowToPdfTable("assetBarCode","Asset Bar Code",pdfTable);
			addRowToPdfTable("tenantId","Parent Tenant ID",pdfTable);
			addTableToPdfTable(2,"neighbouringAssets",new String[] {"Asset ID","Asset Status","Asset Last Inspected Date"},new String[] {"assetId", "assetStatus","assetLastServicedDate"}, pdfTable);
			addTableToPdfTable(1,"assetManuals",new String[] {"manualUrl"},new String[] {""}, pdfTable);			return writePdf(pdfTable);

		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
}