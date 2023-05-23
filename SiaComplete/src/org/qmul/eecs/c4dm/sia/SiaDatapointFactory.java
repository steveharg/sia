package org.qmul.eecs.c4dm.sia;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.qmul.eecs.c4dm.sia.model.Datapoint;
import org.qmul.eecs.c4dm.sia.model.DimensionValue;
import org.qmul.eecs.c4dm.sia.model.DirectlyFollows;
import org.qmul.eecs.c4dm.sia.model.MemberOfOrderedSet;
import org.qmul.eecs.c4dm.sia.model.OrderedIndex;
import org.qmul.eecs.c4dm.sia.model.OrderedSet;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public class SiaDatapointFactory {

	public static List<Datapoint> create(OntModel ontModel) {
		
		OntClass datapointClass = ontModel.getOntClass(Datapoint.RESOURCE_URI);

		Resource datapointResource = ontModel.getOntResource(datapointClass);
		ExtendedIterator<Individual> datapointIter = ontModel.listIndividuals(datapointResource);

		List<Datapoint> datapointsList = new ArrayList<Datapoint>();
		Datapoint datapoint;
		DimensionValue dimVal;
		
		// Find all rdf datapoints
		Individual datapointIndividual;
		StmtIterator dimValStmtIter;
		while (datapointIter.hasNext())
		{
			datapointIndividual = datapointIter.next();
			System.out.println(datapointIndividual.toString());
			datapoint = new Datapoint();
			datapoint.setResource(datapointIndividual);

			Resource subject = ontModel.getResource(datapointIndividual.toString());
			Vector<DimensionValue> dimValsList = SiaDimensionValueFactory.getDimensionValuesForResource(
					ontModel, subject);
			
			datapoint.setDimensionValues(dimValsList);
			datapointsList.add(datapoint);
			
		}
		return datapointsList;
	}

	public static void assertOrder(OntModel ontModel, List<Datapoint> datapoints) {
		
		Resource bnode = ontModel.createResource(AnonId.create());
		Resource siaOrderedSet = ontModel.createResource(OrderedSet.RESOURCE_URI);
		
		ontModel.add(bnode, RDF.type, siaOrderedSet);
		
		int numDatapoints = datapoints.size();

		Datapoint datapoint;
		Datapoint nextDatapoint;
		Property memberOfOrderedSetProperty;
		Property directlyFollowsProperty;
		Property orderedIndexProperty;
		
		for (int orderedIndex = 0; orderedIndex < numDatapoints; orderedIndex++)
		{
			// Assert <bnode, rdf:type, sia:OrderedSet>
			datapoint = datapoints.get(orderedIndex);
			datapoint.setOrderedIndex(orderedIndex);
			memberOfOrderedSetProperty = ontModel.getProperty(MemberOfOrderedSet.PROPERTY_URI);
			ontModel.add(datapoint.getResource(), memberOfOrderedSetProperty, bnode);
			
			if (orderedIndex < numDatapoints - 1)
			{
				// Assert <nextDatapoint, sia:directlyFollows, currentDatapoint>
				nextDatapoint = datapoints.get(orderedIndex + 1);
				directlyFollowsProperty = ontModel.getProperty(DirectlyFollows.PROPERTY_URI);
				ontModel.add(nextDatapoint.getResource(), directlyFollowsProperty, datapoint.getResource());
			}
			
			// Assert <currentDatapoint, sia:orderedIndex, orderedIndex>
			orderedIndexProperty = ontModel.getProperty(OrderedIndex.PROPERTY_URI);
			ontModel.addLiteral(datapoint.getResource(), orderedIndexProperty, orderedIndex);
			
		}
	}

}
