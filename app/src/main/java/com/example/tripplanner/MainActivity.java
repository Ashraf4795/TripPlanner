package com.example.tripplanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.Navigation;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;


import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.example.tripplanner.core.firestoredb.FirestoreConnection;
import com.example.tripplanner.core.model.User;
import com.example.tripplanner.homescreen.homeview.CurrentTripsHomeFragmentDirections;
import com.example.tripplanner.pasttrips.PastTripsFragmentDirections;
import com.example.tripplanner.reminder.AlarmReciever;
import com.example.tripplanner.reminder.ForegroundService;
import com.example.tripplanner.reminder.NotificationHelper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class MainActivity extends AppCompatActivity {

    private final static int ID_HOME=2;
    private final static int ID_HISTORY=1;
    private final static int ID_PROFILE=3;
    public static int currentTab = ID_HOME;


    FirebaseUser currentUser;
    private static final String TAG = "MainActivity";
    FirestoreConnection firestoreConnection;
    MeowBottomNavigation buttomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*Reham*/
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
        googleSignInClient.signOut().addOnCompleteListener(this,new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
            }
        });
        FirebaseAuth.getInstance().signOut();
        /*Reham*/

        /*Manar*/
        //check user
        buttomNavigation = findViewById(R.id.buttom_nav);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser == null){
            buttomNavigation.setVisibility(View.GONE);
            Navigation.findNavController(this,R.id.fragments_functionality_layout).navigate(CurrentTripsHomeFragmentDirections.actionCurrentTripsHomeFragmentToLoginFragment());
        }
        else{
            User user = new User(currentUser.getUid());
            initBar();
            buttomNavigation.setVisibility(View.VISIBLE);
            firestoreConnection = FirestoreConnection.getInstance(user);
        }
        /*Manar*/

    }
    /*Manar*/
    private void initBar(){
        //temp
        //setting nav buttom
        buttomNavigation.add(new MeowBottomNavigation.Model(ID_HISTORY,R.drawable.ic_history_black_24dp));
        buttomNavigation.add(new MeowBottomNavigation.Model(ID_HOME,R.drawable.ic_home_black_24dp));
        buttomNavigation.add(new MeowBottomNavigation.Model(ID_PROFILE,R.drawable.ic_account_circle_black_24dp));
        buttomNavigation.setVisibility(View.INVISIBLE);
        buttomNavigation.show(ID_HOME,true);
        buttomNavigation.setOnShowListener(new Function1<MeowBottomNavigation.Model, Unit>() {
            @Override
            public Unit invoke(MeowBottomNavigation.Model model) {
                switch (model.getId()){
                    case ID_HOME:
                        if(currentTab==ID_HOME+1){
                            //go from profile
                        }
                        else if(currentTab==ID_HOME-1){
                            //go from history
                            Navigation.findNavController(MainActivity.this,R.id.fragments_functionality_layout).navigate(PastTripsFragmentDirections.actionPastTripsFragmentToCurrentTripsHomeFragment());
                        }
                        currentTab = ID_HOME;
                        break;
                    case ID_PROFILE:
                        if(currentTab==ID_PROFILE-1){
                            //go from home
                            Navigation.findNavController(MainActivity.this,R.id.fragments_functionality_layout).navigate(CurrentTripsHomeFragmentDirections.actionCurrentTripsHomeFragmentToReminderFragmnt());
                        }
                        else if(currentTab == ID_PROFILE-2){
                            //go from history
                        }
                        currentTab = ID_PROFILE;
                        break;
                    case ID_HISTORY:
                        if(currentTab==ID_HISTORY+1){
                            //go from home
                            Navigation.findNavController(MainActivity.this,R.id.fragments_functionality_layout).navigate(CurrentTripsHomeFragmentDirections.actionCurrentTripsHomeFragmentToPastTripsFragment());
                        }
                        else if(currentTab==ID_HISTORY+2){
                            //go from profile
                        }
                        currentTab = ID_HISTORY;
                        break;
                }
                return null;
            }
        });
    }
    /*Manar*/
}