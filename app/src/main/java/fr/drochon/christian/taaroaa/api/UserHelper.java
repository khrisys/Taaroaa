package fr.drochon.christian.taaroaa.api;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Classe permettant d'implementer le CRUD au sein de l'application pour les utilisateurs.
 */
public class UserHelper {

    private static final String COLLECTION_NAME = "users";

    // --- COLLECTION REFERENCE ---

    /**
     * Recupere la reference d'une collection en utilisant le singleton de FirebasFirestore et en
     * appellant cette collection "users".
     *
     * @return CollectionReference
     */
    private static CollectionReference getUsersCollection() {
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    // --- CREATE ---

    // --- GET ---

    // --- UPDATE ---

    /**
     * Mehode permettant de mettre à jour un utilisateur.
     * Auncun utilisateur ne peut updater son adresse email pour eviter de perdre don addresse en la cgngeant trop souvent.
     */
    public static Task<Void> updateUser(String uid, String nom, String prenom, String licence, String email, String niveau, String fonction) {
        return UserHelper.getUsersCollection().document(uid).update("uid", uid, "nom", nom, "prenom", prenom, "licence", licence, "email", email, "niveau", niveau, "fonction", fonction);
    }

    /**
     * Mehode permettant de mettre à jour un utilisateur.
     * Auncun utilisateur ne peut updater son adresse email pour eviter de perdre don addresse en la cgngeant trop souvent.
     */
    public static Task<Void> updateUser(String uid, String nom, String prenom, String licence, String email, String niveau, String fonction, String password) {
        return UserHelper.getUsersCollection().document(uid).update("uid", uid, "nom", nom, "prenom", prenom, "licence", licence, "email", email, "niveau", niveau, "fonction", fonction, "password", password);
    }

    // --- DELETE ---

    public static Task<Void> deleteUser(String uid) {
        return UserHelper.getUsersCollection().document(uid).delete();
    }
}
