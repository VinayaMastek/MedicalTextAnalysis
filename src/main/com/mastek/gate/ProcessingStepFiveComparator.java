package com.mastek.gate;

import java.util.Comparator;


public class ProcessingStepFiveComparator implements Comparator<ProcessingStepFive> {

	@Override
	public int compare(ProcessingStepFive arg0, ProcessingStepFive arg1) {
		
		if(arg0.getEventDt().after(arg1.getEventDt()))
            return 1;
        if(arg0.getEventDt().before(arg1.getEventDt()))
            return -1;
        else{
        	return arg0.getAnnotationType().compareTo(arg1.getAnnotationType()); 
        }
	}
	
}
