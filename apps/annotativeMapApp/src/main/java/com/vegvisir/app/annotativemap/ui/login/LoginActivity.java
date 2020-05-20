package com.vegvisir.app.annotativemap.ui.login;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.vegvisir.app.annotativemap.Annotation;
import com.vegvisir.app.annotativemap.AnnotativeMapApplication;
import com.vegvisir.app.annotativemap.Coordinates;
import com.vegvisir.app.annotativemap.MainActivity;
import com.vegvisir.app.annotativemap.R;
import com.vegvisir.app.annotativemap.TwoPSet;
import com.vegvisir.app.annotativemap.picture;
import com.vegvisir.application.VegvisirInstanceV1;
import com.vegvisir.pub_sub.TransactionID;
import com.vegvisir.pub_sub.VegvisirApplicationContext;
import com.vegvisir.pub_sub.VegvisirInstance;
import com.vegvisir.pub_sub.VirtualVegvisirInstance;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LoginActivity extends AppCompatActivity {

    public static ConcurrentHashMap<Coordinates, Annotation> annotations = new ConcurrentHashMap<>();
    public static HashMap<Coordinates,Set<TransactionID>> mapDependencySets = new HashMap<>();
//    public static HashMap<String, TransactionID> mapLatestTransactions = new HashMap<>();
    public static HashMap<TransactionID, TwoPSet> mapTwoPSets = new HashMap<>();
    public static Set<TransactionID> mapTopDeps = new HashSet<>();
    public static TransactionID mapTop = new TransactionID("",-1);
    public static picture currentPicture = null;

    private LoginViewModel loginViewModel;
    private String appID = "456";
    private String desc = "annotated map";
    private Set<String> channels = new HashSet<String>();

    //public static Button loginButton;
    //public static Button registerButton;

    public static VirtualVegvisirInstance virtual = VirtualVegvisirInstance.getInstance();


    public Button loginButton;
    public Button registerButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory(this))
                .get(LoginViewModel.class);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
            }
        }

        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login);
        registerButton = findViewById(R.id.register);
//        loginButton.setEnabled(false);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);

        AnnotativeMapApplication thisApp = (AnnotativeMapApplication) this.getApplication();

        channels.add(thisApp.getTopic());
        thisApp.setContext(new VegvisirApplicationContext(appID, desc, channels));
        Context androidContext = getApplicationContext();
        thisApp.setDelegator(new LoginImpl(this));

//        instance = VegvisirInstanceV1.getInstance(androidContext);
//        instance.registerApplicationDelegator(context, delegator);
//        this.deviceId = instance.getThisDeviceID();
//        thisApp.setInstance(VegvisirInstanceV1.getInstance(androidContext));
//        thisApp.getInstance().registerApplicationDelegator(thisApp.getContext(), thisApp.getDelegator());
//        thisApp.setDeviceId(thisApp.getInstance().getThisDeviceID());

        thisApp.getVirtual().registerApplicationDelegator(thisApp.getContext(), thisApp.getDelegator());
        thisApp.setDeviceId(thisApp.getVirtual().getThisDeviceID());

        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                registerButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                }
                if (loginResult.getSuccess() != null) {
                    updateUiWithUser(loginResult.getSuccess());
                }
                setResult(Activity.RESULT_OK);

                //Complete and destroy login activity once successful
                finish();
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    try {
                        loginViewModel.login(usernameEditText.getText().toString(),
                                passwordEditText.getText().toString());
                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                loadingProgressBar.setVisibility(View.VISIBLE);
                try {
                    loginViewModel.login(usernameEditText.getText().toString(),
                            passwordEditText.getText().toString());
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
//                    finish();
//                    startActivity(getIntent());
                    usernameEditText.setText("");
                    passwordEditText.setText("");
                }
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                loadingProgressBar.setVisibility(View.VISIBLE);
                try {
                    String displayString = loginViewModel.register(usernameEditText.getText().toString(),
                            passwordEditText.getText().toString());
                    Toast.makeText(getApplicationContext(),displayString,Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
//                    finish();
//                    startActivity(getIntent());

                    usernameEditText.setText("");
                    passwordEditText.setText("");
                }
            }
        });
    }

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        // TODO : initiate successful logged in experience
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
//        MainActivity.resume();
//        MainActivity.runningMainActivity = true;
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        Log.d("LOGIN ACT", "onBackPressed: Pressed");
        return;
    }
}