package com.example.travelfriends;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.chaos.view.PinView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class ActivityPhoneLogin extends AppCompatActivity {
    String verificationId;
    CountryCodePicker cpp;
    EditText edt_phone;
    PinView firstPinView;

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks;
    ConstraintLayout phoneLayout;

    FirebaseAuth mAuth;

    TextView txt_otp;
    private String selected_country = "+92";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        cpp = (CountryCodePicker) findViewById(R.id.cpp);
        edt_phone = (EditText) findViewById(R.id.edt_phone);
        firstPinView = (PinView) findViewById(R.id.firstPinView);
        phoneLayout = (ConstraintLayout) findViewById(R.id.phoneLayout);
        txt_otp = (TextView) findViewById(R.id.txt_otp);
        mAuth=FirebaseAuth.getInstance();

        cpp.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                selected_country=cpp.getSelectedCountryCodeWithPlus();
            }
        });


        /*EditText for taking Phone Input*/
        edt_phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // initializing our callbacks for on
                // verification callback method.
                mCallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    // below method is used when
                    // OTP is sent from Firebase
                    @Override
                    public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(s, forceResendingToken);
                        // when we receive the OTP it
                        // contains a unique id which
                        // we are storing in our string
                        // which we have already created.
                        verificationId = s;
                    }

                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        // below line is used for getting OTP code
                        // which is sent in phone auth credentials.
                        final String code = phoneAuthCredential.getSmsCode();

                        // checking if the code
                        // is null or not.
                        if (code != null) {
                            // if the code is not null then
                            // we are setting that code to
                            // our OTP edittext field.
                            firstPinView.setText(code);

                            // after setting this code
                            // to OTP edittext field we
                            // are calling our verifycode method.
                            verifyCode(code);
                        }
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(ActivityPhoneLogin.this, e.getMessage(), Toast.LENGTH_LONG).show();

                    }

                };

                if(charSequence.toString().length()==10){
                    Toast.makeText(ActivityPhoneLogin.this, "Mobile Number Entered", Toast.LENGTH_SHORT).show();
                    phoneLayout.setVisibility(View.GONE);
                    firstPinView.setVisibility(View.VISIBLE);
                    txt_otp.setVisibility(View.VISIBLE);

                    try {


                        PhoneAuthOptions options =
                                PhoneAuthOptions.newBuilder(mAuth).setCallbacks(mCallBacks)
                                        .setPhoneNumber(selected_country + charSequence.toString())       // Phone number to verify
                                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                        .setActivity(ActivityPhoneLogin.this)
                                                 // OnVerificationStateChangedCallbacks
                                        .build();
                        PhoneAuthProvider.verifyPhoneNumber(options);

                    }
                    catch (Exception e){
                        Toast.makeText(ActivityPhoneLogin.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        txt_otp.setText(e.getStackTrace().toString());
                    }

                }
            }


            // below method is use to verify code from Firebase.
            private void verifyCode(String code) {
                // below line is used for getting
                // credentials from our verification id and code.
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);

                // after getting credential we are
                // calling sign in method.
                signInWithCredential(credential);
            }



            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        /*EditText for taking OTP*/
        firstPinView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().length()==6){
                    Toast.makeText(ActivityPhoneLogin.this, "Pin Entered", Toast.LENGTH_SHORT).show();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, charSequence.toString());

                    // after getting credential we are
                    // calling sign in method.
                    signInWithCredential(credential);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        // inside this method we are checking if
        // the code entered is correct or not.
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // if the code is correct and the task is successful
                            // we are sending our user to new activity.
                            Intent i = new Intent(ActivityPhoneLogin.this, HomeActivity.class);
                            startActivity(i);
                            finish();
                        } else {
                            // if the code is not correct then we are
                            // displaying an error message to the user.
                            Toast.makeText(ActivityPhoneLogin.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


}