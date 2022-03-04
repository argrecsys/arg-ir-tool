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
import es.uam.irg.utils.FunctionUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;

/**
 * Argument-enhanced information retrieval engine.
 */
public class InfoRetriever {

    public static final double LAMBDA = 0.60;

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
     * Document indexing module (4). Creates a full-text index (with Apache
     * Lucene) on the documents.
     *
     * @param proposals
     * @param proposalSummaries
     */
    public void createDocumentIndex(Map<Integer, DMProposal> proposals, Map<Integer, DMProposalSummary> proposalSummaries) {
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
     * Information retrieval module (5 and 6).Searches the full-text index for
     * documents that meet the keyword-based query and ranks the results
     * according to a specified criterion.
     *
     * @param querystr
     * @param similarity
     * @return
     */
    public List<Integer> retrieveInformation(String querystr, String similarity) {
        return retrieveInformation(querystr, similarity, null);
    }

    /**
     * Information retrieval module (5 and 6).Searches the full-text index for
     * documents that meet the keyword-based query and ranks the results
     * according to a specified criterion.
     *
     * @param querystr
     * @param similarity
     * @param scores
     * @return
     */
    public List<Integer> retrieveInformation(String querystr, String similarity, Map<Integer, Double> scores) {
        List<Integer> docList = new ArrayList<>();

        try {
            // The "title" arg specifies the default field to use when no field is explicitly specified in the query
            Query q = new QueryParser("title", analyzer).parse(querystr);

            // Search within the index
            try ( IndexReader reader = DirectoryReader.open(index)) {
                IndexSearcher searcher = new IndexSearcher(reader);
                searcher.setSimilarity(getSimilarity(similarity));
                TopDocs docs = searcher.search(q, Integer.MAX_VALUE);
                ScoreDoc[] hits = docs.scoreDocs;

                if (hits.length > 0) {
                    Map<Integer, Double> result = new HashMap<>();

                    // Store results
                    for (int i = 0; i < hits.length; ++i) {
                        int docId = hits[i].doc;
                        Document doc = searcher.doc(docId);
                        int proposalId = Integer.parseInt(doc.get("id"));

                        // Calculate score
                        double proposalScore = hits[i].score;
                        if (scores != null) {
                            double argumentativeScore = (scores.containsKey(proposalId) ? scores.get(proposalId) : 0.0);
                            proposalScore = LAMBDA * proposalScore + (1 - LAMBDA) * argumentativeScore;
                        }

                        result.put(proposalId, proposalScore);
                    }

                    // Final sorting
                    if (scores != null) {
                        docList.addAll(FunctionUtils.sortMapByDblValue(result).keySet());
                    } else {
                        docList.addAll(result.keySet());
                    }
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

    /**
     *
     * @param similarity
     * @return
     */
    private Similarity getSimilarity(String similarity) {
        Similarity metric = null;
        similarity = similarity.toUpperCase();

        if (similarity.equals("BM25")) {
            float k1 = 1.2f;
            float b = 0.75f;
            metric = new BM25Similarity(k1, b);

        } else if (similarity.equals("COSINE")) {
            metric = new ClassicSimilarity();

        } else if (similarity.equals("DIRICHLET")) {
            metric = new LMDirichletSimilarity();
        }

        return metric;
    }

}
