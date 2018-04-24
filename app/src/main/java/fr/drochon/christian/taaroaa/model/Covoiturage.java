package fr.drochon.christian.taaroaa.model;

import java.sql.Date;
import java.util.List;

public class Covoiturage {

    String id;
    String mNomConducteur;
    int mNbPlacesDispo;
    Date mDateAller;
    Date mDateRetour;
    Date mHeureAller;
    Date mHeureRetour;
    String mTypeVehicule;
    Reservation mReservation;
    List<User> mListPassagers;

    public Covoiturage(String id) {
        this.id = id;
    }

    public Covoiturage(String id, String nomConducteur, int nbPlacesDispo, Date dateAller, Date dateRetour, Date heureAller, Date heureRetour, String typeVehicule) {
        this.id = id;
        mNomConducteur = nomConducteur;
        mNbPlacesDispo = nbPlacesDispo;
        mDateAller = dateAller;
        mDateRetour = dateRetour;
        mHeureAller = heureAller;
        mHeureRetour = heureRetour;
        mTypeVehicule = typeVehicule;
    }

    public Covoiturage(String id, String nomConducteur, int nbPlacesDispo, Date dateAller, Date dateRetour, Date heureAller, Date heureRetour, String typeVehicule, Reservation reservation, List<User> listPassagers) {
        this.id = id;
        mNomConducteur = nomConducteur;
        mNbPlacesDispo = nbPlacesDispo;
        mDateAller = dateAller;
        mDateRetour = dateRetour;
        mHeureAller = heureAller;
        mHeureRetour = heureRetour;
        mTypeVehicule = typeVehicule;
        mReservation = reservation;
        mListPassagers = listPassagers;
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

    public int getNbPlacesDispo() {
        return mNbPlacesDispo;
    }

    public void setNbPlacesDispo(int nbPlacesDispo) {
        mNbPlacesDispo = nbPlacesDispo;
    }

    public Date getDateAller() {
        return mDateAller;
    }

    public void setDateAller(Date dateAller) {
        mDateAller = dateAller;
    }

    public Date getDateRetour() {
        return mDateRetour;
    }

    public void setDateRetour(Date dateRetour) {
        mDateRetour = dateRetour;
    }

    public Date getHeureAller() {
        return mHeureAller;
    }

    public void setHeureAller(Date heureAller) {
        mHeureAller = heureAller;
    }

    public Date getHeureRetour() {
        return mHeureRetour;
    }

    public void setHeureRetour(Date heureRetour) {
        mHeureRetour = heureRetour;
    }

    public String getTypeVehicule() {
        return mTypeVehicule;
    }

    public void setTypeVehicule(String typeVehicule) {
        mTypeVehicule = typeVehicule;
    }

    public Reservation getReservation() {
        return mReservation;
    }

    public void setReservation(Reservation reservation) {
        mReservation = reservation;
    }

    public List<User> getListPassagers() {
        return mListPassagers;
    }

    public void setListPassagers(List<User> listPassagers) {
        mListPassagers = listPassagers;
    }
}
