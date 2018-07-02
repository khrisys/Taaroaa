package fr.drochon.christian.taaroaa.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


/**
 * Classe des cours dispensés au sein du club
 */
public class Course implements Serializable {

    private String mUid;
    private String mTypeCours;
    private String mSujetDuCours;
    private String mNiveauDuCours;
    private String mNomDuMoniteur;
    private Date mHoraireDuCours;
    private Date mDateDuCours;
    private Date mTimeDuCours;

    /**
     * Constructeur par defaut utile au bon fonctionnement de la serialisation des infos au travers des activités
     */
    public Course() {
    }

    /**
     * Constructeur permettant de retrouver un cours par son uid
     *
     * @param uid id du cours
     */
    public Course(String uid) {
        mUid = uid;
    }

    /**
     * Methode permettant de creer ou d'updater toutes les caracteristiques d'un cours final
     *
     * @param uid            id du cours
     * @param typeCours      type de cours
     * @param sujetDuCours   sujet du cours
     * @param niveauDuCours  niveau de formation du cours
     * @param nomDuMoniteur  nom du moniteur qui donne le cours
     * @param horaireDuCours date et heure du cours
     */
    public Course(String uid, String typeCours, String sujetDuCours, String niveauDuCours, String nomDuMoniteur, Date horaireDuCours) {
        mUid = uid;
        mTypeCours = typeCours;
        mSujetDuCours = sujetDuCours;
        mNiveauDuCours = niveauDuCours;
        mNomDuMoniteur = nomDuMoniteur;
        mHoraireDuCours = horaireDuCours;
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

    public Date getHoraireDuCours() {
        return mHoraireDuCours;
    }

    public void setHoraireDuCours(Date horaireDuCours) {
        mHoraireDuCours = horaireDuCours;
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

}
