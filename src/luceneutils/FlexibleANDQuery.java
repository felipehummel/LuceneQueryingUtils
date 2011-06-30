package luceneutils;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;

public class FlexibleANDQuery extends Query {
	private static final long serialVersionUID = 8471636503282586897L;
	private final LinkedList<String> splitted_query;
	private final LinkedList<String> deleted_terms;
	private final Analyzer analyzer;
	private boolean first_attempt = true;
	private final String main_field;
	
	public FlexibleANDQuery(String _query, String main_field, Analyzer _analyzer, IndexReader reader) throws IOException {
		this(_query, _analyzer, main_field, new TermFrequencyComparator(reader, main_field));
	}
	
	public FlexibleANDQuery(String _query, Analyzer _analyzer, String main_field, Comparator<String> term_comparator) throws IOException {
		this.main_field = main_field;
		analyzer = _analyzer;
		deleted_terms = new LinkedList<String>(); 
		splitted_query = parseKeywords(analyzer, _query);
		Collections.sort(splitted_query, term_comparator);
	}
	
	public static LinkedList<String> parseKeywords(Analyzer analyzer, String keywords) {
        LinkedList<String> result = new LinkedList<String>();
        TokenStream stream  = analyzer.tokenStream("", new StringReader(keywords));
        try {
        	char[] x;
            while(stream.incrementToken()) {
            	CharTermAttribute att = stream.getAttribute(CharTermAttribute.class);
            	x = att.buffer();
            	result.add(new String(x,0, att.length()));
            }
        }
        catch(IOException e) {} //impossible since using StringReader
        return result;
    }  
	
	public final int getNumberOfMustOccurTerms() {
		return splitted_query.size();
	}
	
	public void removeLessImportantTerm() {
		deleted_terms.add(splitted_query.removeFirst()); //weakest term goes to deleted_terms
	}
	
	@Override
	public String toString(String field) {
		return rewriteToLuceneQuery().toString(field);
	}
	
	protected Query rewriteToLuceneQuery() {
		final BooleanQuery final_query = new BooleanQuery();
		for (String query_term : splitted_query) 
			final_query.add(new TermQuery(new Term(main_field, query_term)), Occur.MUST);
		for (String queryTerm : deleted_terms) 
			final_query.add(new TermQuery(new Term(main_field, queryTerm)), Occur.SHOULD);
		return final_query;
	}
	@Override
	public Query rewrite(IndexReader reader) throws IOException {
		if (!first_attempt) 
			removeLessImportantTerm();
		else 
			first_attempt = false;
		return rewriteToLuceneQuery();
	}
	
	private static class TermFrequencyComparator implements Comparator<String> {
		IndexReader reader;
		String field;
		
		public TermFrequencyComparator(IndexReader _reader, String _field) {
			reader = _reader;
			field = _field;
		}

		@Override
		public int compare(String arg0, String arg1) {
			try {
				return reader.docFreq(new Term(field, arg0)) - reader.docFreq(new Term(field, arg1));
			} catch (IOException e) {
				e.printStackTrace();
			}
			return -1;
		}
	}
}