package com.vegvisir.app.tasklist.data;

import android.app.Activity;
import android.util.Log;

import com.vegvisir.app.tasklist.TasklistApplication;
import com.vegvisir.app.tasklist.data.model.LoggedInUser;
import com.vegvisir.pub_sub.TransactionID;

import java.io.IOException;
import java.util.Arrays;
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
            TasklistApplication thisApp = (TasklistApplication) loginActivity.getApplication();
            Log.i("usernames", thisApp.getUsernames().toString());

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
        TasklistApplication thisApp = (TasklistApplication) loginActivity.getApplication();

        if (thisApp.getUsernames().containsKey(username)){
            throw new IOException("This username already exists");
        }
        else{
            //usernames.put(username, password);
            int hash = (username + password).hashCode();
            String payloadString = "5" +  username + "," + hash;
            byte[] payload = payloadString.getBytes();
            Set<String> topics = new HashSet<String>(Arrays.asList(thisApp.getTopic()));
            Set<TransactionID> dependencies = new HashSet<>();

            if (thisApp.getDependencySets().containsKey(username)) {
                Iterator<TransactionTuple> it = thisApp.getDependencySets().get(username).iterator();
                while (it.hasNext()) {
                    TransactionTuple x = (TransactionTuple) ((Iterator) it).next();
                    dependencies.add(x.transaction);
                }
            }
            if (thisApp.getLatestTransactions().containsKey(thisApp.getDeviceId())){
                dependencies.add(thisApp.getLatestTransactions().get(thisApp.getDeviceId()));
            }

            thisApp.getVirtual().addTransaction(thisApp.getContext(), topics, payload, dependencies);
            //instance.addTransaction(context, topics, payload, dependencies);
            return new Result.Success<>(user);
        }
    }

    public void logout() {
        // TODO: revoke authentication

    }
}
