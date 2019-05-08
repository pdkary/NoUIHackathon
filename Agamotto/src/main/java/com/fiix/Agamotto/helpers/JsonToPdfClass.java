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
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;

//import java.io.FileNotFoundException;
//import com.itextpdf.text.DocumentException;

public class JsonToPdfClass
{
	static BaseColor cellBgTitle = BaseColor.LIGHT_GRAY;
	static BaseColor cellBgValue = BaseColor.WHITE;
	private JsonObject jsonObject;

	public void getJson(AssetDto assetDto) throws IOException {

		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		String jsonStr = mapper.writeValueAsString(assetDto);

		JsonReader jsonReader = Json.createReader(new StringReader(jsonStr));
		jsonObject = jsonReader.readObject();
		jsonReader.close();
		System.out.println(jsonObject.toString());
	}

	public static PdfPCell formatCell(PdfPCell pdfCellInput, BaseColor basecolor) {

		pdfCellInput.setBackgroundColor(basecolor);
        pdfCellInput.setHorizontalAlignment(Element.ALIGN_CENTER);
        pdfCellInput.setVerticalAlignment(Element.ALIGN_LEFT);
		return pdfCellInput;
	}

	public void addRowToPdfTable(String keyInJson, String title, PdfPTable pdfTable) {
		Font titleFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 14, Font.NORMAL, new CMYKColor(100, 80, 0, 0));
		Font dataFont = FontFactory.getFont(FontFactory.TIMES, 14, Font.NORMAL, new CMYKColor(100, 40, 0, 0));
		PdfPCell rowTitle = new PdfPCell(new Paragraph(title,titleFont));
		rowTitle = formatCell(rowTitle, cellBgTitle);
		String rowValueInJson=null;

		try {
		rowValueInJson = jsonObject.getString(keyInJson);
		}
		catch(Exception e) {
			PdfPCell rowValue = new PdfPCell(new Paragraph("NA",dataFont));
	        rowValue = formatCell(rowValue,cellBgValue);
		}
        PdfPCell rowValue = new PdfPCell(new Paragraph(rowValueInJson,dataFont));
        rowValue = formatCell(rowValue,cellBgValue);

        pdfTable.addCell(rowTitle);
        pdfTable.addCell(rowValue);
	}

	public void addTableToPdfTable(int childTableCols, String keyInJsonForMainArray, String[] childArrayTitles, String[] childArrayKeys, PdfPTable mainTable) {
		Font titleFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 14, Font.NORMAL, new CMYKColor(100, 80, 0, 0));
		Font dataFont = FontFactory.getFont(FontFactory.TIMES, 14, Font.NORMAL, new CMYKColor(100, 40, 0, 0));
		//Forming the childTable
		PdfPTable childTable = new PdfPTable(childTableCols);
		childTable.setWidthPercentage(100);
		JsonArray childArray = jsonObject.getJsonArray(keyInJsonForMainArray);
		for(int j=0;j<childArray.size();j++) {
			JsonObject childObject = childArray.getJsonObject(j);
			for(int k = 0; k< childArrayTitles.length;k++) {
				PdfPCell childTitle = new PdfPCell(new Paragraph(childArrayTitles[k]));
				String childInJson = childObject.getString(childArrayKeys[k]);
				PdfPCell childValue = new PdfPCell(new Paragraph(childInJson));
				childTable.addCell(childTitle);
				childTable.addCell(childValue);
			}
		}
		PdfPCell childTableTitle = new PdfPCell(new Paragraph("Neighbouring Assets"));
		mainTable.addCell(childTableTitle);
		mainTable.addCell(childTable);


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
        addRowToPdfTable("assetLocation","Asset Location",pdfTable);
        addRowToPdfTable("assetCreatedDate","Asset Created Date",pdfTable);
        addRowToPdfTable("assetLastInspectedDate","Asset Last Inspected",pdfTable);
        addRowToPdfTable("assetBarCode","Asset Bar Code",pdfTable);
        addRowToPdfTable("tenantId","Parent Tenant ID",pdfTable);
        addTableToPdfTable(2,"neighbouringAssets",new String[] {"Asset ID","Asset Status","Asset Last Inspected Date"},new String[] {"assetId", "assetStatus","assetLastServicedDate"}, pdfTable);


        System.out.println("Json written to PDF");
        return writePdf(pdfTable);

      } catch (Exception e)
      {
         e.printStackTrace();
         return null;
      }
   }
}