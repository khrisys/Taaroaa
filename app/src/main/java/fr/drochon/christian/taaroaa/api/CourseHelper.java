package fr.drochon.christian.taaroaa.api;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Date;

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

    /**
     * Methode permettant de recuperer un document specifié par son id passé en param
     *
     * @param id
     * @return
     */
    public static Query getCourse(String id) {
        return CourseHelper.getCoursesCollection().document(id).collection(COLLECTION_NAME);
    }

    // --- UPDATE ---

    /**
     * Mehode permettant de mettre à jour un utilisateur.
     * Auncun utilisateur ne peut updater son adresse email pour eviter de perdre don addresse en la cgngeant trop souvent.
     */
    public static Task<Void> updateCourse(String id, String type, String sujet, String niveau, String moniteur, Date dateDuCours) {
        return CourseHelper.getCoursesCollection().document(id).update("id", id, "type", type, "sujetDuCours", sujet, "niveauDuCours", niveau, "niveauDuCours", niveau, "moniteur", moniteur, "dateDuCours", dateDuCours);
    }

    // --- DELETE ---

}
