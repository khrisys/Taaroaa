package fr.drochon.christian.taaroaa.course;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.model.Course;

/**
 * l'adapter s'occupe de l'ensemble du contenu alors que le viewholder s'occupe des specificités d'une cellule.
 * <p>
 * Adapter permettant de creer des cellules d'un FirestoreRecyclerView avec des données provenant de l'activité CoursesPupilsActivity.
 * Le FirestoreRecyclerAdapter (disponible dans la librairie "firebaseui") permet de gerer la MAJ en temps reel d'un recyclerview afin de
 * refleter exactement la bdd firestore, de mettre en cache toutes les données afin d'y avoir acces meme sans internet.
 */
class AdapterCoursesPupils extends FirestoreRecyclerAdapter<Course, PupilsViewHolder> {


    //FOR COMMUNICATION
    private final Listener callback;

    AdapterCoursesPupils(FirestoreRecyclerOptions<Course> options, Listener callback) {
        super(options);
        this.callback = callback;
    }

    /**
     * Methode qui applique une donnee à une vue (on bind la donnée à la vue).
     * Cette methode sera appellée à chaque fois qu'une donnée devra etre affichée dans une cellule, que la cellule soit nouvellement créée ou recyclée
     *
     * @param holder   : la vue de la cellule qui va recevoir la donnée
     * @param position : position de la cellule
     * @param model    the model object containing the data that should be used to populate the view.
     */
    @Override
    protected void onBindViewHolder(@NonNull final PupilsViewHolder holder, int position, @NonNull final Course model) {
        holder.updateWithCourse(model);
    }

    /**
     * creation d'un viewholder (ici, on attache la liste des cellules avec la recyclerview) - de la vue d'une cellule, declaré comme argument generique de l'adapter
     *
     * @param parent   : créé la vue
     * @param viewType : sert au cas ou il y aurait differents types de cellules
     * @return le vue d'une cellule
     */
    @NonNull
    @Override
    public PupilsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PupilsViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pupils_cell, parent, false));// creation de la viewholder avec en param la vue du layout
    }

    // --------------------
    // INTERFACE LISTENER : CALLBACK
    // --------------------

    @Override
    public void onDataChanged() {
        super.onDataChanged();
        this.callback.onDataChanged();
    }

    public interface Listener {
        void onDataChanged();
    }
}
