package com.itachi1706.busarrivalsg.gsonObjects.sgLTA;

/**
 * Created by Kenneth on 18/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.GsonObjects.LTA
 */
public class BusArrivalMain {

    private String BusStopCode;
    private BusArrivalArrayObject[] Services;
    private String CurrentTime;

    public BusArrivalArrayObject[] getServices() {
        return Services;
    }

    public String getBusStopCode() {
        return BusStopCode;
    }

    public String getCurrentTime() {
        return CurrentTime;
    }
}
