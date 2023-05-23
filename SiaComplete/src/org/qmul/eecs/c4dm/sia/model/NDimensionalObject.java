package org.qmul.eecs.c4dm.sia.model;

import java.util.Vector;

import org.qmul.eecs.c4dm.sia.exceptions.DimensionException;

public class NDimensionalObject {

	private Vector<DimensionValue> dimensionValues;

	public NDimensionalObject() {
		super();
	}

	/**
	 * @return the dimensionValue
	 */
	public Vector<DimensionValue> getDimensionValues() {
		return dimensionValues;
	}

	/**
	 * @param dimensionValue the dimensionValue to set
	 */
	public void setDimensionValues(Vector<DimensionValue> dimensionValues) {
		this.dimensionValues = dimensionValues;
	}

	public double getDimensionValue(int dimension) throws DimensionException {
		for (DimensionValue dv : this.getDimensionValues())
		{
			if (dv.getDimension() == dimension)
				return dv.getValue();
		}
		throw new DimensionException("No value for dimension " + dimension);
	}

}