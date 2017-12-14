package com.tcs.speechtotextpoc.demo;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.tcs.speechtotextpoc.R;
import com.tcs.speechtotextpoc.translation_engine.TranslatorFactory;
import com.tcs.speechtotextpoc.translation_engine.utils.ConversionCallaback;
import com.tcs.speechtotextpoc.translation_engine.utils.TranslatorUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity implements ConversionCallaback {

    private static final int STT = 1;
    private static int CURRENT_MODE = -1;
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    private TextView sttOutput;
    private ImageButton speechToText;

    private EditText ip_input;
    private ImageButton ip_button;

    private Button on_button;
    private Button off_button;
    private ImageButton imageButton;
    private Button push_btn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        try {
            new HttpUtil().execute("server connect!");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (Build.VERSION.SDK_INT >= 23) {
            // Pain in A$$ Marshmallow+ Permission APIs
            requestForPermission();
        } else {
            // Pre-Marshmallow
            setUpView();
        }
    }

    /**
     * Set up listeners on View
     */
    private void setUpView() {

        //Speech to text output
        sttOutput = (TextView) findViewById(R.id.stt_output);
        //Button to trigger STT
        speechToText = (ImageButton) findViewById(R.id.start_listening);

        ip_input = (EditText) findViewById(R.id.ip_input);
        ip_input.setText("192.168.0.10:1234");

        on_button = (Button) findViewById(R.id.on_button);
        off_button = (Button) findViewById(R.id.off_button);
        imageButton = (ImageButton) findViewById(R.id.imageButton);
        push_btn = (Button) findViewById(R.id.push_btn);


        /*ip_button = (FloatingActionButton) findViewById(R.id.ip_button);

        ip_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HttpUtil.url = "http://" + ip_input.getText().toString();
                Toast.makeText(HomeActivity.this, "IP set : " + HttpUtil.url, Toast.LENGTH_SHORT).show();
            }
        });*/

        //Listen and convert convert speech to text
        speechToText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HttpUtil.url = "http://" + ip_input.getText().toString();
                Toast.makeText(HomeActivity.this, "IP set : " + HttpUtil.url, Toast.LENGTH_SHORT).show();

                Snackbar.make(view, "Listening", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                //Ask translator factory to start speech tpo text convertion
                //Hello There is optional
                TranslatorFactory.getInstance().
                        getTranslator(TranslatorFactory.TRANSLATOR_TYPE.SPEECH_TO_TEXT, HomeActivity.this).
                        initialize("Hello There", HomeActivity.this);

                CURRENT_MODE = STT;
            }
        });

        on_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new HttpUtil().execute("켜줘");
            }
        });

        off_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new HttpUtil().execute("꺼줘");
            }
        });

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new HttpUtil().execute("맑음");
            }
        });

        push_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NotificationManager notificationManager = (NotificationManager) HomeActivity.this.getSystemService(HomeActivity.this.NOTIFICATION_SERVICE);
                Intent intent1 = new Intent(HomeActivity.this.getApplicationContext(), HomeActivity.class); //인텐트 생성.

                Notification.Builder builder = new Notification.Builder(getApplicationContext());
                intent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(HomeActivity.this, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setSmallIcon(R.drawable.setting).setTicker("HETT").setWhen(System.currentTimeMillis())
                        .setNumber(1).setContentTitle("Message").setContentText("동서대학교 CK사업단")
                        .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE).setContentIntent(pendingNotificationIntent).setAutoCancel(true).setOngoing(true);

                notificationManager.notify(1, builder.build());
                new HttpUtil().execute("메세지");
            }
        });
    }


    /**
     * On success is common of both translator so if you had triggered Speech to text show converted text on textview
     *
     * @param result
     */
    @Override
    public void onSuccess(String result) {
        result = result.replaceAll(" ", "");
        try {
            new HttpUtil().execute(result);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Toast.makeText(this, "Result " + result, Toast.LENGTH_SHORT).show();

        switch (CURRENT_MODE) {
            case STT:
                sttOutput.setText(result);
        }
    }

    @Override
    public void onCompletion() {
        Toast.makeText(this, "Done ", Toast.LENGTH_SHORT).show();

    }

    /**
     * If translator failed error message will be come in this callback
     *
     * @param errorMessage
     */
    @Override
    public void onErrorOccured(String errorMessage) {
    }


    /**
     * Request Permission
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void requestForPermission() {
        List<String> permissionsNeeded = new ArrayList<String>();

        final List<String> permissionsList = new ArrayList<String>();

        if (!isPermissionGranted(permissionsList, Manifest.permission.RECORD_AUDIO))
            permissionsNeeded.add("Require for Speech to text");

        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {

                // Need Rationale
                String message = "You need to grant access to " + permissionsNeeded.get(0);

                for (int i = 1; i < permissionsNeeded.size(); i++) {
                    message = message + ", " + permissionsNeeded.get(i);
                }

                showMessageOKCancel(message,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                            }
                        });
                return;
            }
            requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            return;
        }

        //add listeners on view
        setUpView();
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(HomeActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean isPermissionGranted(List<String> permissionsList, String permission) {

        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);

            // Check for Rationale Option
            if (!shouldShowRequestPermissionRationale(permission))
                return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<String, Integer>();
                // Initial
                perms.put(Manifest.permission.RECORD_AUDIO, PackageManager.PERMISSION_GRANTED);

                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);

                // Check for ACCESS_FINE_LOCATION
                if (perms.get(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                        ) {
                    // All Permissions Granted

                    //add listeners on view
                    setUpView();

                } else {
                    // Permission Denied
                    Toast.makeText(HomeActivity.this, "Some Permissions are Denied Exiting App", Toast.LENGTH_SHORT)
                            .show();

                    finish();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
