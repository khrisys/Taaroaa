package fr.drochon.christian.taaroaa.controller;

class QueryDocumentSnapshot {

    private String uid;
    private String mNom;
    private String mPrenom;
    private String mLicence;
    private String mEmail;
    private String mNiveauPlongeur;
    private String mFonction;

    public QueryDocumentSnapshot() {
    }

    public QueryDocumentSnapshot(String uid, String nom, String prenom, String licence, String email, String niveauPlongeur, String fonction) {
        this.uid = uid;
        mNom = nom;
        mPrenom = prenom;
        mLicence = licence;
        mEmail = email;
        mNiveauPlongeur = niveauPlongeur;
        mFonction = fonction;
    }

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
