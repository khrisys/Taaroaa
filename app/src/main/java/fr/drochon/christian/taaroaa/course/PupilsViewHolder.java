package fr.drochon.christian.taaroaa.course;

import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

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
    @BindView(R.id.liste_cell_linear_layout)
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
     * Appelle la methode updateDesignDependingUser permettant de changer la position à l'ecran des vues attachées (donc => les messages)
     * grace à la mise à jour de leurs param de layout
     *
     * @param course
     */
    public void updateWithCourse(Course course) {
        // ajout des Cours dans une liste afin de les retrouver pour l'affichage de chaque cours particulier sous forme de notification
        mCourseList.add(course);

        // Update course TextView
        this.mCourseType.setText(course.getTypeCours());
        this.mCourseSubject.setText(course.getSujetDuCours());
    }
}
