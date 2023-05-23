package org.qmul.eecs.c4dm.sia;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.qmul.eecs.c4dm.sia.model.Datapoint;
import org.qmul.eecs.c4dm.sia.model.DimensionValue;
import org.qmul.eecs.c4dm.sia.model.DirectlyFollows;
import org.qmul.eecs.c4dm.sia.model.FromDatapoint;
import org.qmul.eecs.c4dm.sia.model.MemberOfOrderedSet;
import org.qmul.eecs.c4dm.sia.model.OrderedIndex;
import org.qmul.eecs.c4dm.sia.model.OrderedSet;
import org.qmul.eecs.c4dm.sia.model.ToDatapoint;
import org.qmul.eecs.c4dm.sia.model.VectorTableElement;

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

public class SiaVectorTableElementFactory {

	public static List<VectorTableElement> create(OntModel ontModel, List<Datapoint> datapoints) {
		
		OntClass vteClass = ontModel.getOntClass(VectorTableElement.RESOURCE_URI);

		Resource vteResource = ontModel.getOntResource(vteClass);
		ExtendedIterator<Individual> vteIter = ontModel.listIndividuals(vteResource);
		
		Property dimValProperty = ontModel.getOntProperty(DimensionValue.PROPERTY_URI);
		Property dimensionProperty = ontModel.getOntProperty(DimensionValue.DIMENSION_URI);
		Property valueProperty = ontModel.getOntProperty(DimensionValue.VALUE_URI);
		Property fromDatapointProperty = ontModel.getOntProperty(FromDatapoint.PROPERTY_URI);
		Property toDatapointProperty = ontModel.getOntProperty(ToDatapoint.PROPERTY_URI);

		List<VectorTableElement> vteList = new ArrayList<VectorTableElement>();
		VectorTableElement vte;
		DimensionValue dimVal;
		
		Individual vteIndividual;
		StmtIterator dimValStmtIter;
		StmtIterator fromDPStmtIter;
		StmtIterator toDPStmtIter;
		StmtIterator dimValDimensionStmtIter;
		StmtIterator dimValValueStmtIter;
		Statement fromDatapointStmt;
		Statement toDatapointStmt;
		Resource dimValResource;
		Statement dimValDimensionStmt;
		Statement dimValValueStmt;
		Literal dimension;
		Literal value;

		// Find all rdf VectorTableElements
		while (vteIter.hasNext())
		{
			vteIndividual = vteIter.next();
			System.out.println(vteIndividual.toString());
			vte = new VectorTableElement();
			vte.setResource(vteIndividual);

			// Find the 'fromDatapoint' for this VectorTableElement
			fromDPStmtIter = ontModel.listStatements(vteIndividual, fromDatapointProperty, (RDFNode)null);			
			fromDatapointStmt = fromDPStmtIter.next();
			Datapoint fromDatapoint = findDatapoint(ontModel, datapoints, fromDatapointStmt.getResource());
			vte.setFromDatapoint(fromDatapoint);

			// Find the 'toDatapoint' for this VectorTableElement
			toDPStmtIter = ontModel.listStatements(vteIndividual, toDatapointProperty, (RDFNode)null);			
			toDatapointStmt = toDPStmtIter.next();
			Datapoint toDatapoint = findDatapoint(ontModel, datapoints, toDatapointStmt.getResource());
			vte.setToDatapoint(toDatapoint);

			// Find all dimensionValues for this VectorTableElement
			Vector<DimensionValue> dimensionValues = SiaDimensionValueFactory.getDimensionValuesForResource(ontModel, vteIndividual);
			vte.setDimensionValues(dimensionValues);
			
			vteList.add(vte);
			
		}
		return vteList;
	}

	public static Datapoint findDatapoint(OntModel ontModel, List<Datapoint> datapoints, Resource resource) {
		Iterator<Datapoint> datapointsIter = datapoints.iterator();
		Datapoint datapoint;
		while (datapointsIter.hasNext())
		{
			datapoint = datapointsIter.next();
			if (datapoint.getResource().getLocalName().equals(resource.getLocalName()))
				return datapoint;
		}
		datapoint = new Datapoint();
		datapoint.setResource(resource);
		Vector<DimensionValue> dimensionValues = SiaDimensionValueFactory.getDimensionValuesForResource(ontModel, resource);
		datapoint.setDimensionValues(dimensionValues);
		return datapoint;
	}

	public static void assertOrder(OntModel ontModel, List<VectorTableElement> vteList) {
		
		Resource bnode = ontModel.createResource(AnonId.create());
		Resource siaOrderedSet = ontModel.createResource(OrderedSet.RESOURCE_URI);
		
		ontModel.add(bnode, RDF.type, siaOrderedSet);
		
		int numVectorTableElements = vteList.size();

		VectorTableElement vte;
		for (int orderedIndex = 0; orderedIndex < numVectorTableElements; orderedIndex++)
		{
			// Assert <bnode, rdf:type, sia:OrderedSet>
			vte = vteList.get(orderedIndex);
			Property memberOfOrderedSetProperty = ontModel.getProperty(MemberOfOrderedSet.PROPERTY_URI);
			ontModel.add(vte.getResource(), memberOfOrderedSetProperty, bnode);
			
			if (orderedIndex < numVectorTableElements - 1)
			{
				// Assert <nextVectorTable, sia:directlyFollows, currentVectorTableElement>
				VectorTableElement nextVte = vteList.get(orderedIndex + 1);
				Property directlyFollowsProperty = ontModel.getProperty(DirectlyFollows.PROPERTY_URI);
				ontModel.add(nextVte.getResource(), directlyFollowsProperty, vte.getResource());
			}
			
			// Assert <currentVectorTableElement, sia:orderedIndex, orderedIndex>
			Property orderedIndexProperty = ontModel.getProperty(OrderedIndex.PROPERTY_URI);
			ontModel.addLiteral(vte.getResource(), orderedIndexProperty, orderedIndex);
			
		}
		
	}

}
