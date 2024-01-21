package com.example.froyo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.model.Card;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;
import com.stripe.android.view.CardInputWidget;
import com.stripe.android.view.ErrorListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PurchaseEmojiActivity extends AppCompatActivity {

    private Button purchaseButton7, purchaseButton8, purchaseButton9, purchaseButton10;
    private ImageView emoji7, emoji8, emoji9, emoji10;
    private PaymentSheet paymentSheet;
    private ArrayList<String> purchasedEmoji;
    private String customerID, EphericalKey, ClientSecret, userID, email, selectedEmoji;
    private String SECRET_KEY = "sk_test_51OadtjI48YoKzGqJ80i0nwCyZnkofN7KdTFTnLu4Uu4oHaYZENRCc8LTwMJcRrwTomx8ezwHAnWGQgAnUNIRVJDM00FghjDe2Q";
    private String PUBLIC_KEY = "pk_test_51OadtjI48YoKzGqJCGMReCs1Te181ZUWhxUR2kEXAa0YEjKciSRp1jCow3LGqPsJQboDfjgc30qObHFPM2xQu2Oh00dtaF5mnT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_emoji);

        Intent i = getIntent();
        userID = i.getStringExtra("userId");
        email = i.getStringExtra("email");


        fetchUserData();

        emoji7 = findViewById(R.id.emoji7);
        emoji8 = findViewById(R.id.emoji8);
        emoji9 = findViewById(R.id.emoji9);
        emoji10 = findViewById(R.id.emoji10);
        DrawableImageViewTarget gifImage1 = new DrawableImageViewTarget(emoji7);
        Glide.with(this).load(R.drawable.globe).into(gifImage1);
        DrawableImageViewTarget gifImage2 = new DrawableImageViewTarget(emoji8);
        Glide.with(this).load(R.drawable.fire).into(gifImage2);
        DrawableImageViewTarget gifImage3 = new DrawableImageViewTarget(emoji9);
        Glide.with(this).load(R.drawable.clap).into(gifImage3);
        DrawableImageViewTarget gifImage4 = new DrawableImageViewTarget(emoji10);
        Glide.with(this).load(R.drawable.okie).into(gifImage4);

        purchaseButton7 = findViewById(R.id.purchaseButton7);
        purchaseButton8 = findViewById(R.id.purchaseButton8);
        purchaseButton9 = findViewById(R.id.purchaseButton9);
        purchaseButton10 = findViewById(R.id.purchaseButton10);


        PaymentConfiguration.init(this, PUBLIC_KEY);
        paymentSheet = new PaymentSheet(this,paymentSheetResult -> {
            onPaymentResult(paymentSheetResult);
        });

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                "https://api.stripe.com/v1/customers",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            if (customerID == null){
                                customerID = object.getString("id");
                                updateStripeID(email, customerID);
                            }
                            Toast.makeText(PurchaseEmojiActivity.this, customerID, Toast.LENGTH_SHORT).show();
                            getEphericalKey(customerID);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Authorization","Bearer " + SECRET_KEY);
                return header;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(PurchaseEmojiActivity.this);
        requestQueue.add(stringRequest);

        purchaseButton7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedEmoji = "7";
                PaymentFlow();
            }
        });
        purchaseButton8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedEmoji = "8";
                PaymentFlow();
            }
        });
        purchaseButton9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedEmoji = "9";
                PaymentFlow();
            }
        });
        purchaseButton10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedEmoji = "10";
                PaymentFlow();
            }
        });



    }

    private void onPaymentResult(PaymentSheetResult paymentSheetResult) {

        if (paymentSheetResult instanceof PaymentSheetResult.Completed){
            addPurchasedEmoji(email, selectedEmoji);
            switch(selectedEmoji) {
                case "7":
                    // code block
                    purchaseButton7.setText("Purchased");
                    purchaseButton7.setEnabled(false);
                    purchaseButton7.setOnClickListener(null);
                    break;
                case "8":
                    // code block
                    purchaseButton8.setText("Purchased");
                    purchaseButton8.setEnabled(false);
                    purchaseButton8.setOnClickListener(null);
                    break;
                case "9":
                    // code block
                    purchaseButton9.setText("Purchased");
                    purchaseButton9.setEnabled(false);
                    purchaseButton9.setOnClickListener(null);
                    break;
                case "10":
                    // code block
                    purchaseButton10.setText("Purchased");
                    purchaseButton10.setEnabled(false);
                    purchaseButton10.setOnClickListener(null);
                    break;
                default:
                    // code block
            }

            Toast.makeText(PurchaseEmojiActivity.this, "Payment Success for Emoji " + selectedEmoji, Toast.LENGTH_SHORT).show();
        }
    }

    private void getEphericalKey(String customerID) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                "https://api.stripe.com/v1/ephemeral_keys",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            EphericalKey = object.getString("id");
                            Toast.makeText(PurchaseEmojiActivity.this, EphericalKey, Toast.LENGTH_SHORT).show();
                            getClientSecret(customerID, EphericalKey);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Authorization","Bearer " + SECRET_KEY);
                header.put("Stripe-Version","2023-10-16");
                return header;
            }

            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("customer", customerID);

                return params;

            }
        };


        RequestQueue requestQueue = Volley.newRequestQueue(PurchaseEmojiActivity.this);
        requestQueue.add(stringRequest);


    }

    private void getClientSecret(String customerID, String ephericalKey) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                "https://api.stripe.com/v1/payment_intents",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            ClientSecret = object.getString("client_secret");
                            Toast.makeText(PurchaseEmojiActivity.this, ClientSecret, Toast.LENGTH_SHORT).show();

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Authorization","Bearer " + SECRET_KEY);
                return header;
            }

            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("customer", customerID);
                params.put("amount", "100");
                params.put("currency", "usd");
                params.put("automatic_payment_methods[enabled]", "true");
                return params;

            }
        };


        RequestQueue requestQueue = Volley.newRequestQueue(PurchaseEmojiActivity.this);
        requestQueue.add(stringRequest);

    }

    private void PaymentFlow() {
        paymentSheet.presentWithPaymentIntent(
                ClientSecret, new PaymentSheet.Configuration("FROYO Social Media",
                        new PaymentSheet.CustomerConfiguration(
                                customerID,
                                EphericalKey
                        ))
        );
    }

private void fetchUserData() {
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Fetch user data from the "stripe" collection
    db.collection("stripe")
            .document(email)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // Check if the "customerID" field exists in the document
                    if (documentSnapshot.contains("customerID")) {
                        customerID = documentSnapshot.getString("customerID");

                        // Log the retrieved data for debugging
                        Log.d("UserData", "User data retrieved successfully from 'stripe' collection");
                        Log.d("UserData", "Customer ID: " + customerID);

                        // Now, you have the customerID, you can use it as needed
                        // For example, you can pass it to another function or update UI
                    } else {
                        // Handle case where "customerID" field does not exist
                        customerID = null;
                        Log.d("UserData", "No 'customerID' field found for email: " + email);
                    }
                    if (documentSnapshot.contains("purchased")) {
                        // Get the purchased field as an Object
                        Object purchasedObject = documentSnapshot.get("purchased");

                        // Check if it's an instance of List
                        if (purchasedObject instanceof List) {
                            // Cast it to List<String> or the appropriate type based on your data
                            purchasedEmoji = (ArrayList<String>) purchasedObject;

                            // Log the retrieved data for debugging
                            Log.d("UserData", "User data retrieved successfully from 'stripe' collection");
                            Log.d("UserData", "Purchased Emoji: " + purchasedEmoji);

                            // Now, you have the purchasedEmoji list, you can use it as needed
                            // For example, you can pass it to another function or update UI

                            // Check if the purchasedEmoji list contains "7" and disable the button accordingly
                            if (purchasedEmoji.contains("7")) {
                                purchaseButton7.setText("Purchased");
                                purchaseButton7.setEnabled(false);
                                purchaseButton7.setOnClickListener(null);
                            }
                            if (purchasedEmoji.contains("8")) {
                                purchaseButton8.setText("Purchased");
                                purchaseButton8.setEnabled(false);
                                purchaseButton8.setOnClickListener(null);
                            }
                            if (purchasedEmoji.contains("9")) {
                                purchaseButton9.setText("Purchased");
                                purchaseButton9.setEnabled(false);
                                purchaseButton9.setOnClickListener(null);
                            }
                            if (purchasedEmoji.contains("10")) {
                                purchaseButton10.setText("Purchased");
                                purchaseButton10.setEnabled(false);
                                purchaseButton10.setOnClickListener(null);
                            }

                        } else {
                            Log.d("UserData", "Invalid 'purchased' field type for email: " + email);
                        }
                    } else {
                        // Handle case where "purchased" field does not exist
                        Log.d("UserData", "No 'purchased' field found for email: " + email);
                    }

                } else {
                    // Handle case where user data does not exist in "stripe" collection
                    customerID = null;
                    Log.d("UserData", "No user data found in 'stripe' collection for email: " + email);
                }
            })
            .addOnFailureListener(e -> {
                // Handle failure
                Log.e("UserData", "Error fetching user data from 'stripe' collection", e);
            });
}


    private void updateStripeID(String email, String customerID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a new document in the "stripe" collection with email as the document ID
        DocumentReference stripeDocument = db.collection("stripe").document(email);

        // Set the "customerID" field in the document
        stripeDocument
                .set(new StripeData(email, customerID))
                .addOnSuccessListener(aVoid -> {
                    // Handle success
                    Log.d("FirestoreUpdate", "Document created/updated successfully in stripe collection with email: " + email);
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Log.e("FirestoreUpdate", "Error creating/updating document in stripe collection", e);
                });
    }

    private void addPurchasedEmoji(String email, String selectedEmoji) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a Map to hold the data you want to update
        Map<String, Object> data = new HashMap<>();
        data.put("purchased", FieldValue.arrayUnion(selectedEmoji));

        // Update the document in the "stripe" collection with the new purchased emoji
        db.collection("stripe")
                .document(email)
                .update(data)
                .addOnSuccessListener(aVoid -> {
                    // Handle success
                    Log.d("FirestoreUpdate", "Document updated successfully with purchased emoji: " + selectedEmoji);
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Log.e("FirestoreUpdate", "Error updating document with purchased emoji", e);
                });
    }

}