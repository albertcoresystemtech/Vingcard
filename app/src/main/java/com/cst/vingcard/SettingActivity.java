package com.cst.vingcard;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cst.vingcard.database.RoomCaller;
import com.cst.vingcard.entity.Setup;

import java.util.Date;
import java.util.Objects;

public class SettingActivity extends AppCompatActivity {

    public EditText textHostUrl, textToken;
    public Button buttonSave;
    public boolean unchangedHost = false;
    public boolean unchangedToken = false;
    //Default
    public String hostBuild = "https://visionline-web-api-build.testpigeon.net";
    public String tokenBuild = "xJSPkPs8ujrW1EBW7AtI1Z5b8DMvWl3dAPhmMDEd";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        @SuppressLint("InflateParams") View viewActionBar = getLayoutInflater().inflate(R.layout.action_bar_layout, null);
        ActionBar.LayoutParams params = new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);
        TextView actionBarTitle = viewActionBar.findViewById(R.id.title_action_bar);
        actionBarTitle.setText(getString(R.string.app_setting));
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(viewActionBar, params);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        textHostUrl = findViewById(R.id.text_hostUrl);
        textToken = findViewById(R.id.text_token);
        buttonSave = findViewById(R.id.button_save);

        RoomCaller dbCaller = new RoomCaller(SettingActivity.this);
        Setup setup = dbCaller.getLatestSetup();
        String hostUrlStr = "";
        String tokenStr = "";

        try {
            hostUrlStr = setup.getHostUrl();
            tokenStr = setup.getToken();
        } catch (Exception e) {
            //empty
        }

        if (hostUrlStr.isEmpty()) {
            textHostUrl.setText(hostBuild);
            textToken.setText(tokenBuild);
        } else if (tokenStr.isEmpty()) {
            textToken.setText(tokenBuild);
        } else {
            //have previous data
            hostUrlStr = setup.getHostUrl();
            tokenStr = setup.getToken();
            textHostUrl.setText(hostUrlStr);
            textToken.setText(tokenStr);
            buttonSave.setText("UPDATE");
        }

        buttonSave.setOnClickListener(v -> saveData());

    }

    private void saveData() {
        String currentHostUrl =String.valueOf(textHostUrl.getText());
        String currentToken = String.valueOf(textToken.getText());

        if (currentHostUrl.isEmpty()|| currentToken.isEmpty()){
            //empty
            SettingActivity.showMessage(SettingActivity.this, "Empty Input", "Oppps....\n Please insert correct credential.");
            return;

        } else if (currentHostUrl.equals(hostBuild)) {
            unchangedHost = true;
        } else if (currentToken.equals(tokenBuild)) {
            unchangedToken =true;
        }else {
            unchangedHost = false;
            unchangedToken = false;
        }

        boolean isEqual = (unchangedHost == unchangedToken);

        if (isEqual) {
            //No change
            SettingActivity.showMessage(SettingActivity.this, "Save Data", "Skipped!");

            Intent intent = new Intent(SettingActivity.this, DoorActivity.class);
            startActivity(intent);
        } else {
            //Data change
            String timestamp = MainActivity.databaseDateTimeFormat.format(new Date());
            final RoomCaller dbCaller = new RoomCaller(this);

            Setup setup = new Setup();
            setup.setHostUrl(currentHostUrl);
            setup.setToken(currentToken);
            setup.setCreatedAt(timestamp);
            dbCaller.insertSetup(setup);

            SettingActivity.showMessage(SettingActivity.this, "Save Data", "Saved!");

            Intent intent = new Intent(SettingActivity.this, DoorActivity.class);
            startActivity(intent);
        }


    }

    public static void showMessage(Context context, String title, String message) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.pop_up_message_no_option, null);

        TextView textTitle = dialogView.findViewById(R.id.text_title_pop_up_message_no_option);
        TextView textMessage = dialogView.findViewById(R.id.text_message_pop_up_message_no_option);
        Button buttonOk = dialogView.findViewById(R.id.button_ok_pop_up_message_no_option);

        textTitle.setText(title);
        textMessage.setText(message);

        dialogBuilder.setCancelable(false);
        dialogBuilder.setView(dialogView);

        final AlertDialog alertDialogMessage = dialogBuilder.create();
        alertDialogMessage.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialogMessage.show();

        buttonOk.setOnClickListener(v -> alertDialogMessage.dismiss());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_setting, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.button_return_door) {
            Intent intent = new Intent(SettingActivity.this, DoorActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        super.onResume();

        // Fetching the stored data
        // from the SharedPreference
        SharedPreferences sh = getSharedPreferences("MySharedPref", MODE_PRIVATE);

        String hostStr = sh.getString("textHostUrl", "");
        String tokenStr = sh.getString("textToken", "");

        // Setting the fetched data
        // in the EditTexts
        //textHostUrl.setText(hostStr);
        //textToken.setText(tokenStr);
    }

    // Store the data in the SharedPreference
    // in the onPause() method
    // When the user closes the application
    // onPause() will be called
    // and data will be stored
    @Override
    protected void onPause() {
        super.onPause();

        // Creating a shared pref object
        // with a file name "MySharedPref"
        // in private mode
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();

        // write all the data entered by the user in SharedPreference and apply
        //myEdit.putString("textHostUrl", textHostUrl.getText().toString());
        //myEdit.putString("textToken", textToken.getText().toString());
        //myEdit.apply();


    }


}