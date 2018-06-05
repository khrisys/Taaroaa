package fr.drochon.christian.taaroaa.course;

import android.annotation.SuppressLint;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.model.Course;

/**
 * Classe responsable de chaque ligne de la recyclerview des supervisors, et donc, chaque cours
 */
public class SupervisorsViewHolder extends RecyclerView.ViewHolder {

    // DATA
    private final List<Course> mCourseList;
    // CELLULES
    @BindView(R.id.course_subject_supervisors)
    TextView mCourseSubject;
    @BindView(R.id.course_type_supervisors)
    TextView mCourseType;
    @BindView(R.id.course_level_supervisors)
    TextView mCourseLevel;
    @BindView(R.id.course_moniteur_supervisor)
    TextView mMoniteur;
    @BindView(R.id.course_date_supervisors)
    TextView mDate;
    @BindView(R.id.course_heure_supervisors)
    TextView mHeure;

    // --------------------
    // AFFICHAGE DES NOTIFICATIONS DE CELLULE
    // --------------------

    /**
     * Contructeur qui prend en param la vue affichée.
     * Je recupere les 2 textview du layout pupils_cell.
     * responsable du clic sur les cellules.
     *
     * @param itemView : cellule d'une liste comprenant le titre et la description d'un cours
     */
    SupervisorsViewHolder(final View itemView) {
        super(itemView);
        // liaison des elements du layout recyclerview et pupils_cell avec les variables declarées ici
        ButterKnife.bind(this, itemView);

        mCourseList = new ArrayList<>();

        // Affichage de la notification de l'ensemble des informations des cours
        // j'utilise l'ecouteur sur la cellule et recupere les informations pour les affihcer dans une notification
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String s;
                int i = mCourseList.size() - 1;
                new AlertDialog.Builder(itemView.getContext())
                        .setTitle(mCourseList.get(i).getTypeCours())
                        .setMessage(Html.fromHtml("<b>Sujet : </b>" + mCourseList.get(i).getSujetDuCours() + "<br/><b>Moniteur : </b>" + mCourseList.get(i).getNomDuMoniteur()
                                + "<br/><b>Niveau </b>" + mCourseList.get(i).getNiveauDuCours()
                                + "<br/>" + stDateToString(mCourseList.get(i).getHoraireDuCours()) + "<br/>" + stTimeToString(mCourseList.get(i).getHoraireDuCours())))
                        .show();
            }
        });
    }

    // --------------------
    // ADAPTER
    // --------------------

    /**
     * Methode appellée via l'adapter. Cette methode mettra à jour les differentes view du viewholder en fonction d'un objet course passé en param.
     * On affiche donc ici les données d'une cellule à l'affichage de l'ecran des encadrants (et non pas les notifications
     *
     * @param course
     */
    public void updateWithCourse(final Course course) {
        // ajout des Cours dans une liste afin de les retrouver pour l'affichage de chaque cours particulier sous forme de notification
        mCourseList.add(course);

        // Affichage de tous les cours en bdd
        mCourseType.setText(course.getTypeCours());
        mCourseSubject.setText(Html.fromHtml("<b>Sujet : </b>" + course.getSujetDuCours()));
        mMoniteur.setText(Html.fromHtml("<b>Moniteur : </b>" + course.getNomDuMoniteur()));
        mCourseLevel.setText(Html.fromHtml("<b>Niveau </b>" + course.getNiveauDuCours()));

        // recuperation du datetime
        Date horaireDuCours = course.getHoraireDuCours();

        // formatage de la date seule en string
        mDate.setText(stDateToString(horaireDuCours));

        // formatage de l'heure seule en string
        mHeure.setText(stTimeToString(horaireDuCours));
    }

    // --------------------
    // FORMATAGE DES HEURE ET DATE
    // --------------------

    /**
     * Methode permettant de formatter une date en string avec locale en francais
     *
     * @param horaireDuCours
     * @return
     */
    private String stDateToString(Date horaireDuCours) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("EE dd MMM yyyy", Locale.FRANCE);
        return dateFormat.format(horaireDuCours);

    }

    /**
     * Methode permettant de formatter une date en format heure
     *
     * @param horaireDuCours
     * @return
     */
    private String stTimeToString(Date horaireDuCours) {

        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat1 = new SimpleDateFormat("HH:mm:ss");
        return dateFormat1.format(horaireDuCours);
    }
}
