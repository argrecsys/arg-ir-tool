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
import es.uam.irg.decidemadrid.entities.DMProposal;
import es.uam.irg.utils.FunctionUtils;
import java.io.IOException;
import java.util.HashMap;
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
    private Map<Integer, DMProposal> proposals;

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
        String proposalId;
        DMProposal proposal;

        try {
            try ( IndexWriter w = new IndexWriter(index, config)) {
                // 1. Analize argumentative proposals
                System.out.println("Proposals annotation");
                for (Map.Entry<Integer, DMProposal> entry : proposals.entrySet()) {
                    proposalId = entry.getKey().toString();
                    proposal = entry.getValue();

                    addDoc(w, proposalId, proposal.getTitle(), proposal.getSummary());
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(InfoRetriever.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     *
     * @return
     */
    public String getWrongQueryMessage() {
        String errorMsg = "<div><h2>You must enter a valid query.</h2></div>";
        return errorMsg;
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
                String dbServer = msqlSetup.get("db_server").toString();
                String dbName = msqlSetup.get("db_name").toString();
                String dbUserName = msqlSetup.get("db_user_name").toString();
                String dbUserPwd = msqlSetup.get("db_user_pw").toString();
                
                dbManager = new DMDBManager(dbServer, dbName, dbUserName, dbUserPwd);

            } else {
                dbManager = new DMDBManager();
            }

            // Get proposals with linkers
            proposals = dbManager.selectProposals();

            // Get proposal comments with linkers
            proposalComments = dbManager.selectComments();

        } catch (Exception ex) {
            Logger.getLogger(InfoRetriever.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Show results
        if (VERBOSE) {
            System.out.println(" - Number of proposals: " + proposals.size());
            System.out.println(" - Number of comments: " + proposalComments.size());
        }
    }

    /**
     *
     *
     * @param querystr
     * @param hitsPerPage
     * @param wArgs
     * @return
     */
    public String queryData(String querystr, int hitsPerPage, boolean wArgs) {
        String result = "";

        try {
            // The "title" arg specifies the default field to use when no field is explicitly specified in the query
            Query q = new QueryParser("title", analyzer).parse(querystr);

            // 3. search
            IndexReader reader = DirectoryReader.open(index);
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs docs = searcher.search(q, hitsPerPage);
            ScoreDoc[] hits = docs.scoreDocs;

            // 4. display results
            System.out.println(">> Found " + hits.length + " hits:");
            for (int i = 0; i < hits.length; ++i) {
                int docId = hits[i].doc;
                Document d = searcher.doc(docId);
                System.out.println((i + 1) + ". " + d.get("id") + "\t" + d.get("title") + "\t" + d.get("summary"));
            }

            // Reader can only be closed when there is no need to access the documents any more.
            reader.close();

        } catch (ParseException | IOException ex) {
            Logger.getLogger(InfoRetriever.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }

    /**
     *
     *
     * @param w
     * @param proposalId
     * @param title
     * @param summary
     * @throws IOException
     */
    private void addDoc(IndexWriter w, String proposalId, String title, String summary) throws IOException {
        Document doc = new Document();
        doc.add(new StringField("id", proposalId, Field.Store.YES));
        doc.add(new TextField("title", title, Field.Store.YES));
        doc.add(new TextField("summary", summary, Field.Store.YES));
        w.addDocument(doc);
    }

}
