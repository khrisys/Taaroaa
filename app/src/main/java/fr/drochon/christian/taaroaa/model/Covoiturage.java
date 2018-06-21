package fr.drochon.christian.taaroaa.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Covoiturage implements Serializable {

    /**
     * Parcelable
     * this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
     */

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator<Covoiturage>() {

        public Covoiturage createFromParcel(Parcel in) {
            Covoiturage covoiturage;
            covoiturage = (Covoiturage) in.readValue(Covoiturage.class.getClassLoader());
            return covoiturage;
        }

        public Covoiturage[] newArray(int size) {
            return new Covoiturage[size];
        }
    };

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


    //GETTERS AND SETTERS

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

    // example constructor that takes a Parcel and gives you an object populated with it's values
    private Covoiturage(Parcel in) {
        this.id = in.readString();
        mNomConducteur = in.readString();
        mPrenomConducteur = in.readString();
        mNbPlacesDispo = in.readString();
        mNbPlacesTotal = in.readString();
        mHoraireAller = (Date) in.readValue(Date.class.getClassLoader());
        mHoraireRetour = (Date) in.readValue(Date.class.getClassLoader());
        mTypeVehicule = in.readString();
        mLieuDepartAller = in.readString();
        mLieuDepartRetour = in.readString();
        //in.readStringList(getListPassagers());
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

    /*
    public Reservation getReservation() {
        return mReservation;
    }

    public void setReservation(Reservation reservation) {
        mReservation = reservation;
    }*/

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

    // --------------------
    // PARCELABLE
    // --------------------
    /* everything below here is for implementing Parcelable */

    public void setListPassagers(List<String> listPassagers) {
        this.mListPassagers = listPassagers;
    }

/*    *//**
     * 99.9% of the time you can just ignore this
     *
     * @return code int
     *//*
    @Override
    public int describeContents() {
        return 0;
    }

    *//**
     * write your object's data to the passed-in Parcel
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     *//*
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(mNomConducteur);
        dest.writeString(mPrenomConducteur);
        dest.writeString(mNbPlacesDispo);
        dest.writeString(mNbPlacesTotal);
        dest.writeValue(mHoraireAller);
        dest.writeValue(mHoraireRetour);
        dest.writeString(mTypeVehicule);
        dest.writeString(mLieuDepartAller);
        dest.writeString(mLieuDepartRetour);
        dest.writeList(mListPassagers);
    }*/
}
