/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package es.uam.irg.ir;

import es.uam.irg.decidemadrid.entities.DMProposal;
import es.uam.irg.decidemadrid.entities.DMProposalSummary;

/**
 *
 */
public class DocumentResult {

    private final String categories;
    private final String code;
    private final String date;
    private final String districts;
    private final int id;
    private final int numComments;
    private final int numSupports;
    private final String summary;
    private final String title;
    private final String topics;
    private final String url;

    /**
     *
     * @param prop
     * @param summary
     */
    public DocumentResult(DMProposal prop, DMProposalSummary summary) {
        this.id = prop.getId();
        this.code = prop.getCode();
        this.title = prop.getTitle().toUpperCase();
        this.url = prop.getUrl();
        this.date = prop.getDate();
        this.numComments = prop.getNumComments();
        this.numSupports = prop.getNumSupports();
        this.summary = prop.getSummary();
        this.categories = summary.getCategories();
        this.districts = summary.getDistricts();
        this.topics = summary.getTopics();
    }

    public String getCategories() {
        return categories;
    }

    public String getCode() {
        return code;
    }

    public String getDate() {
        return date;
    }

    public String getDistricts() {
        return districts;
    }

    public int getId() {
        return id;
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

    public String getTitle() {
        return title;
    }

    public String getTopics() {
        return topics;
    }

    public String getUrl() {
        return url;
    }

}
