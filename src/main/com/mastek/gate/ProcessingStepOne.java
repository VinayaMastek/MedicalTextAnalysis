package com.mastek.gate;

import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.creole.ResourceInstantiationException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

public class ProcessingStepOne {

	List<Document> docsToBeProcessed = new ArrayList<Document>();

	
	public List<Document> getDocsToBeProcessed() {
		return docsToBeProcessed;
	}


	public void buildProcessFiles(String files[]) {
		System.out.println("Create Gate Corpus");
		for (int i = 0; i < files.length; i++) {
			// Get the filename and use the same to create the text file
			String filename = files[i].substring(0, files[i].indexOf("."));
			URL sourceURL=null;
			if (files[i].substring(files[i].indexOf(".")).equalsIgnoreCase(".PDF"))
			{
				PDDocument document;
				try {
					document = PDDocument.load(files[i]);

						// Create PDFTextStripper - to get the text
						PDFTextStripper textStripper = new PDFTextStripper();
						textStripper.setStartPage(1);
						textStripper.setEndPage(document.getNumberOfPages());
						String text = textStripper.getText(document);
		
						String textFile = filename + ".txt";
						File file = new File(textFile);
		
						if (!file.exists()) {
							file.createNewFile();
						} else {
							file.delete();
							file.createNewFile();
						}
		
						FileWriter fw = new FileWriter(file.getAbsoluteFile());
						BufferedWriter bw = new BufferedWriter(fw);
						bw.write(text);
						bw.close();
						sourceURL =new URL("file:/" + textFile); 
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				try {
					if (files[i].contains("http"))
						sourceURL = new URL(files[i]);	
					else
						sourceURL = new URL("file:/" +files[i]);
					
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
			FeatureMap params = Factory.newFeatureMap();
			params.put("sourceUrl", sourceURL);
			params.put("preserveOriginalContent", new Boolean(true));
			params.put("collectRepositioningInfo", new Boolean(true));
			Document doc =null;
			try {
				doc = (Document) Factory.createResource(
						"gate.corpora.DocumentImpl", params);
			} catch (ResourceInstantiationException e) {
				e.printStackTrace();
			}
			
			docsToBeProcessed.add(doc);
			System.out.println("Done");

		}
	}
}