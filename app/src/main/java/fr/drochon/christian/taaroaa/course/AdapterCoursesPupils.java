package fr.drochon.christian.taaroaa.course;

import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.controller.Sixth;

/**
 * l'adapter s'occupe de l'ensemble du contenu alors que le viewholder s'occupe des specificités d'une cellule
 */
public class AdapterCoursesPupils extends RecyclerView.Adapter<AdapterCoursesPupils.MyViewHolder> {

    public static final int DROIT_N1 = 1;
    public static final int DROIT_MONITEUR = 2;
    private final List<Sixth<String, String, Integer, String, String, String>> characters;
    CalendarView mCalendarView;
    List<Sixth> listSixth;

    public AdapterCoursesPupils() {
        characters = Arrays.asList(
                Sixth.create("Théorie", "Accidents, barotraumatismes", 1, "Cédric Salvat", "Apr 18 2018", "19:30"),
                Sixth.create("Pratique", "RSE, VDM, LRE, Signes", 1, "Armand Begliomini", "Apr 18 2018", "21:00"),
                Sixth.create("Pratique", "PMT, canard", 1, "Pierre Moizet", "Apr 25 2018", "21:00"),
                Sixth.create("Pratique", "PMT, vidage tuba", 1, "Pierre Moizet", "May 9 2018", "21:00"),
                Sixth.create("Théorie", "Pression, Volume, Flottabilité", 2, "Cedric Salvat", "Apr 25 2018", "19:30"),
                Sixth.create("Théorie", "Prévention des accidents Barotraumatiques", 2, "Armand Begliomini", "May 9 2018", "19:30"),
                Sixth.create("Théorie", "Ventilation et Essoufflement", 2, "Eric Julien", "May 16 2018", "19:30"),
                Sixth.create("Pratique", "Capelage, décapelage au fond", 1, "Cedric Salvat", "May 23 2018", "19:30"),
                Sixth.create("Pratique", "Gestion incident", 3, "Laurent Labbe", "May 2 2018", "20:00"),
                Sixth.create("Théorie", "Les Accidents De Décompression", 2, "Armand Begliomini", "May 30 2018", "19:30")
        );
        listSixth = new ArrayList<>();
    }

    /**
     * Retourne le nb total de cellules que contiendra la liste
     *
     * @return le nombre total d'items que contient la liste
     */
    @Override
    public int getItemCount() {
        return characters.size();
    }

    /**
     * creation d'un viewholder - de la vue d'une cellule, declaré comme argument generique de l'adapter
     *
     * @param parent   : créé la vue
     * @param viewType : sert au cas ou il y aurait differents types de cellules
     * @return le vue d'une cellule
     */
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext()); // creation du layout
        View view = inflater.inflate(R.layout.list_cell, parent, false);
        return new MyViewHolder(view); // creation de la viewholder avec en param la vue du layout
    }

    /**
     * Methode qui applique une donnee à une vue (on bind la donnée à la vue).
     * Cette methode sera appellée à chaque fois qu'une donnée devra etre affichée dans une cellule, que la cellule soit nouvellement créée ou recyclée
     *
     * @param holder   : la vue de la cellule qui va recevoir la donnée
     * @param position : position de la cellule
     */
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Sixth<String, String, Integer, String, String, String> sixth;
        sixth = characters.get(position);

        // AFFICHAGE SEULEMENT POUR LES N1
        if(sixth.third == DROIT_N1)
        holder.display(sixth); // recuperation de la sixth et je la fourni au viewholder pour qu'il l'affiche

    }

    public String getDate(){
        Sixth sixth = null;
        return sixth.fifth.toString();
    }


    /**
     * Classe interne.
     */
    public class MyViewHolder extends RecyclerView.ViewHolder {

        Sixth<String, String, Integer, String, String, String> currentSixth;
        private TextView mCourseType;
        private TextView mCoursesubject;
        /*        private final TextView mCourseSupervisor;
                private final TextView mCourseLevel;
                private final TextView mCourseDate;
                private final TextView mCourseHeure;*/


        /**
         * Contructeur qui prend en param la vue affichée.
         * Je recupere les 2 textview.
         * responsable du clic sur les cellules.
         *
         * @param itemView : cellule d'une liste comprenant le titre et la description d'un cours
         */
        MyViewHolder(final View itemView) {
            super(itemView);

            mCourseType = itemView.findViewById(R.id.course_type);
            mCoursesubject = itemView.findViewById(R.id.course_subject);
/*            mCourseSupervisor = itemView.findViewById(R.id.course_supervisor);
            mCourseLevel = itemView.findViewById(R.id.course_level);
            mCourseDate = itemView.findViewById(R.id.course_date);
            mCourseHeure = itemView.findViewById(R.id.course_heure);*/

            // Affichage de la notification
            // 2 : j'utilise l'ecouteur sur la cellule et recupere les informations pour les affihcer dans une notification
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String s;
                    s = currentSixth.second + "\nNiveau " + currentSixth.third + "\n" + currentSixth.fourth + "\n" + currentSixth.fifth + "\n" + currentSixth.sixth;
                    new AlertDialog.Builder(itemView.getContext())
                            .setTitle(currentSixth.first)
                            .setMessage(s)
                            .show();
                }
            });
        }

        void display() {
            display();
        }

        /**
         * affichage de la sixth qui est fournie, soit leRecy nom et la mCoursesubject avec un String generique pour chacun d'entre eux
         *
         * @param sixth : variable composée des 6 elements d'un cours
         */
        void display(Sixth<String, String, Integer, String, String, String> sixth) {
            // 1 : enregistrer la sixth que je clique + nom et mCoursesubject
            currentSixth = sixth;

                mCourseType.setText(currentSixth.first);
                mCoursesubject.setText(currentSixth.second);
                /* mCourseSupervisor.setText(sixth.third);
            mCourseLevel.setText(sixth.fourth);
            mCourseDate.setText(sixth.fifth);
            mCourseHeure.setText(sixth.sixth);*/

        }
    }
}
