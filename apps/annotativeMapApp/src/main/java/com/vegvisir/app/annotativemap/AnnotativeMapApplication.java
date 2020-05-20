package com.vegvisir.app.annotativemap;

import android.app.Application;

import com.vegvisir.app.annotativemap.ui.login.LoginImpl;
import com.vegvisir.app.annotativemap.ui.login.LoginViewModel;
import com.vegvisir.app.annotativemap.ui.login.TwoPSetUser;
import com.vegvisir.pub_sub.TransactionID;
import com.vegvisir.pub_sub.VegvisirApplicationContext;
import com.vegvisir.pub_sub.VegvisirInstance;
import com.vegvisir.pub_sub.VirtualVegvisirInstance;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AnnotativeMapApplication extends Application {

    private ConcurrentHashMap<Coordinates, Annotation> annotations = new ConcurrentHashMap<>();
    private HashMap<Coordinates, Set<TransactionID>> mapDependencySets = new HashMap<>();
    private HashMap<String, TransactionID> mapLatestTransactions = new HashMap<>();
    private HashMap<TransactionID, TwoPSet> mapTwoPSets = new HashMap<>();
    private Set<TransactionID> mapTopDeps = new HashSet<>();
    private TransactionID mapTop = new TransactionID("",-1);
    private picture currentPicture = null;
    private String deviceId = "";
    // mapping from device ID to Transaction ID
    private HashMap<String, TransactionID> latestTransactions = new HashMap<>();
    // mapping from an item to dependencies
    private HashMap<String, Set<TransactionID>> dependencySets = new HashMap<>();
    //mapping from transaction ID to its 2P set
    private HashMap<TransactionID, TwoPSetUser> twoPSets = new HashMap<>();
    private HashMap<String, String> usernames = new HashMap<>();
    private Set<TransactionID> topDeps = new HashSet<>();
    private TransactionID top = new TransactionID("", -1);
    private VegvisirApplicationContext context = null;
    private LoginImpl delegator;
    private String topic = "Blue team";
    private VegvisirInstance instance = null;
    private VirtualVegvisirInstance virtual = VirtualVegvisirInstance.getInstance();
    private boolean runningMainActivity;

    public boolean isPrintedOnce() {
        return printedOnce;
    }

    public void setPrintedOnce(boolean printedOnce) {
        this.printedOnce = printedOnce;
    }

    private boolean printedOnce = false;

    public boolean isRunningMainActivity() {
        return runningMainActivity;
    }

    public void setRunningMainActivity(boolean runningMainActivity) {
        this.runningMainActivity = runningMainActivity;
    }

    public ConcurrentHashMap<Coordinates, Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(ConcurrentHashMap<Coordinates, Annotation> annotations) {
        this.annotations = annotations;
    }

    public HashMap<Coordinates, Set<TransactionID>> getMapDependencySets() {
        return mapDependencySets;
    }

    public void setMapDependencySets(HashMap<Coordinates, Set<TransactionID>> mapDependencySets) {
        this.mapDependencySets = mapDependencySets;
    }

    public HashMap<String, TransactionID> getMapLatestTransactions() {
        return mapLatestTransactions;
    }

    public void setMapLatestTransactions(HashMap<String, TransactionID> mapLatestTransactions) {
        this.mapLatestTransactions = mapLatestTransactions;
    }

    public HashMap<TransactionID, TwoPSet> getMapTwoPSets() {
        return mapTwoPSets;
    }

    public void setMapTwoPSets(HashMap<TransactionID, TwoPSet> mapTwoPSets) {
        this.mapTwoPSets = mapTwoPSets;
    }

    public Set<TransactionID> getMapTopDeps() {
        return mapTopDeps;
    }

    public void setMapTopDeps(Set<TransactionID> mapTopDeps) {
        this.mapTopDeps = mapTopDeps;
    }

    public TransactionID getMapTop() {
        return mapTop;
    }

    public void setMapTop(TransactionID mapTop) {
        this.mapTop = mapTop;
    }

    public picture getCurrentPicture() {
        return currentPicture;
    }

    public void setCurrentPicture(picture currentPicture) {
        this.currentPicture = currentPicture;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public HashMap<String, TransactionID> getLatestTransactions() {
        return latestTransactions;
    }

    public void setLatestTransactions(HashMap<String, TransactionID> latestTransactions) {
        this.latestTransactions = latestTransactions;
    }

    public HashMap<String, Set<TransactionID>> getDependencySets() {
        return dependencySets;
    }

    public void setDependencySets(HashMap<String, Set<TransactionID>> dependencySets) {
        this.dependencySets = dependencySets;
    }

    public HashMap<TransactionID, TwoPSetUser> getTwoPSets() {
        return twoPSets;
    }

    public void setTwoPSets(HashMap<TransactionID, TwoPSetUser> twoPSets) {
        this.twoPSets = twoPSets;
    }

    public HashMap<String, String> getUsernames() {
        return usernames;
    }

    public void setUsernames(HashMap<String, String> usernames) {
        this.usernames = usernames;
    }

    public Set<TransactionID> getTopDeps() {
        return topDeps;
    }

    public void setTopDeps(Set<TransactionID> topDeps) {
        this.topDeps = topDeps;
    }

    public TransactionID getTop() {
        return top;
    }

    public void setTop(TransactionID top) {
        this.top = top;
    }

    public VegvisirApplicationContext getContext() {
        return context;
    }

    public void setContext(VegvisirApplicationContext context) {
        this.context = context;
    }

    public LoginImpl getDelegator() {
        return delegator;
    }

    public void setDelegator(LoginImpl delegator) {
        this.delegator = delegator;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public VegvisirInstance getInstance() {
        return instance;
    }

    public void setInstance(VegvisirInstance instance) {
        this.instance = instance;
    }

    public VirtualVegvisirInstance getVirtual() {
        return virtual;
    }

    public void setVirtual(VirtualVegvisirInstance virtual) {
        this.virtual = virtual;
    }



}
