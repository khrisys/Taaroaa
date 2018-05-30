package fr.drochon.christian.taaroaa.auth;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.base.BaseActivity;

public class ConnectionActivity extends BaseActivity {

    TextInputEditText mEmail;
    TextInputEditText mPassword;
    Button mValid;

    private static boolean isValidEmail(CharSequence target) {
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        mEmail = findViewById(R.id.email_input);
        mPassword = findViewById(R.id.password_input);
        mValid = findViewById(R.id.creation_identifiants_btn);

        configureToolbar();
        giveToolbarAName(R.string.creation_compte);

        // --------------------
        // LISTENER
        // --------------------

        mValid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mEmail.getText().toString().isEmpty() && !mPassword.getText().toString().isEmpty())
                    connectToFirebaseWithEmailAndPassword();
                else
                    verificationChampsVides();
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
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "createUserWithEmailIndba:success");
                        /*    Toast.makeText(AccountCreateActivity.this, "Registration email succeed.",
                                    Toast.LENGTH_SHORT).show();*/
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(ConnectionActivity.this, "Registration email failed or already in use by another account.",
                                    Toast.LENGTH_SHORT).show();
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
        String url = "https://taaroaa-fe93c.firebaseapp.com/verify?uid=" + auth.getCurrentUser();
        ActionCodeSettings actionCodeSettings = ActionCodeSettings.newBuilder()
                //.setUrl("https://taaroaa-fe93c.firebaseapp.com/__/auth/action?mode=%3Caction%3E&oobCode=%3Ccode%3E")
                //.setUrl(url)
                .setUrl("https://dhu3y.app.goo.gl/taaroaa")
                .setHandleCodeInApp(true)
                //.setIOSBundleId("com.example.ios")
                // The default for this is populated with the current android package name.
                .setAndroidPackageName(
                        "fr.drochon.christian.taaroaa",
                        true,
                        "19")
                .build();

        if (!Objects.requireNonNull(auth.getCurrentUser()).isEmailVerified()) {
            Objects.requireNonNull(auth.getCurrentUser()).sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(ConnectionActivity.this, "Verification email sent to " + Objects.requireNonNull(getCurrentUser()).getEmail(), Toast.LENGTH_LONG).show();
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
