package com.vegvisir.app.tasklist;

import android.app.Application;
import android.content.Context;

import com.vegvisir.app.tasklist.data.TransactionTuple;
import com.vegvisir.app.tasklist.data.TwoPSetUser;
import com.vegvisir.app.tasklist.ui.login.LoginActivity;
import com.vegvisir.app.tasklist.ui.login.LoginImpl;
import com.vegvisir.pub_sub.TransactionID;
import com.vegvisir.pub_sub.VegvisirApplicationContext;
import com.vegvisir.pub_sub.VirtualVegvisirInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class TasklistApplication extends Application {



    private HashMap<String, Set<TransactionTuple>> MainDependencySets = new HashMap<>();
    private HashMap<TransactionID, FourPSet> fourPSets = new HashMap<>();
    private Set<TransactionID> witnessedTransactions = new HashSet<TransactionID>();
    private ArrayList<TransactionID> notWitnessedTransactions = new ArrayList<>();
    private Set<TransactionID> MainTopDeps = new HashSet<>();
    private TransactionID MainTop = new TransactionID("", -1);
    private ArrayList<String> items = new ArrayList<>();
    private HashMap<String, LoginActivity.Priority> priorities = new HashMap<>();
    private String deviceId = "";
    // mapping from device ID to Transaction ID
    private HashMap<String, TransactionID> latestTransactions = new HashMap<>();
    // mapping from an item to dependencies
    private HashMap<String, Set<TransactionTuple>> dependencySets = new HashMap<>();
    //mapping from transaction ID to its 2P set
    private HashMap<TransactionID, TwoPSetUser> twoPSets = new HashMap<>();
    private HashMap<String, String> usernames = new HashMap<>();
    private Set<TransactionID> topDeps = new HashSet<>();
    private TransactionID top = new TransactionID("", -2);
    private HashMap<TransactionID, Set<TransactionID>> transactionDeps = new HashMap<>();
    private Set<String> witnessedItems = new HashSet<>();
    private HashMap<String, LoginActivity.Priority> witnessedPriorities = new HashMap<>();
    private TransactionID witnessedTop = new TransactionID("", -3);
    private Set<TransactionID> witnessedTopDeps = new HashSet<>();
    private VegvisirApplicationContext context = null;
    private LoginImpl delegator = null;
    private String topic = "Blue team";
    //public static VegvisirInstance instance = null;
    private VirtualVegvisirInstance virtual = null;
    private Context androidContext;


    public HashMap<String, Set<TransactionTuple>> getMainDependencySets() {
        return MainDependencySets;
    }

    public HashMap<TransactionID, FourPSet> getFourPSets() {
        return fourPSets;
    }

    public Set<TransactionID> getWitnessedTransactions() {
        return witnessedTransactions;
    }

    public ArrayList<TransactionID> getNotWitnessedTransactions() {
        return notWitnessedTransactions;
    }

    public Set<TransactionID> getMainTopDeps() {
        return MainTopDeps;
    }

    public TransactionID getMainTop() {
        return MainTop;
    }

    public ArrayList<String> getItems() {
        return items;
    }

    public HashMap<String, LoginActivity.Priority> getPriorities() {
        return priorities;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public HashMap<String, TransactionID> getLatestTransactions() {
        return latestTransactions;
    }

    public HashMap<String, Set<TransactionTuple>> getDependencySets() {
        return dependencySets;
    }

    public HashMap<TransactionID, TwoPSetUser> getTwoPSets() {
        return twoPSets;
    }

    public HashMap<String, String> getUsernames() {
        return usernames;
    }

    public Set<TransactionID> getTopDeps() {
        return topDeps;
    }

    public TransactionID getTop() {
        return top;
    }

    public HashMap<TransactionID, Set<TransactionID>> getTransactionDeps() {
        return transactionDeps;
    }

    public Set<String> getWitnessedItems() {
        return witnessedItems;
    }

    public HashMap<String, LoginActivity.Priority> getWitnessedPriorities() {
        return witnessedPriorities;
    }

    public TransactionID getWitnessedTop() {
        return witnessedTop;
    }

    public Set<TransactionID> getWitnessedTopDeps() {
        return witnessedTopDeps;
    }

    public VegvisirApplicationContext getContext() {
        return context;
    }

    public LoginImpl getDelegator() {
        return delegator;
    }

    public String getTopic() {
        return topic;
    }

    public VirtualVegvisirInstance getVirtual() {
        return virtual;
    }

    public Context getAndroidContext() {
        return androidContext;
    }


    public void setMainDependencySets(HashMap<String, Set<TransactionTuple>> mainDependencySets) {
        MainDependencySets = mainDependencySets;
    }

    public void setFourPSets(HashMap<TransactionID, FourPSet> fourPSets) {
        this.fourPSets = fourPSets;
    }

    public void setNotWitnessedTransactions(ArrayList<TransactionID> notWitnessedTransactions) {
        this.notWitnessedTransactions = notWitnessedTransactions;
    }

    public void setMainTopDeps(Set<TransactionID> mainTopDeps) {
        MainTopDeps = mainTopDeps;
    }

    public void setMainTop(TransactionID mainTop) {
        MainTop = mainTop;
    }

    public void setItems(ArrayList<String> items) {
        this.items = items;
    }

    public void setPriorities(HashMap<String, LoginActivity.Priority> priorities) {
        this.priorities = priorities;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setLatestTransactions(HashMap<String, TransactionID> latestTransactions) {
        this.latestTransactions = latestTransactions;
    }

    public void setDependencySets(HashMap<String, Set<TransactionTuple>> dependencySets) {
        this.dependencySets = dependencySets;
    }

    public void setTwoPSets(HashMap<TransactionID, TwoPSetUser> twoPSets) {
        this.twoPSets = twoPSets;
    }

    public void setUsernames(HashMap<String, String> usernames) {
        this.usernames = usernames;
    }

    public void setTopDeps(Set<TransactionID> topDeps) {
        this.topDeps = topDeps;
    }

    public void setTop(TransactionID top) {
        this.top = top;
    }

    public void setTransactionDeps(HashMap<TransactionID, Set<TransactionID>> transactionDeps) {
        this.transactionDeps = transactionDeps;
    }

    public void setWitnessedItems(Set<String> witnessedItems) {
        this.witnessedItems = witnessedItems;
    }

    public void setWitnessedPriorities(HashMap<String, LoginActivity.Priority> witnessedPriorities) {
        this.witnessedPriorities = witnessedPriorities;
    }

    public void setContext(VegvisirApplicationContext context) {
        this.context = context;
    }

    public void setDelegator(LoginImpl delegator) {
        this.delegator = delegator;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setVirtual(VirtualVegvisirInstance virtual) {
        this.virtual = virtual;
    }

    public void setAndroidContext(Context androidContext) {
        this.androidContext = androidContext;
    }

    public void setWitnessedTransactions(Set<TransactionID> witnessedTransactions) {
        this.witnessedTransactions = witnessedTransactions;
    }

    public void setWitnessedTop(TransactionID witnessedTop) {
        this.witnessedTop = witnessedTop;
    }

    public void setWitnessedTopDeps(Set<TransactionID> witnessedTopDeps) {
        this.witnessedTopDeps = witnessedTopDeps;
    }

}
