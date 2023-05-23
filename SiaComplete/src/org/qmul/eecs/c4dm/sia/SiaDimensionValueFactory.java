package org.qmul.eecs.c4dm.sia;

import java.util.Vector;

import org.qmul.eecs.c4dm.sia.model.DimensionValue;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class SiaDimensionValueFactory {

	public static Vector<DimensionValue> getDimensionValuesForResource(
		OntModel ontModel, Resource subject) {
		DimensionValue dimVal;
		StmtIterator dimValStmtIter;
		Property dimValProperty = ontModel.getOntProperty(DimensionValue.PROPERTY_URI);
		Property dimensionProperty = ontModel.getOntProperty(DimensionValue.DIMENSION_URI);
		Property valueProperty = ontModel.getOntProperty(DimensionValue.VALUE_URI);
		dimValStmtIter = ontModel.listStatements(subject, dimValProperty, (RDFNode)null);
		
		Vector<DimensionValue> dimValsList = new Vector<DimensionValue>();
		StmtIterator dimValDimensionStmtIter;
		StmtIterator dimValValueStmtIter;

		while (dimValStmtIter.hasNext())
		{
			Statement dimValStmt = dimValStmtIter.next();
			System.out.println(dimValStmt.getSubject().toString() + " " + dimValStmt.getPredicate().toString() + " " + dimValStmt.getObject().toString());
			
			Resource dimValResource = dimValStmt.getResource();
			
			dimValDimensionStmtIter = ontModel.listStatements(dimValResource, dimensionProperty, (RDFNode)null);
			dimValValueStmtIter = ontModel.listStatements(dimValResource, valueProperty, (RDFNode)null);
			dimVal = new DimensionValue();

			Statement dimValDimensionStmt = dimValDimensionStmtIter.next();
			Statement dimValValueStmt = dimValValueStmtIter.next();
			Literal dimension = dimValDimensionStmt.getObject().asLiteral();
			dimVal.setDimension(dimension.getInt());

			Literal value = dimValValueStmt.getObject().asLiteral();

			dimVal.setValue(value.getDouble());
			
			dimValsList.add(dimVal);				
			System.out.println("dim: " + dimension.getInt() + " val: " + value.getDouble());
			
		}
		return dimValsList;
	}

}
