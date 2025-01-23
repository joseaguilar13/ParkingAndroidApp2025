package com.navix.mapboxvainilla;

import android.app.Activity;

import android.content.Intent;
import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
//import android.support.annotation.NonNull;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.ResponseBody;
import android.widget.Toast;
import java.io.IOException;

/**
 * Using the onClick attribute of the Button. (Beginner)
 * Each button has it's own method that is attributed to the onClick attribute of each button
 */
public class Activity2 extends Activity {

   // private TextView dateEdt;

    //CalendarView calendar;
    //TextView date_view;

    private EditText dateEdt;
    private EditText dateEdt2;

    private EditText dateEdt3;

    private int numberOfDays;
    private int numberOfDays1;
    private int numberOfDays2;
    private double costPerDay;
    private double totalCost;

    // TextView references for the table rows
    private TextView tvDaysParkingValue;
    private TextView tvCostPerDayValue;
    private TextView tvTotalCostValue;
    private TextView tvTotalToPayValue;

    private static final String BASE_URL = "http://18.223.248.183:3000/";

    //private val dateEdt: EditText

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form);

        // on below line we are initializing our variables.


        // on below line we are adding
        // click listener for our edit text.
        // By ID we can use each component
        // which id is assign in xml file
        // use findViewById() to get the
        // CalendarView and TextView



// on below line we are initializing our variables.
        dateEdt = findViewById(R.id.date_view);
        dateEdt2 = findViewById(R.id.date_view2);
        dateEdt3 = findViewById(R.id.date_view3);

        Bundle bundle = getIntent().getExtras();
        String count = bundle.getString("no");
        dateEdt3.setText("Selected parking slot" + ": " + count);

        makeReservation();

        // on below line we are adding click listener
        // for our pick date button
        dateEdt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // on below line we are getting
                // the instance of our calendar.
                final Calendar c = Calendar.getInstance();

                // on below line we are getting
                // our day, month and year.
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);
                int hour = c.get(Calendar.HOUR);
                int minute = c.get(Calendar.MINUTE);
                // on below line we are creating a variable for date picker dialog.
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        // on below line we are passing context.
                        Activity2.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override

                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // on below line we are setting date to our edit text.
                                dateEdt.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                                numberOfDays1 = dayOfMonth;

                            }
                        },
                        // on below line we are passing year,
                        // month and day for selected date in our date picker.
                        year, month, day);

                // at last we are calling show to
                // display our date picker dialog.
                datePickerDialog.show();
            }
        });

        // on below line we are adding click listener
        // for our pick date button
        dateEdt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // on below line we are getting
                // the instance of our calendar.
                final Calendar c = Calendar.getInstance();

                // on below line we are getting
                // our day, month and year.
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);
                int hour = c.get(Calendar.HOUR);
                int minute = c.get(Calendar.MINUTE);
                // on below line we are creating a variable for date picker dialog.
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        // on below line we are passing context.
                        Activity2.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override

                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // on below line we are setting date to our edit text.
                                dateEdt2.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                                numberOfDays2=dayOfMonth;

                            }
                        },
                        // on below line we are passing year,
                        // month and day for selected date in our date picker.
                        year, month, day);

                // at last we are calling show to
                // display our date picker dialog.
                datePickerDialog.show();
            }
        });
numberOfDays = numberOfDays2;
costPerDay = 10.0;


        calculateTotalCost();

        // Initialize TextViews from the layout
        initializeViews();

        // Set values in the table
        updateTableValues();



                    }


    private void calculateTotalCost() {
        // Simple calculation of total cost
        totalCost = numberOfDays * costPerDay;
    }

    private void initializeViews() {
        // Find TextViews by their IDs from the XML layout
        tvDaysParkingValue = findViewById(R.id.tv_days_parking_value);
        tvCostPerDayValue = findViewById(R.id.tv_cost_per_day_value);
        tvTotalCostValue = findViewById(R.id.tv_total_cost_value);
        tvTotalToPayValue = findViewById(R.id.tv_total_to_pay_value);
    }

    private void updateTableValues() {
        // Update TextViews with calculated values
        tvDaysParkingValue.setText(String.valueOf(numberOfDays));
        tvCostPerDayValue.setText(String.format("$%.2f", costPerDay));
        tvTotalCostValue.setText(String.format("$%.2f", totalCost));
        tvTotalToPayValue.setText(String.format("$%.2f", totalCost));
    }
    public void goToActivity2(View view) {
        Intent Intent = new Intent(this, Activity2.class);
        startActivity(Intent);
    }

    public void goToActivity3(View view) {
        Intent Intent = new Intent(this, Activity2.class);
        startActivity(Intent);
    }


    private void makeReservation() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        ReservationRequest request = new ReservationRequest(
                "USER003",
                "SLOT039",
                "37.7787,-122.4232",
                "2025-01-05T18:00:00Z",
                3
        );

        Call<ResponseBody> call = apiService.createReservation(request);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful()) {
                        // Obtener el cuerpo de la respuesta como String
                        String responseBody = response.body().string();
                        Toast.makeText(Activity2.this, "Respuesta: " + responseBody, Toast.LENGTH_LONG).show();
                    } else {
                        // Si hay un error, intentar obtener el mensaje de error
                        String errorBody = response.errorBody().string();
                        Toast.makeText(Activity2.this, "Error: " + response.code() + " " + errorBody, Toast.LENGTH_LONG).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(Activity2.this, "Error al leer la respuesta: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(Activity2.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}




