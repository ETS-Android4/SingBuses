package com.itachi1706.busarrivalsg.GsonObjects.LTA;

/**
 * Created by Kenneth on 18/6/2015
 * for SingBuses in package com.itachi1706.busarrivalsg.GsonObjects.LTA
 */
public class BusArrivalArrayObject {
    private String ServiceNo, Status, Operator;
    private BusArrivalArrayObjectEstimate NextBus, SubsequentBus;

    private String stopCode;

    public BusArrivalArrayObjectEstimate getSubsequentBus() {
        return SubsequentBus;
    }

    public BusArrivalArrayObjectEstimate getNextBus() {
        return NextBus;
    }

    public String getOperator() {
        return Operator;
    }

    public String getStatus() {
        return Status;
    }

    public String getServiceNo() {
        return ServiceNo;
    }

    public String getStopCode() {
        return stopCode;
    }

    public void setStopCode(String stopCode) {
        this.stopCode = stopCode;
    }
}
