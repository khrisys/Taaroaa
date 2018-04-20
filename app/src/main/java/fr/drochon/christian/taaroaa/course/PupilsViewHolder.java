package fr.drochon.christian.taaroaa.course;

import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.model.Course;

/**
 * Classe responsable de chaque ligne de la recyclerview, et donc, chaque cours
 */
public class PupilsViewHolder extends RecyclerView.ViewHolder {

    // DATA
    List<Course> mCourseList;
    // CELLULES
    RecyclerView mRecyclerViewCoursesPupils;
    LinearLayout mLinearLayout;
    @BindView(R.id.list_cell_course_subject)
    TextView mCourseSubject;
    @BindView(R.id.liste_cell_course_type)
    TextView mCourseType;

    /**
     * Contructeur qui prend en param la vue affichée.
     * Je recupere les 2 textview du layout list_cell.
     * responsable du clic sur les cellules.
     *
     * @param itemView : cellule d'une liste comprenant le titre et la description d'un cours
     */
    public PupilsViewHolder(final View itemView) {
        super(itemView);
        // liaison des elements du layout recyclerview et list_cell avec les variables declarées ici
        ButterKnife.bind(this, itemView);

        mRecyclerViewCoursesPupils = itemView.findViewById(R.id.recyclerViewCoursesPupils);
        mLinearLayout = itemView.findViewById(R.id.list_supervisors_linear_layout);

        mCourseList = new ArrayList<>();

        // Affichage de la notification
        // j'utilise l'ecouteur sur la cellule et recupere les informations pour les affihcer dans une notification
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String s;
                for (int i = 0; i < mCourseList.size(); i++) {
                    new AlertDialog.Builder(itemView.getContext())
                            .setTitle(mCourseList.get(i).getTypeCours())
                            .setMessage(Html.fromHtml("<b>Sujet : </b>" + mCourseList.get(i).getSujetDuCours() + "<br/><b>Moniteur : </b>" + mCourseList.get(i).getNomDuMoniteur()
                                    + "\n<br/><b>Niveau </b>" + mCourseList.get(i).getNiveauDuCours()
                                    + "<br/>" + stDateToString(mCourseList.get(i).getHoraireDuCours()) + "<br/>" + stTimeToString(mCourseList.get(i).getHoraireDuCours())))
                            .show();
                    break;
                }
            }
        });
    }

    /**
     * Methode appellée via l'adapter. Cette methode mettra à jour les differentes view du viewholder en fonction de l'utilisateur connecté
     *
     * @param course
     */
    public void updateWithCourse(final Course course) {
        // ajout des Cours dans une liste afin de les retrouver pour l'affichage de chaque cours particulier sous forme de notification

        mCourseType.setText(course.getTypeCours());
        mCourseSubject.setText( Html.fromHtml("<b>Sujet : </b>" + course.getSujetDuCours()));
        mCourseList.add(course);
/*        // Affichage en fonction du niveau de la personne connectée
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference currentUser = db.collection("users").document(firebaseUser.getUid());

        currentUser.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (documentSnapshot.exists()) {
                    Map<String, Object> user = documentSnapshot.getData();

                    if (user.get("niveau") != null) {
                        // si le niveau du cours correspond au niveau de l'utilisateur connecté, on affiche le cours
                        if (user.get("niveau").equals(course.getNiveauDuCours())) {
                            mCourseList.add(course);
                            showListCourses(mCourseList);
                        }
                    } else {
                        //TODO notification à l'user de se creer un compte pour cacceder aux cours
                      *//*  AlertDialog.Builder adb = new AlertDialog.Builder();
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
                }
            }
        });*/
    }


    // --------------------
    // HEURE & DATE PARSING
    // --------------------


    /**
     * Methode permettant de formatter une date en string avec locale en francais
     * @param horaireDuCours
     * @return
     */
    public String  stDateToString(Date horaireDuCours){

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.FRANCE);
        String dateDuCours = dateFormat.format(horaireDuCours);
        return dateDuCours;

    }

    /**
     * Methode permettant de formatter une date en format heure
     * @param horaireDuCours
     * @return
     */
    public String stTimeToString(Date horaireDuCours){

        SimpleDateFormat dateFormat1 = new SimpleDateFormat("HH:mm:ss");
        String heureDuCours = dateFormat1.format(horaireDuCours);
        return heureDuCours;
    }
}
