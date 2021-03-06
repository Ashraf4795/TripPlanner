package com.example.tripplanner.login;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.tripplanner.R;
import com.example.tripplanner.core.firestoredb.FirestoreConnection;
import com.example.tripplanner.core.model.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.navigation.Navigation;

public class LoginViewModel extends ViewModel {

    private final String TAG = "LoginViewModel";
    private FirestoreConnection firestoreConnection;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    public void loginWithGoogle(Intent data, Activity activity){
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());

            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
            firebaseAuth.signInWithCredential(credential)
                    .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                FirebaseUser userFirebase = firebaseAuth.getCurrentUser();
                                User user = new User(userFirebase.getUid());
                                //navigate to main fragment
                                firestoreConnection = FirestoreConnection.getInstance(user);
                                Navigation.findNavController(activity, R.id.fragments_functionality_layout).navigate(LoginFragmentDirections.actionLoginFragmentToCurrentTripsHomeFragment());

                                Log.i(TAG, "From onactres: " + userFirebase.getEmail());

                            } else {
                                Log.i(TAG, "signInWithCredential:failure, " + task.getException().getCause());
                            }

                        }
                    });

        }catch (ApiException e){
            Log.w(TAG, "Google sign in failed", e);
        }

    }

    public void loginWithEmailAndPassword(String email, String password, Activity activity, ProgressBar progressBar){

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {

                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser userFirebase = firebaseAuth.getCurrentUser();
                            User user = new User(userFirebase.getUid());
                            //navigate to main fragment
                            firestoreConnection = FirestoreConnection.getInstance(user);
                            Navigation.findNavController(activity, R.id.fragments_functionality_layout).navigate(LoginFragmentDirections.actionLoginFragmentToCurrentTripsHomeFragment());
                            Toast.makeText(activity,"Welcome "+email,Toast.LENGTH_LONG).show();
                            Log.i(TAG, userFirebase.getEmail());

                        } else {
                            //Toast.makeText(activity, "Email or Password is incorrect!", Toast.LENGTH_LONG).show();
                            Log.i("MESAG", "****************" + task.getException().getMessage());
                            //
                            if(task.getException().getMessage().equals("The password is invalid or the user does not have a password"))
                                Toast.makeText(activity, "Invalid password!", Toast.LENGTH_LONG).show();
                            else if(task.getException().getMessage().equals("The email address is badly formatted"))
                                Toast.makeText(activity, "Wrong Email Format!", Toast.LENGTH_LONG).show();
                            else if(task.getException().getMessage().equals("There is no user record corresponding to this identifier. The user may have been deleted"))
                                Toast.makeText(activity, "Email Not Exist!", Toast.LENGTH_LONG).show();

                        }
                    }
                });
    }
}
