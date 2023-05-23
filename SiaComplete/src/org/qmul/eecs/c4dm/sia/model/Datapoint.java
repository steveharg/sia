package org.qmul.eecs.c4dm.sia.model;

import java.util.Iterator;
import java.util.Vector;

import org.qmul.eecs.c4dm.sia.SiaMain;
import org.qmul.eecs.c4dm.sia.exceptions.DimensionException;

import com.hp.hpl.jena.rdf.model.Resource;

public class Datapoint extends NDimensionalObject implements Comparable {
	
	private Resource resource;
	private int orderedIndex;
	
	public static final String RESOURCE_URI    = SiaMain.SIA_NS_URI + "Datapoint";

	/**
	 * @return the resource
	 */
	public Resource getResource() {
		return resource;
	}

	/**
	 * @param resource the node to set
	 */
	public void setResource(Resource resource) {
		this.resource = resource;
	}

	/**
	 * @return the orderedIndex
	 */
	public int getOrderedIndex() {
		return orderedIndex;
	}

	/**
	 * @param orderedIndex the orderedIndex to set
	 */
	public void setOrderedIndex(int orderedIndex) {
		this.orderedIndex = orderedIndex;
	}

	@Override
	public int compareTo(Object o2) {
		Datapoint datapoint2 = (Datapoint)o2;
		
		Vector<DimensionValue> datapoint2DimVals = datapoint2.getDimensionValues();
		
		int datapoint2DimSize = datapoint2DimVals.size();
		
		if (datapoint2DimSize != this.getDimensionValues().size())
			throw new ClassCastException("Datapoints have an unequal number of dimensions");
		
		Iterator<DimensionValue> datapoint1DimValsIter = this.getDimensionValues().iterator();
		Iterator<DimensionValue> datapoint2DimValsIter = datapoint2DimVals.iterator();
		
		DimensionValue datapoint1DimVal;
		DimensionValue datapoint2DimVal;
		
		for (int dimension = 1; dimension <= datapoint2DimSize; dimension++)
		{
			double dimension1 = 0;
			try {
				dimension1 = this.getDimensionValue(dimension);
			} catch (DimensionException e) {
				// TODO Auto-generated catch block
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
			double dimension2 = 0;
			try {
				dimension2 = datapoint2.getDimensionValue(dimension);
			} catch (DimensionException e) {
				// TODO Auto-generated catch block
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
			
			if (dimension1 < dimension2)
			{
				return -1;
			}
			else if (dimension1 > dimension2)
			{
				return 1;
			}
		}
		
		return 0;
	}

}
