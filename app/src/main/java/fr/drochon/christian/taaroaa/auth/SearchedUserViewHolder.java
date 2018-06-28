package fr.drochon.christian.taaroaa.auth;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.model.User;

public class SearchedUserViewHolder extends RecyclerView.ViewHolder {

    //DATA
    private final List<User> mSearchedUserList;
    // DESIGN
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
     * Je recupere les 2 textview du layout pupils_cell.
     * responsable du clic sur les cellules.
     *
     * @param itemView : cellule d'une liste comprenant le nom prenom et email de la personne recherchée.
     */
    SearchedUserViewHolder(View itemView) {
        super(itemView);
        // liaison des elements du layout recyclerview et pupils_cell avec les variables declarées ici
        ButterKnife.bind(this, itemView);

        mSearchedUserList = new ArrayList<>();

        // --------------------
        // LISTENER
        // --------------------

        // Affichage du contenu de la cellule
        // j'utilise l'ecouteur sur la cellule et recupere les informations pour les affihcer dans une notification
        // j'envoie un intent vers la classe ModificationAccount pour que cette classe sache quel utilisateur a été selectionné par l'encadrant
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (User u: mSearchedUserList
                     ) {
                    Intent intent = new Intent(v.getContext(), AccountModificationActivity.class).putExtra("searchedUser", u);
                    v.getContext().startActivity(intent);
                }
            }
        });
    }

    /**
     * Methode appellée via l'adapter. Cette methode mettra à jour les differentes view du viewholder en fonction de l'utilisateur connecté
     *
     * @param user utilisateur
     */
    public void updateWithUser(final User user) {
        // ajout des Covoit dans une liste afin de les retrouver pour l'affichage de chaque cours particulier sous forme de notification
        mSearchedUserList.add(user);

        for (int i = 0; i < mSearchedUserList.size(); i++) {
            mPrenomSearched.setText(user.getPrenom());
            mNomSearched.setText(user.getNom());
            mEmailSearched.setText(user.getEmail());
        }
    }
}
