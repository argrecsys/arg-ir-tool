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

import es.uam.irg.decidemadrid.db.DMDBManager;
import es.uam.irg.decidemadrid.entities.DMComment;
import es.uam.irg.decidemadrid.entities.DMCommentTree;
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
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;

/**
 * Argument-enhanced information retrieval engine.
 */
public class InfoRetriever {

    // Class constants
    private static final boolean VERBOSE = true;

    // The same analyzer should be used for indexing and searching
    private final StandardAnalyzer analyzer;
    private final Directory index;

    // Class members
    private final Map<String, Object> mdbSetup;
    private final Map<String, Object> msqlSetup;
    private Map<Integer, DMComment> proposalComments;
    private Map<Integer, DMProposalSummary> proposalSummaries;
    private Map<Integer, DMProposal> proposals;
    private Map<Integer, List<DMCommentTree>> proposalCommentTrees;

    /**
     * Constructor
     */
    public InfoRetriever() {
        this.mdbSetup = FunctionUtils.getDatabaseConfiguration(FunctionUtils.MONGO_DB);
        this.msqlSetup = FunctionUtils.getDatabaseConfiguration(FunctionUtils.MYSQL_DB);
        this.analyzer = new StandardAnalyzer();
        this.index = new ByteBuffersDirectory();
    }

    /**
     *
     */
    public void createIndex() {
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        DMProposal proposal;
        int proposalId;
        String title;
        String summary;
        String categories;
        String districts;
        String topics;

        try {
            // Storing proposals
            try ( IndexWriter w = new IndexWriter(index, config)) {
                System.out.println("Proposals annotation");
                for (Map.Entry<Integer, DMProposal> entry : proposals.entrySet()) {
                    proposalId = entry.getKey();
                    proposal = entry.getValue();
                    title = proposal.getTitle();
                    summary = proposal.getSummary();
                    categories = proposalSummaries.get(proposalId).getCategories();
                    districts = proposalSummaries.get(proposalId).getDistricts();
                    topics = proposalSummaries.get(proposalId).getTopics();

                    addDocToIndex(w, proposalId, title, summary, categories, districts, topics);
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(InfoRetriever.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     *
     */
    public void loadData() {
        proposals = new HashMap<>();
        proposalComments = new HashMap<>();

        // Connecting to databse
        try {
            DMDBManager dbManager = null;
            if (msqlSetup != null && msqlSetup.size() == 4) {
                dbManager = new DMDBManager(msqlSetup);
            } else {
                dbManager = new DMDBManager();
            }

            // Get proposals
            proposals = dbManager.selectProposals();

            // Get proposal summaries
            proposalSummaries = dbManager.selectProposalsSummary();

            // Get proposal comments
            proposalComments = dbManager.selectComments();
            
            // Get comments trees
            proposalCommentTrees = dbManager.selectCommentTrees();

        } catch (Exception ex) {
            Logger.getLogger(InfoRetriever.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Show results
        if (VERBOSE) {
            System.out.println(" - Number of proposals: " + proposals.size());
            System.out.println(" - Number of proposal summaries: " + proposals.size());
            System.out.println(" - Number of comments: " + proposalComments.size());
        }
    }

    /**
     *
     *
     * @param querystr
     * @param hitsPerPage
     * @param reRankBy
     * @return
     */
    public List<DocumentResult> queryData(String querystr, int hitsPerPage, String reRankBy) {
        List<DocumentResult> docList = new ArrayList<>();

        try {
            // The "title" arg specifies the default field to use when no field is explicitly specified in the query
            Query q = new QueryParser("title", analyzer).parse(querystr);

            // Search
            IndexReader reader = DirectoryReader.open(index);
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs docs = searcher.search(q, hitsPerPage);
            ScoreDoc[] hits = docs.scoreDocs;

            // Store results
            for (int i = 0; i < hits.length; ++i) {
                int docId = hits[i].doc;
                Document doc = searcher.doc(docId);
                int proposalId = Integer.parseInt(doc.get("id"));
                DocumentResult docResult = new DocumentResult(proposals.get(proposalId), proposalSummaries.get(proposalId));
                docList.add(docResult);
            }
            System.out.println(">> Found " + docList.size() + " hits:");

            // Reader can only be closed when there is no need to access the documents any more.
            reader.close();

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
     * @param title
     * @param summary
     * @param categories
     * @param districts
     * @param topics
     * @throws IOException
     */
    private void addDocToIndex(IndexWriter iw, int proposalId, String title, String summary, String categories, String districts, String topics) throws IOException {
        Document doc = new Document();
        doc.add(new StringField("id", "" + proposalId, Field.Store.YES));
        doc.add(new TextField("title", title, Field.Store.YES));
        doc.add(new TextField("summary", summary, Field.Store.YES));
        doc.add(new TextField("categories", categories, Field.Store.YES));
        doc.add(new TextField("districts", districts, Field.Store.YES));
        doc.add(new TextField("topics", topics, Field.Store.YES));
        iw.addDocument(doc);
    }

}
