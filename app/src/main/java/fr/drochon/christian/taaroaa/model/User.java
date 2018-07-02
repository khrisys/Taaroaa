package fr.drochon.christian.taaroaa.model;

import java.io.Serializable;

/**
 * Classe represantant les adherents du club
 */
public class User implements Serializable {

    private String uid;
    private String mNom;
    private String mPrenom;
    private String mLicence;
    private String mEmail;
    private String mNiveau;
    private String mFonction;
    private String mPassword;

    /**
     * Constrcteur par def aut utile à la serialisation des informations des users au travers des activités
     */
    public User() {
    }

    /**
     * Constructeur permettant de retrouver un user par son uid
     *
     * @param uid id de l'user
     */
    public User(String uid) {
        this.uid = uid;
    }

    /**
     * Methode servant à la recherche d'un utilisateur dans la classe SearchUser
     * ainsi qu'à la modification d'un user
     *
     * @param uid   id de l'user
     * @param nom   nom de l'user
     * @param email email de l'user
     */
    public User(String uid, String nom, String email) {
        this.uid = uid;
        mNom = nom;
        mEmail = email;
    }

    /**
     * Creation d'user lors de la recuperation des infos juste apres la creation d'un compte via l'auth firabse
     *
     * @param uid    id de l'user
     * @param nom    nom de l'user
     * @param prenom prenom de l'user
     * @param email  email de l'user
     */
    public User(String uid, String nom, String prenom, String email) {
        this.uid = uid;
        mNom = nom;
        mPrenom = prenom;
        mEmail = email;
    }

    /**
     * Methode permettant de creer ou d'updater toutes les caracteristiques d'un user final
     *
     * @param uid      id de l'user
     * @param nom      nom de l'user
     * @param prenom   prenom de l'user
     * @param licence  licence de l'user
     * @param email    email de l'user
     * @param niveau   niveau de plongée de l'user
     * @param fonction fonction de l'user au sein du club
     */
    public User(String uid, String nom, String prenom, String licence, String email, String niveau, String fonction) {
        this.uid = uid;
        mNom = nom;
        mPrenom = prenom;
        mLicence = licence;
        mEmail = email;
        mNiveau = niveau;
        mFonction = fonction;
    }

    public User(String uid, String nom, String prenom, String licence, String email, String niveau, String fonction, String password) {
        this.uid = uid;
        mNom = nom;
        mPrenom = prenom;
        mLicence = licence;
        mEmail = email;
        mNiveau = niveau;
        mFonction = fonction;
        mPassword = password;
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

    public String getNiveau() {
        return mNiveau;
    }

    public void setNiveau(String niveau) {
        mNiveau = niveau;
    }

    public String getFonction() {
        return mFonction;
    }

    public void setFonction(String fonction) {
        mFonction = fonction;
    }

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String password) {
        mPassword = password;
    }
}
