package com.mnanti.rdftp;

import java.io.InputStream;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;

public class Dana {
	static final String file  = "rdf.ttl";

	public static void main (String args[]) {
		Model model = ModelFactory.createDefaultModel();

		InputStream in = FileManager.get().open( file );
		if (in == null) {
			throw new IllegalArgumentException( "File: " + file + " not found");
		}

		model.read(in, null, "Turtle");
		
		String prefix = "PREFIX dbo: <http://dbpedia.org/ontology/> \n" + 
				"PREFIX eus: <http://ontologycentral.com/2009/01/eurostat/ns#> \n" + 
				"PREFIX sdmx-measure: <http://purl.org/linked-data/sdmx/2009/measure#> \n" + 
				"PREFIX dcterms: <http://purl.org/dc/terms/> \n"+
				"PREFIX xsd:  <http://www.w3.org/2001/XMLSchema#> \n";			

		String query1 = prefix+ 
				"SELECT DISTINCT ?pays  (MAX(?nb) AS ?max) WHERE {\n" + 
				" ?x eus:geo \"WORLD\"; \n" + 				
				" dbo:Country ?pays ; \n" + 
				" sdmx-measure:obsValue ?nb \n" + 
				"} \n" + 
				"GROUP BY ?pays \n"+
				"ORDER BY DESC(?max) \n";		

		String query2 = prefix +
				"SELECT ?pays ?evolution WHERE {\n" + 
				"  ?x eus:geo \"WORLD\";\n" + 
				"  dcterms:date 2007;\n" + 
				"  dbo:Country ?pays;\n" + 		
				"  sdmx-measure:obsValue ?nb2007. \n"+	
				"  ?y eus:geo \"WORLD\";\n" + 
				"  dcterms:date 2018;\n" + 
				"  dbo:Country ?pays;\n" + 		
				"  sdmx-measure:obsValue ?nb2018 \n"+
				"  BIND (?nb2018 / ?nb2007 AS ?evolution) \n"+
				"}\n" + 
				"GROUP BY ?pays ?evolution \n"+
				"ORDER BY ASC(?pays) \n";

		Query query = QueryFactory.create(query1) ;
		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			ResultSet results = qexec.execSelect() ;
			ResultSetFormatter.out(System.out, results, query) ;
		}
		
		query = QueryFactory.create(query2) ;
		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			ResultSet results = qexec.execSelect() ;
			ResultSetFormatter.out(System.out, results, query) ;
		}
	}
}
