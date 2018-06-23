package fr.drochon.christian.taaroaa.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


/**
 * Classe des cours dispensés au sein du club
 */
public class Course {

    private String mSujetDuCours;
    private String mNomDuMoniteur;
    private LocalDateTime mDateTimeDuCours;
    private int mNiveauDuCours;
    List<User> listUsers;

    public Course() {
    }

    public Course(String sujetDuCours, String nomDuMoniteur, LocalDateTime dateTimeDuCours, int niveauDuCours) {
        mSujetDuCours = sujetDuCours;
        mNomDuMoniteur = nomDuMoniteur;
        mDateTimeDuCours = dateTimeDuCours;
        mNiveauDuCours = niveauDuCours;
        this.listUsers = new ArrayList<>();
    }


    // GETTERS
    public String getSujetDuCours() {
        return mSujetDuCours;
    }

    public String getNomDuMoniteur() {
        return mNomDuMoniteur;
    }

    public LocalDateTime getDateTimeDuCours() {
        return mDateTimeDuCours;
    }

    public int getNiveauDuCours() {
        return mNiveauDuCours;
    }

    public List<User> getListUsers() {
        return listUsers;
    }

    // SETTERS
    public void setSujetDuCours(String sujetDuCours) {
        mSujetDuCours = sujetDuCours;
    }

    public void setNomDuMoniteur(String nomDuMoniteur) {
        mNomDuMoniteur = nomDuMoniteur;
    }

    public void setDateTimeDuCours(LocalDateTime dateTimeDuCours) {
        mDateTimeDuCours = dateTimeDuCours;
    }

    public void setNiveauDuCours(int niveauDuCours) {
        mNiveauDuCours = niveauDuCours;
    }

    public void setListUsers(List<User> listUsers) {
        this.listUsers = listUsers;
    }
}
