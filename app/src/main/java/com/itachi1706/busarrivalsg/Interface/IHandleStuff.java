package com.itachi1706.busarrivalsg.Interface;

import com.itachi1706.busarrivalsg.GsonObjects.LTA.BusArrivalArrayObject;
import com.itachi1706.busarrivalsg.Objects.BusServices;

/**
 * Created by Kenneth on 31/10/2015.
 * for SingBuses in package com.itachi1706.busarrivalsg
 */
public interface IHandleStuff {
    void favouriteOrUnfavourite(BusServices fav, BusArrivalArrayObject item);
}
