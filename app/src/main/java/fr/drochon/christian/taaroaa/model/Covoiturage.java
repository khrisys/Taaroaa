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
    //private Reservation mReservation;
    private List<String> mListPassagers;

    public Covoiturage() {
    }

    /**
     * Constructeur permettant de retrouver un covoiturage par son uid
     *
     * @param id
     */
    public Covoiturage(String id) {
        this.id = id;
    }

    /**
     * Methode permettant de creer ou d'updater toutes les caracteristiques d'un covoiturage final
     *
     * @param id
     * @param nomConducteur
     * @param prenomConducteur
     * @param nbPlacesDispo
     * @param nbPlacesTotal
     * @param typeVehicule
     * @param horaireAller
     * @param horaireRetour
     * @param lieuDeparttAller
     * @param lieuDepartRetour
     * @param passagers
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


    //GETTERS AND SETTERS

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

    /*
    public Reservation getReservation() {
        return mReservation;
    }

    public void setReservation(Reservation reservation) {
        mReservation = reservation;
    }*/

    public List<String> getListPassagers() {
        return mListPassagers;
    }

    public void setListPassagers(List<String> listPassagers) {
        this.mListPassagers = listPassagers;
    }
}
