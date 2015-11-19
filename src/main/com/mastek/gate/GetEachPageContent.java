package com.mastek.gate;

import com.mastek.gate.Constants;

import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;


public class GetEachPageContent {
	
	public static void main(String[] args) {
		String[] files = new String[1];
		files[0] = "Test eSAR.pdf";
		PDDocument document;
		
		for (int i = 1; i < files.length; i++) {
			try {
				document = PDDocument.load(Constants.FILEPATH + files[i]);
				int pgCount = document.getNumberOfPages();
				// Create PDFTextStripper - to get the text
	            PDFTextStripper textStripper=new PDFTextStripper();
	            String pages= null;
	            textStripper.setStartPage(1); 
                textStripper.setEndPage(pgCount);
	            String fullDocument = textStripper.getText(document);
                System.out.println("*******************************" + " Full Document "+ "*******************************");
                System.out.println(fullDocument);
	            for(int j = 1; j < pgCount; j++) {
	            	textStripper.setStartPage(j); 
	                textStripper.setEndPage(j);
	                pages = textStripper.getText(document);
	                System.out.println("*******************************" + "PAGE - " + j + " of " + pgCount + "*******************************");
	                System.out.println(pages);
	            }
	            
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
}
