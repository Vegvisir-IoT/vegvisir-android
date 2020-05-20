package com.vegvisir.app.tasklist;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.vegvisir.app.tasklist.ui.login.LoginActivity;
import com.vegvisir.pub_sub.TransactionID;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OrderAdapter extends ArrayAdapter<String> {
    private List<String> list;
    private int resource;
    private Context context;
    private Activity loginActivity;

    TextView selected_item,
            low_priority,
            medium_priority,
            high_priority,
            delete_item;

    public OrderAdapter(Context ctxt, int res, List<String> its, Activity a) {
        super(ctxt, res, its);
        this.list = its;
        this.context = ctxt;
        this.resource = res;
        this.loginActivity = a;
    }



    public View getView(final int position, View convertView, ViewGroup parent){
        View listItemView = convertView;
        if(listItemView == null){
            listItemView = LayoutInflater.from(getContext()).inflate(
                   this.resource,null,false
            );
        }

        final String item = getItem(position);

        TasklistApplication thisApp = (TasklistApplication) loginActivity.getApplication();

        selected_item    = (TextView)listItemView.findViewById(R.id.selected_item);
        high_priority    = (TextView)listItemView.findViewById(R.id.high_prioirty);
        low_priority     = (TextView)listItemView.findViewById(R.id.low_priority);
        medium_priority  = (TextView)listItemView.findViewById(R.id.medium_priority);
        delete_item      = (TextView)listItemView.findViewById(R.id.delete_item);

        selected_item.setText(item);
        LoginActivity.Priority p = thisApp.getPriorities().get(item);
        selected_item.setTextColor( p.getAssociatedColor(context));

        String tickMark = "✓ ";

        if (p.equals(LoginActivity.Priority.Remove)){
            listItemView.setBackgroundColor(this.context.getResources().getColor(R.color.LightGrey));
        }
        else{
            listItemView.setBackgroundColor(0);
        }

        if (thisApp.getWitnessedItems().contains(item) && thisApp.getWitnessedPriorities().get(item).equals(p)){
            selected_item.setText(tickMark + item);
        }

        //OnClick listeners for all the buttons on the ListView Item
        low_priority.setOnClickListener( e -> setTheTransaction( item, 1, convertView));
        medium_priority.setOnClickListener( e -> setTheTransaction( item, 2, convertView));
        high_priority.setOnClickListener( e -> setTheTransaction( item, 3, convertView));
        delete_item.setOnClickListener( e -> setTheTransaction( item, 0, convertView));

        return listItemView;
    }

    /**
     * Handler for TextView
     * @param item
     * @param myNumber
     */
    public void setTheTransaction (String item, int myNumber, View v ) {
        if (v != null){
            if (myNumber == 1){
                v.setBackgroundColor(this.context.getResources().getColor(R.color.LightGreen));
            }
            else if (myNumber == 2){
                v.setBackgroundColor(this.context.getResources().getColor(R.color.LightSkyBlue));
            }
            else if (myNumber == 3){
                v.setBackgroundColor(this.context.getResources().getColor(R.color.LightPink));
            }
            else{
                v.setBackgroundColor(this.context.getResources().getColor(R.color.LightGrey));
            }

        }

        TasklistApplication thisApp = (TasklistApplication) loginActivity.getApplication();

        String payloadString = myNumber + item;
        byte[] payload = payloadString.getBytes();
        Set<String> topics = new HashSet<>(Arrays.asList( thisApp.getTopic()));
        Set<TransactionID> dependencies = new HashSet<>();

        dependencies = thisApp.getMainTopDeps();

        thisApp.getVirtual().addTransaction(thisApp.getContext(), topics, payload, dependencies);
    }

    /**
     * Handles MainActivity Calls to populate with most cu
     * @param aList :: List of Strings
     */
    public void handleActivity(List<String> aList){
        this.clear();
        this.addAll( aList  );
        this.notifyDataSetChanged();
    }

}
