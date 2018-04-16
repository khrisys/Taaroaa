package fr.drochon.christian.taaroaa.api;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;

import fr.drochon.christian.taaroaa.model.Course;

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

    /**
     * Methode permettant de creer un cours.
     *
     * @param typeDuCours
     * @param sujetDuCours
     * @param niveauDuCours
     * @param moniteur
     * @param dateDuCours
     * @return Task
     */
    public static Task<Void> createCourse(String id, String typeDuCours, String sujetDuCours, String niveauDuCours, String moniteur, Date dateDuCours) {
        // creation de l'objet Course
        Course courseToCreate = new Course(id, typeDuCours, sujetDuCours, niveauDuCours, moniteur, dateDuCours);

        return CourseHelper.getCoursesCollection().document().set(courseToCreate);
    }

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

    public static Query getAllCourses() {
        //return CourseHelper.getCoursesCollection().document().collection(COLLECTION_NAME).orderBy("dateDuCours");

        return  CourseHelper.getCoursesCollection();
    }
    public static Query queryAllCourses() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection("courses");

        // Query mq = CourseHelper.getCoursesCollection().document().collection("users");

       /* Query mQuery = db.collection("courses");
        mQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                // condition de creation d'un user ou affichage simple d'un message indiquant que l'user existe dejà en bdd.
                // Avec les uid, il ne peut y avoir de doublon.
                if (documentSnapshots.size() != 0) {
                    Log.e("TAG", "Le document existe !");
                }
            }
        });*/
        //return CourseHelper.queryAllCourses();
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

    public static Task<Void> deleteCourse(String id) {
        return CourseHelper.getCoursesCollection().document(id).delete();
    }
}
