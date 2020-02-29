package com.example.tripplanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;


import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.example.tripplanner.core.firestoredb.FirestoreConnection;
import com.example.tripplanner.core.model.User;
import com.example.tripplanner.homescreen.homeview.CurrentTripsHomeFragmentDirections;
import com.example.tripplanner.reminder.AlarmReciever;
import com.example.tripplanner.reminder.ForegroundService;
import com.example.tripplanner.reminder.NotificationHelper;
import com.example.tripplanner.tripdetail.TripDetailsFragment;
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
    NotificationHelper notificationHelper;
    public static final String channel1ID = "Channel1ID";
    public static final String channel1Name = "Channel1Name";
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    public static final int ALARM_REQUEST_CODE=101;


    FirebaseUser currentUser;
    private static final String TAG = "MainActivity";
    FirestoreConnection firestoreConnection;
    MeowBottomNavigation buttomNavigation;


    //   public static User me = new User("1","Manar","manara@gmail.com","123");
    //todo >> array list may have duplicated document names inside it.. to be fix

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //temp

        /*GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
        googleSignInClient.signOut().addOnCompleteListener(this,new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
            }
        });
        FirebaseAuth.getInstance().signOut();*/
        //check user
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser == null){
            Navigation.findNavController(this,R.id.fragments_functionality_layout).navigate(CurrentTripsHomeFragmentDirections.actionCurrentTripsHomeFragmentToLoginFragment());
        }
        else{
            User user = new User(currentUser.getUid());
            initBar();
            buttomNavigation.setVisibility(View.VISIBLE);
            firestoreConnection = FirestoreConnection.getInstance(user);
        }


        /*findViewById(R.id.clickme).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseFirestore = FirebaseFirestore.getInstance();
                firestoreConnection = FirestoreConnection.getInstance(me);
                firestoreConnection.addTrip(new Trip(MainActivity.this.title.getText().toString(), MainActivity.this.desc.getText().toString(),"giza","haram"));
            }
        });*/
    }
    private void initBar(){
        //temp
        //setting nav buttom
        buttomNavigation = findViewById(R.id.buttom_nav);
        buttomNavigation.add(new MeowBottomNavigation.Model(ID_HISTORY,R.drawable.ic_history_black_24dp));
        buttomNavigation.add(new MeowBottomNavigation.Model(ID_HOME,R.drawable.ic_home_black_24dp));
        buttomNavigation.add(new MeowBottomNavigation.Model(ID_PROFILE,R.drawable.ic_account_circle_black_24dp));
        buttomNavigation.setVisibility(View.INVISIBLE);
        buttomNavigation.show(ID_HOME,true);
        buttomNavigation.setOnShowListener(new Function1<MeowBottomNavigation.Model, Unit>() {
            @Override
            public Unit invoke(MeowBottomNavigation.Model model) {
                switch (model.getId()){
                    /*case ID_HOME:
                        Navigation.findNavController(MainActivity.this,R.id.fragments_functionality_layout).navigate(CurrentTripsHomeFragmentDirections.toSelf());
                        break;*/
                    case ID_PROFILE:
                        Navigation.findNavController(MainActivity.this,R.id.fragments_functionality_layout).navigate(CurrentTripsHomeFragmentDirections.actionCurrentTripsHomeFragmentToReminderFragmnt());
                        break;
                    case ID_HISTORY:
                        Navigation.findNavController(MainActivity.this,R.id.fragments_functionality_layout).navigate(CurrentTripsHomeFragmentDirections.actionCurrentTripsHomeFragmentToPastTripsFragment());
                        break;
                }
                return null;
            }
        });
    }
    private void stopService() {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        stopService(serviceIntent);
    }

    private void startService() {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        serviceIntent.putExtra("inputExtra", "Foreground Service Example in Android");
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    private void sendOnChannel(String title, String message) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            notificationHelper.createChannel(channel1Name,channel1ID);
        }
        NotificationCompat.Builder noticitaion2 = notificationHelper.buildNotification(channel1ID,title,message);
        notificationHelper.getNotificationManager().notify(1,noticitaion2.build());
    }

    public void normalNotificationToChannel(View view) {
        //sendOnChannel(title.getText().toString(),desc.getText().toString());
    }


   /* @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND,0);
        updateTime(calendar);
        startAlarm(calendar);
    }*/


    // alarm manager >> Background service >> alarm Intent
    private void startAlarm(Calendar calendar) {
        alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReciever.class);
        alarmIntent = PendingIntent.getBroadcast(this, ALARM_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmMgr.setExact(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),alarmIntent);
        }
    }
}