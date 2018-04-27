package fr.drochon.christian.taaroaa.model;

import java.io.Serializable;

/**
 * Classe represantant les adherents du club
 */
public class User implements Serializable{

    private String uid;
    private String mNom;
    private String mPrenom;
    private String mLicence;
    private String mEmail;
    private String mNiveauPlongeur;
    private String mFonction;

    public User() {
    }

    public User(String uid) {
        this.uid = uid;
    }

    public User(String nom, String prenom) {
        mNom = nom;
        mPrenom = prenom;
    }

    public User(String uid, String nom, String email) {
        this.uid = uid;
        mNom = nom;
        mEmail = email;
    }

    public User(String uid, String nom, String prenom, String licence, String email, String niveauPlongeur, String fonction) {
        this.uid = uid;
        mNom = nom;
        mPrenom = prenom;
        mLicence = licence;
        mEmail = email;
        mNiveauPlongeur = niveauPlongeur;
        mFonction = fonction;
    }

    // GETTERS & SETTERS

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNom() {
        return mNom;
    }

    public void setNom(String nom) {
        mNom = nom;
    }

    public String getPrenom() {
        return mPrenom;
    }

    public void setPrenom(String prenom) {
        mPrenom = prenom;
    }

    public String getLicence() {
        return mLicence;
    }

    public void setLicence(String licence) {
        mLicence = licence;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public String getNiveauPlongeur() {
        return mNiveauPlongeur;
    }

    public void setNiveauPlongeur(String niveauPlongeur) {
        mNiveauPlongeur = niveauPlongeur;
    }

    public String getFonction() {
        return mFonction;
    }

    public void setFonction(String fonction) {
        mFonction = fonction;
    }
}
