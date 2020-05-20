package com.vegvisir.app.annotativemap.ui.login;

import android.app.Activity;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.NonNull;

import com.vegvisir.app.annotativemap.data.LoginDataSource;
import com.vegvisir.app.annotativemap.data.LoginRepository;

/**
 * ViewModel provider factory to instantiate LoginViewModel.
 * Required given LoginViewModel has a non-empty constructor
 */
public class LoginViewModelFactory implements ViewModelProvider.Factory {

    private Activity loginActivity;
    public LoginViewModelFactory(Activity a) {
        this.loginActivity = a;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LoginViewModel.class)) {
            return (T) new LoginViewModel(LoginRepository.getInstance(new LoginDataSource(loginActivity)));
        } else {
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}