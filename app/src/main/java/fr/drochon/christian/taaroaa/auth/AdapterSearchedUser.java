package fr.drochon.christian.taaroaa.auth;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.model.User;

public class AdapterSearchedUser extends FirestoreRecyclerAdapter<User, SearchedUserViewHolder> {

    //FOR COMMUNICATION
    private Listener callback;

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public AdapterSearchedUser(@NonNull FirestoreRecyclerOptions<User> options, Listener callback) {
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
     * @see #onBindViewHolder(RecyclerView.ViewHolder, int)
     */
    @Override
    protected void onBindViewHolder(@NonNull SearchedUserViewHolder holder, int position, @NonNull User model) {
        holder.updateWithUser(model);
    }

    /**
     * creation d'un viewholder (ici, on attache la liste des cellules avec la recyclerview) - de la vue d'une cellule, declaré comme argument generique de l'adapter
     *
     * @param parent   : créé la vue
     * @param viewType : sert au cas ou il y aurait differents types de cellules
     * @return le vue d'une cellule
     */
    @Override
    public SearchedUserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SearchedUserViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_cell, parent, false));// creation de la viewholder avec en param la vue du layout
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
        this.callback.onDataChanged();
    }

    public interface Listener {
        void onDataChanged();
    }
}
