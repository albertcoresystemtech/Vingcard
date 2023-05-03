package com.cst.vingcard;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.cst.vingcard.api.RetrofitAPICollection;
import com.cst.vingcard.api.RetrofitClient;
import com.cst.vingcard.database.RoomCaller;
import com.cst.vingcard.entity.Setup;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DoorActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    public String hostUrlBackup = "https://visionline-web-api-build.testpigeon.net";
    public String clientSecretBackup = "xJSPkPs8ujrW1EBW7AtI1Z5b8DMvWl3dAPhmMDEd"; //token
    public String hostUrl ;
    public String clientSecret ;

    private List<String> hotelDoorDetails = new ArrayList<String>();
    private List<String> hotelSingleDoorDetails = new ArrayList<String>();

    private EditText textRoomNumber,  textDoorGroup;
    private TextView textResult;
    private Button btnListDoor, btnShowDoor, btnSubmitInfo, btnSubmitShowInfo;
    private Spinner spinnerDoorDetails, spinnerDoorGroup;
    private LinearLayout layoutRoomNumber, layoutDoorDetailInput, layoutDoorGroup;
    private static final String[] spinnerDoorDetailsStr= {"True", "False"};
    private static final String[] spinnerDoorGroupsStr= {"GuestRooms", "Other"};
    private String doorDetailsOption= "";
    private String doorGroupsOption= "";
    //private String doorGroupsOption= "GuestRooms";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_door);

        @SuppressLint("InflateParams") View viewActionBar = getLayoutInflater().inflate(R.layout.action_bar_layout, null);
        ActionBar.LayoutParams params = new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);
        TextView actionBarTitle = viewActionBar.findViewById(R.id.title_action_bar);
        actionBarTitle.setText(getString(R.string.app_door_name));
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(viewActionBar, params);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        textRoomNumber = findViewById(R.id.input_room_number);
        textDoorGroup = findViewById(R.id.input_door_groups);
        textResult = findViewById(R.id.text_result);
        btnListDoor = findViewById(R.id.btn_list_door);
        btnShowDoor = findViewById(R.id.btn_show_door);
        btnSubmitInfo = findViewById(R.id.btn_submit_info);
        btnSubmitShowInfo = findViewById(R.id.btn_submit_show_info);
        layoutRoomNumber = findViewById(R.id.layout_room_number);
        layoutDoorDetailInput = findViewById(R.id.layout_door_detail_input);
        layoutDoorGroup = findViewById(R.id.layout_doorGroup);


        RoomCaller dbCaller = new RoomCaller(DoorActivity.this);
        Setup setup = dbCaller.getLatestSetup();

        String hostUrlStr = setup.getHostUrl();
        String tokenStr = setup.getToken();

        if (!hostUrlStr.isEmpty() && !tokenStr.isEmpty()) {

            hostUrl = hostUrlStr;
            clientSecret = tokenStr;

            //get Room number list
            //getRoomNumberList();

        } else {
            //empty data
            //hostUrl = hostUrlBackup;
            //clientSecret = clientSecretBackup;

            //or vice versa
            MainActivity.showMessage(DoorActivity.this, "Empty Data", "Oppps....\n Please insert correct credential.");

        }

        spinnerDoorDetails = findViewById(R.id.spinner_door_details);
        ArrayAdapter<String> adapterDoorDetails = new ArrayAdapter<String>(DoorActivity.this,
                android.R.layout.simple_spinner_item,spinnerDoorDetailsStr);

        adapterDoorDetails.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDoorDetails.setAdapter(adapterDoorDetails);
        spinnerDoorDetails.setOnItemSelectedListener(this);

        spinnerDoorGroup = findViewById(R.id.spinner_door_groups);
        ArrayAdapter<String> adapterDoorGroups = new ArrayAdapter<String>(DoorActivity.this,
                android.R.layout.simple_spinner_item,spinnerDoorGroupsStr);

        adapterDoorGroups.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDoorGroup.setAdapter(adapterDoorGroups);
        spinnerDoorGroup.setOnItemSelectedListener(this);

        //Menu show list
        btnListDoor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnListDoor.setBackground(ContextCompat.getDrawable(DoorActivity.this, R.drawable.btn_selected_show_bg));
                btnShowDoor.setBackground(ContextCompat.getDrawable(DoorActivity.this, R.drawable.btn_not_selected_show_bg));
                btnSubmitInfo.setVisibility(View.VISIBLE);
                btnSubmitShowInfo.setVisibility(View.GONE);
                layoutRoomNumber.setVisibility(View.GONE);
                layoutDoorDetailInput.setVisibility(View.VISIBLE);
            }
        });

        //Menu Single room number
        btnShowDoor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnShowDoor.setBackground(ContextCompat.getDrawable(DoorActivity.this, R.drawable.btn_selected_show_bg));
                btnListDoor.setBackground(ContextCompat.getDrawable(DoorActivity.this, R.drawable.btn_not_selected_show_bg));
                btnSubmitInfo.setVisibility(View.GONE);
                btnSubmitShowInfo.setVisibility(View.VISIBLE);
                layoutRoomNumber.setVisibility(View.VISIBLE);
                layoutDoorDetailInput.setVisibility(View.GONE);
            }
        });

        btnSubmitInfo.setOnClickListener(v -> checkDoorGroupInput());
        //Button check all door list
        //btnSubmitInfo.setOnClickListener(v -> getListDoor(doorDetailsOption));

        //Button check single door list
        btnSubmitShowInfo.setOnClickListener(v -> getSingleDoor());



    }

    private void checkDoorGroupInput() {
        String textDoorGroupStr = String.valueOf(textDoorGroup.getText());

        if (textDoorGroupStr.isEmpty()) {
            //empty means no need the api query
            if (doorDetailsOption.compareTo("true")==0) {
                //detail == TRUE, doorGroup = empty
                doorGroupsOption = textDoorGroup.getText().toString();
                //Button check all door list filtered by doorGroup
                btnSubmitInfo.setOnClickListener(v -> getListDoor(doorDetailsOption, doorGroupsOption));
            } else {
                //detail == FALSE, doorGroup = empty
                //Button check all door list
                btnSubmitInfo.setOnClickListener(v -> getListDoor());
            }

        } else {
            //textDoorGroupStr.isNOTEmpty()
            //have api query
            //Button check all door list
            doorGroupsOption = textDoorGroup.getText().toString();
            btnSubmitInfo.setOnClickListener(v -> getListDoor(doorDetailsOption, doorGroupsOption));
        }

    }

    private void getSingleDoor() {

        String roonNumber = String.valueOf(textRoomNumber.getText());

        if (textRoomNumber != null) {
            //
            getSingleDoorFromApi(roonNumber);

        } else {
            //room
            DoorActivity.showMessage(DoorActivity.this,  "Incorrect room number", "Room Number Missing");
        }


    }

    private void getSingleDoorFromApi(String roomNumberStr) {

        final AlertDialog alertDialogFetchApiResponse = showLoading(DoorActivity.this, "Fetching Information");
        RetrofitAPICollection service = RetrofitClient.getRetrofitClient(hostUrl).create(RetrofitAPICollection.class);
        Call<String> callDoorList = service.getSingleDoor("Bearer " + clientSecret, roomNumberStr);
        callDoorList.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                alertDialogFetchApiResponse.dismiss();
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        JSONObject responseJSON = new JSONObject(response.body());
                        JSONObject dataJSON = responseJSON.getJSONObject("data");
                        ArrayList<String> doorInfoArray = new ArrayList<>();

                        if (dataJSON.length() == 0) {
                            //throw error
                            DoorActivity.showMessage(DoorActivity.this, "Empty!", "The door info is empty. Please contact support.");
                            return;
                        }

                        hotelSingleDoorDetails = new ArrayList<String>();
                        //method to get multiple Card ID from API Response
                        String doorCategory = dataJSON.getString("doorCategory");
                        String doorID = dataJSON.getString("doorID");
                        String doorName = dataJSON.getString("doorName");
                        String localName = dataJSON.getString("localName");
                        String separator = "\n";
                        hotelSingleDoorDetails.add("DoorCategory:" + doorCategory);
                        hotelSingleDoorDetails.add(separator);
                        hotelSingleDoorDetails.add("DoorId:" + doorID);
                        hotelSingleDoorDetails.add(separator);
                        hotelSingleDoorDetails.add("DoorName:" + doorName);
                        hotelSingleDoorDetails.add(separator);
                        hotelSingleDoorDetails.add("LocalName:" + localName);
                        hotelSingleDoorDetails.add(separator);

                        DoorActivity.showMessageScrollable(DoorActivity.this, "Door Detail", hotelSingleDoorDetails
                                + "\n" + "\n" );


                    } else {

                        String title = "Oops!";
                        String error = "";

                        JSONObject responseJSON = new JSONObject(response.errorBody().string());
                        if (responseJSON.has("data")) {
                            JSONObject dataObject = responseJSON.getJSONObject("data");
                            if (dataObject.has("message") && !dataObject.isNull("message")) {
                                error = dataObject.getString("message");
                            }
                        }

                        if (error.trim().isEmpty()) {
                            error = "Something went wrong. Please try again.";

                            try {
                                error = "Something went wrong.\nServer response: " + response.code() + "\n" + response.message();
                            } catch (Exception ignored) {
                            }
                        } else {
                            error = error + "\nServer response: " + response.code();
                        }

                        //textCardStatus.setText("- - -");
                        MainActivity.showMessage(DoorActivity.this, title, error);
                    }
                } catch (Exception e) {
                    MainActivity.showMessage(DoorActivity.this, "Oops!", "Fail to get response. Please try again.");
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                alertDialogFetchApiResponse.dismiss();
                MainActivity.showMessage(DoorActivity.this, "Oops!", "Server response is unavailable. Please ensure the Terminal are correctly setup and  connected to internet.");
            }
        });

    }

    private void getListDoor(String doorDetailsOption, String doorGroupsOptionStr) {

        doorGroupsOption = textDoorGroup.getText().toString();
        final AlertDialog alertDialogFetchApiResponse = showLoading(DoorActivity.this, "Fetching Information");

        if (doorDetailsOption.compareTo("true")==0) {

            RetrofitAPICollection service = RetrofitClient.getRetrofitClient(hostUrl).create(RetrofitAPICollection.class);
            Call<String> callDoorList = service.getAllDoor("Bearer " + clientSecret, doorDetailsOption, doorGroupsOption);
            callDoorList.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    alertDialogFetchApiResponse.dismiss();
                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            JSONObject responseJSON = new JSONObject(response.body());
                            JSONArray dataJSON = responseJSON.getJSONArray("data");
                            ArrayList<String> doorInfoArray = new ArrayList<>();

                            if (dataJSON.length() == 0) {
                                //throw error
                                DoorActivity.showMessage(DoorActivity.this, "Empty!", "The door info is empty. Please contact support.");
                                return;
                            }

                            hotelDoorDetails = new ArrayList<String>();
                            //method to get multiple Card ID from API Response
                            for (int i =0; i < dataJSON.length(); i++) {
                                JSONObject jsonObject = dataJSON.getJSONObject(i);
                                String doorCategory = jsonObject.getString("doorCategory");
                                String doorGroup= jsonObject.getString("doorGroup");
                                String doorID = jsonObject.getString("doorID");
                                String doorName = jsonObject.getString("doorName");
                                String localName = jsonObject.getString("localName");
                                String separator = "\n";
                                hotelDoorDetails.add("DoorCategory:" + doorCategory);
                                hotelDoorDetails.add("DoorGroup:" + doorGroup);
                                hotelDoorDetails.add("DoorId:" + doorID);
                                hotelDoorDetails.add("DoorName:" + doorName);
                                hotelDoorDetails.add("LocalName:" + localName);
                                hotelDoorDetails.add(separator);
                                hotelDoorDetails.add(separator);

                            }

                            DoorActivity.showMessageScrollable(DoorActivity.this, "Door Info", hotelDoorDetails
                                    + "\n" + "\n" );


                        } else {

                            String title = "Oops!";
                            String error = "";

                            JSONObject responseJSON = new JSONObject(response.errorBody().string());
                            if (responseJSON.has("data")) {
                                JSONObject dataObject = responseJSON.getJSONObject("data");
                                if (dataObject.has("message") && !dataObject.isNull("message")) {
                                    error = dataObject.getString("message");
                                }
                            }

                            if (error.trim().isEmpty()) {
                                error = "Something went wrong. Please try again.";

                                try {
                                    error = "Something went wrong.\nServer response: " + response.code() + "\n" + response.message();
                                } catch (Exception ignored) {
                                }
                            } else {
                                error = error + "\nServer response: " + response.code();
                            }

                            //textCardStatus.setText("- - -");
                            MainActivity.showMessage(DoorActivity.this, title, error);
                        }
                    } catch (Exception e) {
                        MainActivity.showMessage(DoorActivity.this, "Oops!", "Fail to get response. Please try again.");
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    alertDialogFetchApiResponse.dismiss();
                    MainActivity.showMessage(DoorActivity.this, "Oops!", "Server response is unavailable. Please ensure the KIOSK is connected to internet.");
                }
            });

        } else {

            //doorDetailsOption == false
            RetrofitAPICollection service = RetrofitClient.getRetrofitClient(hostUrl).create(RetrofitAPICollection.class);
            Call<String> callDoorList = service.getAllDoorNoDoorGroup("Bearer " + clientSecret, doorDetailsOption);
            callDoorList.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    alertDialogFetchApiResponse.dismiss();
                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            JSONObject responseJSON = new JSONObject(response.body());
                            JSONArray dataJSON = responseJSON.getJSONArray("data");
                            ArrayList<String> doorInfoArray = new ArrayList<>();

                            if (dataJSON.length() == 0) {
                                //throw error
                                DoorActivity.showMessage(DoorActivity.this, "Empty!", "The door info is empty. Please contact support.");
                                return;
                            }

                            hotelDoorDetails = new ArrayList<String>();
                            //method to get multiple Card ID from API Response
                            for (int i =0; i < dataJSON.length(); i++) {
                                JSONObject jsonObject = dataJSON.getJSONObject(i);
                                String doorId = jsonObject.getString("id");
                                String separator = "\n";
                                hotelDoorDetails.add("Door ID:" + doorId);
                                hotelDoorDetails.add(separator);
                                hotelDoorDetails.add(separator);

                            }

                            DoorActivity.showMessageScrollable(DoorActivity.this, "Door Info", hotelDoorDetails
                                    + "\n" + "\n" );


                        } else {

                            String title = "Oops!";
                            String error = "";

                            JSONObject responseJSON = new JSONObject(response.errorBody().string());
                            if (responseJSON.has("data")) {
                                JSONObject dataObject = responseJSON.getJSONObject("data");
                                if (dataObject.has("message") && !dataObject.isNull("message")) {
                                    error = dataObject.getString("message");
                                }
                            }

                            if (error.trim().isEmpty()) {
                                error = "Something went wrong. Please try again.";

                                try {
                                    error = "Something went wrong.\nServer response: " + response.code() + "\n" + response.message();
                                } catch (Exception ignored) {
                                }
                            } else {
                                error = error + "\nServer response: " + response.code();
                            }

                            //textCardStatus.setText("- - -");
                            MainActivity.showMessage(DoorActivity.this, title, error);
                        }
                    } catch (Exception e) {
                        MainActivity.showMessage(DoorActivity.this, "Oops!", "Fail to get response. Please try again.");
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    alertDialogFetchApiResponse.dismiss();
                    MainActivity.showMessage(DoorActivity.this, "Oops!", "Server response is unavailable. Please ensure the KIOSK is connected to internet.");
                }
            });
        }


    }

    private void getListDoor() {

        final AlertDialog alertDialogFetchApiResponse = showLoading(DoorActivity.this, "Fetching Information");

        doorDetailsOption ="false";

            RetrofitAPICollection service = RetrofitClient.getRetrofitClient(hostUrl).create(RetrofitAPICollection.class);
            Call<String> callDoorList = service.getAllDoorNoDoorGroup("Bearer " + clientSecret, doorDetailsOption);
            callDoorList.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    alertDialogFetchApiResponse.dismiss();
                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            JSONObject responseJSON = new JSONObject(response.body());
                            JSONArray dataJSON = responseJSON.getJSONArray("data");
                            ArrayList<String> doorInfoArray = new ArrayList<>();

                            if (dataJSON.length() == 0) {
                                //throw error
                                DoorActivity.showMessage(DoorActivity.this, "Empty!", "The door info is empty. Please contact support.");
                                return;
                            }

                            hotelDoorDetails = new ArrayList<String>();
                            //method to get multiple Card ID from API Response
                            for (int i =0; i < dataJSON.length(); i++) {
                                JSONObject jsonObject = dataJSON.getJSONObject(i);
                                String doorID = jsonObject.getString("id");
                                String separator = "\n";
                                hotelDoorDetails.add("DoorId:" + doorID);
                                hotelDoorDetails.add(separator);

                            }

                            DoorActivity.showMessageScrollable(DoorActivity.this, "Door Info", hotelDoorDetails
                                    + "\n" + "\n" );


                        } else {

                            String title = "Oops!";
                            String error = "";

                            JSONObject responseJSON = new JSONObject(response.errorBody().string());
                            if (responseJSON.has("data")) {
                                JSONObject dataObject = responseJSON.getJSONObject("data");
                                if (dataObject.has("message") && !dataObject.isNull("message")) {
                                    error = dataObject.getString("message");
                                }
                            }

                            if (error.trim().isEmpty()) {
                                error = "Something went wrong. Please try again.";

                                try {
                                    error = "Something went wrong.\nServer response: " + response.code() + "\n" + response.message();
                                } catch (Exception ignored) {
                                }
                            } else {
                                error = error + "\nServer response: " + response.code();
                            }

                            //textCardStatus.setText("- - -");
                            MainActivity.showMessage(DoorActivity.this, title, error);
                        }
                    } catch (Exception e) {
                        MainActivity.showMessage(DoorActivity.this, "Oops!", "Fail to get response. Please try again.");
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    alertDialogFetchApiResponse.dismiss();
                    MainActivity.showMessage(DoorActivity.this, "Oops!", "Server response is unavailable. Please ensure the KIOSK is connected to internet.");
                }
            });




    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        switch (position) {
            case 0:
                //Toast.makeText(this, "YOUR SELECTION IS : " + parent.getItemAtPosition(position).toString(), Toast.LENGTH_SHORT).show();
                btnSubmitInfo.setEnabled(true);
                doorDetailsOption = "true";
                layoutDoorGroup.setVisibility(View.VISIBLE);
                break;
            case 1:
                //Toast.makeText(this, "YOUR SELECTION IS : " + parent.getItemAtPosition(position).toString(), Toast.LENGTH_SHORT).show();
                btnSubmitInfo.setEnabled(true);
                doorDetailsOption = "false";
                layoutDoorGroup.setVisibility(View.GONE);
                break;

        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // TODO Auto-generated method stub
    }

    public static AlertDialog showLoading(Context context, String title) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.pop_up_loading, null);

        TextView textTitle = dialogView.findViewById(R.id.text_title_pop_up_loading);
        TextView textMessage = dialogView.findViewById(R.id.text_message_pop_up_loading);

        textTitle.setText(title);
        textMessage.setText("Please wait...");

        dialogBuilder.setCancelable(false);
        dialogBuilder.setView(dialogView);

        final AlertDialog alertDialogLoading = dialogBuilder.create();
        alertDialogLoading.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialogLoading.show();

        return alertDialogLoading;
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

    public static void showMessageScrollable(Context context, String title, String message) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.pop_up_message_no_option_scrollable, null);

        TextView textTitle = dialogView.findViewById(R.id.text_title_pop_up_message_no_option_scrollable);
        TextView textMessage = dialogView.findViewById(R.id.text_message_pop_up_message_no_option_scrollable);
        Button buttonOk = dialogView.findViewById(R.id.button_ok_pop_up_message_no_option_scrollable);

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
        getMenuInflater().inflate(R.menu.menu_door, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.button_return_menu) {
            Intent intent = new Intent(DoorActivity.this, MainActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }



}