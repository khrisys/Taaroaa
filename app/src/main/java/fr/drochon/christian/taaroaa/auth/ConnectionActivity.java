package fr.drochon.christian.taaroaa.auth;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;

import java.util.List;
import java.util.Objects;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.base.BaseActivity;
import fr.drochon.christian.taaroaa.model.User;

public class ConnectionActivity extends BaseActivity {

    private TextInputEditText mEmail;
    private TextInputEditText mPassword;


    // --------------------
    // LIFECYCLE
    // --------------------

    /**
     * Verification de la validité de l'adresse email par la reponse au token envoyé
     * par firebase à un nouvel user, de meniere à s'assurer que son adresse email
     * soit vlaide (ce qi l'erreur la plis courante).
     *
     * @param target adresse email
     * @return validité ou non de l'adresse email recupérée
     */
    private static boolean isValidEmail(CharSequence target) {
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }


    // --------------------
    // UI
    // --------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        //DESIGN
        mEmail = findViewById(R.id.email_input);
        mPassword = findViewById(R.id.password_input);
        Button valid = findViewById(R.id.creation_identifiants_btn);
        // passage de bundle depuis la connexion pour afficher les caracteristqiues d'un user connecté

        //APPLI BASIQUE
        configureToolbar();
        giveToolbarAName(R.string.creation_compte);


        // --------------------
        // LISTENER
        // --------------------

        //Si tous les champs du formulaire sont remplis, alors l'user
        //se connecte à l 'application. C' est la methode goToAdaptedActivity ()
        // qui determinera si cet user doit se créer un compte ou s'il n'aura qu'ç se connecter.
        valid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (verificationChampsVides())
                    goToAdaptedActivity();
            }
        });
    }

    // --------------------
    // UI
    // --------------------

    @Override
    public int getFragmentLayout() {
        return 0;
    }

    /**
     * Methode permettant de signaler une erreur et de demander de reseigner ce ou ces champs
     * lorsqu'un champ est resté vide pour valider une bonne soulmission du formulaire.
     */
    private boolean verificationChampsVides() {

        if (mPassword.getText().toString().isEmpty()) {
            mPassword.setError("Merci de saisir ce champ !");
            mPassword.requestFocus();
            return false;
        }
        if (!isValidEmail(mEmail.getText().toString())) {
            mEmail.setError("Adresse email non valide !");
            mEmail.requestFocus();
            return false;
        }
        if (mEmail.getText().toString().isEmpty()) {
            mEmail.setError("Merci de saisir ce champ !");
            mEmail.requestFocus();
            return false;
        }
        return true;
    }

    // --------------------
    // GESTION VALIDATION EMAIL ET REINITILAISATION MOT DE PASSE
    // --------------------

    /**
     * Methode permettant à un nouvel utilisateur de se creer un compte, puisde recevoir un token afin de valider
     * l'adresse qu'il a saisi. La validation du lien recu permettra de cofirmer que l'adresse email est valide et de creer un
     * compte au nouvel utilisateur.
     */
    private void connectToFirebaseWithEmailAndPassword() {
        // Test performance de l'update d'user en bdd
        final Trace myTrace = FirebasePerformance.getInstance().newTrace("connectionWithEmailAndPassword_trace");
        myTrace.start();

        // recuperation de la bdd FirebaseAuth avec en param l'app taaroaa
        final FirebaseAuth auth = FirebaseAuth.getInstance(FirebaseFirestore.getInstance().getApp());

        // creation d'un user avec email et password en bdd FirebaseAuth
        auth.createUserWithEmailAndPassword(this.mEmail.getText().toString(), mPassword.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // appel de la methode de verification d'email depuis firebase
                            verifEmailUser();

                            myTrace.stop();
                        } else {
                            // Dans le cas ou l'user ne renseignerait pas cette notification lui informant de valider son adresse,
                            // il ne pourra pas se creer de compte.
                            AlertDialog.Builder adb = new AlertDialog.Builder(ConnectionActivity.this);
                            adb.setTitle("Adresse email incorrecte ou déjà utilisée !");
                            // création d'un icon de warning
                            Drawable warning = getResources().getDrawable(android.R.drawable.ic_dialog_alert);
                            ColorFilter filter = new LightingColorFilter(Color.RED, Color.BLUE);
                            warning.setColorFilter(filter);
                            adb.setIcon(warning);

                            adb.setMessage("L'adresse mail '" + mEmail.getText().toString() + "' est incorrecte ou déjà utilisée.");
                            adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    mEmail.setText("");
                                    mPassword.setText("");
                                }
                            });
                            adb.show();
                        }
                    }
                });
    }

    /**
     * Methode permettant d'envoyer un email via un token pour la confirmation d'adresse mail d'un nouvek utilisateur
     */
    protected void verifEmailUser() {
        if (getCurrentUser() != null) Objects.requireNonNull(getCurrentUser()).reload();

        // un ActionCodeSetting est necessaire à Firebase por savoir à qui envoyer l'email de confilration
        //et quel type de message. Ainsi, l'user recevra un lien de validation qu'il devra soumettre dans une
        //durée impartie. La validation de ce lien de l'user validera automatiquement la creation de son compte.

        // In order to securely pass a continue URL, the domain for the URL will need to be whitelisted in
        // the Firebase console. This is done in the Authentication section by adding this domain to
        // the list of OAuth redirect domains if it is not already there.
        ActionCodeSettings actionCodeSettings = ActionCodeSettings.newBuilder()
                .setUrl("https://taaroaa-fe93c.firebaseapp.com/?page/Auth?mode=%3Caction%3E&oobCode=%3Ccode%3E")
                .setHandleCodeInApp(true)
                //.setIOSBundleId("com.example.ios")
                .setAndroidPackageName(
                        "fr.drochon.christian.taaroaa",// Nom du package unique dde li'application. Ainsi ,des emails
                        // ne peuvent pas etree envoyés pour des autres applications par erreur.
                        true,
                        "19") // minimum SDK
                .build();

        //Afin de valider son formulaire, l'user devra cliquer sur la notif et il recevra alors automatiquement le token via Firebase
        if (!Objects.requireNonNull(
                getCurrentUser()).isEmailVerified()) {
            Objects.requireNonNull(getCurrentUser()).sendEmailVerification(actionCodeSettings)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {

                                Toast.makeText(ConnectionActivity.this,
                                        "Verification d'email envoyée à " + Objects.requireNonNull(getCurrentUser()).getEmail() + "\"",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Log.e("TAG", "sendEmailVerification", task.getException());
                              /*  Toast.makeText(getBaseContext().getApplicationContext(),
                                        "Echec de l'envoi de vérification d'email !",
                                        Toast.LENGTH_LONG).show();*/
                            }
                        }
                    });
        }
    }

    /**
     * Methode permettant de savoir sil'utilisateur avalidé son adresse email et faut alors de lui un utilisateur
     * automitique, voire actuellement dejà connecté. Cette validation de mail fair=t de lui un user enregistré
     * en bdd des utilisateurs, mais aussi un utilisateur de ka bdd de l'authentification de l'application.
     */
    private void goToAdaptedActivity() {

        setupDb().collection("users").whereEqualTo("email", mEmail.getText().toString()).addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                // SI USER EXISTE
                if (queryDocumentSnapshots != null) {
                    if (queryDocumentSnapshots.size() != 0) {
                        List<DocumentSnapshot> ds = queryDocumentSnapshots.getDocuments();
                        for (int i = 0; i < ds.size(); i++) {
                            if (ds.get(i).exists()) {
                                User user = new User(Objects.requireNonNull(ds.get(i).get("uid")).toString(), Objects.requireNonNull(ds.get(i).get("nom")).toString(),
                                        Objects.requireNonNull(ds.get(i).get("prenom")).toString(),
                                        Objects.requireNonNull(ds.get(i).get("licence")).toString(), Objects.requireNonNull(ds.get(i).get("email")).toString(),
                                        Objects.requireNonNull(ds.get(i).get("niveau")).toString(), Objects.requireNonNull(ds.get(i).get("fonction")).toString());
                                Intent intent = new Intent(ConnectionActivity.this, AccountModificationActivity.class).putExtra("user", user);
                                startActivity(intent);
                                break;
                            }
                        }
                    }
                    // SI USER N'EXISTE PAS CAR IL N' PAS VALIDE SON ADRESSE OU S4EST TROMPE DANS LE NOM DE SON ADRESSE  MAIL
                    else if (queryDocumentSnapshots.size() == 0) {
                        connectToFirebaseWithEmailAndPassword();
                        //ici, on peut avoir le choix de lui rappeller qu'il souhaitait souscrire un compte par
                        // une notification, un email ou de le laisser transuille!

                        //TODO A decommenter ou pas selon le choix du client
                     /*   AlertDialog.Builder adb = new AlertDialog.Builder(ConnectionActivity.this);
                        adb.setTitle("Adresse email incorrecte !");
                        // ajouter une couleur à l'icon de warning
                        Drawable warning = getResources().getDrawable(android.R.drawable.ic_dialog_alert);
                        ColorFilter filter = new LightingColorFilter(Color.RED, Color.BLUE);
                        warning.setColorFilter(filter);
                        adb.setIcon(warning);
                        adb.setMessage("L'adresse mail '" + mEmail.getText().toString() + "' est incorrecte.");
                        adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mEmail.setText("");
                                mPassword.setText("");
                            }
                        });
                        adb.show();*/
                    }
                }
            }
        });
    }
}


