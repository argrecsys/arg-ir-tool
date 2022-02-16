/**
 * Copyright 2022
 * Andrés Segura-Tinoco
 * Information Retrieval Group at Universidad Autonoma de Madrid
 *
 * This is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * the current software. If not, see <http://www.gnu.org/licenses/>.
 */
package es.uam.irg.ir;

import es.uam.irg.decidemadrid.entities.DMProposal;
import es.uam.irg.decidemadrid.entities.DMProposalSummary;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;

/**
 * Argument-enhanced information retrieval engine.
 */
public class InfoRetriever {

    // The same analyzer should be used for indexing and searching
    private final StandardAnalyzer analyzer;
    private final Directory index;

    /**
     * Constructor
     */
    public InfoRetriever() {
        this.analyzer = new StandardAnalyzer();
        this.index = new ByteBuffersDirectory();
    }

    /**
     *
     *
     * @param proposals
     * @param proposalSummaries
     */
    public void createIndex(Map<Integer, DMProposal> proposals, Map<Integer, DMProposalSummary> proposalSummaries) {
        DMProposal proposal;
        int proposalId;
        String code;
        String title;
        String summary;
        String categories;
        String districts;
        String topics;

        try {
            // Storing proposals
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            try ( IndexWriter w = new IndexWriter(index, config)) {

                for (Map.Entry<Integer, DMProposal> entry : proposals.entrySet()) {
                    proposalId = entry.getKey();
                    proposal = entry.getValue();
                    code = proposal.getCode();
                    title = proposal.getTitle();
                    summary = proposal.getSummary();
                    categories = proposalSummaries.get(proposalId).getCategories();
                    districts = proposalSummaries.get(proposalId).getDistricts();
                    topics = proposalSummaries.get(proposalId).getTopics();

                    addDocToIndex(w, proposalId, code, title, summary, categories, districts, topics);
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(InfoRetriever.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Searches (within the index) for records that fulfill a certain
     * information need (query).
     *
     * @param querystr
     * @return
     */
    public List<Integer> queryData(String querystr) {
        List<Integer> docList = new ArrayList<>();

        try {
            // The "title" arg specifies the default field to use when no field is explicitly specified in the query
            Query q = new QueryParser("title", analyzer).parse(querystr);

            // Search within the index
            try ( IndexReader reader = DirectoryReader.open(index)) {
                IndexSearcher searcher = new IndexSearcher(reader);
                TopDocs docs = searcher.search(q, Integer.MAX_VALUE);
                ScoreDoc[] hits = docs.scoreDocs;

                // Store results
                for (int i = 0; i < hits.length; ++i) {
                    int docId = hits[i].doc;
                    Document doc = searcher.doc(docId);
                    int proposalId = Integer.parseInt(doc.get("id"));
                    docList.add(proposalId);
                }
            }

        } catch (ParseException | IOException ex) {
            Logger.getLogger(InfoRetriever.class.getName()).log(Level.SEVERE, null, ex);
        }

        return docList;
    }

    /**
     *
     *
     * @param iw
     * @param proposalId
     * @param code
     * @param title
     * @param summary
     * @param categories
     * @param districts
     * @param topics
     * @throws IOException
     */
    private void addDocToIndex(IndexWriter iw, int proposalId, String code, String title, String summary, String categories, String districts, String topics) throws IOException {
        Document doc = new Document();
        doc.add(new StringField("id", "" + proposalId, Field.Store.YES));
        doc.add(new TextField("code", "" + code, Field.Store.YES));
        doc.add(new TextField("title", title, Field.Store.YES));
        doc.add(new TextField("summary", summary, Field.Store.YES));
        doc.add(new TextField("categories", categories, Field.Store.YES));
        doc.add(new TextField("districts", districts, Field.Store.YES));
        doc.add(new TextField("topics", topics, Field.Store.YES));
        iw.addDocument(doc);
    }

}
