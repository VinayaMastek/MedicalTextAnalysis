package com.mastek.gate;

import java.util.Comparator;

public class ProcessingStepFourComparator implements Comparator<ProcessingStepFour> {

	@Override
	public int compare(ProcessingStepFour arg0, ProcessingStepFour arg1) {
		if(arg0.getStartPos() > arg1.getStartPos())
            return 1;
        if(arg0.getStartPos() < arg1.getStartPos())
            return -1;
        else{
        	if(arg0.getId() > arg1.getId())
                return 1;
            if(arg0.getId() < arg1.getId())
                return -1;
         
            return 0;
        }
        	    
	}
	
}
