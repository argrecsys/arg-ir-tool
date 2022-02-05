package es.uam.irg.decidemadrid.db;

import es.uam.irg.db.MySQLDBConnector;
import es.uam.irg.decidemadrid.entities.*;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class DMDBManager {

    // Public constants
    public static final String DB_NAME = "decide.madrid_2019_09";
    public static final String DB_SERVER = "localhost";
    public static final String DB_USERNAME = "root";
    public static final String DB_USERPASSWORD = "";

    // Private connector object
    private MySQLDBConnector db;

    public DMDBManager() throws Exception {
        this(DB_SERVER, DB_NAME, DB_USERNAME, DB_USERPASSWORD);
    }

    public DMDBManager(String dbServer, String dbName, String dbUserName, String dbUserPassword) throws Exception {
        this.db = new MySQLDBConnector();
        this.db.connect(dbServer, dbName, dbUserName, dbUserPassword);
    }

    public DMDBManager(Map<String, Object> setup) throws Exception {
        String dbServer = setup.get("db_server").toString();
        String dbName = setup.get("db_name").toString();
        String dbUserName = setup.get("db_user_name").toString();
        String dbUserPassword = setup.get("db_user_pw").toString();
        this.db = new MySQLDBConnector();
        this.db.connect(dbServer, dbName, dbUserName, dbUserPassword);
    }

    @Override
    public void finalize() {
        this.db.disconnect();
    }

    public Map<Integer, DMComment> selectComments() throws Exception {
        Map<Integer, DMComment> comments = new HashMap<>();

        String query = "SELECT * FROM proposal_comments;";
        ResultSet rs = this.db.executeSelect(query);

        while (rs != null && rs.next()) {
            int id = rs.getInt("id");
            int parentId = rs.getInt("parentId");
            int proposalId = rs.getInt("proposalId");
            int userId = rs.getInt("userId");
            String date = rs.getDate("date").toString();
            String time = rs.getTime("time").toString();
            String text = rs.getString("text");
            int votes = rs.getInt("numVotes");
            int votesUp = rs.getInt("numPositiveVotes");
            int votesDown = rs.getInt("numNegativeVotes");

            DMComment comment = new DMComment(id, parentId, proposalId, userId, date, time, text, votes, votesUp, votesDown);
            comments.put(id, comment);
        }
        rs.close();

        return comments;
    }

    public Map<Integer, DMProposal> selectProposals() throws Exception {
        Map<Integer, DMProposal> proposals = new HashMap<>();

        String query = "SELECT * FROM proposals;";
        ResultSet rs = this.db.executeSelect(query);

        while (rs != null && rs.next()) {
            int id = rs.getInt("id");
            String code = rs.getString("code");
            String title = rs.getString("title");
            int userId = rs.getInt("userId");
            String date = rs.getString("date");
            String summary = rs.getString("summary");
            String text = rs.getString("text");
            int numComments = rs.getInt("numComments");
            int numSupports = rs.getInt("numSupports");
            String url = rs.getString("url");

            DMProposal proposal = new DMProposal(id, code, title, userId, date, summary, text, numComments, numSupports, url);
            proposals.put(id, proposal);
        }
        rs.close();

        return proposals;
    }

    public Map<Integer, DMProposalSummary> selectProposalsSummary() throws Exception {
        Map<Integer, DMProposalSummary> proposals = new HashMap<>();

        String query = "SELECT p.id, "
                + "       IFNULL(GROUP_CONCAT(DISTINCT pc.category), '') AS categories, "
                + "       IFNULL(GROUP_CONCAT(DISTINCT pd.district), '') AS districts, "
                + "       IFNULL(GROUP_CONCAT(DISTINCT pt.topic), '') AS topic "
                + "  FROM proposals AS p "
                + "  LEFT OUTER JOIN "
                + "       proposal_categories AS pc ON p.id = pc.id "
                + "  LEFT OUTER JOIN "
                + "       proposal_locations AS pd ON p.id = pd.id "
                + "  LEFT OUTER JOIN "
                + "       proposal_topics AS pt ON p.id = pt.id "
                + " GROUP BY p.id, p.date, p.title;";
        ResultSet rs = this.db.executeSelect(query);

        while (rs != null && rs.next()) {
            int id = rs.getInt("id");
            String categories = rs.getString("categories").toLowerCase();
            String districts = rs.getString("districts").toLowerCase();
            String topics = rs.getString("topic").toLowerCase();

            DMProposalSummary proposal = new DMProposalSummary(id, categories, districts, topics);
            proposals.put(id, proposal);
        }
        rs.close();

        return proposals;
    }

}
