package fr.drochon.christian.taaroaa.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.auth.MainActivity;
import fr.drochon.christian.taaroaa.base.BaseActivity;

/**
 * A login screen that offers login via email/password.
 */
public class PasswordActivity extends BaseActivity {

    private View mProgressView;
    private AutoCompleteTextView mEmailView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        configureToolbar();
        giveToolbarAName(R.string.password_name);

        mProgressView = findViewById(R.id.login_progress);
        mEmailView = findViewById(R.id.email_recup);


        // --------------------
        // LISTENER
        // --------------------

        Button mEmailSignInButton = findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                final FirebaseAuth auth = FirebaseAuth.getInstance();
                String emailAddress = mEmailView.getText().toString();

                // Test performance de recuperation de mot de passe
                final Trace myTrace = FirebasePerformance.getInstance().newTrace("passwordActivityResetEmail_trace");
                myTrace.start();

                if (verificationChampsVides()) {
                    auth.sendPasswordResetEmail(emailAddress)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d("TAG", "Email sent.");
                                        mProgressView.setVisibility(View.VISIBLE);
                                        Toast.makeText(PasswordActivity.this, "Réinitialisation de mot de passe à envoyé à l'adresse mail : '"
                                                + mEmailView.getText().toString() + "'.", Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(PasswordActivity.this, MainActivity.class);
                                        startActivity(intent);

                                        myTrace.stop();
                                    }
                                }
                            });
                }
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
    private boolean verificationChampsVides() {

        if (mEmailView.getText().toString().isEmpty()) {
            mEmailView.setError("Merci de saisir ce champ !");
            mEmailView.requestFocus();
            return false;
        }
        if (!isValidEmail(mEmailView.getText().toString())) {
            mEmailView.setError("Adresse email non valide !");
            mEmailView.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * Verification de la validité de l'adresse email
     *
     * @param target adresse email
     * @return validité ou non de l'adresse email recupérée
     */
    private static boolean isValidEmail(CharSequence target) {
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
}

