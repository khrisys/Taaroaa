package fr.drochon.christian.taaroaa.api;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class CovoiturageHelper {

    private static final String COLLECTION_NAME = "covoiturages";

    // --- COLLECTION REFERENCE ---

    /**
     * Recupere la reference de la collection racine "covoiturages" en utilisant le singleton de FirebaseFirestore.
     *
     * @return CollectionReference
     */
    public static CollectionReference getCovoituragesCollection() {
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    // --- UPDATE ---

    /**
     * Mehode permettant de mettre à jour un covoiturage
     */
    public static Task<Void> updateCovoiturage(String id, String nomConducteur, String prenomConducteur) {
        return CovoiturageHelper.getCovoituragesCollection().document(id).update("id", id, "nomConducteur", nomConducteur, "prenomConducteur", prenomConducteur);
    }

    /**
     * Mehode permettant de mettre à jour un covoiturage lors de la saisie de passagers s'inscrivant à un covoiturage
     */
    public static Task<Void> updateCovoiturage(String id, String nbPlacesDispo, List<String> passagers) {
        return CovoiturageHelper.getCovoituragesCollection().document(id).update("id", id, "nbPlacesDispo", nbPlacesDispo, "listPassagers", passagers);
    }

    // --- DELETE ---

    /**
     * Methode permettant de supprimer un covoiturage
     *
     * @param id
     * @return
     */
    public static Task<Void> deleteCovoiturage(String id) {
        return CovoiturageHelper.getCovoituragesCollection().document(id).delete();
    }
}
