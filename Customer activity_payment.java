<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <!-- Toolbar with Back Button -->
    <androidx.appcompat.widget.Toolbar
        android:id="@+id/toolbar"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="#6200EA"
        app:navigationIcon="?attr/homeAsUpIndicator"
        android:theme="@style/ThemeOverlay.AppCompat.Dark.ActionBar">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Order Summary"
            android:textSize="20sp"
            android:textColor="#FFFFFF"/>
    </androidx.appcompat.widget.Toolbar>

    <!-- Main content -->
    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="16dp">

            <!-- Order Details Card -->
            <androidx.cardview.widget.CardView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginBottom="16dp"
                app:cardElevation="4dp"
                app:cardCornerRadius="8dp">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:padding="16dp">

                    <!-- Pickup Location -->
                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Pickup Location"
                        android:textStyle="bold"
                        android:textSize="16sp"
                        android:layout_marginBottom="4dp"/>
                    <TextView
                        android:id="@+id/textPickupLocation"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Campus Center, University Road"
                        android:layout_marginBottom="12dp"/>

                    <!-- Drop-off Location -->
                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Drop-off Location"
                        android:textStyle="bold"
                        android:textSize="16sp"
                        android:layout_marginBottom="4dp"/>
                    <TextView
                        android:id="@+id/textDropOffLocation"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Library Building, East Wing"
                        android:layout_marginBottom="12dp"/>

                    <!-- Item Description -->
                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Item Description"
                        android:textStyle="bold"
                        android:textSize="16sp"
                        android:layout_marginBottom="4dp"/>
                    <TextView
                        android:id="@+id/textItemDescription"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Textbooks and laptop"
                        android:layout_marginBottom="12dp"/>

                    <!-- Delivery Time -->
                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Delivery Time"
                        android:textStyle="bold"
                        android:textSize="16sp"
                        android:layout_marginBottom="4dp"/>
                    <TextView
                        android:id="@+id/textDeliveryTime"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Dec 29, 2024 02:30 PM"
                        android:layout_marginBottom="12dp"/>

                    <!-- Selected Runner -->
                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Selected Runner"
                        android:textStyle="bold"
                        android:textSize="16sp"
                        android:layout_marginBottom="4dp"/>
                    <TextView
                        android:id="@+id/textSelectedRunner"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Runner 1 (4.5★)"
                        android:layout_marginBottom="12dp"/>

                    <!-- Delivery Notes -->
                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Delivery Notes"
                        android:textStyle="bold"
                        android:textSize="16sp"
                        android:layout_marginBottom="4dp"/>
                    <TextView
                        android:id="@+id/textDeliveryNotes"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Please handle with care"
                        android:layout_marginBottom="12dp"/>
                </LinearLayout>
            </androidx.cardview.widget.CardView>

            <!-- Price Details Card -->
            <androidx.cardview.widget.CardView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginBottom="16dp"
                app:cardElevation="4dp"
                app:cardCornerRadius="8dp">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:padding="16dp">

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Price Details"
                        android:textStyle="bold"
                        android:textSize="18sp"
                        android:layout_marginBottom="16dp"/>

                    <!-- Base Price -->
                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="horizontal"
                        android:layout_marginBottom="8dp">
                        <TextView
                            android:layout_width="0dp"
                            android:layout_height="wrap_content"
                            android:layout_weight="1"
                            android:text="Base Price"/>
                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="RM 15.00"/>
                    </LinearLayout>

                    <!-- Service Fee -->
                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="horizontal"
                        android:layout_marginBottom="8dp">
                        <TextView
                            android:layout_width="0dp"
                            android:layout_height="wrap_content"
                            android:layout_weight="1"
                            android:text="Service Fee"/>
                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="RM 2.00"/>
                    </LinearLayout>

                    <!-- Total -->
                    <View
                        android:layout_width="match_parent"
                        android:layout_height="1dp"
                        android:background="#DDDDDD"
                        android:layout_marginVertical="8dp"/>
                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="horizontal">
                        <TextView
                            android:layout_width="0dp"
                            android:layout_height="wrap_content"
                            android:layout_weight="1"
                            android:text="Total"
                            android:textStyle="bold"
                            android:textSize="16sp"/>
                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="RM 17.00"
                            android:textStyle="bold"
                            android:textSize="16sp"/>
                    </LinearLayout>
                </LinearLayout>
            </androidx.cardview.widget.CardView>
        </LinearLayout>
    </ScrollView>

    <!-- Make Payment Button -->
    <com.google.android.material.button.MaterialButton
        android:id="@+id/buttonMakePayment"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Make Payment"
        android:textSize="18sp"
        android:backgroundTint="#8BC34A"
        android:layout_margin="16dp"/>

</LinearLayout>
