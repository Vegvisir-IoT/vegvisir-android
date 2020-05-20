package com.vegvisir.app.annotativemap.ui.login;

import android.app.Activity;
import android.util.Log;

import com.vegvisir.app.annotativemap.Annotation;
import com.vegvisir.app.annotativemap.AnnotativeMapApplication;
import com.vegvisir.app.annotativemap.Coordinates;
import com.vegvisir.app.annotativemap.FullAnnotation;
import com.vegvisir.app.annotativemap.MainActivity;
import com.vegvisir.app.annotativemap.PictureTagLayout;
import com.vegvisir.app.annotativemap.PictureTagView;
import com.vegvisir.app.annotativemap.R;
import com.vegvisir.app.annotativemap.TwoPSet;
import com.vegvisir.app.annotativemap.User;
import com.vegvisir.pub_sub.TransactionID;
import com.vegvisir.pub_sub.VegvisirApplicationDelegator;
import com.vegvisir.pub_sub.VegvisirInstance;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Ideally, all applications should implement this interface.
 */
public class LoginImpl implements VegvisirApplicationDelegator {

    private Activity loginActivity;

    public LoginImpl(Activity a) {
        this.loginActivity = a;
    }

    /**
     * Vegvisir will call this function to init and run application.
     * @param instance a underlying Vegvisir instance for application use.
     */
    public void init(VegvisirInstance instance) {

    }

    private Set<TransactionID> getUpdatedSet(Set<TransactionID> prevSets, TransactionID tx_id, Set<TransactionID> deps) {
        Set<TransactionID> updatedSet = new HashSet<>();

        if (prevSets != null) {
            Iterator<TransactionID> itr = prevSets.iterator();
            while (itr.hasNext()) {
                TransactionID x = (TransactionID) ((Iterator) itr).next();

                if (!deps.contains(x)) {
                    updatedSet.add(x);
                }
            }
        }

        updatedSet.add(tx_id);

        return updatedSet;
    }

    /**
     * An application implemented function. This function will get called whenever a new transaction
     * subscribed by this application arrives.
     * @param topics topics that this transaction is created for.
     * @param payload application specific data.
     * @param tx_id A unique identifier for the transaction.
     * @param deps which transactions this transaction depends on.
     */
    public void applyTransaction(
            Set<String> topics,
            byte[] payload,
            TransactionID tx_id,
            Set<TransactionID> deps) {

        AnnotativeMapApplication thisApp = (AnnotativeMapApplication) loginActivity.getApplication();
        String payloadString = new String(payload);
        Log.i("payload",payloadString);
        int transactionType = Integer.parseInt(payloadString.substring(0,1));
        if (transactionType > 4 && transactionType < 8) {
            int usernamePos = payloadString.indexOf(",");
            String username = payloadString.substring(1,usernamePos);
            String password = payloadString.substring(usernamePos + 1);

            Set<TransactionID> prevSets = thisApp.getDependencySets().get(username);
            String deviceId = tx_id.getDeviceID();
            Set<TransactionID> updatedSet = getUpdatedSet(prevSets, tx_id, deps);

            thisApp.getDependencySets().put(username, updatedSet);

            thisApp.getLatestTransactions().put(deviceId, tx_id);

            HashSet<User> addSet = new HashSet<>();
            HashSet<User> removeSet = new HashSet<>();

            for (TransactionID d : deps) {
                if (thisApp.getTwoPSets().containsKey(d)) {
                    addSet.addAll(thisApp.getTwoPSets().get(d).getAddSet());
                    removeSet.addAll(thisApp.getTwoPSets().get(d).getRemoveSet());
                }
            }

            if (transactionType == 5) {
                addSet.add(new User(username, password));
                removeSet.remove(username);
            } else {
                addSet.remove(username);
                removeSet.add(new User(username, password));
            }

            thisApp.getTwoPSets().put(tx_id, new TwoPSetUser(addSet, removeSet));

            HashSet<User> addSetTop = new HashSet<>();
            HashSet<User> removeSetTop = new HashSet<>();

            for (TransactionID d : deps) {
                thisApp.getTopDeps().remove(d);
            }
            thisApp.getTopDeps().add(tx_id);

            for (TransactionID d : thisApp.getTopDeps()) {
                if (thisApp.getTwoPSets().containsKey(d)) {
                    addSetTop.addAll(thisApp.getTwoPSets().get(d).getAddSet());
                    removeSetTop.addAll(thisApp.getTwoPSets().get(d).getRemoveSet());
                }
            }

            thisApp.getTwoPSets().put(thisApp.getTop(), new TwoPSetUser(addSetTop, removeSetTop));


            Set<User> newSet = addSetTop;
            newSet.removeAll(removeSetTop);
            Log.i("new set", newSet.toString());
            thisApp.getUsernames().clear();
            for (User u: newSet){
                thisApp.getUsernames().put(u.getUsername(), u.getPassword());
            }
        }

        else{
            Log.i("start","of anno case");
            Coordinates coords = null;
            String anno = "";
            int x = -1;
            int y = -1;
            if (transactionType < 5){
                if (transactionType == 0) {
                    String item = payloadString.substring(1);
                    boolean entryFound = false;
                    for (Map.Entry<Coordinates, Annotation> entry : thisApp.getAnnotations().entrySet()) {
                        Coordinates c = entry.getKey();
                        Annotation a = entry.getValue();
                        if (item.equals(a.getAnnotation())) {
                            anno = item;
                            coords = c;
                            x = c.getX();
                            y = c.getY();
                            entryFound = true;
                            break;
                        }
                    }
                    if (!entryFound) {
                        return;
                    }

                }
                else {
                    return;
                }
            }

            else {
                int first = payloadString.indexOf(",");
                x = Integer.parseInt(payloadString.substring(1, first));
                int second = payloadString.indexOf(",", first + 1);
                y = Integer.parseInt(payloadString.substring(first + 1, second));
                anno = payloadString.substring(second + 1);

                coords = new Coordinates(x, y);
            }

            PictureTagLayout image = null;
            PictureTagView pointView;
            if (thisApp.getCurrentPicture() != null) {
                image = thisApp.getCurrentPicture().findViewById(R.id.image);
                pointView = image.justHasView(x,y);
                if (pointView != null) {
                    Log.i("View","found");
                    x = pointView.getXVal();
                    y = pointView.getYVal();
                }
                else {
                    Log.i("View","not found");
                }
            }


            Set<TransactionID> prevSets = thisApp.getMapDependencySets().get(coords);
            String deviceId = tx_id.getDeviceID();
            Set<TransactionID> updatedSet = getUpdatedSet(prevSets, tx_id, deps);

            thisApp.getMapDependencySets().put(coords, updatedSet);
            //thisApp.getMapLatestTransactions().put(deviceId, tx_id);

            for (TransactionID d : deps) {
                thisApp.getMapTopDeps().remove(d);
            }
            thisApp.getMapTopDeps().add(tx_id);
            HashSet<FullAnnotation> addSet = new HashSet<>();
            HashSet<FullAnnotation> removeSet = new HashSet<>();

            for (TransactionID d : deps) {
                if (thisApp.getMapTwoPSets().containsKey(d)) {
                    addSet.addAll(thisApp.getMapTwoPSets().get(d).getAddSet());
                    removeSet.addAll(thisApp.getMapTwoPSets().get(d).getRemoveSet());
                }
            }

            if (transactionType == 9) {
                PictureTagView view = null;
                if (image != null) {
                    view = image.justHasView(x, y);
                }
                if (view != null) {

                    boolean doesExist = false;
                    for (FullAnnotation fa: addSet) {
                        Coordinates c = fa.getCoords();
                        if (image.justHasView(c.getX(),c.getY()) != null) {
                            doesExist = true;
                            break;
                        }
                    }

                    if (addSet.isEmpty() || !doesExist) {
                        addSet.add(new FullAnnotation(coords,anno));
                    }

                    for(FullAnnotation fa: removeSet) {
                        Coordinates c = fa.getCoords();
                        if (image.justHasView(c.getX(),c.getY()) != null) {
                            removeSet.remove(fa);
                            break;
                        }
                    }
                }

                else {
                    addSet.add(new FullAnnotation(coords,anno));
                }

            } else {

                PictureTagView view = null;
                if (image != null) {
                    view = image.justHasView(x,y);
                }
                if (view != null) {

                    for (FullAnnotation fa: addSet) {
                        Coordinates c = fa.getCoords();
                        if (image.justHasView(c.getX(),c.getY()) != null) {
                            addSet.remove(fa);
                            break;
                        }
                    }

                    boolean doesExist = false;
                    for(FullAnnotation fa: removeSet) {
                        Coordinates c = fa.getCoords();

                        if (image.justHasView(c.getX(),c.getY()) != null) {
                            doesExist = true;
                            break;
                        }
                    }

                    if (removeSet.isEmpty() || !doesExist) {
                        removeSet.add(new FullAnnotation(coords,anno));
                    }
                }

                else {
                    removeSet.add(new FullAnnotation(coords,anno));
                }

            }
            FullAnnotation faToModify = null;
            for (FullAnnotation fa: addSet) {
                Coordinates c = fa.getCoords();
                if (coords.equals(c)) {
                    faToModify = fa;
                }
            }
            if (faToModify != null) {
                FullAnnotation newFa = new FullAnnotation(faToModify.getCoords(),anno);
                addSet.remove(faToModify);
                addSet.add(newFa);
            }

            thisApp.getMapTwoPSets().put(tx_id, new TwoPSet(addSet, removeSet));

            Log.i("addset",addSet.toString());
            Log.i("remset",removeSet.toString());

            HashSet<FullAnnotation> addSetTop = new HashSet<>();
            HashSet<FullAnnotation> removeSetTop = new HashSet<>();


            for (TransactionID d : thisApp.getMapTopDeps()) {
                if (thisApp.getMapTwoPSets().containsKey(d)) {
                    addSetTop.addAll(thisApp.getMapTwoPSets().get(d).getAddSet());
                    removeSetTop.addAll(thisApp.getMapTwoPSets().get(d).getRemoveSet());
                }
            }

            Log.i("addsettop",addSetTop.toString());
            Log.i("remsettop",removeSetTop.toString());


            thisApp.getMapTwoPSets().put(thisApp.getMapTop(), new TwoPSet(addSetTop, removeSetTop));
            
            PictureTagView v = null;
            if (image != null) {
                v = image.justHasView(coords.getX(), coords.getY());
            }
            if (v != null) {
                if (thisApp.getAnnotations().containsKey(coords)) {
                    Log.i("set","case");
                    thisApp.getAnnotations().get(coords).setAnnotation(anno);

                }
                else {
                    thisApp.getAnnotations().put(coords,new Annotation(anno));
                }
            }
            else {
                Log.i("View","does not exist");
                thisApp.getAnnotations().put(coords,new Annotation(anno));
            }

            for (FullAnnotation fa: addSetTop) {
                Coordinates c = fa.getCoords();
                String annotation = fa.getAnnotation();

                if (!thisApp.getAnnotations().containsKey(c)) {
                    Log.i("hashmap","doesn't contain anno");
                    thisApp.getAnnotations().put(c, new Annotation(annotation));
                }

            }

            for (FullAnnotation fa: removeSetTop) {
                Coordinates c = fa.getCoords();
                if (thisApp.getAnnotations().containsKey(c)) {
                    Log.i("removefound", thisApp.getAnnotations().get(c).getAnnotation());
                    thisApp.getAnnotations().get(c).setShouldRemove(true);
                }
            }

            HashSet<Coordinates> entriesToRemove = new HashSet<>();

            Log.i("annosinimpl", thisApp.getAnnotations().toString());
            thisApp.setPrintedOnce(false);
        }
    }

    public void onNewReconciliationFinished(){

    }


}
