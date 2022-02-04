package es.uam.irg.decidemadrid.entities;

public class DMProposal {

    public final static String HOME_PAGE = "https://decide.madrid.es";

    private String code;
    private String date;
    private int day;
    private int id;
    private int month;
    private int numComments;
    private int numSupports;
    private String summary;
    private String text;
    private String title;
    private String url;
    private int userId;
    private int year;

    public DMProposal(int id, String code, String title, int userId, String date, String summary, String text, int numComments, int numSupports, String url) {
        this.id = id;
        this.code = code;
        this.title = title;
        this.userId = userId;
        this.date = date;
        String tokens[] = date.split("-");
        this.day = Integer.valueOf(tokens[2]);
        this.month = Integer.valueOf(tokens[1]);
        this.year = Integer.valueOf(tokens[0]);
        this.summary = summary;
        this.text = text;
        this.numComments = numComments;
        this.numSupports = numSupports;
        this.url = HOME_PAGE + url;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DMProposal other = (DMProposal) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    public String getCode() {
        return code;
    }

    public String getDate() {
        return date;
    }

    public int getDay() {
        return day;
    }

    public int getId() {
        return id;
    }

    public int getMonth() {
        return month;
    }

    public int getNumComments() {
        return numComments;
    }

    public int getNumSupports() {
        return numSupports;
    }

    public String getSummary() {
        return summary;
    }

    public String getText() {
        return text;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public int getUserId() {
        return userId;
    }

    public int getYear() {
        return year;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + this.id;
        return hash;
    }

    @Override
    public String toString() {
        return "DMProposal{" + "id=" + id + ", title=" + title + ", userId=" + userId + ", date=" + date + ", summary=" + summary + ", text=" + text + ", numComments=" + numComments + ", numSupports=" + numSupports + '}';
    }

}
