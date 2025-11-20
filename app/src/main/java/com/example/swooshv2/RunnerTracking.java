package com.example.swooshv2;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class RunnerTracking extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "RunnerTracking";
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private FirebaseFirestore firebaseFirestore;
    private String runnerId;
    private String userEmail;
    private String status, pickupID;
    private Marker runnerMarker;

    // UI Components
    private TextView tvETA, tvStatus, tvRunnerName, tvRunnerPhone, tvPickupID;
    private ImageView ivRunnerProfile;

    private Button btnContactRunner, btnRefresh;
    private MapView mapView;
    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_runner_tracking);
        firebaseFirestore = FirebaseFirestore.getInstance();

        // Retrieve Intent Data
        runnerId = getIntent().getStringExtra("RunnerID");
        userEmail = getIntent().getStringExtra("UEmail");
        status = getIntent().getStringExtra("Status");
        pickupID = getIntent().getStringExtra("PickupID");

        //tvPickupID.setText(pickupID);

        // Validate Input Data
        if (runnerId == null || runnerId.isEmpty()) {
            Log.e(TAG, "RunnerID is null or empty. Exiting.");
            Toast.makeText(this, "No Runner ID provided!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (userEmail == null || userEmail.isEmpty()) {
            Log.e(TAG, "User Email is null or empty. Exiting.");
            Toast.makeText(this, "No User Email provided!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Log.d(TAG, "RunnerID received: " + runnerId);
        Log.d(TAG, "User Email received: " + userEmail);

        // Bind UI Components
        tvETA = findViewById(R.id.tvETA);
        tvStatus = findViewById(R.id.tvStatus);
        tvRunnerName = findViewById(R.id.tvRunnerName);
        tvPickupID = findViewById(R.id.tvPickupID);
        tvRunnerPhone = findViewById(R.id.tvRunnerPhone);
        ivRunnerProfile = findViewById(R.id.ivRunnerProfile);

        btnContactRunner = findViewById(R.id.btnContactRunner);
        btnRefresh = findViewById(R.id.btnRefresh);
        mapView = findViewById(R.id.mapView);

        // Initialize MapView
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        btnRefresh.setOnClickListener(v -> {
            Log.d("RunnerTracking", "Refresh button clicked.");
            fetchPickupStatusAndUpdateETA(); // Update the pickup status and ETA
            fetchRunnerLocation();          // Update runner's location on the map
        });

        setupContactRunnerButton();

        // Fetch Data
        fetchRunnerDetails();
        //startRealTimeETAUpdates();
        fetchPickupStatusAndUpdateETA();
        fetchRouteToDestination();

    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;
        checkLocationPermission();
    }

    private void fetchRunnerDetails() {
        // Fetch Runner Details
        firebaseFirestore.collection("Runners")
                .whereEqualTo("RunnerID", runnerId) // Query by RunnerID
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot document = querySnapshot.getDocuments().get(0); // Get the first matching document
                        String runnerName = document.getString("RName");
                        String runnerPhone = document.getString("RPhoneNo");
                        String runnerVehicle = document.getString("RVehicleNo");

                        // Debug Logs
                        Log.d(TAG, "RunnerID used for query: " + runnerId);
                        Log.d(TAG, "Fetched Runner Name: " + (runnerName != null ? runnerName : "NULL"));
                        Log.d(TAG, "Fetched Runner Phone: " + (runnerPhone != null ? runnerPhone : "NULL"));
                        Log.d(TAG, "Fetched Runner Vehicle: " + (runnerVehicle != null ? runnerVehicle : "NULL"));

                        // Update Runner Details on the UI
                        tvRunnerName.setText("Runner: " + runnerName);
                        tvRunnerPhone.setText("Phone: " + runnerPhone);
                        tvStatus.setText("Vehicle: " + runnerVehicle);

                        // Fetch Status from Pickups collection
                        fetchPickupStatus();
                    } else {
                        tvETA.setText("Runner not found");
                        Log.e(TAG, "Runner not found: " + runnerId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch runner details", e);
                    Toast.makeText(this, "Error fetching runner details.", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchPickupStatus() {
        // Fetch Status from Pickups collection
        firebaseFirestore.collection("Pickups")
                .whereEqualTo("RunnerID", runnerId) // Query by RunnerID
                .whereEqualTo("PickupID", pickupID) // Add specific PickupID if available
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot pickupDoc = querySnapshot.getDocuments().get(0); // Get the first matching document
                        String pickupStatus = pickupDoc.getString("Status");

                        // Debug Logs
                        Log.d(TAG, "RunnerID used for query: " + runnerId);
                        Log.d(TAG, "Fetched Pickup Status: " + (pickupStatus != null ? pickupStatus : "NULL"));

                        // Update Status on the UI
                        if (pickupStatus != null) {
                            tvStatus.setText("Status: " + pickupStatus);
                        } else {
                            tvStatus.setText("Status unavailable");
                            Log.e(TAG, "Pickup Status is null");
                        }
                    } else {
                        tvStatus.setText("Pickup not found");
                        Log.e(TAG, "No matching Pickup found for RunnerID: " + runnerId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch Pickup Status", e);
                    Toast.makeText(this, "Error fetching Pickup Status.", Toast.LENGTH_SHORT).show();
                });
    }



    void fetchPickupStatusAndUpdateETA() {
        firebaseFirestore.collection("Pickups")
                .whereEqualTo("RunnerID", runnerId) // Filter by RunnerID
                .whereEqualTo("PickupID", pickupID) // Filter by PickupID
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error fetching pickup status", e);
                        return;
                    }

                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        DocumentSnapshot document = querySnapshot.getDocuments().get(0); // Get the first matching document
                        String pickupStatus = document.getString("Status");

                        // Debug Logs
                        Log.d(TAG, "RunnerID used for query: " + runnerId);
                        Log.d(TAG, "Pickup status updated: " + pickupStatus);

                        if (pickupStatus != null) {
                            switch (pickupStatus) {
                                case "Assigned":
                                    updateETAFromPPPToUser();
                                    tvStatus.setText("Runner has been assigned. Waiting for updates...");
                                    break;
                                case "Picked Up":
                                    updateETAFromPPPToUser();
                                    tvStatus.setText("Runner has picked up your parcel.");// Update ETA from PPP to user
                                    break;
                                case "Incoming":
                                    fetchRunnerLocation(); // Update ETA based on runner's location
                                    tvStatus.setText("Runner is on their way!");
                                    break;
                                case "Delivered":
                                    showDeliveryConfirmationDialog();
                                    tvETA.setText("Delivery completed.");
                                    tvStatus.setText("Delivery completed.");
                                    break;
                                default:
                                    tvStatus.setText("Status: " + pickupStatus);
                                    break;
                            }
                        } else {
                            tvETA.setText("Status unavailable");
                            Log.e(TAG, "Status field is null");
                        }
                    } else {
                        tvETA.setText("Pickup not found");
                        Log.e(TAG, "No matching pickup found for RunnerID: " + runnerId);
                    }
                });
    }

    private void fetchDeliveryCompletionTime() {
        firebaseFirestore.collection("Pickups").document(pickupID)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        com.google.firebase.Timestamp timeCompleted = document.getTimestamp("TimePickupCompleted");
                        if (timeCompleted != null) {
                            // Convert the timestamp to a human-readable date-time string
                            String completionTime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                    .format(timeCompleted.toDate());
                            tvETA.setText("Delivered at: " + completionTime);
                        } else {
                            Log.e("RunnerTracking", "TimePickupCompleted is null for PickupID: " + pickupID);
                            tvETA.setText("Delivery time unavailable.");
                        }
                    } else {
                        Log.e("RunnerTracking", "Pickup document not found for ID: " + pickupID);
                        tvETA.setText("Delivery time unavailable.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("RunnerTracking", "Failed to fetch delivery time", e);
                    tvETA.setText("Error fetching delivery time.");
                });
    }

    private void showDeliveryConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delivery Completed")
                .setMessage("The runner has arrived! Please collect your parcel.")
                .setPositiveButton("OK", (dialog, which) -> {
                    fetchDeliveryCompletionTime();
                    tvETA.setText("Delivery completed.");
                    tvStatus.setText("Delivery completed.");
                    Intent feedbackIntent = new Intent(RunnerTracking.this, FeedbackActivity.class);
                    feedbackIntent.putExtra("PickupID", pickupID); // Pass PickupID if needed
                    feedbackIntent.putExtra("UEmail", userEmail);
                    startActivity(feedbackIntent);
                    dialog.dismiss();
                })
                .setCancelable(false) // Prevent dialog from being dismissed by tapping outside
                .show();
    }


    private void updateETAFromPPPToUser() {
        LatLng pppLocation = new LatLng(2.310064406660653, 102.31859876730901);

        // Fetch user's location
        firebaseFirestore.collection("Users")
                .whereEqualTo("UEmail", userEmail) // Query using the user's email
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot userDocument = querySnapshot.getDocuments().get(0);
                        GeoPoint userGeoPoint = userDocument.getGeoPoint("UDormLoc");
                        if (userGeoPoint != null) {
                            double distance = calculateDistance(
                                    pppLocation.latitude,
                                    pppLocation.longitude,
                                    userGeoPoint.getLatitude(),
                                    userGeoPoint.getLongitude()
                            );


                            double speedMetersPerSecond = 1.4;
                            int etaMinutes = calculateETA(distance, speedMetersPerSecond);

                            // Update ETA TextView
                            tvETA.setText("Arriving in: " + etaMinutes + " mins");
                        } else {
                            Log.e(TAG, "User's UDormLoc field is null");
                            tvETA.setText("User location unavailable.");
                        }
                    } else {
                        Log.e(TAG, "User not found with email: " + userEmail);
                        tvETA.setText("User data unavailable.");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching user location", e));
    }


    private int calculateETA(double distanceInMeters, double speedInMetersPerSecond) {
        return (int) (distanceInMeters / speedInMetersPerSecond / 60); // Convert to minutes
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371e3; // Earth's radius in meters
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double deltaPhi = Math.toRadians(lat2 - lat1);
        double deltaLambda = Math.toRadians(lon2 - lon1);

        double a = Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2)
                + Math.cos(phi1) * Math.cos(phi2)
                * Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // Distance in meters
    }




    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{ Manifest.permission.ACCESS_FINE_LOCATION },
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
        else {
            enableLocationOnMap();
        }
    }

    private void enableLocationOnMap() {
        if (googleMap != null) {
            try {
                googleMap.setMyLocationEnabled(true);
            }
            catch (SecurityException e) {
                Log.e(TAG, "Location permission not granted.", e);
            }
        }
    }

    private void fetchRunnerLocation() {
        firebaseFirestore.collection("Runners")
                .whereEqualTo("RunnerID", runnerId) // Query for the specific RunnerID
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error listening for location updates", e);
                        return;
                    }

                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        DocumentSnapshot document = querySnapshot.getDocuments().get(0); // Get the first matching document
                        GeoPoint location = document.getGeoPoint("RLocation");
                        if (location != null) {
                            LatLng runnerLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            updateRunnerLocation(runnerLatLng); // Update the map with runner's current location

                            // Fetch User Location
                            firebaseFirestore.collection("Users")
                                    .whereEqualTo("UEmail", userEmail)
                                    .get()
                                    .addOnSuccessListener(querySnapshotUser -> {
                                        if (!querySnapshotUser.isEmpty()) {
                                            DocumentSnapshot userDocument = querySnapshotUser.getDocuments().get(0);
                                            GeoPoint userLocation = userDocument.getGeoPoint("UDormLoc");
                                            if (userLocation != null) {
                                                double distance = calculateDistance(
                                                        location.getLatitude(),
                                                        location.getLongitude(),
                                                        userLocation.getLatitude(),
                                                        userLocation.getLongitude()
                                                );

                                                double speedMetersPerSecond = 1.4; // Average walking speed
                                                int etaMinutes = calculateETA(distance, speedMetersPerSecond);

                                                // Update ETA TextView
                                                tvETA.setText("Arriving in: " + etaMinutes + " mins");
                                                LatLng userLatLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
                                                googleMap.addMarker(new MarkerOptions()
                                                        .position(userLatLng)
                                                        .title("User's Location")
                                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.loc)) // Use your custom icon
                                                );

                                                // Optionally move the camera to show the user's location
                                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15));
                                            }
                                        }
                                    });
                        } else {
                            Log.e(TAG, "Runner's location (RLocation) is null");
                        }
                    } else {
                        Log.e(TAG, "Runner not found or document does not exist: " + runnerId);
                    }
                });
    }



    private void updateRunnerLocation(LatLng location) {
        if (googleMap != null) {
            if (runnerMarker == null) {
                // Add a new marker if it doesn't exist
                runnerMarker = googleMap.addMarker(new MarkerOptions()
                        .position(location)
                        .title("Runner's Current Location")
                        .icon(getScaledBitmapDescriptor(R.drawable.markerrun))); // Use dynamically scaled icon
            } else {
                // Update the marker's position if it already exists
                runnerMarker.setPosition(location);
            }

            // Optionally move the camera to follow the runner
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        }
    }

    // Helper function to scale bitmap
    private BitmapDescriptor getScaledBitmapDescriptor(int resourceId) {
        int height = 80; // Desired height in pixels
        int width = 80;  // Desired width in pixels
        BitmapDrawable bitmapDrawable = (BitmapDrawable) ContextCompat.getDrawable(this, resourceId);
        if (bitmapDrawable == null) return null;
        Bitmap originalBitmap = bitmapDrawable.getBitmap();
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, false);
        return BitmapDescriptorFactory.fromBitmap(scaledBitmap);
    }


    private void fetchRouteToDestination() {
        // PPP Location
        LatLng pppLocation = new LatLng(2.310064406660653, 102.31859876730901);

        // Fetch User's Location by querying `UEmail`
        firebaseFirestore.collection("Users")
                .whereEqualTo("UEmail", userEmail) // Query using the field
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                        GeoPoint userGeoPoint = documentSnapshot.getGeoPoint("UDormLoc");
                        if (userGeoPoint != null) {
                            LatLng userLocation = new LatLng(userGeoPoint.getLatitude(), userGeoPoint.getLongitude());
                            fetchRunnerLocationAndDrawRoutes(pppLocation, userLocation);
                        } else {
                            Log.e(TAG, "User's UDormLoc field is null");
                            Toast.makeText(this, "User location is unavailable.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "User not found: " + userEmail);
                        Toast.makeText(this, "User data unavailable.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching user location", e));
    }


    private void fetchRunnerLocationAndDrawRoutes(LatLng pppLocation, LatLng userLocation) {
        firebaseFirestore.collection("Runners")
                .whereEqualTo("RunnerID", runnerId) // Use `whereEqualTo` to query by RunnerID
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error fetching runner location", e);
                        return;
                    }

                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        DocumentSnapshot document = querySnapshot.getDocuments().get(0); // Get the first matching document
                        GeoPoint runnerGeoPoint = document.getGeoPoint("RLocation");
                        if (runnerGeoPoint != null) {
                            LatLng runnerLocation = new LatLng(runnerGeoPoint.getLatitude(), runnerGeoPoint.getLongitude());
                            double distanceToPPP = calculateDistance(
                                    runnerLocation.latitude,
                                    runnerLocation.longitude,
                                    pppLocation.latitude,
                                    pppLocation.longitude
                            );
                            if (distanceToPPP < 50) { // Assume 50 meters proximity
                                Log.d(TAG, "Runner is at or leaving PPP. Drawing route to user's location.");
                                fetchAndDrawSingleRoute(buildDirectionsUrl(runnerLocation, userLocation), this::drawRoute);
                            } else {
                                Log.d(TAG, "Runner is between PPP and user's location. Drawing updated route.");
                                fetchAndDrawSingleRoute(buildDirectionsUrl(runnerLocation, userLocation), this::drawRoute);
                            }
                            //fetchAndDrawRoute(runnerLocation, pppLocation, userLocation);
                        } else {
                            Log.e(TAG, "Runner location is null");
                        }
                    } else {
                        Log.e(TAG, "Runner not found with RunnerID: " + runnerId);
                    }
                });
    }



    // Fetch and Draw Routes Between 3 Locations
    private void fetchAndDrawRoute(LatLng runnerLocation, LatLng pppLocation, LatLng userLocation) {
        // Define API URLS
        String url1 = buildDirectionsUrl(runnerLocation, pppLocation);
        String url2 = buildDirectionsUrl(pppLocation, userLocation);

        // Fetch and Draw Routes
        fetchAndDrawSingleRoute(url1, routePoints -> {
            drawRoute(routePoints);
            fetchAndDrawSingleRoute(url2, this::drawRoute);
        });
    }

    private String buildDirectionsUrl(LatLng origin, LatLng destination) {
        return "YOUR_API_KEY_HERE"
                + origin.latitude + "," + origin.longitude
                + "&destination=" + destination.latitude + "," + destination.longitude
                + "YOUR_API_KEY_HERE";
    }


    // Fetch a Single Route
    private void fetchAndDrawSingleRoute(String url, RouteCallback callback) {
        new Thread(() -> {
            try {
                URL apiUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }

                JSONObject jsonResponse = new JSONObject(builder.toString());
                JSONArray routes = jsonResponse.getJSONArray("routes");
                if (routes.length() > 0) {
                    JSONObject route = routes.getJSONObject(0);
                    String polyline = route.getJSONObject("overview_polyline").getString("points");
                    List<LatLng> routePoints = PolyUtil.decode(polyline);

                    runOnUiThread(() -> callback.onRouteFetched(routePoints));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching route", e);
            }
        }).start();
    }

    // Callback Interface for Handling Routes
    private interface RouteCallback {
        void onRouteFetched(List<LatLng> routePoints);
    }

    private void setupContactRunnerButton() {
        btnContactRunner.setOnClickListener(view -> {
            // Fetch the runner's phone number from Firestore
            firebaseFirestore.collection("Runners")
                    .whereEqualTo("RunnerID", runnerId)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            DocumentSnapshot document = querySnapshot.getDocuments().get(0); // Get the first matching document
                            String runnerPhone = document.getString("RPhoneNo");

                            if (runnerPhone != null && !runnerPhone.isEmpty()) {
                                // Initiate phone call
                                makePhoneCall(runnerPhone);
                            } else {
                                Toast.makeText(this, "Runner's phone number is unavailable.", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Runner phone number is null or empty");
                            }
                        } else {
                            Toast.makeText(this, "Runner not found.", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Runner not found with RunnerID: " + runnerId);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to fetch runner details.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error fetching runner details", e);
                    });
        });
    }

    private void makePhoneCall(String phoneNumber) {
        // Check if the CALL_PHONE permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            // Create an Intent to initiate the call
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(callIntent);
        } else {
            // Request the CALL_PHONE permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 101);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted! Try calling again.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied to make calls.", Toast.LENGTH_SHORT).show();
            }
        }
    }



    private void drawRoute(List<LatLng> routePoints) {
        if (googleMap != null) {
            googleMap.clear();

            // Draw the route on the map
            PolylineOptions polylineOptions = new PolylineOptions()
                    .addAll(routePoints)
                    .width(10)
                    .color(ContextCompat.getColor(this, R.color.routeColor))
                    .geodesic(true);
            googleMap.addPolyline(polylineOptions);

            // Adjust camera to show the entire route
            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
            for (LatLng point : routePoints) {
                boundsBuilder.include(point);
            }
            LatLngBounds bounds = boundsBuilder.build();
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100)); // 100 = padding in pixels

        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        mapView.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }
        mapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
