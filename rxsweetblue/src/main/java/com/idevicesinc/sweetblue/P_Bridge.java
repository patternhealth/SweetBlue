package com.idevicesinc.sweetblue;


public final class P_Bridge
{

    public static P_SweetHandler getUpdateHandler()
    {
        BleManager mgr = BleManager.s_instance;
        if (mgr == null)
            throw new NullPointerException("BleManager is null! You must first instantiate BleManager.");

        return mgr.getPostManager().getUpdateHandler();
    }


    private P_Bridge()
    {
        throw new AssertionError("No instances.");
    }

}
