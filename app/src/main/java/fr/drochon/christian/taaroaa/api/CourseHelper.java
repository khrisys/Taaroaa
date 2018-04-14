package fr.drochon.christian.taaroaa.api;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.sql.Time;
import java.time.LocalDate;
import java.util.Date;

import fr.drochon.christian.taaroaa.model.Course;

/**
 * Classe permettant d'implementer le CRUD au sein de l'application pour les cours.
 */
public class CourseHelper {

    private static final String COLLECTION_NAME = "courses";

    // --- COLLECTION REFERENCE ---

    /**
     * Recupere la reference d'une collection en utilisant le singleton de FirebasFirestore et en
     * appellant cette collection "users".
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
    public static Query getCourse(String id){
        return CourseHelper.getCoursesCollection().document(id).collection(COLLECTION_NAME);
    }

    public static Query getAllCourses() {
        return CourseHelper.getCoursesCollection().document().collection(COLLECTION_NAME).orderBy("dateDuCours");
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
