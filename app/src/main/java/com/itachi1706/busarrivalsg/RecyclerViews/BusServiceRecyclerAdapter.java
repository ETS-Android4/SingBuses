package com.itachi1706.busarrivalsg.RecyclerViews;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusArrivalArrayObject;
import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusArrivalArrayObjectEstimate;
import com.itachi1706.busarrivalsg.R;
import com.itachi1706.busarrivalsg.StaticVariables;

import java.util.List;

/**
 * Created by Kenneth on 31/10/2015.
 * for SingBuses in package com.itachi1706.busarrivalsg.RecyclerViews
 */
public class BusServiceRecyclerAdapter extends RecyclerView.Adapter<BusServiceRecyclerAdapter.BusServiceViewHolder> {

    /**
     * This recycler adapter is used in the internal retrive all bus services from bus stop activity
     */

    private List<BusArrivalArrayObject> items;
    private Context context;

    public BusServiceRecyclerAdapter(List<BusArrivalArrayObject> objectList, Context context){
        this.items = objectList;
        this.context = context;
    }

    public void updateAdapter(List<BusArrivalArrayObject> newObjects){
        this.items = newObjects;
        notifyDataSetChanged();
    }

    @Override
    public BusServiceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View busServiceView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_bus_numbers, parent, false);
        return new BusServiceViewHolder(busServiceView);
    }

    @Override
    public void onBindViewHolder(BusServiceViewHolder holder, int position) {
        BusArrivalArrayObject i = items.get(position);

        holder.operatingStatus.setText(i.getStatus());
        if (i.getStatus().contains("Not") || i.getStatus().contains("not")){
            holder.operatingStatus.setTextColor(Color.RED);
        } else {
            holder.operatingStatus.setTextColor(Color.GREEN);
        }

        holder.busOperator.setText(i.getOperator());
        switch (i.getOperator().toUpperCase()){
            case "SMRT": holder.busOperator.setTextColor(Color.RED); break;
            case "SBST": holder.busOperator.setTextColor(Color.MAGENTA); break;
        }
        holder.busNumber.setText(i.getServiceNo());
        //TODO: Implement 3rd arrival status
        if (i.getStatus().equalsIgnoreCase("not")){
            notArriving(holder.busArrivalNow);
            notArriving(holder.busArrivalNext);
            return;
        }

        //Current Bus
        if (i.getNextBus().getEstimatedArrival() == null){
            notArriving(holder.busArrivalNow);
        } else {
            long est = StaticVariables.parseLTAEstimateArrival(i.getNextBus().getEstimatedArrival());
            String arrivalStatusNow;
            if (est <= 0)
                arrivalStatusNow = "Arr";
            else if (est == 1)
                arrivalStatusNow = est + " min";
            else
                arrivalStatusNow = est + " mins";
            if (!i.getNextBus().getMonitoredStatus()) arrivalStatusNow += "*";
            holder.busArrivalNow.setText(arrivalStatusNow);
            applyColorLoad(holder.busArrivalNow, i.getNextBus());
        }

        //2nd bus (Next bus)
        if (i.getSubsequentBus().getEstimatedArrival() == null){
            notArriving(holder.busArrivalNext);
        } else {
            long est = StaticVariables.parseLTAEstimateArrival(i.getSubsequentBus().getEstimatedArrival());
            String arrivalStatusNext;
            if (est <= 0)
                arrivalStatusNext = "Arr";
            else if (est == 1)
                arrivalStatusNext = est + " min";
            else
                arrivalStatusNext = est + " mins";
            if (!i.getSubsequentBus().getMonitoredStatus()) arrivalStatusNext += "*";
            holder.busArrivalNext.setText(arrivalStatusNext);
            applyColorLoad(holder.busArrivalNext, i.getSubsequentBus());
        }
    }

    private void notArriving(TextView view){
        view.setText("-");
        view.setTextColor(Color.GRAY);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void applyColorLoad(TextView view, BusArrivalArrayObjectEstimate obj){
        switch (obj.getLoad()){
            case "Seats Available": view.setTextColor(Color.GREEN); break;
            case "Standing Available": view.setTextColor(Color.YELLOW); break;
            case "Limited Standing": view.setTextColor(Color.RED); break;
        }
    }


    public class BusServiceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        protected TextView busOperator, busNumber, busArrivalNow, busArrivalNext, operatingStatus;

        public BusServiceViewHolder(View v){
            super(v);
            busOperator = (TextView) v.findViewById(R.id.tvBusOperator);
            busNumber = (TextView) v.findViewById(R.id.tvBusService);
            busArrivalNow = (TextView) v.findViewById(R.id.tvBusArrivalNow);
            busArrivalNext = (TextView) v.findViewById(R.id.tvBusArrivalNext);
            operatingStatus = (TextView) v.findViewById(R.id.tvBusStatus);
        }

        @Override
        public void onClick(View v) {

        }
    }
}
