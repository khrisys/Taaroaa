package fr.drochon.christian.taaroaa.course;

import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.model.Course;

/**
 * Classe responsable de chaque ligne de la recyclerview des supervisors, et donc, chaque cours
 */
public class SupervisorsViewHolder extends RecyclerView.ViewHolder {

    // DATA
    List<Course> mCourseList;
    // CELLULES
    RecyclerView mRecyclerViewCoursesSupervisors;
    LinearLayout mLinearLayout;
    @BindView(R.id.course_subject_supervisors)
    TextView mCourseSubject;
    @BindView(R.id.course_type_supervisors)
    TextView mCourseType;
    @BindView(R.id.course_level_supervisors) TextView mCourseLevel;
    @BindView(R.id.course_moniteur_supervisor) TextView mMoniteur;
    @BindView(R.id.course_date_supervisors) TextView mDate;
    @BindView(R.id.course_heure_supervisors) TextView mHeure;


    /**
     * Contructeur qui prend en param la vue affichée.
     * Je recupere les 2 textview du layout list_cell.
     * responsable du clic sur les cellules.
     *
     * @param itemView : cellule d'une liste comprenant le titre et la description d'un cours
     */
    public SupervisorsViewHolder(final View itemView) {
        super(itemView);
        // liaison des elements du layout recyclerview et list_cell avec les variables declarées ici
        ButterKnife.bind(this, itemView);

        mRecyclerViewCoursesSupervisors = itemView.findViewById(R.id.recyclerViewCoursesSupervisors);
        mLinearLayout = itemView.findViewById(R.id.supervisors_linear_layout);
        mCourseList = new ArrayList<>();

        // Affichage de la notification de l'ensemble des informations des cours
        // j'utilise l'ecouteur sur la cellule et recupere les informations pour les affihcer dans une notification
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String s;
                for (int i = 0; i < mCourseList.size(); i++) {
                    new AlertDialog.Builder(itemView.getContext())
                            .setTitle(mCourseList.get(i).getTypeCours())
                            .setMessage("Sujet : " + mCourseList.get(i).getSujetDuCours() + "\nMoniteur : " + mCourseList.get(i).getNomDuMoniteur()
                                    + "\nNiveau " + mCourseList.get(i).getNiveauDuCours()
                                    + "\n" + mCourseList.get(i).getDateDuCours() + "\n" + mCourseList.get(i).getTimeDuCours()).show();
                    break;
                }
            }
        });
    }

    /**
     * Methode appellée via l'adapter. Cette methode mettra à jour les differentes view du viewholder en fonction d'un objet course passé en param
     *
     * @param course
     */
    public void updateWithCourse(final Course course) {
        // ajout des Cours dans une liste afin de les retrouver pour l'affichage de chaque cours particulier sous forme de notification
        mCourseList.add(course);

        // Affichage de tous les cours en bdd
        mCourseType.setText(course.getTypeCours());
        mCourseSubject.setText("Sujet : " + course.getSujetDuCours());
        mMoniteur.setText("Moniteur : " + course.getNomDuMoniteur());
        mCourseLevel.setText("Niveau " + course.getNiveauDuCours());
        mDate.setText(course.getDateDuCours().toString());
        mHeure.setText(course.getTimeDuCours().toString());

        /*// Affichage en fonction du niveau de la personne connectée
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference currentUser = db.collection("users").document(firebaseUser.getUid());

        currentUser.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (documentSnapshot.exists()) {
                    Map<String, Object> user = documentSnapshot.getData();
                    if(user.get("niveau") == null){
                        //TODO notification à l'user de se creer un compte pour cacceder aux cours
   *//*                     AlertDialog.Builder adb = new AlertDialog.Builder();
                        adb.setTitle(R.string.alertDialog_account);
                        adb.setIcon(android.R.drawable.ic_dialog_alert);
                        adb.setTitle("Merci de completer votre compte pour acceder à la liste des cours !");
                        adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // rien à appeler. pas la peine de faire de toast
                            }
                        });
                        adb.show(); // affichage de l'artdialog*//*
                    }
                    else if (user.get("niveau").equals(course.getNiveauDuCours())) {
                        mCourseType.setText(course.getTypeCours());
                        mCourseSubject.setText(course.getSujetDuCours());
                    }
                }
            }
        });*/
    }
}
