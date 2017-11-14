package com.idevicesinc.sweetblue.rx.schedulers;


import com.idevicesinc.sweetblue.P_Bridge;
import com.idevicesinc.sweetblue.P_SweetHandler;
import com.idevicesinc.sweetblue.rx.plugins.RxSweetBluePlugins;

import java.util.concurrent.Callable;

import io.reactivex.Scheduler;

/**
 * SweetBlue specific schedulers.
 */
public final class SweetBlueSchedulers
{

    public static final class SweetBlueHandlerHolder
    {
        static final Scheduler INSTANCE = new SweetBlueScheduler(P_Bridge.getUpdateHandler());
    }

    private static final Scheduler SWEETBLUE_THREAD = RxSweetBluePlugins.initSweetBlueThreadScheduler(new Callable<Scheduler>()
    {
        @Override
        public Scheduler call() throws Exception
        {
            return SweetBlueHandlerHolder.INSTANCE;
        }
    });

    /** A {@link Scheduler} which executes actions on SweetBlue's update thread. */
    public static Scheduler sweetBlueThread()
    {
        return RxSweetBluePlugins.onSweetBlueThreadScheduler(SWEETBLUE_THREAD);
    }


    private SweetBlueSchedulers()
    {
        throw new AssertionError("No instances.");
    }

}
