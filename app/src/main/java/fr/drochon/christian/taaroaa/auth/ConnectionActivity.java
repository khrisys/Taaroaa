package fr.drochon.christian.taaroaa.auth;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;

import java.util.Objects;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.base.BaseActivity;

public class ConnectionActivity extends BaseActivity {

    private TextInputEditText mEmail;
    private TextInputEditText mPassword;

    /**
     * Verification de la validité de l'adresse email
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

        mEmail = findViewById(R.id.email_input);
        mPassword = findViewById(R.id.password_input);
        Button valid = findViewById(R.id.creation_identifiants_btn);

        // Test performance de l'update d'user en bdd
        final Trace myTrace = FirebasePerformance.getInstance().newTrace("connectionActivity_trace");
        myTrace.start();

        configureToolbar();
        giveToolbarAName(R.string.creation_compte);

        // --------------------
        // LISTENER
        // --------------------

        valid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mEmail.getText().toString().isEmpty() && !mPassword.getText().toString().isEmpty()) {
                    connectToFirebaseWithEmailAndPassword();
                    myTrace.stop();
                } else
                    verificationChampsVides();
            }
        });
    }

    @Override
    public int getFragmentLayout() {
        return 0;
    }

    /**
     * Methode permettant de signaler une erreur lorsqu'un champ est resté vide alors que la soumission du formulaire a été faite.
     */
    private void verificationChampsVides() {

        if (mPassword.getText().toString().isEmpty()) {
            mPassword.setError("Merci de saisir ce champ !");
            mPassword.requestFocus();
        }
        if (!isValidEmail(mEmail.getText().toString())) {
            mEmail.setError("Adresse email non valide !");
            mEmail.requestFocus();
        }
        if (mEmail.getText().toString().isEmpty()) {
            mEmail.setError("Merci de saisir ce champ !");
            mEmail.requestFocus();
        }
    }

    // --------------------
    // VALIDATION EMAIL PAR LIEN ENVOYE DEPUIS FIREBASE
    // --------------------
    private void connectToFirebaseWithEmailAndPassword() {
        // recuperation de la bdd FirebaseAuth avec en param l'app taaroaa
        final FirebaseAuth auth = FirebaseAuth.getInstance(FirebaseFirestore.getInstance().getApp());

        // 1 : creation d'un user avec email et password en bdd FirebaseAuth
        auth.createUserWithEmailAndPassword(this.mEmail.getText().toString(), mPassword.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // appel de la methode de verification d'email depuis firebase
                            verifEmailUser();
                        } else {
                            // If sign in fails, display a message to the user.
                            AlertDialog.Builder adb = new AlertDialog.Builder(ConnectionActivity.this);
                            adb.setTitle("Adresse email déjà utilisée !");
                            // ajouter une couleur à l'icon de warning
                            Drawable warning = getResources().getDrawable(android.R.drawable.ic_dialog_alert);
                            ColorFilter filter = new LightingColorFilter(Color.RED, Color.BLUE);
                            warning.setColorFilter(filter);
                            adb.setIcon(warning);
                            adb.setMessage("L'adresse mail " + mEmail.getText().toString() + " est déjà utilisée. \nMerci d'en choisir une autre.");
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
    private void verifEmailUser() {
        final FirebaseAuth auth = FirebaseAuth.getInstance(FirebaseFirestore.getInstance().getApp());
        // An ActionCodeSettings instance needs to be provided when sending a password reset email or a verification email.

        // In order to securely pass a continue URL, the domain for the URL will need to be whitelisted in the Firebase console.
        // This is done in the Authentication section by adding this domain to the list of OAuth redirect domains if it is not already there.
        ActionCodeSettings actionCodeSettings = ActionCodeSettings.newBuilder()
                .setUrl("https://taaroaa-fe93c.firebaseapp.com/__/auth/action?mode=%3Caction%3E&oobCode=%3Ccode%3E")
                .setHandleCodeInApp(true)
                //.setIOSBundleId("com.example.ios")
                .setAndroidPackageName(
                        "fr.drochon.christian.taaroaa",// The default for this is populated with the current android package name.
                        true,
                        "19") // minimum SDK
                .build();

        if (!Objects.requireNonNull(auth.getCurrentUser()).isEmailVerified()) {
            Objects.requireNonNull(auth.getCurrentUser()).sendEmailVerification(actionCodeSettings)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                //Toast.makeText(ConnectionActivity.this, "Verification email sent to " + Objects.requireNonNull(getCurrentUser()).getEmail(), Toast.LENGTH_LONG).show();
                                Log.d("TAG", "Verification Email sent.");
                                Intent intent = new Intent(ConnectionActivity.this, AccountCreateActivity.class)
                                        .putExtra("email", mEmail.getText().toString())
                                        .putExtra("password", mPassword.getText().toString());
                                startActivity(intent);
                            } else
                                System.out.println("nok");
                        }
                    });
        }
    }
}
