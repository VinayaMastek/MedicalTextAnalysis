package com.mastek.controllers;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.util.GateException;
import gate.util.InvalidOffsetException;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.Message.Level;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import com.mastek.gate.AnnieImpl;
import com.mastek.gate.GateImpl;
import com.mastek.gate.ProcessingStepFive;
import com.mastek.gate.ProcessingStepFiveComparator;
import com.mastek.gate.ProcessingStepFour;
import com.mastek.gate.ProcessingStepFourComparator;
import com.mastek.gate.ProcessingStepOne;

import net.sf.json.JSONObject;

@Path("/textanalysis")
public class PDFReadController {
	//private KieServices kService = null;
	//private KieContainer kContainer = null;
	//private StatelessKieSession kSession = null;

	@Context 
	private HttpServletRequest request;
	
	@Path("/annotatePDFwithmedicalterms")
	@GET
	@Produces("text/json")
	public List<JSONObject> annotatePDFwithmedicalterms(
			@QueryParam("files") String files) {
		
		HttpSession session =    request.getSession();
		List<JSONObject> p5JSONList = new ArrayList<JSONObject>();
		ServletContext ctx = session.getServletContext();

		
		// Initialize GATE
		GateImpl gateComponent = new GateImpl(ctx);
		gateComponent.initializeGate();

		// Convert the files selected into the format recognized by GATE
		List<Document> docs = processingStepOne(files.split(";"));

		// Create GATE Corpus
		processingStepTwo(gateComponent, docs);

		// Execute Annie
		AnnieImpl annie = processingStepThree(gateComponent);

		Iterator<Document> iter = gateComponent.getCorpus().iterator();
		
		KieSession kSession = invokeDrools(ctx);
	
		while (iter.hasNext()) {
			Document doc = (Document) iter.next();

			// Get Annotation Data - Date, AnnotationType, AnnotationText,
			// Sentence
			List<ProcessingStepFour> processingStepFourList = processingStepFour(doc);

			// Create groups of Data by Timeboxing data (data within 25days of
			// each other)
			List<ProcessingStepFive> processingStepFiveList = processingStepFive(processingStepFourList);

			processingStepFiveList = processingStepSix(processingStepFiveList);

			for (ProcessingStepFive p5 : processingStepFiveList) {

				kSession.insert(p5);
				kSession.fireAllRules();

				SimpleDateFormat sf = new SimpleDateFormat("dd-MM-yyyy");
				String eventDt = sf.format(p5.getEventDt());
				
				JSONObject p5JsonObj = new JSONObject();
				p5JsonObj.put("filename", doc.getName().toString());
				p5JsonObj.put("page", p5.getPage());
				p5JsonObj.put("group", p5.getGroup());
				p5JsonObj.put("eventDt", eventDt );
				p5JsonObj.put("annotationtype", p5.getAnnotationType());
				p5JsonObj.put("annotationtext", p5.getAnnotatedText());
				p5JsonObj.put("sentence", p5.getSentence());
				p5JsonObj.put("unit", p5.getUnit());
				p5JsonObj.put("value", p5.getValue());
				p5JsonObj.put("measure", p5.getMeasure());
				p5JsonObj.put("startRange", p5.getStartRange());
				p5JsonObj.put("endRange", p5.getEndRange());
				p5JsonObj.put("risk", p5.getRisk());
				p5JSONList.add(p5JsonObj);

				System.out.print(doc.getName().toString());
				System.out.print(" ; ");
				System.out.print(p5.getPage());
				System.out.print(" ; ");
				System.out.print(p5JsonObj.get("eventDt"));
				System.out.print(" ; ");
				System.out.print(p5.getAnnotationType());
				System.out.print(" ; ");
				System.out.print(p5.getAnnotatedText());
				System.out.print(" ; ");
				System.out.print(p5.getSentence());
				System.out.print(" ; ");
				System.out.print(p5.getUnit());
				System.out.print(" ; ");
				System.out.print(p5.getMeasure());
				System.out.print(" ; ");
				System.out.print(p5.getValue());
				System.out.print(" ; ");
				System.out.print(p5.getStartRange());
				System.out.print(" ; ");
				System.out.print(p5.getEndRange());
				System.out.print(" ; ");
				System.out.print(p5.getRisk());
				System.out.println("");

			}
		}
		annie.closeAnnie();
		System.out.println("Done");

		return p5JSONList;

	}

	// http://localhost:8080/MedicalTextAnalysisWeb/rest/textanalysis/annotatePDFwithmedicalterms?files=D:/workspace/MedicalTextAnalysis/DocRepository/Test%20eSAR.pdf;

	private KieSession invokeDrools(ServletContext ctx) {
		KieServices ks = KieServices.Factory.get();
		KieRepository kr = ks.getRepository();
		KieFileSystem kfs = ks.newKieFileSystem();
		
		//String path  = ctx.getRealPath("WEB-INF\\resources\\medicalRules\\Sample.drl");
		//String path  = ctx.getRealPath("/WEB-INF/resources/medicalRules");
		//System.out.println(path);
		
		File path = new File(ctx.getRealPath("/WEB-INF/resources/medicalRules"));
		
		Resource resource = ks.getResources().newFileSystemResource(
				new File(path, "Sample.drl"));
		
		System.out.println(resource.toString());

		kfs.write(resource);
		KieBuilder kb = ks.newKieBuilder(kfs);
		kb.buildAll(); // kieModule is automatically deployed to KieRepository
						// if successfully built.

		if (kb.getResults().hasMessages(Level.ERROR)) {
			throw new RuntimeException("Build Errors:\n"
					+ kb.getResults().toString());
		}
		KieContainer kContainer = ks.newKieContainer(kr.getDefaultReleaseId());
		KieSession kSession = kContainer.newKieSession();
		return kSession;
		
	}

	private List<ProcessingStepFive> processingStepSix(
			List<ProcessingStepFive> processingStepFiveList) {
		String sentence = null;
		for (ProcessingStepFive p5 : processingStepFiveList) {
			sentence = p5.getSentence();

			Pattern pattern = Pattern.compile("([0-9]+[ ])*(mg|gram)");
			Pattern pattern1 = Pattern
					.compile("(([0-9]*|[0-9]+)[ ]+([Tt]ablet[/(/)s]*|[Cc]apsule[s]))|(([Cc]ream[ ]*[0-9]+[ ]*%|[0-9]+[ ]*%[ ]*[cC]ream))");
			
			Matcher matcher = pattern.matcher(sentence);
			Matcher matcher1 = pattern1.matcher(sentence);

			while (matcher.find()) {
				p5.setUnit(matcher.group(1));
				p5.setMeasure(matcher.group(2));
			}

			while (matcher1.find()) {
				if (matcher1.group(1) != null) 
					if (!matcher1.group(1).isEmpty())
							p5.setValue(matcher1.group(1));
				if (matcher1.group(2) != null) 
					if (!matcher1.group(2).isEmpty())
						p5.setUnit(matcher1.group(2));
			}

			if ((sentence
					.matches("(.*?)([\\d]+[\\.]?[\\d]*)(.*?)([\\d]+[\\.]?[\\d]*)(.*?)([\\d]+[\\.]?[\\d]*)(.*)"))
					&& (p5.getAnnotationType().equals("2.procedure"))) {
				
				Pattern plasma = Pattern
						.compile("([a-zA-Z_0-9 /-]+)[ ]*(.*?)([0-9]+[/.]?[0-9]*)(.*?)([0-9]+[/.]?[0-9]*)(.*[ ]*[-][ ]*)([0-9]+[/.]?[0-9]*)");
				
				Matcher matcher2 = plasma.matcher(sentence);
				while (matcher2.find()) {

					p5.setValue(matcher2.group(1));
					p5.setUnit(matcher2.group(3));
					p5.setStartRange(matcher2.group(5));
					p5.setEndRange(matcher2.group(7));
					break;
				}
			}
		}
		return processingStepFiveList;
	}

	private List<ProcessingStepFive> processingStepFive(
			List<ProcessingStepFour> processingStepFourList) {
		Collections.sort(processingStepFourList,
				new ProcessingStepFourComparator());

		Map<String, String> terms = new HashMap<String, String>();
		terms.put("disorder", "1.disorder");
		terms.put("product", "4.product");
		terms.put("substance", "4.substance");
		terms.put("finding", "3.finding");
		terms.put("procedure", "2.procedure");

		int sentenceCtr = 0;
		Date actualDt = null;
		Date prevDt = null;
		String sentence = "";
		Integer group = 0;
		List<ProcessingStepFive> processingStepFiveList = new ArrayList<ProcessingStepFive>();
		String pgNo = null;

		for (ProcessingStepFour p : processingStepFourList) {

			if (p.getText().contains("\n")) {
				System.out.println(p.getType() + p.getText());
			}
			if (p.getText().contains("PAGE")) {
				int index = p.getText().indexOf("PAGE");
				pgNo = p.getText().substring(index + 5, index + 7);
			}

			if ((p.getType().equals("Sentence"))
					&& (!p.getText().contains("SAR DATA EXTRACT"))) {
				sentenceCtr++;
				sentence = p.getText();
			}

			if (p.getType().equals("Date")) {
				if (!p.getText().contains(":")) {
					actualDt = convertDate(p.getText());
				}
			}

			if ((sentenceCtr > 0) && (terms.containsKey(p.getType()))) {
				ProcessingStepFive processingStepFive = new ProcessingStepFive();
				processingStepFive.setPage(pgNo);
				processingStepFive.setEventDt(actualDt);
				processingStepFive.setAnnotationType(terms.get(p.getType()));
				processingStepFive.setAnnotatedText(p.getText());
				processingStepFive.setSentence(sentence);
				processingStepFiveList.add(processingStepFive);

			}
		}
		Collections.sort(processingStepFiveList,
				new ProcessingStepFiveComparator());

		SimpleDateFormat sf = new SimpleDateFormat("dd-MM-yyyy");
		try {
			actualDt = sf.parse("01-01-1900");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		for (ProcessingStepFive p5 : processingStepFiveList) {
			prevDt = actualDt;
			actualDt = p5.getEventDt();
			if (calculateDays(prevDt, actualDt) > 25) {
				group++;
			}
			p5.setGroup(group.toString());
		}
		return processingStepFiveList;
	}

	private List<ProcessingStepFour> processingStepFour(Document doc) {
		AnnotationSet defaultAnnotSet = doc.getAnnotations();
		Set<String> annotTypesRequired = new HashSet<String>();
		annotTypesRequired.add("Sentence");
		annotTypesRequired.add("Date");
		annotTypesRequired.add("disorder");
		annotTypesRequired.add("product");
		annotTypesRequired.add("substance");
		annotTypesRequired.add("finding");
		annotTypesRequired.add("procedure");
		Set<Annotation> peopleAndPlaces = new HashSet<Annotation>(
				defaultAnnotSet.get(annotTypesRequired));

		Annotation currAnnot;
		Iterator<Annotation> it = peopleAndPlaces.iterator();
		List<ProcessingStepFour> processingStepFourList = new ArrayList<ProcessingStepFour>();

		while ((it.hasNext())) {
			currAnnot = it.next();
			Long startPos = currAnnot.getStartNode().getOffset().longValue();
			Long endPos = currAnnot.getEndNode().getOffset().longValue();

			ProcessingStepFour processingStepFour = null;
			try {

				String text = doc.getContent().getContent(startPos, endPos)
						.toString();
				if ((currAnnot.getType().equals("Date"))
						&& ((endPos - startPos) > 5)) {
					processingStepFour = new ProcessingStepFour();
					processingStepFour.setId(currAnnot.getId());
					processingStepFour.setType(currAnnot.getType());
					processingStepFour.setStartPos(startPos.intValue());
					processingStepFour.setEndPos(endPos.intValue());
					processingStepFour.setText(text.trim());
				}
				if (!currAnnot.getType().equals("Date")) {
					processingStepFour = new ProcessingStepFour();
					processingStepFour.setId(currAnnot.getId());
					processingStepFour.setType(currAnnot.getType());
					processingStepFour.setStartPos(startPos.intValue());
					processingStepFour.setEndPos(endPos.intValue());
					processingStepFour.setText(text.trim());
				}
			} catch (InvalidOffsetException e) {
				e.printStackTrace();
			}
			if (processingStepFour != null)
				processingStepFourList.add(processingStepFour);

		}
		return processingStepFourList;
	}

	private AnnieImpl processingStepThree(GateImpl gateComponent) {
		AnnieImpl annie = new AnnieImpl();
		try {
			annie.initAnnie();
			annie.setCorpus(gateComponent.getCorpus());
			annie.execute();
			//annie.closeAnnie();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (ResourceInstantiationException e) {
			e.printStackTrace();
		} catch (GateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return annie;
	}

	private void processingStepTwo(GateImpl gateComponent, List<Document> docs) {
		gateComponent.setGateCorpus(docs);
	}

	private List<Document> processingStepOne(String[] files) {
		ProcessingStepOne processingStepOne = new ProcessingStepOne();
		processingStepOne.buildProcessFiles(files);
		return processingStepOne.getDocsToBeProcessed();
	}

	private Date convertDate(String dt) {
		DateFormat df = null;
		if (dt.contains("-")) {
			df = new SimpleDateFormat("dd-MM-yyyy");
		}

		if (dt.contains("/")) {
			df = new SimpleDateFormat("dd/MM/yyyy");
		}

		if (dt.contains(" ")) {
			df = new SimpleDateFormat("d MMM yyyy");
		}

		Date convertedDt = null;
		try {
			convertedDt = df.parse(dt);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return convertedDt;
	}

	/** Manual Method - YIELDS INCORRECT RESULTS - DO NOT USE **/
	/* This method is used to find the no of days between the given dates */
	private long calculateDays(Date dateEarly, Date dateLater) {
		return (dateLater.getTime() - dateEarly.getTime())
				/ (24 * 60 * 60 * 1000);
	}

}

// http://localhost:8080/MedicalTextAnalysisWeb/rest/textanalysis/annotatePDFwithmedicalterms?files=D:/workspace/MedicalTextAnalysis/DocRepository/Test%20eSAR.pdf;
