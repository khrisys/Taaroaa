package fr.drochon.christian.taaroaa.api;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Classe permettant d'implementer le CRUD au sein de l'application pour les cours.
 */
public class CourseHelper {

    private static final String COLLECTION_NAME = "courses";

    // --- COLLECTION REFERENCE ---

    /**
     * Recupere la reference de la collection racine "courses" en utilisant le singleton de FirebasFirestore.
     *
     * @return CollectionReference
     */
    public static CollectionReference getCoursesCollection() {
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    // --- CREATE ---


    // --- GET ---


    // --- UPDATE ---


    // --- DELETE ---

}
