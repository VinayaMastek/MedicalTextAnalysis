package com.mastek.gate;

import java.util.Date;

public class ProcessingStepFive {
	String page;
	Date eventDt;
	String annotationType;
	String group;
	String annotatedText;
	String sentence;
	
	String value;
	
	String unit;

	String measure;
	String startRange;
	String endRange;
	
	String risk;
	
	
	public String getRisk() {
		return risk;
		
		
	}
	public void setRisk(String risk) {
		this.risk = risk;
	}
	public String getStartRange() {
		return startRange;
	}
	public void setStartRange(String startRange) {
		this.startRange = startRange;
	}
	public String getEndRange() {
		return endRange;
	}
	public void setEndRange(String endRange) {
		this.endRange = endRange;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	public String getMeasure() {
		return measure;
	}
	public void setMeasure(String measure) {
		this.measure = measure;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getPage() {
		return page;
	}
	public void setPage(String page) {
		this.page = page;
	}
	public Date getEventDt() {
		return eventDt;
	}
	public void setEventDt(Date eventDt) {
		this.eventDt = eventDt;
	}
	public String getAnnotationType() {
		return annotationType;
	}
	public void setAnnotationType(String annotationType) {
		this.annotationType = annotationType;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public String getAnnotatedText() {
		return annotatedText;
	}
	public void setAnnotatedText(String annotatedText) {
		this.annotatedText = annotatedText;
	}
	public String getSentence() {
		return sentence;
	}
	public void setSentence(String sentence) {
		this.sentence = sentence;
	}
	
	

}
