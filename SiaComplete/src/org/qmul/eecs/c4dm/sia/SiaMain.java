// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public
// License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of
// proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package org.qmul.eecs.c4dm.sia;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.mindswap.pellet.jena.PelletReasonerFactory;
import org.qmul.eecs.c4dm.sia.model.Datapoint;
import org.qmul.eecs.c4dm.sia.model.VectorTableElement;

import com.clarkparsia.pellet.sparqldl.jena.SparqlDLExecutionFactory;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

/**
 * <p>
 * Title: SPARQLDLExample
 * </p>
 * <p>
 * Description: This program uses the Pellet SPARQL-DL engine
 * </p>
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 * 
 * @author Markus Stocker
 * @author Steven Hargreaves
 */
public class SiaMain {

	// The ontology loaded as dataset
	private static final String ontology = "file:src/rdf/siaDatapointOntology.n3";

	// The final output file
	private static final String finalModelFileName = "src/rdf/finalModel";

	// SPARQL queries
	private static final String[] selectQueries = new String[] {
		// A SPARQL-DL query
		"file:src/sparql/select_sia_results.sparql" };

	// CONSTRUCT queries
	private static final String[] constructQueries = new String[] {
		// A SPARQL-DL CONSTRUCT query
		"file:src/sparql/construct_vector_table_bnodes.sparql",
		"file:src/sparql/construct_vector_table_details.sparql" };

	// ASK queries
	private static final String[] askQueries = new String[] {
		// A SPARQL-DL ASK query
		"file:src/sparql/ask.sparql"
		};
	
	public static String SIA_NS_URI = null;

	public void run() {

		// First create a Jena ontology model backed by the Pellet reasoner
		// (note, the Pellet reasoner is required)
		OntModel ontModel = ModelFactory
				.createOntologyModel(PelletReasonerFactory.THE_SPEC);

		// Then read the data from the file into the ontology model
		ontModel.read(ontology, "N3");
		
		SIA_NS_URI = ontModel.getNsPrefixURI("sia");

		// Print out what we've got so far
		StmtIterator stmtIterator = ontModel.listStatements();
		printStmts(stmtIterator);
		
		// Create custom sia Datapoint objects
		List<Datapoint> datapoints = SiaDatapointFactory.create(ontModel);
		Collections.sort(datapoints);
		
		// Add datapoint order info to model
		SiaDatapointFactory.assertOrder(ontModel, datapoints);
		
		for (Datapoint datapoint : datapoints)
		{
			System.out.println(datapoint.getResource().getLocalName());
		}

		// Run all the CONSTRUCT queries
		for (int i = 0; i < constructQueries.length; i++) {
			String constructQuery = constructQueries[i];

			Model newModel = executeConstructQuery(constructQuery, ontModel);

			// Add new triples to the current model
			ontModel.add(newModel);

			// Print out what we've got now
			System.out.println("------------------");
			stmtIterator = ontModel.listStatements();
			printStmts(stmtIterator);
		}

		// Create custom sia VectorTableElement objects
		List<VectorTableElement> vteList = SiaVectorTableElementFactory.create(ontModel, datapoints);
		Iterator<VectorTableElement> vteIter = vteList.iterator();
		System.out.println("Unsorted");
		System.out.println("----------");
		while (vteIter.hasNext())
		{
			VectorTableElement vte = vteIter.next();
			System.out.println("from: " + vte.getFromDatapoint().getResource().getLocalName() + " to: " + vte.getToDatapoint().getResource().getLocalName());			
		}
		Collections.sort(vteList);
		vteIter = vteList.iterator();
		System.out.println("Now Sorted");
		System.out.println("----------");
		while (vteIter.hasNext())
		{
			VectorTableElement vte = vteIter.next();
			System.out.println("from: " + vte.getFromDatapoint().getResource().getLocalName() + " to: " + vte.getToDatapoint().getResource().getLocalName());			
		}

		// Add vector table element order info to model
		SiaVectorTableElementFactory.assertOrder(ontModel, vteList);

		// Run all the SELECT queries
		for (int i = 0; i < selectQueries.length; i++) {

			String selectQuery = selectQueries[i];
			queryTheModel(selectQuery, ontModel);

		}

		// Run all the ASK queries
		for (int i = 0; i < askQueries.length; i++) {

			String askQuery = askQueries[i];
			askTheModel(askQuery, ontModel);

		}

		// Write the model to a file
		File outFileRdf = new File(finalModelFileName + ".rdf");
		File outFileN3 = new File(finalModelFileName + ".n3");
		FileOutputStream outFileOutputStreamRdf;
		FileOutputStream outFileOutputStreamN3;
		System.out.println("Model written to files: "
				+ outFileRdf.getAbsolutePath() + " and " + outFileN3.getAbsolutePath());
		try {
			outFileOutputStreamRdf = new FileOutputStream(outFileRdf);
			outFileOutputStreamN3 = new FileOutputStream(outFileN3);
			ontModel.writeAll(outFileOutputStreamRdf, "RDF/XML", null);
			ontModel.writeAll(outFileOutputStreamN3, "N3", null);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("Unable to write to one or both of files: "
					+ outFileRdf.getAbsolutePath() + " and " + outFileN3.getAbsolutePath());
			e.printStackTrace();
		}
	}

	private Model executeConstructQuery(String query, Model m) {

		// Now read the query file into a query object
		Query q = QueryFactory.read(query, Syntax.syntaxARQ); // only required
																// if using
																// (e.g.) LET in
																// SPARQL

		QueryExecution qe = QueryExecutionFactory.create(q, m);

		// We want to execute a CONSTRUCT query, do it, and return the new
		// triples
		Model newModel = qe.execConstruct();

		// Print the query for better understanding
		System.out.println(q.toString());

		// Print the new triples
		StmtIterator iter = newModel.listStatements();
		printStmts(iter);

		return newModel;
	}

	private void printStmts(StmtIterator iter) {
		Statement statement;

		while (iter.hasNext()) {
			statement = iter.nextStatement();
			System.out.println(" | <" + statement.getSubject() + "> | <"
					+ statement.getPredicate() + "> | <"
					+ statement.getObject() + "> | ");
		}

		// And an empty line to make it pretty
		System.out.println();
	}

	private void queryTheModel(String query, Model m) {

		// Now read the query file into a query object
		Query q = QueryFactory.read(query, Syntax.syntaxARQ); // only required
																// if using
																// (e.g.) LET in
																// SPARQL

		// Create a SPARQL-DL query execution for the given query and
		// ontology model
		QueryExecution qe = SparqlDLExecutionFactory.create(q, m);

		// We want to execute a SELECT query, do it, and return the result set
		ResultSet rs = qe.execSelect();

		// Print the query for better understanding
		System.out.println(q.toString());

		// There are different things we can do with the result set, for
		// instance iterate over it and process the query solutions or, what we
		// do here, just print out the results
		ResultSetFormatter.out(rs);

		// And an empty line to make it pretty
		System.out.println();
	}

	private void askTheModel(String query, Model m) {

		// Now read the query file into a query object
		Query q = QueryFactory.read(query);

		// Create a SPARQL-DL query execution for the given query and
		// ontology model
		QueryExecution qe = SparqlDLExecutionFactory.create(q, m);

		// We want to execute a SELECT query, do it, and return the result set
		boolean result = qe.execAsk();

		// Print the query for better understanding
		System.out.println(q.toString());

		// Print the result
		System.out.println("Result: " + result);

		// And an empty line to make it pretty
		System.out.println();
	}

	public static void main(String[] args) {
		SiaMain app = new SiaMain();
		app.run();
	}

}
