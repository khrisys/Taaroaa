package fr.drochon.christian.taaroaa.auth;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.model.User;

public class SearchedUserViewHolder extends RecyclerView.ViewHolder {

    //DATA
    List<User> mSearchedUserList;
    //CELLULE
    @BindView(R.id.linearLayout_user_cell)
    LinearLayout mLinearLayoutSearchedUser;
    @BindView(R.id.liste_cell_nom)
    TextView mNomSearched;
    @BindView(R.id.list_cell_prenom)
    TextView mPrenomSearched;
    @BindView(R.id.list_cell_email)
    TextView mEmailSearched;

    /**
     * Contructeur qui prend en param la vue affichée.
     * Je recupere les 2 textview du layout list_cell.
     * responsable du clic sur les cellules.
     *
     * @param itemView : cellule d'une liste comprenant le nom prenom et email de la personne recherchée.
     */
    public SearchedUserViewHolder(View itemView) {
        super(itemView);
        // liaison des elements du layout recyclerview et list_cell avec les variables declarées ici
        ButterKnife.bind(this, itemView);

        mSearchedUserList = new ArrayList<>();

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        if (documentSnapshots.size() != 0) {
                            List<DocumentSnapshot> ds = documentSnapshots.getDocuments();
                            for (int i = 0; i < ds.size(); i++) {
                                Map<String, Object> user = ds.get(i).getData();
                                //TODO recuperer l'objet User via le nom le prenom et l'email en requetant. Passer ensuite l'objet à la liste .
                            }
                        }
                    }
                });
            }
        });
    }

    /**
     * Methode appellée via l'adapter. Cette methode mettra à jour les differentes view du viewholder en fonction de l'utilisateur connecté
     *
     * @param user
     */
    public void updateWithUser(final User user) {
        // ajout des Covoit dans une liste afin de les retrouver pour l'affichage de chaque cours particulier sous forme de notification
        mSearchedUserList.add(user);

        for (int i = 0; i < mSearchedUserList.size(); i++) {
            mNomSearched.setText(user.getNom());
            mPrenomSearched.setText(user.getPrenom());
            mEmailSearched.setText(user.getEmail());
        }

    }
}
