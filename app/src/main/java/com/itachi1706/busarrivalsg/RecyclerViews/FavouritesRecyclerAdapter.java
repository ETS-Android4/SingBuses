package com.itachi1706.busarrivalsg.RecyclerViews;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.itachi1706.busarrivalsg.BusLocationMapsActivity;
import com.itachi1706.busarrivalsg.BusLocationMapsDialogFragment;
import com.itachi1706.busarrivalsg.BusServicesAtStopRecyclerActivity;
import com.itachi1706.busarrivalsg.Database.BusStopsDB;
import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusStopJSON;
import com.itachi1706.busarrivalsg.Objects.BusServices;
import com.itachi1706.busarrivalsg.Objects.BusStatus;
import com.itachi1706.busarrivalsg.Objects.CommonEnums;
import com.itachi1706.busarrivalsg.R;
import com.itachi1706.busarrivalsg.Services.BusStorage;
import com.itachi1706.busarrivalsg.Util.StaticVariables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.itachi1706.busarrivalsg.Util.StaticVariables.CUR;
import static com.itachi1706.busarrivalsg.Util.StaticVariables.NEXT;
import static com.itachi1706.busarrivalsg.Util.StaticVariables.SUB;

/**
 * Created by Kenneth on 31/10/2015.
 * for SingBuses in package com.itachi1706.busarrivalsg.RecyclerViews
 */
public class FavouritesRecyclerAdapter extends RecyclerView.Adapter<FavouritesRecyclerAdapter.FavouritesViewHolder> {

    /**
     * This recycler adapter is used in the internal retrieve all bus services from main activity's favourites list
     */

    private List<BusServices> items;
    private AppCompatActivity activity;
    private boolean serverTime;
    private String currentTime;

    @Deprecated
    public FavouritesRecyclerAdapter(List<BusServices> objectList, AppCompatActivity activity){
        this(objectList, activity, false);
    }

    public FavouritesRecyclerAdapter(List<BusServices> objectList, AppCompatActivity activity, boolean useServerTime){
        this.items = objectList;
        this.activity = activity;
        this.serverTime = useServerTime;
    }

    public void updateAdapter(List<BusServices> newObjects, String currentTime){
        this.items = newObjects;
        this.currentTime = currentTime;
        notifyDataSetChanged();
    }

    public boolean moveItem(int from, int to) {
        if (from < to) {
            for (int i = from; i < to; i++) {
                Collections.swap(items, i, i + 1);
            }
        } else {
            for (int i = from; i > to; i--) {
                Collections.swap(items, i, i - 1);
            }
        }
        notifyItemMoved(from, to);
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        BusStorage.updateBusJSON(sp, (ArrayList<BusServices>) items);
        return true;
    }

    @Override
    public FavouritesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View busServiceView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_bus_numbers, parent, false);
        return new FavouritesViewHolder(busServiceView);
    }

    @Override
    public void onBindViewHolder(FavouritesViewHolder holder, int position) {
        BusServices i = items.get(position);

        holder.busOperator.setText(i.getOperator());
        switch (i.getOperator().toUpperCase()) {
            case "SMRT": holder.busOperator.setTextColor(Color.RED); break;
            case "SBST": holder.busOperator.setTextColor(Color.MAGENTA); break;
            case "TTS": holder.busOperator.setTextColor(Color.GREEN); break;
            case "GAS": holder.busOperator.setTextColor(Color.YELLOW); break;
        }

        holder.busNumber.setText(i.getServiceNo());
        holder.stopName.setVisibility(View.VISIBLE);
        holder.stopName.setText((i.getStopName() == null) ? i.getStopID().trim() : i.getStopName().trim() + " (" + i.getStopID().trim() + ")");

        if (!i.isObtainedNextData()) {
            processing(holder.busArrivalNow);
            processing(holder.busArrivalNext);
            processing(holder.busArrivalSub);
            return;
        }

        if (!i.isSvcStatus()) {
            holder.operatingStatus.setText(activity.getString(R.string.service_not_operational));
            holder.operatingStatus.setTextColor(Color.RED);
            notArriving(holder.busArrivalNow, holder.wheelchairNow);
            notArriving(holder.busArrivalNext, holder.wheelchairNext);
            notArriving(holder.busArrivalSub, holder.wheelchairSub);
            return;
        }
        holder.operatingStatus.setText(activity.getString(R.string.service_operational));
        holder.operatingStatus.setTextColor(Color.GREEN);

        //Current Bus
        if (i.getCurrentBus().getEstimatedArrival() == null) notArriving(holder.busArrivalNow, holder.wheelchairNow);
        else {
            long est = StaticVariables.parseLTAEstimateArrival(i.getCurrentBus().getEstimatedArrival(), serverTime, currentTime);
            String arrivalStatusNow;
            if (est == -9999)
                arrivalStatusNow = "-";
            else if (est <= 0)
                arrivalStatusNow = "Arr";
            else if (est == 1)
                arrivalStatusNow = est + "";
            else
                arrivalStatusNow = est + "";
            holder.busArrivalNow.setText(arrivalStatusNow);
            applyColorLoad(holder.busArrivalNow, i.getCurrentBus());
            holder.wheelchairNow.setVisibility(View.INVISIBLE);
            if (i.getCurrentBus().isWheelChairAccessible())
                holder.wheelchairNow.setVisibility(View.VISIBLE);
            holder.busArrivalNow.setOnClickListener(new ArrivalButton(i, i.getStopID(), i.getServiceNo(), CUR));
        }

        //2nd Bus (Next Bus)
        if (i.getNextBus().getEstimatedArrival() == null) notArriving(holder.busArrivalNext, holder.wheelchairNext);
        else {
            long est = StaticVariables.parseLTAEstimateArrival(i.getNextBus().getEstimatedArrival(), serverTime, currentTime);
            String arrivalStatusNext;
            if (est == -9999)
                arrivalStatusNext = "-";
            else if (est <= 0)
                arrivalStatusNext = "Arr";
            else if (est == 1)
                arrivalStatusNext = est + "";
            else
                arrivalStatusNext = est + "";
            holder.busArrivalNext.setText(arrivalStatusNext);
            applyColorLoad(holder.busArrivalNext, i.getNextBus());
            holder.wheelchairNext.setVisibility(View.INVISIBLE);
            if (i.getNextBus().isWheelChairAccessible())
                holder.wheelchairNext.setVisibility(View.VISIBLE);
            holder.busArrivalNext.setOnClickListener(new ArrivalButton(i, i.getStopID(), i.getServiceNo(), NEXT));
        }

        //3rd bus (Subsequent Bus)
        if (i.getSubsequentBus() == null){
            comingSoon(holder.busArrivalSub);
            return;
        }
        if (i.getSubsequentBus().getEstimatedArrival() == null) notArriving(holder.busArrivalSub, holder.wheelchairSub);
        else {
            long est = StaticVariables.parseLTAEstimateArrival(i.getSubsequentBus().getEstimatedArrival(), serverTime, currentTime);
            String arrivalStatusSub;
            if (est == -9999)
                arrivalStatusSub = "-";
            else if (est <= 0)
                arrivalStatusSub = "Arr";
            else if (est == 1)
                arrivalStatusSub = est + "";
            else
                arrivalStatusSub = est + "";
            holder.busArrivalSub.setText(arrivalStatusSub);
            applyColorLoad(holder.busArrivalSub, i.getSubsequentBus());
            holder.wheelchairSub.setVisibility(View.INVISIBLE);
            if (i.getSubsequentBus().isWheelChairAccessible())
                holder.wheelchairSub.setVisibility(View.VISIBLE);
            holder.busArrivalSub.setOnClickListener(new ArrivalButton(i, i.getStopID(), i.getServiceNo(), SUB));
        }

    }

    private void comingSoon(TextView view){
        view.setText(R.string.feature_coming_soon);
        view.setTextColor(Color.GRAY);
    }

    private void processing(TextView view){
        view.setText("...");
        view.setTextColor(Color.GRAY);
    }

    private void notArriving(TextView view, ImageView wheelchair){
        view.setText("-");
        view.setTextColor(Color.GRAY);
        wheelchair.setVisibility(View.INVISIBLE);
        view.setOnClickListener(new UnavailableButton());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void applyColorLoad(TextView view, BusStatus obj){
        if (view.getText().toString().equalsIgnoreCase("") || view.getText().toString().equalsIgnoreCase("-")) {
            view.setTextColor(Color.GRAY);
            return;
        }
        switch (obj.getLoad()){
            case CommonEnums.BUS_SEATS_AVAIL: view.setTextColor(Color.GREEN); break;
            case CommonEnums.BUS_STANDING_AVAIL: view.setTextColor(Color.YELLOW); break;
            case CommonEnums.BUS_LIMITED_SEATS: view.setTextColor(Color.RED); break;
            default: view.setTextColor(Color.GRAY); break;
        }
    }

    public boolean removeFavourite(final int position) {
        final BusServices item = items.get(position);
        String message;
        if (item.getStopName() != null)
            message = activity.getString(R.string.dialog_message_remove_from_fav_with_stop_name, item.getServiceNo(), item.getStopName(), item.getStopID());
        else
            message = activity.getString(R.string.dialog_message_remove_from_fav, item.getServiceNo(), item.getStopID());

        // Companion addition
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        switch (sp.getString("companionDevice", "none")) {
            case "pebble": message += activity.getString(R.string.dialog_message_remove_from_fav_pebble);
        }

        AlertDialog alert = new AlertDialog.Builder(activity).setTitle(R.string.dialog_title_remove_from_fav)
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    //Remove from favourites
                    for (int i = 0; i < items.size(); i++){
                        BusServices s = items.get(i);
                        if (s.getStopID().equalsIgnoreCase(item.getStopID()) && s.getServiceNo().equalsIgnoreCase(item.getServiceNo())) {
                            items.remove(i);
                            break;
                        }
                    }
                    BusStorage.updateBusJSON(sp, (ArrayList<BusServices>) items);
                    notifyItemRemoved(position);
                    Toast.makeText(activity.getApplicationContext(), R.string.toast_message_remove_from_fav, Toast.LENGTH_SHORT).show();
                }).setNegativeButton(android.R.string.no, null).create();
        alert.setOnDismissListener(dialogInterface -> notifyItemChanged(position));
        alert.show();
        return true;
    }


    class FavouritesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        TextView busOperator, busNumber, operatingStatus, stopName;
        Button busArrivalNow, busArrivalNext, busArrivalSub;
        ImageView wheelchairNow, wheelchairNext, wheelchairSub;

        FavouritesViewHolder(View v){
            super(v);
            busOperator = v.findViewById(R.id.tvBusOperator);
            busNumber = v.findViewById(R.id.tvBusService);
            busArrivalNow = v.findViewById(R.id.btnBusArrivalNow);
            busArrivalNext = v.findViewById(R.id.btnBusArrivalNext);
            busArrivalSub = v.findViewById(R.id.btnBusArrivalSub);
            operatingStatus = v.findViewById(R.id.tvBusStatus);
            stopName = v.findViewById(R.id.tvBusStopName);
            wheelchairNow = v.findViewById(R.id.ivWheelchairNow);
            wheelchairNext = v.findViewById(R.id.ivWheelchairNext);
            wheelchairSub = v.findViewById(R.id.ivWheelchairSub);
            wheelchairNow.setVisibility(View.INVISIBLE);
            wheelchairNext.setVisibility(View.INVISIBLE);
            wheelchairSub.setVisibility(View.INVISIBLE);
            v.setOnLongClickListener(this);
            v.setOnClickListener(this);
            busArrivalNext.setOnLongClickListener(this);
            busArrivalNow.setOnLongClickListener(this);
            busArrivalSub.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = this.getLayoutPosition();
            final BusServices item = items.get(position);
            Log.d("Size", "" + items.size());
            Intent serviceIntent = new Intent(activity, BusServicesAtStopRecyclerActivity.class);
            serviceIntent.putExtra("stopCode", item.getStopID());
            if (item.getStopName() != null)
                serviceIntent.putExtra("stopName", item.getStopName());
            activity.startActivity(serviceIntent);

        }

        @Override
        public boolean onLongClick(View v) {
            int position = this.getLayoutPosition();
            final BusServices item = items.get(position);

            final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
            if (sp.getBoolean("showFavHint", true)) {
                String message;
                if (item.getStopName() != null)
                    message = activity.getString(R.string.snackbar_message_remove_from_fav_with_stop_name, item.getServiceNo());
                else
                    message = activity.getString(R.string.snackbar_message_remove_from_fav, item.getServiceNo());
                    Snackbar.make(v, message
                            , Snackbar.LENGTH_SHORT).setAction("Hide Tips", view -> sp.edit().putBoolean("showFavHint", false).apply()).show();
            }
            return false;
        }
    }

    private class UnavailableButton implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            new AlertDialog.Builder(v.getContext()).setTitle(R.string.dialog_title_bus_timing_unavailable)
                    .setMessage(R.string.dialog_message_bus_timing_unavailable)
                    .setPositiveButton(R.string.dialog_action_positive_close, null).show();
        }
    }

    private class ArrivalButton implements View.OnClickListener{

        private double longitude = -1000, latitude = -1000;
        private String stopCode = "", serviceNo = "Unknown";
        private BusServices busObj;
        private int state;

        ArrivalButton(BusServices busObj, String busStopCode, String svcNo, int state) {
            BusStatus status = (state == CUR) ? busObj.getCurrentBus() :
                    (state == NEXT) ? busObj.getNextBus() : busObj.getSubsequentBus();
            this.state = state;
            this.busObj = busObj;
            this.longitude = status.getLongitude();
            this.latitude = status.getLatitude();
            this.stopCode = busStopCode.trim();
            this.serviceNo = svcNo.trim();
        }

        @Override
        public void onClick(View v) {
            if (longitude == -1000 || latitude == -1000){
                //Error, invalid location
                new AlertDialog.Builder(activity).setTitle(R.string.dialog_title_bus_location_unavailable)
                        .setMessage(R.string.dialog_message_bus_location_unavailable)
                        .setPositiveButton(R.string.dialog_action_positive_close, null).show();
                return;
            }
            if (longitude == -11 && latitude == -11){
                new AlertDialog.Builder(activity).setTitle(R.string.dialog_title_bus_timing_unavailable)
                        .setMessage(R.string.dialog_message_bus_timing_unavailable)
                        .setPositiveButton(R.string.dialog_action_positive_close, null).show();
                return;
            }

            if (latitude == 0 && longitude == 0) {
                new AlertDialog.Builder(activity).setTitle("Bus Service in Depot")
                        .setMessage("The Bus Service is currently still in the depot so no location can be obtained!")
                        .setPositiveButton("Close", null).show();
                return;
            }

            //Check if Google Play Services is enabled
            int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity);
            if (code != ConnectionResult.SUCCESS){
                GoogleApiAvailability.getInstance().getErrorDialog(activity, code, 0);
                return;
            }

            Intent mapsIntent = new Intent(activity, BusLocationMapsActivity.class);
            mapsIntent.putExtra("busCode", stopCode);
            mapsIntent.putExtra("busSvcNo", serviceNo);

            // 3 Bus statuses
            mapsIntent.putExtra("lat1", busObj.getCurrentBus().getLatitude());
            mapsIntent.putExtra("lng1", busObj.getCurrentBus().getLongitude());
            mapsIntent.putExtra("arr1", busObj.getCurrentBus().getEstimatedArrival());
            mapsIntent.putExtra("lat2", busObj.getNextBus().getLatitude());
            mapsIntent.putExtra("lng2", busObj.getNextBus().getLongitude());
            mapsIntent.putExtra("arr2", busObj.getNextBus().getEstimatedArrival());
            mapsIntent.putExtra("lat3", busObj.getSubsequentBus().getLatitude());
            mapsIntent.putExtra("lng3", busObj.getSubsequentBus().getLongitude());
            mapsIntent.putExtra("arr3", busObj.getSubsequentBus().getEstimatedArrival());
            mapsIntent.putExtra("type1", busObj.getCurrentBus().getBusType());
            mapsIntent.putExtra("type2", busObj.getNextBus().getBusType());
            mapsIntent.putExtra("type3", busObj.getSubsequentBus().getBusType());
            mapsIntent.putExtra("useSTime", serverTime);
            mapsIntent.putExtra("sTime", currentTime);
            mapsIntent.putExtra("state", state);

            //Get Bus stop longitude and latitude
            BusStopsDB db = new BusStopsDB(activity);
            BusStopJSON busStop = db.getBusStopByBusStopCode(stopCode);
            if (busStop != null) {
                mapsIntent.putExtra("buslat", busStop.getLatitude());
                mapsIntent.putExtra("buslng", busStop.getLongitude());
            }

            if (!PreferenceManager.getDefaultSharedPreferences(v.getContext()).getBoolean("mapPopup", true))
                activity.startActivity(mapsIntent);
            else {
                final BusLocationMapsDialogFragment dialog = new BusLocationMapsDialogFragment();
                dialog.setArguments(mapsIntent.getExtras());
                dialog.show(activity.getSupportFragmentManager(), "123");
            }
        }
    }
}
