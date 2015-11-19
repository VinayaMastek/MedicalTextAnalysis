package com.mastek.gate;

import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.Gate;
import gate.creole.ResourceInstantiationException;
import gate.util.GateException;
import gate.util.Out;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.servlet.ServletContext;

import com.mastek.gate.Constants;

public class GateImpl {

	private final static Logger LOGGER = Logger.getLogger(GateImpl.class.getName()); 
	private Corpus gateCorpus=null;;
	private ServletContext ctx;
	
	public GateImpl(ServletContext ctx) {
		super();
		this.ctx = ctx;
	}


	public GateImpl() {
		super();
	}


	public Corpus getCorpus()
	{
		return gateCorpus;
	}

	
	public void setGateCorpus(List<Document> docs) {
		if (gateCorpus == null){
			try {
				gateCorpus = Factory.newCorpus(Constants.CORPUSNAME);
			} catch (ResourceInstantiationException e) {
				e.printStackTrace();
			}
		}
		for (Document doc :docs)
		{
			gateCorpus.add(doc);
		}
	}



	public void initializeGate()
	{
		
		// Logging code
		LOGGER.setLevel(Level.INFO);
		FileHandler fileTxt = null;
				try {
					fileTxt = new FileHandler("Logging.txt");
				} catch (SecurityException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}

	    // create a TXT formatter
	    SimpleFormatter formatterTxt = new SimpleFormatter();
	    fileTxt.setFormatter(formatterTxt);
	    LOGGER.addHandler(fileTxt);

		if (!Gate.isInitialised()){
			Out.prln("Initialising GATE...");
			File gatehome = new File(ctx.getRealPath("/WEB-INF"));
			Gate.setGateHome(gatehome);
			Gate.setUserConfigFile (new File ( gatehome , "user-gate .xml"));
			LOGGER.info(gatehome.getAbsolutePath());
			
			try {
				Gate.init();
			} catch (GateException e) {
				e.printStackTrace();
			}
			Out.prln("...GATE initialised");
		}
		else {
			Out.prln("...GATE is already Initialized");
		}
	}
	
	
}
