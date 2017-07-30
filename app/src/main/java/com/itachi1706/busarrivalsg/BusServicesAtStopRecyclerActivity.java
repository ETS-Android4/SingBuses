package com.itachi1706.busarrivalsg;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.gson.Gson;
import com.itachi1706.busarrivalsg.AsyncTasks.GetBusServicesHandler;
import com.itachi1706.busarrivalsg.Database.BusStopsDB;
import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusArrivalArrayObject;
import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusArrivalMain;
import com.itachi1706.busarrivalsg.Interface.IHandleStuff;
import com.itachi1706.busarrivalsg.Objects.BusServices;
import com.itachi1706.busarrivalsg.RecyclerViews.BusServiceRecyclerAdapter;
import com.itachi1706.busarrivalsg.Services.BusStorage;
import com.itachi1706.busarrivalsg.Util.StaticVariables;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class BusServicesAtStopRecyclerActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, IHandleStuff {

    RecyclerView buses;
    String busStopCode, busStopName, busServicesString;
    BusServiceRecyclerAdapter adapter;
    SwipeRefreshLayout swipeToRefresh;
    SharedPreferences sp;
    String[] busServices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_services_at_stop_recycler);

        if (this.getIntent().hasExtra("stopCode")) busStopCode = this.getIntent().getStringExtra("stopCode");
        if (this.getIntent().hasExtra("stopName")) busStopName = this.getIntent().getStringExtra("stopName");
        if (this.getIntent().hasExtra("busServices")) busServicesString = this.getIntent().getStringExtra("busServices");

        buses = (RecyclerView) findViewById(R.id.rvBusService);
        if (buses != null) buses.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        buses.setLayoutManager(linearLayoutManager);
        buses.setItemAnimator(new DefaultItemAnimator());

        adapter = new BusServiceRecyclerAdapter(new ArrayList<BusArrivalArrayObject>(), this);
        buses.setAdapter(adapter);

        swipeToRefresh = (SwipeRefreshLayout) findViewById(R.id.refresh_swipe);
        if (swipeToRefresh != null) {
            swipeToRefresh.setOnRefreshListener(this);
            swipeToRefresh.setColorSchemeResources(
                    R.color.refresh_progress_1,
                    R.color.refresh_progress_2,
                    R.color.refresh_progress_3,
                    R.color.refresh_progress_4);
        }

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (sp.getBoolean("showHint", true))
            Toast.makeText(this, R.string.hint_add_bus_to_fav, Toast.LENGTH_SHORT).show();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onResume(){
        super.onResume();
        if (busStopCode == null){
            Log.e("BUS-SERVICE", "You aren't supposed to be here. Exiting");
            Toast.makeText(this, R.string.invalid_activity_access, Toast.LENGTH_SHORT).show();
            this.finish();
        } else {
            if (busStopName != null)
                getSupportActionBar().setTitle(busStopName.trim() + " (" + busStopCode.trim() + ")");
            else
                getSupportActionBar().setTitle(busStopCode.trim() + "");
            swipeToRefresh.setRefreshing(true);

            if (busServicesString == null || busServicesString.isEmpty()) {
                // Retrieve it from DB
                BusStopsDB db = new BusStopsDB(this);
                busServicesString = db.getBusStopByBusStopCode(busStopCode).getServices();
            }

            busServices = busServicesString.split(",");
            updateBusStop();
        }
        sp = PreferenceManager.getDefaultSharedPreferences(this);
    }

    private void addOrRemoveFav(final BusServices fav, final ArrayList<BusServices> all, boolean alrFav){
        String message;
        if (alrFav){
            if (busStopName != null)
                message = getString(R.string.dialog_message_remove_from_fav_with_stop_name, fav.getServiceNo(), busStopName, fav.getStopID());
            else
                message = getString(R.string.dialog_message_remove_from_fav, fav.getServiceNo(), fav.getStopID());
            new AlertDialog.Builder(this).setTitle(R.string.dialog_title_remove_from_fav)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Remove from favourites
                            for (int i = 0; i < all.size(); i++){
                                BusServices s = all.get(i);
                                if (s.getStopID().equalsIgnoreCase(fav.getStopID()) && s.getServiceNo().equalsIgnoreCase(fav.getServiceNo())) {
                                    all.remove(i);
                                    break;
                                }
                            }
                            BusStorage.updateBusJSON(sp, all);
                            Toast.makeText(getApplicationContext(), R.string.toast_message_remove_from_fav, Toast.LENGTH_SHORT).show();
                        }
                    }).setNegativeButton(android.R.string.no, null).show();
        } else {
            if (busStopName != null)
                message = getString(R.string.dialog_message_add_to_fav_with_stop_name, fav.getServiceNo(), busStopName, fav.getStopID());
            else
                message = getString(R.string.dialog_message_add_to_fav, fav.getServiceNo(), fav.getStopID());
            new AlertDialog.Builder(this).setTitle(R.string.dialog_title_add_to_fav)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Remove from favourites
                            BusStorage.addNewBus(fav, sp);
                            Toast.makeText(getApplicationContext(), R.string.toast_message_add_to_fav, Toast.LENGTH_SHORT).show();
                        }
                    }).setNegativeButton(android.R.string.no, null).show();
        }
    }

    private void updateBusStop(){
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setTitle(getString(R.string.dialog_title_retrieve_data_bus_service));
        dialog.setMessage(getString(R.string.dialog_message_retrieve_data_bus_service,busStopCode));
        dialog.show();
        new GetBusServicesHandler(dialog, this, new BusServicesAtStopHandler(this)).execute(busStopCode);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bus_services_at_stop, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, MainSettings.class));
            return true;
        } else if (id == R.id.action_refresh){
            swipeToRefresh.setRefreshing(true);
            updateBusStop();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        updateBusStop();
    }

    @Override
    public void favouriteOrUnfavourite(BusServices fav, BusArrivalArrayObject item) {
        if (busStopName != null)
            fav.setStopName(busStopName);

        boolean alrFavourited = false;
        ArrayList<BusServices> exist = BusStorage.getStoredBuses(sp);
        if (exist != null) {
            //Compare if in favourites already
            for (BusServices s : exist) {
                if (s.getServiceNo().equals(item.getServiceNo()) && s.getStopID().equals(item.getStopCode())) {
                    alrFavourited = true;
                    break;
                }
            }
        }

        addOrRemoveFav(fav, exist, alrFavourited);
    }


    static class BusServicesAtStopHandler extends Handler {
        WeakReference<BusServicesAtStopRecyclerActivity> mActivity;

        BusServicesAtStopHandler(BusServicesAtStopRecyclerActivity activity){
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg){
            BusServicesAtStopRecyclerActivity activity = mActivity.get();

            super.handleMessage(msg);

            switch (msg.what){
                case StaticVariables.BUS_SERVICE_JSON_RETRIEVED:
                    String json = (String) msg.getData().get("jsonString");
                    activity.processMessage(json);
                    break;
            }
        }
    }

    private void processMessage(String json){
        Gson gson = new Gson();
        if (!StaticVariables.checkIfYouGotJsonString(json)){
            //Invalid string, retrying
            Toast.makeText(this, R.string.toast_message_invalid_json_string, Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<BusArrivalArrayObject> items = new ArrayList<>();
        BusArrivalMain mainArr = gson.fromJson(json, BusArrivalMain.class);
        if (mainArr == null) return;
        if (mainArr.getServices() == null) return;
        BusArrivalArrayObject[] array = mainArr.getServices();
        String stopID = mainArr.getBusStopCode();
        if (swipeToRefresh.isRefreshing())
            swipeToRefresh.setRefreshing(false);
        for (BusArrivalArrayObject obj : array){
            obj.setStopCode(stopID);
            // Check for service status
            items.add(obj);
        }
        adapter.updateAdapter(items);
        adapter.notifyDataSetChanged();
    }

    private boolean containsService(String busService) {
        for (String s : busServices) {
            if (s.equals(busService)) return true;
        }
        return false;
    }
}
