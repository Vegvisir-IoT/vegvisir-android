package com.vegvisir.app.tasklist.ui.login;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.vegvisir.app.tasklist.MainActivity;
import com.vegvisir.app.tasklist.R;
import com.vegvisir.app.tasklist.TasklistApplication;
import com.vegvisir.pub_sub.VegvisirApplicationContext;
import com.vegvisir.pub_sub.VirtualVegvisirInstance;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    private String appID = "123";
    private String desc = "task list";
    private Set<String> channels = new HashSet<>();
    private Timer timer;

    TasklistApplication thisApp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory(this))
                .get(LoginViewModel.class);

        //Get permission
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
        final Button loginButton = findViewById(R.id.login);
        final Button registerButton = findViewById(R.id.register);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);

        thisApp = (TasklistApplication) this.getApplication();

        channels.add(thisApp.getTopic());
        thisApp.setContext(new VegvisirApplicationContext(appID, desc, channels));
        thisApp.setAndroidContext(getApplicationContext());

//        instance = VegvisirInstanceV1.getInstance(androidContext);
//        instance.registerApplicationDelegator(context, delegator);
//        this.deviceId = instance.getThisDeviceID();

        thisApp.setVirtual(VirtualVegvisirInstance.getInstance());
        thisApp.setDelegator(new LoginImpl(this));
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
        passwordEditText.setOnEditorActionListener(( view, actionId, event) -> {

                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    try {
                        loginViewModel.login(usernameEditText.getText().toString(),
                                passwordEditText.getText().toString());
                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
                return false;
            });

        loginButton.setOnClickListener((view -> {
            try {
                loginViewModel.login(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                usernameEditText.setText("");
                passwordEditText.setText("");
            }
        }));

        registerButton.setOnClickListener((view ->  {
                try {
                    String displayString = loginViewModel.register(usernameEditText.getText().toString(),
                            passwordEditText.getText().toString());
                    Toast.makeText(getApplicationContext(),displayString,Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();

                    usernameEditText.setText("");
                    passwordEditText.setText("");
                }
        }));
    }

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        // TODO : initiate successful logged in experience
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }

    public void updateByPriority(Set<String> aSet, Priority priority, boolean flag){
        if (flag){
            for(String entry: aSet) {
                thisApp.getItems().add( entry );
                thisApp.getPriorities().put(entry, priority);
            }
        }
        else{
            for(String entry: aSet) {
                thisApp.getWitnessedItems().add( entry );
                 thisApp.getWitnessedPriorities().put(entry, priority);
            }
        }

    }

    public enum Priority{
        High(3), Medium(2), Low(1), Remove(0);
        private final int value;
        Priority(int val){
            value = val;
        }

        public int getValue() {
            return value;
        }

        public static int priorityComparator( Priority p1, Priority p2) {
            if (p1.equals(p2))
                return p1.getValue() - p2.getValue();
            else
                return p1.compareTo(p2);
        }


        /**
         * @dependency :: Context must be initiated prior to calling
         * @return Integer representation of Priority Color
         */
        public int getAssociatedColor( Context c) {

            if (this == High)
                return ContextCompat.getColor(c, R.color.Red);
            else if (this == Medium)
                return ContextCompat.getColor(c, R.color.Blue);
            else if (this == Low)
                return ContextCompat.getColor(c, R.color.Green);
            else{
                return ContextCompat.getColor(c, R.color.Gray);
            }

            }


    }

    @Override
    public void onBackPressed() {
    }
}