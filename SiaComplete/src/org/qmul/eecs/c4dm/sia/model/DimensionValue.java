package org.qmul.eecs.c4dm.sia.model;

import org.qmul.eecs.c4dm.sia.SiaMain;

public class DimensionValue {
	
	private int dimension;
	private double value;
	
	public static final String PROPERTY_URI    = SiaMain.SIA_NS_URI + "dimVal";
	public static final String DIMENSION_URI    = SiaMain.SIA_NS_URI + "dimension";
	public static final String VALUE_URI    = SiaMain.SIA_NS_URI + "value";

	public int getDimension() {
		return dimension;
	}
	public void setDimension(int dimension) {
		this.dimension = dimension;
	}
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}

}
