package fr.drochon.christian.taaroaa.model;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Classe des cours dispensés au sein du club
 */
public class Course {

    private String mUid;
    private String mTypeCours;
    private String mSujetDuCours;
    private String mNiveauDuCours;
    private String mNomDuMoniteur;
    private Date  mDateDuCours;
    private Date mTimeDuCours;

    List<User> listUsers;

    public Course(){}

    public Course(String uid) {
        mUid = uid;
    }

    public Course(String uid, String typeCours, String sujetDuCours, String niveauDuCours, String nomDuMoniteur, Date dateDuCours) {
        mUid = uid;
        mTypeCours = typeCours;
        mSujetDuCours = sujetDuCours;
        mNiveauDuCours = niveauDuCours;
        mNomDuMoniteur = nomDuMoniteur;
        mDateDuCours = dateDuCours;
        //this.listUsers = listUsers;
    }

    public Course(String uid, String typeCours, String sujetDuCours, String niveau, String nomDuMoniteur, Date dateDuCours, Date timeDuCours) {
        mUid = uid;
        mTypeCours = typeCours;
        mSujetDuCours = sujetDuCours;
        mNiveauDuCours = niveau;
        mNomDuMoniteur = nomDuMoniteur;
        mDateDuCours = dateDuCours;
        mTimeDuCours = timeDuCours;
        //this.listUsers = new ArrayList<>();
    }

    // GETTERS & SETTERS

    public String getUid() {
        return mUid;
    }

    public void setUid(String uid) {
        mUid = uid;
    }

    public String getTypeCours() {
        return mTypeCours;
    }

    public void setTypeCours(String typeCours) {
        mTypeCours = typeCours;
    }

    public String getSujetDuCours() {
        return mSujetDuCours;
    }

    public void setSujetDuCours(String sujetDuCours) {
        mSujetDuCours = sujetDuCours;
    }

    public String getNiveauDuCours() {
        return mNiveauDuCours;
    }

    public void setNiveauDuCours(String niveauDuCours) {
        mNiveauDuCours = niveauDuCours;
    }

    public String getNomDuMoniteur() {
        return mNomDuMoniteur;
    }

    public void setNomDuMoniteur(String nomDuMoniteur) {
        mNomDuMoniteur = nomDuMoniteur;
    }

    public Date getDateDuCours() {
        return mDateDuCours;
    }

    public void setDateDuCours(Date dateDuCours) {
        mDateDuCours = dateDuCours;
    }

    public Date getTimeDuCours() {
        return mTimeDuCours;
    }

    public void setTimeDuCours(Date timeDuCours) {
        mTimeDuCours = timeDuCours;
    }

    public List<User> getListUsers() {
        return listUsers;
    }

    public void setListUsers(List<User> listUsers) {
        this.listUsers = listUsers;
    }
}
