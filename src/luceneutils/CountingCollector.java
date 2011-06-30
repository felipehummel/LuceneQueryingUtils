package luceneutils;


import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;

/**
 * Collector that just counts the number of results. 
 */
public class CountingCollector extends Collector {
	private int count = 0;
	public final int getCount() {
		return count;
	}
	@Override
	public final void collect(final int doc) throws IOException {
	  count++;
	}
	@Override
	public final void setNextReader(final IndexReader reader, final int docBase) throws IOException {	}
	@Override
	public final void setScorer(final Scorer scorer) throws IOException {}
	@Override
	public boolean acceptsDocsOutOfOrder() {	return false;  }
}