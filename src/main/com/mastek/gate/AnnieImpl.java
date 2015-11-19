package com.mastek.gate;

import gate.Corpus;
import gate.CorpusController;
import gate.Document;
import gate.Factory;
import gate.Gate;
import gate.util.GateException;
import gate.util.Out;
import gate.util.persistence.PersistenceManager;

import java.io.File;
import java.io.IOException;

public class AnnieImpl {
	/** The Corpus Pipeline application to contain ANNIE */
	private CorpusController annieController;

	/**
	 * Initialise the ANNIE system. This creates a "corpus pipeline" application
	 * that can be used to run sets of documents through the extraction system.
	 */
	public void initAnnie() throws GateException, IOException {
		Out.prln("Initialising ANNIE...");
		File pluginsHome = Gate.getPluginsHome();
		File anniePlugin = new File(pluginsHome, "ANNIE");
		System.out.println("Annie path = " + anniePlugin.getAbsolutePath());

		File annieGapp = new File(anniePlugin, "ANNIE_with_defaults.gapp");
		annieController = (CorpusController) PersistenceManager
				.loadObjectFromFile(annieGapp);

		Out.prln("...ANNIE loaded");
	} // initAnnie()

	/** Tell ANNIE's controller about the corpus you want to run on */
	public void setCorpus(Corpus corpus) {
		annieController.setCorpus(corpus);
	} // setCorpus

	/** Run ANNIE */
	public void execute() throws GateException {
		Out.prln("Running ANNIE...");
		annieController.execute();
		Out.prln("...ANNIE complete");
	} // execute()

	public void closeAnnie()
	{
		Corpus corp= annieController.getCorpus();
		if(!corp.isEmpty()){
			for(int i=0;i<corp.size();i++){
				Document doc1 = (Document)corp.remove(i);
				corp.unloadDocument(doc1);
				Factory.deleteResource(doc1);
				Factory.deleteResource(corp);
			}
		}
	}
}
