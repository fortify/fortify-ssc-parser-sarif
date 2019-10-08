package com.fortify.ssc.parser.sarif.parser;

import java.util.List;
import java.util.Map;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fortify.plugin.api.VulnerabilityHandler;
import com.fortify.ssc.parser.sarif.parser.subentity.ResultParser;
import com.fortify.ssc.parser.sarif.parser.subentity.ResultParser.Result;
import com.fortify.ssc.parser.sarif.parser.subentity.RuleParser;
import com.fortify.ssc.parser.sarif.parser.subentity.RuleParser.Rule;

public class VulnerabilitiesParser extends AbstractParser {
	final VulnerabilityHandler vulnerabilityHandler;
	
	public VulnerabilitiesParser(VulnerabilityHandler vulnerabilityHandler) {
		this.vulnerabilityHandler = vulnerabilityHandler;
	}
	
	@Override
	protected void addHandlers(Map<String, Handler> pathToHandlerMap) {
		pathToHandlerMap.put("/runs", jp->parseArrayEntries(jp, ()->new RunParser()));
	}
    
    private class RunParser extends AbstractParser {
    	private final DB db;
    	private final Map<String, Rule> rules;
    	private List<Result> results;
    	@JsonProperty private Map<String,String> originalUriBaseIds;

        @SuppressWarnings("unchecked")
    	public RunParser() {
    		this.db = DBMaker.tempFileDB()
    				.closeOnJvmShutdown().fileDeleteAfterClose()
    				.fileMmapEnableIfSupported()
    				.make();
    		rules = db.hashMap("rules", Serializer.STRING, Serializer.ELSA).create();
    		results = (List<Result>) db.indexTreeList("results", Serializer.ELSA).create();
    	}
        
        @Override
        protected void addHandlers(Map<String, Handler> pathToHandlerMap) {
        	pathToHandlerMap.put("/resources/rules/*", jp->new RuleParser(rules, jp.getCurrentName()).parseObjectPropertiesAndFinish(jp, "/"));
        	pathToHandlerMap.put("/results", jp->parseArrayEntries(jp, ()->new ResultParser(results)));
        }
        
        @Override
        protected <T> T finish() {
        	System.err.println("originalUriBaseIds: "+originalUriBaseIds);
        	for ( Result result : results ) {
        		// TODO Pass rules map, originalUriBaseIds, ...
        		result.buildVulnerability(vulnerabilityHandler);
        	}
        	db.close();
        	return null;
        }
    	
    }
}
