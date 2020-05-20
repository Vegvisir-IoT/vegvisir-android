package com.vegvisir.app.annotativemap.data;

import android.app.Activity;
import android.util.Log;

import com.vegvisir.app.annotativemap.AnnotativeMapApplication;
import com.vegvisir.app.annotativemap.data.model.LoggedInUser;
import com.vegvisir.app.annotativemap.ui.login.LoginActivity;
import com.vegvisir.pub_sub.TransactionID;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    private Activity loginActivity;
    public LoginDataSource(Activity a) {
        this.loginActivity = a;
    }

    public Result<LoggedInUser> login(String username, String password) throws IOException {

        // TODO: handle loggedInUser authentication
        LoggedInUser user = null;
        AnnotativeMapApplication thisApp = (AnnotativeMapApplication) loginActivity.getApplication();
        Log.i("usernames",thisApp.getUsernames().toString());
        if (thisApp.getUsernames().containsKey(username)){
            String hash = Integer.toString((username + password).hashCode());

            if (thisApp.getUsernames().get(username).equals(hash)){
                user =  new LoggedInUser(
                        username,
                        username);
            }
            else{
                throw new IOException("Incorrect password, please try again");
            }
        }
        else{
            throw new IOException("Incorrect username, please try again");
        }
        return new Result.Success<>(user);
    }

    public Result<LoggedInUser> register(String username, String password) throws IOException {

        // TODO: handle loggedInUser authentication
        LoggedInUser user = null;
        AnnotativeMapApplication thisApp = (AnnotativeMapApplication) loginActivity.getApplication();

        if (thisApp.getUsernames().containsKey(username)){
            throw new IOException("This username already exists");
        }
        else{
            Log.i("Usernames","doesn't have this");
//            thisApp.getloginButton.setEnabled(false);
            //usernames.put(username, password);
            int hash = (username + password).hashCode();
            String payloadString = "5" +  username + "," + hash;
            byte[] payload = payloadString.getBytes();
            Set<String> topics = new HashSet<String>();
            topics.add(thisApp.getTopic());
            Set<TransactionID> dependencies = new HashSet<>();

            if (thisApp.getDependencySets().containsKey(username)) {
                Iterator<TransactionID> it = thisApp.getDependencySets().get(username).iterator();
                while (it.hasNext()) {
                    TransactionID x = (TransactionID) ((Iterator) it).next();
                    dependencies.add(x);
                }
            }
            if (thisApp.getLatestTransactions().containsKey(thisApp.getDeviceId())){
                dependencies.add(thisApp.getLatestTransactions().get(thisApp.getDeviceId()));
            }
            Log.i("before","add");

            thisApp.getVirtual().addTransaction(thisApp.getContext(), topics, payload, dependencies);
            Log.i("after","add");
            //instance.addTransaction(context, topics, payload, dependencies);
            return new Result.Success<>(user);
        }
    }

    public void logout() {
        // TODO: revoke authentication

    }
}