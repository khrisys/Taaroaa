package fr.drochon.christian.taaroaa.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Covoiturage implements Serializable {

    /*
    LES NOMS DES VARIABLES DOIVENT CORRESPONDRENT AU NOM DES CHAMPS DE LA BDD AVEC LE PREFIXE "m"
     */
    private String id;
    private String mNomConducteur;
    private String mPrenomConducteur;
    private String mNbPlacesDispo;
    private String mNbPlacesTotal;
    private Date mHoraireAller;
    private Date mHoraireRetour;
    private String mTypeVehicule;
    private String mLieuDepartAller;
    private String mLieuDepartRetour;
    private List<String> mListPassagers;

    /**
     * Constructeur par defaut utile à la bonne serialisation des informations au travers des activtés,
     * notamment l'affichage des passagers
     */
    public Covoiturage() {
    }


    //GETTERS AND SETTERS

    /**
     * Methode permettant de creer ou d'updater toutes les caracteristiques d'un covoiturage final
     *
     * @param id               id du covoiturage
     * @param nomConducteur    nom du conducteur
     * @param prenomConducteur prenom du conducteur
     * @param nbPlacesDispo    nombre de places disponibles dans le covoiturage
     * @param nbPlacesTotal    nombre de places totales proposées au debut du covoiturage
     * @param typeVehicule     type du vehicule
     * @param horaireAller     date et heure du trajet aller
     * @param horaireRetour    date et heure du depart du trajet retour
     * @param lieuDeparttAller lieu de depart aller
     * @param lieuDepartRetour lieu de depart retour
     * @param passagers        nom et prenom des passagers
     */
    public Covoiturage(String id, String nomConducteur, String prenomConducteur, String nbPlacesDispo, String nbPlacesTotal, String typeVehicule, Date horaireAller, Date horaireRetour,
                       String lieuDeparttAller, String lieuDepartRetour, List<String> passagers) {
        this.id = id;
        mNomConducteur = nomConducteur;
        mPrenomConducteur = prenomConducteur;
        mNbPlacesDispo = nbPlacesDispo;
        mNbPlacesTotal = nbPlacesTotal;
        mHoraireAller = horaireAller;
        mHoraireRetour = horaireRetour;
        mTypeVehicule = typeVehicule;
        mLieuDepartAller = lieuDeparttAller;
        mLieuDepartRetour = lieuDepartRetour;
        mListPassagers = passagers;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNomConducteur() {
        return mNomConducteur;
    }

    public void setNomConducteur(String nomConducteur) {
        mNomConducteur = nomConducteur;
    }

    public String getPrenomConducteur() {
        return mPrenomConducteur;
    }

    public void setPrenomConducteur(String prenomConducteur) {
        mPrenomConducteur = prenomConducteur;
    }

    public String getNbPlacesDispo() {
        return mNbPlacesDispo;
    }

    public void setNbPlacesDispo(String nbPlacesDispo) {
        mNbPlacesDispo = nbPlacesDispo;
    }

    public String getNbPlacesTotal() {
        return mNbPlacesTotal;
    }

    public void setNbPlacesTotal(String nbPlacesTotal) {
        mNbPlacesTotal = nbPlacesTotal;
    }

    public Date getHoraireAller() {
        return mHoraireAller;
    }

    public void setHoraireAller(Date horaireAller) {
        mHoraireAller = horaireAller;
    }

    public Date getHoraireRetour() {
        return mHoraireRetour;
    }

    public void setHoraireRetour(Date horaireRetour) {
        mHoraireRetour = horaireRetour;
    }

    public String getTypeVehicule() {
        return mTypeVehicule;
    }

    public void setTypeVehicule(String typeVehicule) {
        mTypeVehicule = typeVehicule;
    }

    public String getLieuDepartAller() {
        return mLieuDepartAller;
    }

    public void setLieuDepartAller(String lieuDepartAller) {
        mLieuDepartAller = lieuDepartAller;
    }

    public String getLieuDepartRetour() {
        return mLieuDepartRetour;
    }

    public void setLieuDepartRetour(String lieuDepartRetour) {
        mLieuDepartRetour = lieuDepartRetour;
    }

    public List<String> getListPassagers() {
        return mListPassagers;
    }

    public void setListPassagers(List<String> listPassagers) {
        mListPassagers = listPassagers;
    }
}
