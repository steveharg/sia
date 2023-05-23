package org.qmul.eecs.c4dm.sia.model;

import java.util.Iterator;
import java.util.Vector;

import org.qmul.eecs.c4dm.sia.SiaMain;
import org.qmul.eecs.c4dm.sia.exceptions.DimensionException;

import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author stevenhargreaves
 *
 */
public class VectorTableElement extends NDimensionalObject implements Comparable {
	
	private Datapoint fromDatapoint;
	private Datapoint toDatapoint;
	private Resource resource;
	
	public static final String RESOURCE_URI = SiaMain.SIA_NS_URI + "VectorTableElement";

	/**
	 * @return
	 */
	public Resource getResource() {
		return resource;
	}

	/**
	 * @param resource
	 */
	public void setResource(Resource resource) {
		this.resource = resource;
	}

	/**
	 * @return
	 */
	public Datapoint getFromDatapoint() {
		return fromDatapoint;
	}

	/**
	 * @param fromDatapoint
	 */
	public void setFromDatapoint(Datapoint fromDatapoint) {
		this.fromDatapoint = fromDatapoint;
	}

	/**
	 * @return
	 */
	public Datapoint getToDatapoint() {
		return toDatapoint;
	}

	/**
	 * @param toDatapoint
	 */
	public void setToDatapoint(Datapoint toDatapoint) {
		this.toDatapoint = toDatapoint;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Object o2) {
		VectorTableElement vte2 = (VectorTableElement)o2;
		
		Vector<DimensionValue> vte2DimVals = vte2.getDimensionValues();
		
		int vte2DimSize = vte2DimVals.size();
		
		if (vte2DimSize != this.getDimensionValues().size())
			throw new ClassCastException("VectorTableElements have an unequal number of dimensions");
		
		Iterator<DimensionValue> vte1DimValsIter = this.getDimensionValues().iterator();
		Iterator<DimensionValue> vte2DimValsIter = vte2DimVals.iterator();
		
		DimensionValue vte1DimVal;
		DimensionValue vte2DimVal;
		
		for (int dimension = 1; dimension <= vte2DimSize; dimension++)
		{
			double dimension1 = 0;
			try {
				dimension1 = this.getDimensionValue(dimension);
			} catch (DimensionException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
				System.exit(-1);
			}
			double dimension2 = 0;
			try {
				dimension2 = vte2.getDimensionValue(dimension);
			} catch (DimensionException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
				System.exit(-1);
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
		
		if (this.getFromDatapoint().getOrderedIndex() < vte2.getFromDatapoint().getOrderedIndex())
		{
			return -1;
		}
		else if (this.getFromDatapoint().getOrderedIndex() > vte2.getFromDatapoint().getOrderedIndex())
		{
			return 1;
		}
		return 0;
	}

}
