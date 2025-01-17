package com.example.uniride;

import android.util.Log;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;

public class HomeBookingActivity extends BottomNavigationActivity {

    TextView titleText;
    String bookingType;

    private RecyclerView recyclerView;
    private MyHomeBookingAdapter adapter;
    private List<BookingModel> bookingList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_booking);

        titleText = findViewById(R.id.titleText);

        Intent i = getIntent();
        bookingType = i.getStringExtra("bookingTypePassed");

        if (bookingType.equals("scheduled")) {
            titleText.setText("Bookings Scheduled Today");
        } else if (bookingType.equals("requests")) {
            titleText.setText("Pending Booking Requests");
        } else if (bookingType.equals("accepted")) {
            titleText.setText("Other Accepted Bookings");
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            startActivity(new Intent(this, AccountLoginActivity.class));
            finish();
            return;
        }

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        bookingList = new ArrayList<>();
        adapter = new MyHomeBookingAdapter(bookingList, bookingType, this);
        recyclerView.setAdapter(adapter);

        loadDriverBookings();
    }

    private void loadDriverBookings() {
        db.collection(MyFirestoreReferences.USERS_COLLECTION)
            .document(currentUser.getUid())
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    int userId = ((Long) documentSnapshot.get("userID")).intValue();

                    // Query rides from user ID
                    db.collection(MyFirestoreReferences.RIDES_COLLECTION)
                        .whereEqualTo("driverID", userId)
                        .get()
                        .addOnSuccessListener(ridesSnapshot -> {
                            Log.d("HomeBookingActivity", "Number of rids for driver ID " + userId + ": " + ridesSnapshot.size());

                            final int[] completedRides = {0};
                            int totalRides = ridesSnapshot.size();

                            if (totalRides == 0) {
                                bookingList.clear();
                                adapter.notifyDataSetChanged();
                                return;
                            }

                            for (QueryDocumentSnapshot rideDoc : ridesSnapshot) {
                                // RideModel ride = RideModel.fromMap(rideDoc.getData());
                                int rideID = ((Long) rideDoc.get("rideID")).intValue();
                                fetchBookingsForRideIds(rideID, completedRides, totalRides);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(HomeBookingActivity.this,
                                "Error fetching rides: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        });
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(HomeBookingActivity.this,
                    "Error fetching user data: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            });
    }

    private void fetchBookingsForRideIds(int rideID, final int[] completedRides, int totalRides) {
        // Query bookings where the ride_id is of type Long
        db.collection(MyFirestoreReferences.BOOKINGS_COLLECTION)
            .whereEqualTo("ride_id", rideID) // Use Long for the comparison
            .get()
            .addOnSuccessListener(bookingsSnapshot -> {
                Log.d("HomeBookingActivity", "Number of bookings for ride ID " + rideID + ": " + bookingsSnapshot.size());

                for (QueryDocumentSnapshot bookingDoc : bookingsSnapshot) {
                    BookingModel booking = BookingModel.fromMap(bookingDoc.getData());
                    bookingList.add(booking);

                    if (bookingType.equals("scheduled")) {
                        if (isSameDateAsToday(booking.getDate())) {
                            bookingList.add(booking);
                            adapter.notifyDataSetChanged();
                            completedRides[0]++;
                        }
                    } else if (bookingType.equals("requests")) {
                        if (!booking.isAccepted()) {
                            bookingList.add(booking);
                            adapter.notifyDataSetChanged();
                            completedRides[0]++;
                        }
                    } else if (bookingType.equals("accepted")) {
                        if (booking.isAccepted()) {
                            bookingList.add(booking);
                            adapter.notifyDataSetChanged();
                            completedRides[0]++;
                        }
                    }
                }

                // After fetching all bookings, notify the adapter
                if (completedRides[0] == totalRides) {
                    adapter.notifyDataSetChanged();
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(HomeBookingActivity.this,
                        "Error fetching bookings: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            });
    }

    public boolean isSameDateAsToday(String bookingDate) {
        // Format of the booking date string
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        try {
            // Parse booking date string into a Date object
            Date bookingDateObj = dateFormat.parse(bookingDate);

            // Get today's date as a Date object
            Date today = new Date(); // Current date and time
            String todayStr = dateFormat.format(today); // Format to match booking date
            Date todayObj = dateFormat.parse(todayStr); // Parse to Date object

            // Compare the two Date objects
            return bookingDateObj.equals(todayObj);

        } catch (ParseException e) {
            // Handle parsing error
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentUser != null) {
            loadDriverBookings();
        }
    }

    @Override
    protected int getSelectedItemId() {
        return R.id.driver;
    }
}