package fr.drochon.christian.taaroaa.course;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

import com.bumptech.glide.RequestManager;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.model.Course;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * l'adapter s'occupe de l'ensemble du contenu alors que le viewholder s'occupe des specificités d'une cellule
 */
public class AdapterCoursesPupils extends FirestoreRecyclerAdapter<Course, PupilsViewHolder> {


    public interface Listener {
        void onDataChanged();
    }

    //FOR COMMUNICATION
    private Listener callback;

    public AdapterCoursesPupils(FirestoreRecyclerOptions<Course> options, Listener callback) {
        super(options);
        this.callback = callback;
    }

    /**
     * Methode qui applique une donnee à une vue (on bind la donnée à la vue).
     * Cette methode sera appellée à chaque fois qu'une donnée devra etre affichée dans une cellule, que la cellule soit nouvellement créée ou recyclée
     *
     * @param holder : la vue de la cellule qui va recevoir la donnée
     * @param position : position de la cellule
     * @param model    the model object containing the data that should be used to populate the view.
     * @see #onBindViewHolder(RecyclerView.ViewHolder, int)
     */
    @Override
    protected void onBindViewHolder(@NonNull PupilsViewHolder holder, int position, @NonNull Course model) {
        holder.updateWithCourse(model);
    }

    /**
     * creation d'un viewholder (ici, on attache la liste des cellules avec la recyclerview) - de la vue d'une cellule, declaré comme argument generique de l'adapter
     *
     * @param parent   : créé la vue
     * @param viewType : sert au cas ou il y aurait differents types de cellules
     * @return le vue d'une cellule
     */
    @Override
    public PupilsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PupilsViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_cell, parent, false));// creation de la viewholder avec en param la vue du layout
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
        this.callback.onDataChanged();
    }
}
