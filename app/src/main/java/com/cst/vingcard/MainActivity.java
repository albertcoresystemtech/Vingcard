package com.cst.vingcard;

import static android.app.PendingIntent.getActivity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cst.vingcard.api.RetrofitAPICollection;
import com.cst.vingcard.api.RetrofitClient;
import com.cst.vingcard.database.RoomCaller;
import com.cst.vingcard.entity.Setup;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    public static final SimpleDateFormat databaseDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
    public static final SimpleDateFormat databaseTimeFormat = new SimpleDateFormat("HHmm", Locale.ENGLISH);
    public static final SimpleDateFormat databaseDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

    public String hostUrlBackup = "https://visionline-web-api-build.testpigeon.net";
    public String clientSecretBackup = "xJSPkPs8ujrW1EBW7AtI1Z5b8DMvWl3dAPhmMDEd"; //token
    public String hostUrl ;
    public String clientSecret ;

    private TextView inputRoomNumber, inputNumberOfNight, inputExpirationTime, textCardSerialNumber, textCardStatus, textResult,
            textEncryptedImage, textStartDate, textEndDate, textTitleWriteCard, textTitleDoorDetail;
    DatePickerDialog datePickerDialog;
    private List<String> hotelDoorRoom= new ArrayList<String>();
    private List<String> hotelDoorRoomStr= new ArrayList<String>();
    private Spinner spinnerCardOption, spinnerRoomNumber;
    private String doorDetailsOption= "false";
    private String doorGroupsOption= "GuestRooms";
    private static final String[] spinnerCardOptionStr = {"Yes", "No"};
    //private static final String[] spinnerRoomNumberStr = new String[];
    private Button checkInfoButton, btnCheckDoorDetail, btnWriteNewCard, btnStartDate, btnEndDate, submitInfoButton;
    private LinearLayout layoutWriteCard, layoutDoorDetail, layoutFormInput;
    private String newCardSelection = "";

    public AlertDialog alertDialog;

    private NfcAdapter adapter = null;
    private PendingIntent pendingIntent = null;
    private Tag tag = null;
    private AlertDialog alertDialogReadCardOption, alertDialogWriteCardOption, alertDialogWriteCard ;
    Boolean onClickReadButton = false;
    String trueStartDate , formattedDate, onTextChangeExpirationDate, rawExpirationDate, trueExpirationDate,
            trueRoomNumber, trueCardSerialNumber, trueWriteCardStartDate, trueWriteCardEndDate, trueHotelCardId;
    String strRoomNumber, strNumberOfNight, strExpirationDate, strReadCard, strStartDate, strEndDate;
    String rawEncryptedImage;
    private List<String> roomNumber = new ArrayList<String>();
    private List<String> cardSerialNumber = new ArrayList<String>();
    private List<String> hotelCardHolderId = new ArrayList<String>();
    private List<String> hotelCardHolderDetails = new ArrayList<String>();
    private List<String> hotelCardHolderDoor = new ArrayList<String>();
    private List<JSONArray> hotelCardDoorOperations = new ArrayList<JSONArray>();
    private String serialNumber, type; //write card
    private String serialNumberCard, hotelCardId; //check card info/detail
    Boolean inputWriteCard = true;
    Boolean writeCard = false;
    static String TAG = "MainActivity.";

    private Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        @SuppressLint("InflateParams") View viewActionBar = getLayoutInflater().inflate(R.layout.action_bar_layout, null);
        ActionBar.LayoutParams params = new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);
        TextView actionBarTitle = viewActionBar.findViewById(R.id.title_action_bar);
        actionBarTitle.setText(getString(R.string.app_name));
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(viewActionBar, params);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mActivity = MainActivity.this;

        inputRoomNumber = findViewById(R.id.input_room_number_ma);
        inputNumberOfNight = findViewById(R.id.input_number_of_night_ma);
        inputExpirationTime = findViewById(R.id.input_expiration_time_ma);

        spinnerRoomNumber = findViewById(R.id.spinner_room_number_ma);

        RoomCaller dbCaller = new RoomCaller(MainActivity.this);
        Setup setup = dbCaller.getLatestSetup();

        String hostUrlStr = setup.getHostUrl();
        String tokenStr = setup.getToken();
        try {
            setup.getHostUrl();

        } catch (Exception e) {

        }

        if (!hostUrlStr.isEmpty() && !tokenStr.isEmpty()) {

            hostUrl = hostUrlStr;
            clientSecret = tokenStr;

            //get Room number list
            getRoomNumberList();

        } else {
            //empty data
            //hostUrl = hostUrlBackup;
            //clientSecret = clientSecretBackup;

            //or vice versa
            MainActivity.showMessage(MainActivity.this, "Empty Data", "Oppps....\n Please insert correct credential.");
        }

        spinnerCardOption = findViewById(R.id.spinner_card_option_ma);
        ArrayAdapter<String> adapterCardOption = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_spinner_item,spinnerCardOptionStr);

        adapterCardOption.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCardOption.setAdapter(adapterCardOption);
        spinnerCardOption.setOnItemSelectedListener(this);

        checkInfoButton = findViewById(R.id.btn_check_card_info_ma);
        btnCheckDoorDetail = findViewById(R.id.btn_door_info_ma);
        btnWriteNewCard = findViewById(R.id.btn_write_card_ma);
        btnStartDate = findViewById(R.id.btn_start_date_ma);
        btnEndDate = findViewById(R.id.btn_end_date_ma);
        submitInfoButton = findViewById(R.id.btn_submit_info_ma);

        layoutWriteCard = findViewById(R.id.layout_card_validation_ma);
        layoutDoorDetail = findViewById(R.id.layout_door_detail_ma);
        layoutFormInput = findViewById(R.id.layout_form_ma);

        textCardSerialNumber = findViewById(R.id.text_card_sn_ma);
        textCardStatus = findViewById(R.id.text_status_ma);
        textResult = findViewById(R.id.text_result_ma);
        textEncryptedImage = findViewById(R.id.text_encrypted_image_ma);

        textStartDate = findViewById(R.id.text_start_date_ma);
        textEndDate = findViewById(R.id.text_end_date_ma);

        textTitleWriteCard  = findViewById(R.id.title_write_card_instruction_ma);
        textTitleDoorDetail  = findViewById(R.id.title_door_detail_instruction_ma);

        //get current date
        Date dateCalender = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        formattedDate = df.format(dateCalender);
        inputExpirationTime.setText(formattedDate + " 12:00 PM");

        adapter = NfcAdapter.getDefaultAdapter(this);

        inputRoomNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                inputNumberOfNight.setText("1");
            }
        });

        inputNumberOfNight.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                pickNumberOfNight();
            }

            @Override
            public void afterTextChanged(Editable s) {
                pickNumberOfNight();
            }
        });

        checkInfoButton.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_not_selected_show_bg));
        //TODO: RECHECK FLOW
        checkInfoButton.setOnClickListener(v -> checkCardInfo());
        btnCheckDoorDetail.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_not_selected_show_bg));
        btnCheckDoorDetail.setOnClickListener(v -> enableFormDatePicker());
        btnWriteNewCard.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_selected_show_bg));
        btnWriteNewCard.setOnClickListener(v -> enableFormWriteNewCard());

        btnStartDate.setOnClickListener(v -> pickStartDate());
        btnEndDate.setOnClickListener(v -> pickEndDate());

        submitInfoButton.setOnClickListener(v -> checkInfo());



    }

    private void getRoomNumberList() {

        final AlertDialog alertDialogFetchApiResponse = showLoading(MainActivity.this, "Fetching Information");

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
                            DoorActivity.showMessage(MainActivity.this, "Empty!", "The door info is empty. Please contact support.");
                            return;
                        }

                        hotelDoorRoom = new ArrayList<String>();
                        //method to get multiple Card ID from API Response
                        for (int i =0; i < dataJSON.length(); i++) {
                            JSONObject jsonObject = dataJSON.getJSONObject(i);
                            String doorId = jsonObject.getString("id");
                            hotelDoorRoom.add(doorId);

                        }
                        hotelDoorRoomStr = hotelDoorRoom;

                        ArrayAdapter<String> adapterRoomNumber = new ArrayAdapter<String>(MainActivity.this,
                                android.R.layout.simple_spinner_item,hotelDoorRoomStr);

                        adapterRoomNumber.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerRoomNumber.setAdapter(adapterRoomNumber);
                        spinnerRoomNumber.setOnItemSelectedListener(MainActivity.this);

                        spinnerRoomNumber.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                                String selectedRoom = parent.getItemAtPosition(position).toString();
                                inputRoomNumber.setText(selectedRoom);


                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });


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
                        MainActivity.showMessage(MainActivity.this, title, error);
                    }
                } catch (Exception e) {
                    MainActivity.showMessage(MainActivity.this, "Oops!", "Fail to get response. Please try again.");
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                alertDialogFetchApiResponse.dismiss();
                MainActivity.showMessage(MainActivity.this, "Oops!", "Server response is unavailable. \n\nPlease ensure the Terminal are correctly setup and connected to internet.");
            }
        });

    }

    private void pickStartDate() {
        final Calendar calendar = Calendar.getInstance();
        int startDay = calendar.get(Calendar.DAY_OF_MONTH);
        int startMonth = calendar.get(Calendar.MONTH);
        int startYear = calendar.get(Calendar.YEAR);

        datePickerDialog = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                int newIntMonthOfYear = monthOfYear + 1;
                int sizeMonthOfYear = String.valueOf(newIntMonthOfYear).length();
                int sizeDayOfMonth = String.valueOf(dayOfMonth).length();
                String newMonthOfYear = "";
                String newDayOfMonth = "";

                if (sizeMonthOfYear!= 1) {
                    newMonthOfYear = String.valueOf(newIntMonthOfYear);
                } else {
                    newMonthOfYear = "0" + String.valueOf(newIntMonthOfYear);
                }
                if (sizeDayOfMonth != 1) {
                    newDayOfMonth = String.valueOf(dayOfMonth);
                } else {
                    newDayOfMonth = "0" + String.valueOf(dayOfMonth);
                }

                textStartDate.setText(year + "-" + (newMonthOfYear) + "-" + newDayOfMonth);
            }
        }, startYear, startMonth, startDay);
        datePickerDialog.show();
        btnStartDate.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_pick_date_button_bg));
    }

    private void pickEndDate() {
        final Calendar calendar = Calendar.getInstance();
        int endDay = calendar.get(Calendar.DAY_OF_MONTH);
        int endMonth = calendar.get(Calendar.MONTH);
        int endYear = calendar.get(Calendar.YEAR);

        datePickerDialog = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                int newIntMonthOfYear = monthOfYear + 1;
                int sizeMonthOfYear = String.valueOf(newIntMonthOfYear).length();
                int sizeDayOfMonth = String.valueOf(dayOfMonth).length();
                String newMonthOfYear = "";
                String newDayOfMonth = "";

                if (sizeMonthOfYear!= 1) {
                    newMonthOfYear = String.valueOf(newIntMonthOfYear);
                } else {
                    newMonthOfYear = "0" + String.valueOf(newIntMonthOfYear);
                }
                if (sizeDayOfMonth != 1) {
                    newDayOfMonth = String.valueOf(dayOfMonth);
                } else {
                    newDayOfMonth = "0" + String.valueOf(dayOfMonth);
                }

                textEndDate.setText(year + "-" + (newMonthOfYear) + "-" + newDayOfMonth);
            }
        }, endYear, endMonth, endDay);
        datePickerDialog.show();
        btnEndDate.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_pick_date_button_bg));
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {

        switch (position) {
            case 0:
                //Toast.makeText(this, "YOUR SELECTION IS : " + parent.getItemAtPosition(position).toString(), Toast.LENGTH_SHORT).show();
                submitInfoButton.setEnabled(true);
                newCardSelection = "yes";
                break;
            case 1:
                //Toast.makeText(this, "YOUR SELECTION IS : " + parent.getItemAtPosition(position).toString(), Toast.LENGTH_SHORT).show();
                submitInfoButton.setEnabled(true);
                newCardSelection = "no";
                break;

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // TODO Auto-generated method stub
    }

    private void pickNumberOfNight() {

        String numberOfNightStr = String.valueOf(inputNumberOfNight.getText());
        if (numberOfNightStr.isEmpty() || numberOfNightStr.equals(null)) {
            numberOfNightStr = "0";
        }
        int numberOfNightPickInt = Integer.parseInt(numberOfNightStr);

        SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd");
        try {
            //get current date
            Date dateCalender = Calendar.getInstance().getTime();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String formattedDate = df.format(dateCalender);

            //Date date = dateParser.parse("01/01/2015");
            Date date = dateParser.parse(formattedDate);
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            //c.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR) + 20);
            c.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR) + numberOfNightPickInt);

            Date newDate = c.getTime();
            String newFormattedDate = dateParser.format(newDate);
            inputExpirationTime.setText(newFormattedDate + " 12:00 PM");
            onTextChangeExpirationDate = newFormattedDate;

        } catch (ParseException e) {
            e.printStackTrace();
            //handle exception
        }


    }

    @Override
    public void onResume() {
        super.onResume();

        if (adapter == null)
            return;

        if (!adapter.isEnabled()) {
            Utils.showNfcSettingsDialog(this);
            return;
        }

        if (pendingIntent == null) {
            pendingIntent = getActivity(this, 0,
                    new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        }

        adapter.enableForegroundDispatch(this, pendingIntent, null, null);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (adapter != null)
            adapter.disableForegroundDispatch(this);
    }

    //check input
    private void checkInfo() {

        if (inputWriteCard) {
            //write card mode
            strRoomNumber = String.valueOf(inputRoomNumber.getText());
            strNumberOfNight = String.valueOf(inputNumberOfNight.getText());
            strExpirationDate = String.valueOf(inputExpirationTime.getText());

            //check if field is empty
            if (isAnyStringNullOrEmpty(strRoomNumber, strNumberOfNight, strExpirationDate)) {

                Toast.makeText(MainActivity.this, "Oops, you forgot to fill in some fields!", Toast.LENGTH_SHORT).show();
                textCardStatus.setText("Some field is empty");
                showErrorInfo();
            } else {
                //correct info
                showReadCardOption();
            }

        } else {
            //check door info mode
            strRoomNumber = String.valueOf(inputRoomNumber.getText());
            strStartDate = String.valueOf(textStartDate.getText());
            strEndDate = String.valueOf(textEndDate.getText());

            //check if field is empty
            if (isAnyStringNullOrEmpty(strRoomNumber, strEndDate)) {

                Toast.makeText(MainActivity.this, "Oops, you forgot to fill in some fields!", Toast.LENGTH_SHORT).show();
                textCardStatus.setText("Some field is empty");
                showErrorInfo();
            } else {
                //correct input
                //check text date value is not 'YYYY-MM-DD'
                if (strEndDate.compareToIgnoreCase("YYYY-MM-DD")==0) {

                    Toast.makeText(MainActivity.this, "Oops, you forgot to fill in the correct date!", Toast.LENGTH_SHORT).show();
                    textCardStatus.setText("Some field is empty");
                    showErrorInfo();

                } else {
                    //correct data value input
                    //Get true date for payload (API Door - Card)  //remove '-' symbols from a string
                    String formattedStartDateReplaced = strStartDate.replaceAll("[^a-zA-Z0-9]", "");
                    String addTimeToStartDate = formattedStartDateReplaced + "T0000";
                    trueWriteCardStartDate = addTimeToStartDate;

                    //Used as valid date
                    String formattedEndDateReplaced = strEndDate.replaceAll("[^a-zA-Z0-9]", "");
                    String addTimeToEndDate = formattedEndDateReplaced + "T1159";
                    trueWriteCardEndDate = addTimeToEndDate;

                    getDoorDetails(strRoomNumber, trueWriteCardEndDate);

                }

            }

        }

    }

    //enable door detail view
    private void enableFormDatePicker() {
        inputWriteCard = false;  //check if input text empty or not
        btnCheckDoorDetail.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_selected_show_bg));
        btnWriteNewCard.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_not_selected_show_bg));
        checkInfoButton.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_not_selected_show_bg));
        layoutFormInput.setVisibility(View.VISIBLE);
        textTitleWriteCard.setVisibility(View.GONE);
        layoutWriteCard.setVisibility(View.GONE);
        textTitleDoorDetail.setVisibility(View.VISIBLE);
        layoutDoorDetail.setVisibility(View.VISIBLE);

    }

    //enable write new card view
    private void enableFormWriteNewCard() {
        inputWriteCard = true;
        btnWriteNewCard.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_selected_show_bg));
        btnCheckDoorDetail.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_not_selected_show_bg));
        checkInfoButton.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_not_selected_show_bg));
        layoutFormInput.setVisibility(View.VISIBLE);
        textTitleWriteCard.setVisibility(View.VISIBLE);
        layoutWriteCard.setVisibility(View.VISIBLE);
        textTitleDoorDetail.setVisibility(View.GONE);
        layoutDoorDetail.setVisibility(View.GONE);

    }

    private void getDoorDetails(String guestDoor, String validTimeStr) {

        final AlertDialog alertDialogFetchApiResponse = showLoading(MainActivity.this, "Fetching Information");

        RetrofitAPICollection service = RetrofitClient.getRetrofitClient(hostUrl).create(RetrofitAPICollection.class);
        Call<String> callEncodeCard = service.getDoorCard("Bearer " + clientSecret, guestDoor, validTimeStr);
        callEncodeCard.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                alertDialogFetchApiResponse.dismiss();
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        JSONObject responseJSON = new JSONObject(response.body());
                        JSONArray dataJSON = responseJSON.getJSONArray("data");

                        if (!responseJSON.getString("data").equals("null")) {

                            if (dataJSON.length() == 0) {
                                //throw error
                                MainActivity.showMessage(MainActivity.this, "Empty Info", "The room number have empty door card. Please contact support.");
                                return;
                            }

                            hotelCardHolderId = new ArrayList<String>();
                            //method to get multiple Card ID from API Response
                            for (int i =0; i < dataJSON.length(); i++) {
                                JSONObject jsonObject = dataJSON.getJSONObject(i);
                                String strHotelCardId = jsonObject.getString("id");
                                hotelCardHolderId.add(strHotelCardId);

                                //TODO: Get more card detail
                                //call API Show -Card -Get one by one ID of the hotel card
                                getShowCardInfoFromDoor(strHotelCardId);

                            }

                            /*MainActivity.showMessage(MainActivity.this, "Hotel Card Found", "Found " + hotelIdCount +
                                    " Card ID : " + trueHotelCardId + "\n" + "\n" + hotelCardHolderDetails);
                            textResult.setText(trueHotelCardId);*/

                            //inputRoomNumber.setText("");

                            //TODO: FUNCTION to call another API to get card holder, etc..*IF NEEDED
                            //getShowCardInfo(); // post API -> card/show

                            alertDialogFetchApiResponse.dismiss();

                        } else {
                            /* alertDialogAddToCart.dismiss(); */
                            alertDialogFetchApiResponse.dismiss();
                            MainActivity.showMessage(MainActivity.this, "Oops!", "Fail to fetch data. Please try again.");
                        }
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

                        textCardStatus.setText("- - -");
                        alertDialogFetchApiResponse.dismiss();
                        MainActivity.showMessage(MainActivity.this, title, error);
                    }
                } catch (Exception e) {
                    /*alertDialog.dismiss();
                    ExceptionUtils.reportException(e);*/
                    alertDialogFetchApiResponse.dismiss();
                    MainActivity.showMessage(MainActivity.this, "Oops!", "Fail to get response. Please try again.");
                }
            }

            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                /* alertDialog.dismiss();*/
                alertDialogFetchApiResponse.dismiss();
                MainActivity.showMessage(MainActivity.this, "Oops!", "Server response is unavailable. Please ensure the terminal is connected to internet.");
            }
        });


    }

    private void checkCardInfo() {
        //check value of card S/N (serialNumberCard = tagId;)
        if (serialNumberCard!= null) {
            //TODO: get 2 request & 2 response from API [ Cards-Card  -> Show-Card ]
            //cards-card API
            getCardSerialNumber();

        } else {
            //if null, pop message say null/empty value, by default this message box will not appear as per condition scan/read card
            MainActivity.showMessage(MainActivity.this, "Oops!", "Fail to get Card S/N. Please try again.");

        }

    }

    private void getCardSerialNumber() {

        final AlertDialog alertDialogFetchApiResponse = showLoading(MainActivity.this, "Fetching Information");

        String serialNumberCardStr = serialNumberCard + "ff";

        RetrofitAPICollection service = RetrofitClient.getRetrofitClient(hostUrl).create(RetrofitAPICollection.class);
        Call<String> callEncodeCard = service.getSNCard("Bearer " + clientSecret, serialNumberCardStr);
        callEncodeCard.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                alertDialogFetchApiResponse.dismiss();
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        JSONObject responseJSON = new JSONObject(response.body());
                        JSONArray dataJSON = responseJSON.getJSONArray("data");

                        if (!responseJSON.getString("data").equals("null")) {
                            for (int i =0; i < dataJSON.length(); i++) {
                                JSONObject jsonObject = dataJSON.getJSONObject(i);
                                String hotelCardIdStr = jsonObject.getString("id");
                                hotelCardId = hotelCardIdStr;
                            }
                            String hotelId = hotelCardId;
                            getShowCardInfo(hotelId); // post API -> card/show


                        } else {
                            /* alertDialogAddToCart.dismiss(); */
                            MainActivity.showMessage(MainActivity.this, "Oops!", "Fail to fetch data. Please try again.");
                        }
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

                        textCardStatus.setText("- - -");
                        MainActivity.showMessage(MainActivity.this, title, error);
                    }
                } catch (Exception e) {
                    /*alertDialog.dismiss();
                    ExceptionUtils.reportException(e);*/
                    MainActivity.showMessage(MainActivity.this, "Oops!", "Fail to get response. Please try again.");
                }
            }

            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                /* alertDialog.dismiss();*/
                MainActivity.showMessage(MainActivity.this, "Oops!", "Server response is unavailable. Please ensure the terminal is connected to internet.");
            }
        });


    }

    private void getShowCardInfo(String hotelCardIdStr) {

        final AlertDialog alertDialogFetchApiResponse = showLoading(MainActivity.this, "Fetching Information");

        RetrofitAPICollection service = RetrofitClient.getRetrofitClient(hostUrl).create(RetrofitAPICollection.class);
        Call<String> callEncodeCard = service.getShowCard("Bearer " + clientSecret, hotelCardIdStr);
        callEncodeCard.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                alertDialogFetchApiResponse.dismiss();
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        JSONObject responseJSON = new JSONObject(response.body());

                        if (!responseJSON.getString("data").equals("null")) {
                            JSONObject dataObject = responseJSON.getJSONObject("data");
                            String hotelCardIdStr = dataObject.getString("id");
                            String hotelCardHolderStr = dataObject.getString("cardHolder");
                            String startTimeStr = dataObject.getString("startTime");
                            String expireTimeStr = dataObject.getString("expireTime");
                            String createdStr = dataObject.getString("created");
                            JSONArray jsonArray = dataObject.getJSONArray("doorOperations");

                            hotelCardDoorOperations.add(jsonArray);

                            String finalOutput = "Hotel Card ID : " + hotelCardIdStr + "\n" +
                                    "Card Holder : " + hotelCardHolderStr + "\n" +
                                    "Card S/N : " + serialNumberCard + "\n" +
                                    "Start Date : " + startTimeStr + "\n" +
                                    "Expire Date : " + expireTimeStr + "\n" +
                                    "Door Operations : " + hotelCardDoorOperations + "\n" +
                                    "Created : " + createdStr + "\n" ;
                            textCardStatus.setText("Get Card Info Success");
                            textResult.setText(finalOutput);
                            checkInfoButton.setEnabled(false);
                            checkInfoButton.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.btn_not_selected_show_bg));

                            MainActivity.showMessage(MainActivity.this, "Card Info", finalOutput
                                    + "\n" + "\n" + " *Note: You can remove the card from terminal." );


                        } else {
                             //alertDialogAddToCart.dismiss();
                            MainActivity.showMessage(MainActivity.this, "Oops!", "Fail to fetch data. Please try again.");
                        }
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

                        textCardStatus.setText("- - -");
                        MainActivity.showMessage(MainActivity.this, title, error);
                    }
                } catch (Exception e) {
                    alertDialog.dismiss();
                    //ExceptionUtils.reportException(e);
                    MainActivity.showMessage(MainActivity.this, "Oops!", "Fail to get response. Please try again.");
                }
            }

            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                 alertDialog.dismiss();
                MainActivity.showMessage(MainActivity.this, "Oops!", "Server response is unavailable. Please ensure the terminal is connected to internet.");
            }
        });


    }

    private void getShowCardInfoFromDoor(String hotelCardIdStr) {

        //final AlertDialog alertDialogFetchApiResponseGetShowCardInfoFromDoor = showLoading(MainActivity.this, "Fetching Information");

        RetrofitAPICollection service = RetrofitClient.getRetrofitClient(hostUrl).create(RetrofitAPICollection.class);
        Call<String> callEncodeCard = service.getShowCard("Bearer " + clientSecret, hotelCardIdStr);
        callEncodeCard.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                //alertDialogFetchApiResponseGetShowCardInfoFromDoor.dismiss();
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        JSONObject responseJSON = new JSONObject(response.body());

                        if (!responseJSON.getString("data").equals("null")) {
                            JSONObject dataObject = responseJSON.getJSONObject("data");
                            String hotelCardIdStr = dataObject.getString("id");
                            String hotelCardHolderStr = dataObject.getString("cardHolder");
                            String startTimeStr = dataObject.getString("startTime");
                            String expireTimeStr = dataObject.getString("expireTime");

                            String overriddenStr = dataObject.getString("overridden");
                            String overwrittenStr = dataObject.getString("overwritten");
                            String pendingAutoUpdateStr = dataObject.getString("pendingAutoUpdate");

                            //check overridden status -> TRUE = Invalid card/replaced/overwrite with new card, FALSE: Valid/active card
                            String overriddenStatus = "";
                            if (overriddenStr.compareToIgnoreCase("true")==0) {
                                overriddenStatus = "Invalid Card";
                            } else {
                                overriddenStatus = "Valid Card";
                            }

                            //get card S/N
                            JSONArray ja = dataObject.getJSONArray("serialNumbers");
                            String snCard = String.valueOf(ja);

                            String snCardStr = snCard.toUpperCase();

                            //remove '["' & '"]' at array
                            String snCardReplace= snCardStr.replaceAll("[^a-zA-Z0-9]", "");

                            String snCardFinal = snCardReplace.substring(0, snCardReplace.length() - 2);

                            hotelCardHolderDoor = new ArrayList<String>();
                            //get doorOperation
                            JSONArray jda = dataObject.getJSONArray("doorOperations");
                            //method to get multiple Card ID from API Response
                            for (int i =0; i < jda.length(); i++) {
                                JSONObject jsonObject = jda.getJSONObject(i);
                                String strHotelCardDoor = jsonObject.getString("doors");
                                hotelCardHolderDoor.add(strHotelCardDoor);
                            }
                            String doorInfoStr = String.valueOf(hotelCardHolderDoor);

                            String finalOutput = "Hotel Card ID : " + hotelCardIdStr + "\n" +
                                    "Card Status : " + overriddenStatus + "\n" +
                                    "Card Holder : " + hotelCardHolderStr + "\n" +
                                    "Card S/N : " + snCardFinal + "\n" +
                                    "Start Date : " + startTimeStr + "\n" +
                                    "Expire Date : " + expireTimeStr + "\n" +
                                    "Door Info: " + doorInfoStr + "\n" +
                                    "Overridden: " + overriddenStr + "\n" +
                                    "Overwritten: " + overwrittenStr + "\n" +
                                    "PendingAutoUpdate: " + pendingAutoUpdateStr + "\n"
                                    + "--------------------------------------------------------" ;
                            //textCardStatus.setText("Get Card Info Success");
                            //textResult.setText(finalOutput);
                            //checkInfoButton.setEnabled(false);
                            //checkInfoButton.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.btn_not_selected_show_bg));

                            /*MainActivity.showMessage(MainActivity.this, "Card Info", finalOutput
                                    + "\n" + "\n" + " *Note: You can remove the card from terminal." );*/
                            hotelCardHolderDetails.add(finalOutput);

                            showResult();


                        } else {
                            /* alertDialogAddToCart.dismiss(); */
                            MainActivity.showMessage(MainActivity.this, "Oops!", "Fail to fetch data. Please try again.");
                        }
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

                        textCardStatus.setText("- - -");
                        MainActivity.showMessage(MainActivity.this, title, error);
                    }
                } catch (Exception e) {
                    /*alertDialog.dismiss();
                    ExceptionUtils.reportException(e);*/
                    MainActivity.showMessage(MainActivity.this, "Oops!", "Fail to get response. Please try again.");
                }
            }

            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                /* alertDialog.dismiss();*/
                MainActivity.showMessage(MainActivity.this, "Oops!", "Server response is unavailable. Please ensure the terminal is connected to internet.");
            }
        });


    }

    private void showResult() {

        //remove '[]' at array
        String hotelCardIdStr = "";
        int i = 0;
        for (String str : hotelCardHolderId) {
            if (i != 0) {
                hotelCardIdStr += ",";
            }
            hotelCardIdStr += str;
            i++;
        }
        int hotelIdCount = hotelCardHolderId.size();

        trueHotelCardId = hotelCardIdStr; //100060, 100069,1000071

                            /*MainActivity.showMessage(MainActivity.this, "Hotel Card ID Found", "The ID :" + trueHotelCardId +
                                    "\n" + "\n" + hotelCardHolderDetails);
                            textResult.setText(trueHotelCardId);*/

        if (hotelCardHolderDetails.isEmpty()) {

            //alertDialogFetchApiResponse.dismiss();
            MainActivity.showMessage(MainActivity.this, "Hotel Card Found", "Found " + hotelIdCount +
                    " Card ID : " + trueHotelCardId + "\n" + "\n" + "*Note: Please click submit button again to get more detail.");
            textResult.setText(trueHotelCardId);

        } else {
            //user want to get more detail - check card valid/invalid
            //remove '[]' at array
            String hotelCardHolderDetailsStr = "";
            int x = 0;
            for (String str : hotelCardHolderDetails) {
                if (x != 0) {
                    hotelCardHolderDetailsStr += "";
                }
                hotelCardHolderDetailsStr += str;
                x++;
            }

            String titleMessageBox = "Hotel Card Found (Room:" + strRoomNumber + ")";
            MainActivity.showMessageScrollable(MainActivity.this, titleMessageBox, "Found " + hotelIdCount +
                    " Card ID : " + trueHotelCardId + "\n" + "\n" + hotelCardHolderDetailsStr);
            textResult.setText(trueHotelCardId);

            inputRoomNumber.setText("");
            textEncryptedImage.setVisibility(View.GONE);
            hotelCardHolderDetails = new ArrayList<>();
            //alertDialogFetchApiResponse.dismiss();

        }

    }

    public static boolean isAnyStringNullOrEmpty(String... strings) {
        for (String s : strings)
            if (s == null || s.isEmpty())
                return true;
        return false;
    }

    private void submit() {

        String roomNumberStr = String.valueOf(inputRoomNumber.getText());
        String startDateStr = MainActivity.databaseDateFormat.format(new Date());
        String startTimeStr = MainActivity.databaseTimeFormat.format(new Date());
        String startDateTimeStr = startDateStr + "T" + startTimeStr;
        trueStartDate = startDateTimeStr;

        //expiration date & time for payload  //remove '-' symbols from a string
        String expirationDateTimeStr = onTextChangeExpirationDate;
        String formattedDateReplaced = expirationDateTimeStr.replaceAll("[^a-zA-Z0-9]", "");
        rawExpirationDate = formattedDateReplaced;

        String checkoutTime = "1200";
        String expirationDateStr = rawExpirationDate;
        String expirationTimeStr = checkoutTime;
        String endDateTimeStr = expirationDateStr + "T" + expirationTimeStr;
        trueExpirationDate = endDateTimeStr;

        String cardSerialNumberStr = String.valueOf(textCardSerialNumber.getText());
        String finalResult = "Room Number : " + roomNumberStr + "\n" +
                "Start Date : " + startDateTimeStr + "\n" +
                "End Date : " + endDateTimeStr + "\n" +
                "Card S/N : " + cardSerialNumberStr;
        textCardStatus.setText("Read Card Success");
        textResult.setText(finalResult);
        textEncryptedImage.setText(rawEncryptedImage);


        if (rawEncryptedImage != null) {
            writeCard();
        }

    }

    private void writeCard() {

        writeCard = true;
        showWriteCardDialog();

    }

    private void resolveIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) {
                serialNumber = TagUtils.detectTagSerialNumber(tag)
                        .replaceAll(" ", "");
                Log.d(TAG + "resolveIntent()", "Serial Number: " + serialNumber);

                type = TagUtils.detectTagTech(tag);
                Log.d(TAG + "resolveIntent()", "Type:" + type);

                if (TagUtils.MIFARE_ULTRALIGHT.equalsIgnoreCase(type)) {
                    //readUltralightTag(tag);

                    alertDialogWriteCard.dismiss();
                    String dataToWrite = rawEncryptedImage;
                    /*String dataToWrite =
                            "1A2B3C4D" +
                            "22222222" +
                            "33333333" +
                            "44444444" +
                            "55555555" +
                            "66666666" ;*/

                    writeUltralightTag(tag, dataToWrite);
                }

            }
        }
    }

    private void writeUltralightTag(Tag tag, String dataToWrite) {
        for (String tech : tag.getTechList()) {
            if (tech.equals(MifareUltralight.class.getName())) {
                MifareUltralight mifareUlTag = MifareUltralight.get(tag);
                writeTag(mifareUlTag, dataToWrite);
                Log.d(TAG + "writeUltralightTag()", "Write Done");
                textCardStatus.setText("Write Done");
                resetCurrentValue();
                //MainActivity.showMessage(MainActivity.this, "Write Card Success", "The card write successfully.");
                //restartActivity(mActivity);
                return;
            }
        }
        Log.e(TAG + "writeUltralightTag()", "Not Ultralight Tag!");
    }

    private void resetCurrentValue() {
        //reset value for string & array
        strRoomNumber = null;
        strExpirationDate = null;
        strNumberOfNight = null;
        strReadCard = null;
        rawEncryptedImage = null;

        newCardSelection = null;
        trueStartDate = null;
        formattedDate = null;
        //onTextChangeExpirationDate = null;
        rawExpirationDate = null;
        trueExpirationDate = null;
        trueRoomNumber = null;
        trueCardSerialNumber = null;

        roomNumber = new ArrayList<String>();
        cardSerialNumber = new ArrayList<String>();

        writeCard = false;
        onClickReadButton = false;

        ArrayAdapter<String> adapterCardOption = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_spinner_item,spinnerCardOptionStr);

        adapterCardOption.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCardOption.setAdapter(adapterCardOption);
        spinnerCardOption.setOnItemSelectedListener(this);

        submitInfoButton.setEnabled(false);
        checkInfoButton.setEnabled(false);
        checkInfoButton.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.btn_not_selected_show_bg));

    }

    /**
     * We are assuming that the dataToWrite is a fixed 48 character string.
     * @param mifareUlTag
     * @param dataToWrite
     */
    public void writeTag(MifareUltralight mifareUlTag, String dataToWrite) {
        assert dataToWrite.length() == 48; // Crash application if not met

        try {
            int charactersPerPage = 8;
            int pageOffsetStart = 4; //can change based on row need to write
            int pagesToWrite = dataToWrite.length() / charactersPerPage;

            String[] strings = Utils.splitString(dataToWrite, charactersPerPage);

            mifareUlTag.connect();

            for (int i = 0; i < pagesToWrite; i++) {
                int pageOffset = pageOffsetStart + i;
                byte[] data = Utils.stringToHex(strings[i]);
                mifareUlTag.writePage(pageOffset, data);
                Log.v(TAG + "writeTag()", "Written " + strings[i] + " to PageOffset " + pageOffset);
            }

        } catch (IOException e) {
            Log.e(TAG, "IOException while writing MifareUltralight...", e);
        } finally {
            try {
                mifareUlTag.close();
            } catch (IOException e) {
                Log.e(TAG, "IOException while closing MifareUltralight...", e);
            }
        }
    }

    public AlertDialog showAlertDialog(String message) {
        this.alertDialog = new AlertDialog.Builder(this).setTitle("Scan MIFARE® Tag").setIcon(R.drawable.img_card_payment).setMessage(message).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //FileSelectorActivity.this.alertDialog.cancel();
                //FileSelectorActivity.this.scanAction = FileSelectorActivity.ACTION_NONE;
            }
        }).create();
        return this.alertDialog;
    }

    private void showReadCardOption() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.pop_up_no_option, null);

        TextView textTitle = dialogView.findViewById(R.id.text_title_pop_up_no_option);
        TextView textMessage = dialogView.findViewById(R.id.text_message_pop_up_no_option);
        ImageView imageScanCard = dialogView.findViewById(R.id.image_scan_card_no_option);
        TextView btnReturn = dialogView.findViewById(R.id.button_return_pop_up_no_option);

        textTitle.setText("Read Card");
        textMessage.setText("Please place the card at the top of this terminal");

        Glide.with(this).load(R.drawable.img_card_payment).into(imageScanCard);

        dialogBuilder.setCancelable(false);
        dialogBuilder.setView(dialogView);

        alertDialogReadCardOption = dialogBuilder.create();
        alertDialogReadCardOption.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialogReadCardOption.show();
        onClickReadButton = true;

        btnReturn.setOnClickListener(v -> alertDialogReadCardOption.dismiss());

        dialogBuilder.setCancelable(false);
        dialogBuilder.setView(dialogView);
    }

    private void showWriteCardDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.pop_up_no_option, null);

        TextView textTitle = dialogView.findViewById(R.id.text_title_pop_up_no_option);
        TextView textMessage = dialogView.findViewById(R.id.text_message_pop_up_no_option);
        ImageView imageScanCard = dialogView.findViewById(R.id.image_scan_card_no_option);
        TextView btnReturn = dialogView.findViewById(R.id.button_return_pop_up_no_option);

        textTitle.setText("Write Card");
        textMessage.setText("Please remove and place again the card at the top of this terminal");

        Glide.with(this).load(R.drawable.img_card_payment).into(imageScanCard);

        dialogBuilder.setCancelable(false);
        dialogBuilder.setView(dialogView);

        alertDialogWriteCard = dialogBuilder.create();
        alertDialogWriteCard.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialogWriteCard.show();
        onClickReadButton = true;

        btnReturn.setOnClickListener(v -> alertDialogWriteCard.dismiss());

        dialogBuilder.setCancelable(false);
        dialogBuilder.setView(dialogView);
    }

    private void showErrorInfo() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.pop_up_check_option, null);

        TextView btnReturn = dialogView.findViewById(R.id.button_check_return_pop_up_option);
        TextView textTitle = dialogView.findViewById(R.id.text_check_title_pop_up_option);
        TextView textMessage = dialogView.findViewById(R.id.text_check_message_pop_up_option);
        Button btnOk = dialogView.findViewById(R.id.button_check_ok_pop_up_option);

        textTitle.setText("Write Card Error");
        textMessage.setText("Please enter the correct info.");

        dialogBuilder.setCancelable(false);
        dialogBuilder.setView(dialogView);

        alertDialogReadCardOption = dialogBuilder.create();
        alertDialogReadCardOption.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialogReadCardOption.show();

        btnReturn.setOnClickListener(v -> alertDialogReadCardOption.dismiss());

        btnOk.setOnClickListener(v -> alertDialogReadCardOption.dismiss());



        dialogBuilder.setCancelable(false);
        dialogBuilder.setView(dialogView);
    }

    public void showWriteCardOption() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.pop_up_option, null);

        TextView textTitle = dialogView.findViewById(R.id.text_title_pop_up_option);
        TextView textMessage = dialogView.findViewById(R.id.text_message_pop_up_option);
        Button btnJoiner = dialogView.findViewById(R.id.button_joiner_pop_up_option);
        Button btnOverwrite = dialogView.findViewById(R.id.button_overwrite_pop_up_option);
        TextView btnReturn = dialogView.findViewById(R.id.button_return_pop_up_option);

        textTitle.setText("Write Card Option");
        textMessage.setText("Choose Card Option");

        dialogBuilder.setCancelable(false);
        dialogBuilder.setView(dialogView);

        alertDialogWriteCardOption = dialogBuilder.create();
        alertDialogWriteCardOption.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialogWriteCardOption.show();

        btnReturn.setOnClickListener(v -> alertDialogWriteCardOption.dismiss());

        //button OVERWRITE
        btnOverwrite.setOnClickListener(v -> {
            alertDialogWriteCardOption.dismiss();

            //TODO: override function
            submit();
            encodeCardOverride();
        });

        //button JOINER
        btnJoiner.setOnClickListener(v -> {
            alertDialogWriteCardOption.dismiss();

            //TODO: joiner function
            submit();
        });

        dialogBuilder.setCancelable(false);
        dialogBuilder.setView(dialogView);

        //submit();

    }

    private void encodeCardOverride() {

        final AlertDialog alertDialogFetchApiResponse = showLoading(MainActivity.this, "Fetching Information");

        submit(); //set view on text result
        strRoomNumber = String.valueOf(inputRoomNumber.getText());
        strNumberOfNight = String.valueOf(inputNumberOfNight.getText());
        strExpirationDate = String.valueOf(inputExpirationTime.getText());
        strReadCard = String.valueOf(textCardSerialNumber.getText());

        roomNumber.add(strRoomNumber);
        //remove '[]' at array
        String roomNumberStr = "";
        int i = 0;
        for (String str : roomNumber) {
            if (i != 0) {
                roomNumberStr += "";
            }
            roomNumberStr += str;
            i++;
        }
        trueRoomNumber = roomNumberStr;

        cardSerialNumber.add(strReadCard);
        //remove '[]' at array
        String cardSerialNumberStr = "";
        int x = 0;
        for (String str : cardSerialNumber) {
            if (x != 0) {
                cardSerialNumberStr += "";
            }
            cardSerialNumberStr += str;
            x++;
        }
        trueCardSerialNumber = cardSerialNumberStr;

        String override = "true";
        String pendingImage = "true";
        String sendOnline = "false";

        RetrofitAPICollection service = RetrofitClient.getRetrofitClient(hostUrl).create(RetrofitAPICollection.class);
        Call<String> callEncodeCard = service.encodeCard("Bearer " + clientSecret, override, pendingImage, sendOnline, prepareEncodeCardPayload().toString());
        callEncodeCard.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                alertDialogFetchApiResponse.dismiss();
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        JSONObject responseJSON = new JSONObject(response.body());

                        if (!responseJSON.getString("data").equals("null")) {
                            JSONObject dataObject = responseJSON.getJSONObject("data");
                            String encryptedImage = dataObject.getString("encryptedImage");
                            rawEncryptedImage = encryptedImage;
                            submit();


                        } else {
                            /* alertDialogAddToCart.dismiss(); */
                            MainActivity.showMessage(MainActivity.this, "Oops!", "Fail to fetch data. Please try again.");
                        }
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

                        textCardStatus.setText("- - -");
                        MainActivity.showMessage(MainActivity.this, title, error);
                    }
                } catch (Exception e) {
                    /*alertDialog.dismiss();
                    ExceptionUtils.reportException(e);*/
                    MainActivity.showMessage(MainActivity.this, "Oops!", "Fail to get response. Please try again.");
                }
            }

            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                /* alertDialog.dismiss();*/
                MainActivity.showMessage(MainActivity.this, "Oops!", "Server response is unavailable. Please ensure the terminal is connected to internet.");
            }
        });

    }

    private void encodeCardJoiner() {

        final AlertDialog alertDialogFetchApiResponse = showLoading(MainActivity.this, "Fetching Information");

        submit();
        strRoomNumber = String.valueOf(inputRoomNumber.getText());
        strNumberOfNight = String.valueOf(inputNumberOfNight.getText());
        strExpirationDate = String.valueOf(inputExpirationTime.getText());
        strReadCard = String.valueOf(textCardSerialNumber.getText());

        roomNumber.add(strRoomNumber);
        //remove '[]' at array
        String roomNumberStr = "";
        int i = 0;
        for (String str : roomNumber) {
            if (i != 0) {
                roomNumberStr += "";
            }
            roomNumberStr += str;
            i++;
        }
        trueRoomNumber = roomNumberStr;

        cardSerialNumber.add(strReadCard);
        //remove '[]' at array
        String cardSerialNumberStr = "";
        int x = 0;
        for (String str : cardSerialNumber) {
            if (x != 0) {
                cardSerialNumberStr += "";
            }
            cardSerialNumberStr += str;
            x++;
        }
        trueCardSerialNumber = cardSerialNumberStr;

        String autoJoin = "true";
        String override = "false";
        String pendingImage = "true";
        String sendOnline = "false";

        RetrofitAPICollection service = RetrofitClient.getRetrofitClient(hostUrl).create(RetrofitAPICollection.class);
        Call<String> callEncodeCard = service.encodeJoinerCard("Bearer " + clientSecret,
                autoJoin, override, pendingImage, sendOnline, prepareEncodeCardJoinerPayload().toString());
        callEncodeCard.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                alertDialogFetchApiResponse.dismiss();
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        JSONObject responseJSON = new JSONObject(response.body());

                        if (!responseJSON.getString("data").equals("null")) {
                            JSONObject dataObject = responseJSON.getJSONObject("data");
                            String encryptedImage = dataObject.getString("encryptedImage");
                            rawEncryptedImage = encryptedImage;
                            submit();


                        } else {
                            /* alertDialogAddToCart.dismiss(); */
                            MainActivity.showMessage(MainActivity.this, "Oops!", "Fail to fetch data. Please try again.");
                        }
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

                        MainActivity.showMessage(MainActivity.this, title, error);
                    }
                } catch (Exception e) {
                    /*alertDialog.dismiss();
                    ExceptionUtils.reportException(e);*/
                    MainActivity.showMessage(MainActivity.this, "Oops!", "Fail to get response. Please try again.");
                }
            }

            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                /* alertDialog.dismiss();*/
                MainActivity.showMessage(MainActivity.this, "Oops!", "Server response is unavailable. Please ensure the terminal is connected to internet.");
            }
        });

    }

    @SuppressLint({"StringFormatInvalid", "StringFormatMatches"})
    @Override
    public void onNewIntent(Intent intent) {
        // Discovered tag
        super.onNewIntent(intent);
        if (!writeCard) {

            String action = intent.getAction();
            if (action != null && !action.isEmpty()) {
                if (action.equalsIgnoreCase("android.nfc.action.TAG_DISCOVERED")) {
                    tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                    String tagId = Utils.bytesToHex(tag.getId());
                    if (!onClickReadButton) {
                        if (verifyMFULEV1()) {
                            Toast.makeText(getApplicationContext(), String.format(getString(R.string.ulev1_tag_found), tagId), Toast.LENGTH_LONG).show();
                            serialNumberCard = tagId;
                            textResult.setText("Card S/N: " + tagId);
                            checkInfoButton.setEnabled(true);
                            checkInfoButton.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.btn_selected_show_bg));
                            btnCheckDoorDetail.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.btn_not_selected_show_bg));
                            btnWriteNewCard.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.btn_not_selected_show_bg));
                            layoutFormInput.setVisibility(View.GONE);

                            textEncryptedImage.setText("");
                            textCardSerialNumber.setText(tagId);
                            textCardStatus.setText("Valid Card");
                            //alertDialogReadCardOption.dismiss();
                            //checkInfo();
                        } else {
                            tag = null;
                            //alertDialogReadCardOption.dismiss();
                        }
                    } else {
                        //clicked Read Button
                        if (verifyMFULEV1()) {
                            Toast.makeText(getApplicationContext(), String.format(getString(R.string.ulev1_tag_found), tagId), Toast.LENGTH_LONG).show();
                            //textCardSerialNumber.setVisibility(View.VISIBLE);
                            //textCardSerialNumberSample.setVisibility(View.GONE);
                            textCardSerialNumber.setText(tagId);
                            textCardStatus.setText("Valid Card");
                            strReadCard = String.valueOf(textCardSerialNumber.getText());
                            alertDialogReadCardOption.dismiss();

                            if (newCardSelection.compareTo("yes")==0) {
                                //mean user choose 'yes' on new card option , == override card function
                                encodeCardOverride();
                            } else {
                                //mean user choose 'no' on new card option , == joiner card function
                                encodeCardJoiner();
                            }

                        } else {
                            tag = null;
                            //alertDialogReadCardOption.dismiss();
                        }
                    }
                } else {
                    //if the card/tag is not MiFare Ultralight EV1 type
                    if (!onClickReadButton) {
                        Toast.makeText(getApplicationContext(), String.format(getString(R.string.not_ulev1_tag_found), ""), Toast.LENGTH_LONG).show();
                        textCardStatus.setText("Invalid Card");
                        textCardSerialNumber.setVisibility(View.VISIBLE);
                        checkInfoButton.setEnabled(false);
                        checkInfoButton.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.btn_not_selected_show_bg));
                        textResult.setText(" - -");
                        textEncryptedImage.setText("");
                        textCardSerialNumber.setText("##############");

                    } else {
                        Toast.makeText(getApplicationContext(), String.format(getString(R.string.not_ulev1_tag_found), ""), Toast.LENGTH_LONG).show();
                        textCardStatus.setText("Invalid Card");
                        alertDialogReadCardOption.dismiss();
                        textCardSerialNumber.setVisibility(View.VISIBLE);
                        checkInfoButton.setEnabled(false);
                        checkInfoButton.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.btn_not_selected_show_bg));
                        textResult.setText(" - -");
                        textEncryptedImage.setText("");
                        textCardSerialNumber.setText("##############");

                    }

                }
            }

        } else {

            //write card action
            resolveIntent(intent);
        }


    }

    // Verifies that the identified tag is an ULEV1 one
    private boolean verifyMFULEV1() {
        // Check if UL tag
        boolean isUL = false;
        boolean isULEV1 = false;

        // First check if MFUL
        for (String tech : tag.getTechList()) {
            if (tech.equals("android.nfc.tech.MifareUltralight")) {
                isUL = true;
            }
        }

        if (isUL) {
            MifareUltralight mFUL = MifareUltralight.get(tag);
            try {
                mFUL.connect();
                // send a GET_VERSION command
                byte[] resp = mFUL.transceive(new byte[]{0x60});
                if (resp[4] == (byte) 0x01) {
                    // Mifare Ultralight EV1 tag found
                    isULEV1 = true;
                }

            } catch (Exception e) {
                // Unable to connect with the Mifare Ultralight tag
            } finally {
                if (mFUL != null) {
                    try {
                        mFUL.close();
                    } catch (Exception ex) {
                        //Log.e(TAG, ex.toString());
                    }
                }
            }
        }

        return isULEV1;
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

    private JSONObject prepareEncodeCardPayload() {
        JSONObject payload = new JSONObject();

        JSONArray arrayRoomNumber = new JSONArray();
        arrayRoomNumber.put(trueRoomNumber);

        //another jo
        JSONObject miniPayload = new JSONObject();
        JSONArray arrayRoomNumbers = new JSONArray();
        arrayRoomNumbers.put(trueRoomNumber);
        try {
            miniPayload.put("doors", arrayRoomNumbers);
            miniPayload.put("operation", "guest");
        } catch (Exception e) {
            //ExceptionUtils.reportException(e);
            return null; // Meant to be checked by calling method for error checking.
        }

        JSONArray arrayDoorOperation = new JSONArray();
        arrayDoorOperation.put(miniPayload);


        JSONArray arrayCardSerialNumber = new JSONArray();
        arrayCardSerialNumber.put(trueCardSerialNumber);

        /*JSONObject objectQuery = new JSONObject();
        try {
            objectQuery.put("override", "true");
            objectQuery.put("pendingImage", "true");
            objectQuery.put("sendOnline", "false");
        } catch (JSONException e) {
            e.printStackTrace();
        }*/

        try {

            //payload.put("card_holder", "Patrick");
            payload.put("cardHolder", strRoomNumber);
            payload.put("doorOperations", arrayDoorOperation);

            payload.put("startTime",trueStartDate);

            payload.put("expireTime", trueExpirationDate);

            payload.put("serialNumbers", arrayCardSerialNumber);

            payload.put("format", "rfid48");

        } catch (Exception e) {
            //ExceptionUtils.reportException(e);
            return null; // Meant to be checked by calling method for error checking.
        }

        return payload;
    }

    private JSONObject prepareEncodeCardJoinerPayload() {
        JSONObject payload = new JSONObject();

        JSONArray arrayRoomNumber = new JSONArray();
        arrayRoomNumber.put(trueRoomNumber);

        //another jo
        JSONObject miniPayload = new JSONObject();
        JSONArray arrayRoomNumbers = new JSONArray();
        arrayRoomNumbers.put(trueRoomNumber);
        try {
            miniPayload.put("doors", arrayRoomNumbers);
            miniPayload.put("operation", "guest");
        } catch (Exception e) {
            //ExceptionUtils.reportException(e);
            return null; // Meant to be checked by calling method for error checking.
        }

        JSONArray arrayDoorOperation = new JSONArray();
        arrayDoorOperation.put(miniPayload);

        JSONArray arrayCardSerialNumber = new JSONArray();
        arrayCardSerialNumber.put(trueCardSerialNumber);

        try {

            //payload.put("card_holder", "Marques");
            payload.put("cardHolder", strRoomNumber);
            payload.put("doorOperations", arrayDoorOperation);

            payload.put("startTime",trueStartDate);

            payload.put("expireTime", trueExpirationDate);

            payload.put("serialNumbers", arrayCardSerialNumber);

            payload.put("format", "rfid48");


        } catch (Exception e) {
            //ExceptionUtils.reportException(e);
            return null; // Meant to be checked by calling method for error checking.
        }

        return payload;
    }

    private JSONObject prepareCardsCardPayload() {
        JSONObject payload = new JSONObject();

        JSONObject objectQuery = new JSONObject();
        try {
            objectQuery.put("serialNumber", serialNumberCard);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            payload.put("query_string", objectQuery);

        } catch (Exception e) {
            //ExceptionUtils.reportException(e);
            return null; // Meant to be checked by calling method for error checking.
        }

        return payload;
    }

    private JSONObject prepareShowCardPayloadFromDoor(String hotelCardIdStr) {
        JSONObject payload = new JSONObject();

        try {
            payload.put("id", hotelCardIdStr);

        } catch (Exception e) {
            //ExceptionUtils.reportException(e);
            return null; // Meant to be checked by calling method for error checking.
        }

        return payload;
    }

    private JSONObject prepareShowCardPayload() {
        JSONObject payload = new JSONObject();

        try {
            payload.put("id", hotelCardId);

        } catch (Exception e) {
            //ExceptionUtils.reportException(e);
            return null; // Meant to be checked by calling method for error checking.
        }

        return payload;
    }

    private JSONObject prepareDoorCardPayload() {
        JSONObject payload = new JSONObject();

        JSONObject objectQuery = new JSONObject();
        try {
            objectQuery.put("expireTime", trueWriteCardEndDate);
            objectQuery.put("guestDoor", strRoomNumber);
            objectQuery.put("startTime", trueWriteCardStartDate);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            payload.put("query_string", objectQuery);

        } catch (Exception e) {
            //ExceptionUtils.reportException(e);
            return null; // Meant to be checked by calling method for error checking.
        }

        return payload;
    }

    public static void restartActivity(Activity activity) {
        if (Build.VERSION.SDK_INT >= 11) {
            activity.recreate();
        } else {
            activity.finish();
            activity.startActivity(activity.getIntent());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_reset, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.button_reset_menu) {
            final AlertDialog alertDialogLoading = MainActivity.showLoading(MainActivity.this, "Reset Input Value");

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
            LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View dialogView = inflater.inflate(R.layout.pop_up_message_option, null);

            TextView textTitle = dialogView.findViewById(R.id.text_title_pop_up_message_option);
            TextView textMessage = dialogView.findViewById(R.id.text_message_pop_up_message_option);
            Button buttonYes = dialogView.findViewById(R.id.button_yes_pop_up_message_option);
            Button buttonNo = dialogView.findViewById(R.id.button_no_pop_up_message_option);

            textTitle.setText("Reset");
            textMessage.setText("Confirm to reset this screen?");

            dialogBuilder.setCancelable(false);
            dialogBuilder.setView(dialogView);

            final AlertDialog alertDialogResetValue = dialogBuilder.create();
            alertDialogResetValue.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialogResetValue.show();

            buttonYes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    alertDialogResetValue.dismiss();
                    inputRoomNumber.setText("");
                    inputNumberOfNight.setText("");
                    restartActivity(mActivity);
                    alertDialogLoading.dismiss();
                }
            });

            buttonNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    alertDialogResetValue.dismiss();
                    alertDialogLoading.dismiss();
                }
            });
        } else if (id == R.id.button_door_menu) {
            Intent intent = new Intent(MainActivity.this, DoorActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }


}