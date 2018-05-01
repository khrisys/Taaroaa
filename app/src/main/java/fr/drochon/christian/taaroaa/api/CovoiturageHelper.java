package fr.drochon.christian.taaroaa.api;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

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
}
