package com.example.uniride;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.DatePickerDialog;
import android.util.Log;
import android.widget.DatePicker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;



public class BookingHomeActivity extends BottomNavigationActivity {

    private ArrayList<BookingModel> myBookingData;
    AutoCompleteTextView originInput;
    AutoCompleteTextView destinationInput;
    EditText dateInput;
    AutoCompleteTextView passengerInput;
    Button searchBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.booking_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Connect the recyclerview
        RecyclerView recyclerView = findViewById(R.id.myBookingsRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load data
        ArrayList<BookingModel> myBookingData = DataGenerator.loadBookingData();
        ArrayList<LocationModel> locations = DataGenerator.loadLocationData();
        Integer[] numPassengers = {1, 2, 3, 4, 5, 6};

        // Set Adapter
        MyBookingHomeAdapter myHomeAdapter = new MyBookingHomeAdapter(myBookingData, BookingHomeActivity.this);
        recyclerView.setAdapter(myHomeAdapter);

        // Declarations
        originInput = findViewById(R.id.originInput);
        destinationInput = findViewById(R.id.destinationInput);
        dateInput = findViewById(R.id.dateInput);
        passengerInput = findViewById(R.id.passengerInput);
        searchBtn = findViewById(R.id.searchBtn);

        // Declare autocomplete fields
        ArrayAdapter<LocationModel> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, locations);
        originInput.setThreshold(0);
        originInput.setAdapter(adapter);
        destinationInput.setThreshold(0);
        destinationInput.setAdapter(adapter);
        ArrayAdapter<Integer> passengerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, numPassengers);
        passengerInput.setThreshold(0);
        passengerInput.setAdapter(passengerAdapter);

        // Open date selector when clicked
        dateInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the current date as default in the DatePicker
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                // Create a DatePickerDialog
                DatePickerDialog datePickerDialog = new DatePickerDialog(BookingHomeActivity.this, R.style.CustomDatePickerDialog,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                // month is 0-indexed, so add 1 for display
                                String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                                dateInput.setText(selectedDate);
                            }
                        }, year, month, day);

                datePickerDialog.show();
            }
        });

        originInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                originInput.showDropDown();
            }
        });

        destinationInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                destinationInput.showDropDown();
            }
        });

        passengerInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passengerInput.showDropDown();
            }
        });

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isAnyFieldEmpty()) {
                    Toast.makeText(BookingHomeActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else {
                    Intent i = new Intent(BookingHomeActivity.this, BookingSearchActivity.class);
                    i.putExtra("originInput", originInput.getText().toString());
                    i.putExtra("destinationInput", destinationInput.getText().toString());
                    i.putExtra("dateInput", dateInput.getText().toString());
                    i.putExtra("passengerInput", Integer.parseInt(passengerInput.getText().toString()));
                    i.putExtra("myBookingData", myBookingData);
                    i.putExtra("locations", locations);
                    startActivity(i);
                }
            }
        });

    }

    @Override
    protected int getSelectedItemId() {
        return R.id.home;
    }



    // Helper to check if any EditText is empty
    private boolean isAnyFieldEmpty() {
        return originInput.getText().toString().trim().isEmpty() ||
                destinationInput.getText().toString().trim().isEmpty() ||
                dateInput.getText().toString().trim().isEmpty() ||
                passengerInput.getText().toString().trim().isEmpty();
    }
}