import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
//import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
//import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.CMYKColor;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
 
public class JsonToPdfClass

{
	static BaseColor cellBgTitle = BaseColor.LIGHT_GRAY;
	static BaseColor cellBgValue = BaseColor.WHITE;
	private static JsonObject jsonObject;
	
	public static void getJson() throws IOException {
		HttpURLConnection connection = null;
		String url = "http://10.0.0.159:8080/consolidated/5904071";

	    URL obj = new URL(url);
	    HttpURLConnection con = (HttpURLConnection) obj.openConnection();

	    // optional default is GET
	    con.setRequestMethod("GET");

	    int responseCode = con.getResponseCode();
	    System.out.println("\nSending 'GET' request to URL : " + url);
	    System.out.println("Response Code : " + responseCode);
	    
	    
	    
	    final InputStream fis = con.getInputStream();
	    
	    //final byte[] bytes = fis.readAllBytes();
	    //System.out.println(new String(bytes));
	    
		JsonReader jread = Json.createReader(fis);
		jsonObject = jread.readObject();
		System.out.println(jsonObject.toString());
		jread.close();
	}
	
	public static PdfPCell formatCell(PdfPCell pdfCellInput, BaseColor basecolor) {
		
		pdfCellInput.setBackgroundColor(basecolor);
        pdfCellInput.setHorizontalAlignment(Element.ALIGN_CENTER);
        pdfCellInput.setVerticalAlignment(Element.ALIGN_LEFT);
		return pdfCellInput;
	}
	
	public static void addRowToPdfTable(String keyInJson, String title, PdfPTable pdfTable) {
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
	
	public static void addTableToPdfTable(int childTableCols, String keyInJsonForMainArray, String[] childArrayTitles, String[] childArrayKeys, PdfPTable mainTable) {
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
	
	public static void writePdf(PdfPTable pdfTable) throws FileNotFoundException, DocumentException {
		Document document = new Document(); 
    	PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("/Users/neerajavinoba/Desktop/Agamotto/HelloWorld.pdf"));
        document.open();
        document.add(pdfTable);
        document.close();
        writer.close();
	}
	
	public static void main(String[] args) throws Exception
   {
      
	  //Get JSON
		getJson();
		
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
        
        writePdf(pdfTable);
        System.out.println("Json written to PDF");
        
        
      } catch (Exception e)
      {
         e.printStackTrace();
      } 
   }
}