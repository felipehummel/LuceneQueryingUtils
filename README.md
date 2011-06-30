FlexibleANDQuery
--------

A Lucene `Query` implementation that behaves like a typical AND query. But you may choose to re-run it if there wasn't enough results.

At each re-run it removes the 'least important' term of the query, by basically changing it from MUST to SHOULD occur. The notion of 'least important' is customizable through a given `Comparator<String>`. 

If no comparator is given, the doc frequency of a term is used (more docs, more 'importance'). 

With `FlexibleANDQuery` you guarantee that documents with all query terms appear in the top of the ranking before other documents. But you can still return documents that do not have all query terms. Normally, the vector space model (TF-IDF) takes care of that, but not always.

Typical usage:

    final FlexibleANDQuery query = new FlexibleANDQuery(keyword_query, "content", analyzer, current_reader);
    boolean tryAgain = true;
    TopDocs top_docs;
    while (tryAgain) {
	        top_docs = searcher.search(query, 20);
	        if (top_docs.scoreDocs.length > 0 || nquery.getNumberOfMustOccurTerms() < 2) 
        		tryAgain = false;
    } 
    // ...
    // use top_docs
    
    
CountingCollector
-----
A simple counting collector. It does not generate any kind of document ranking.
