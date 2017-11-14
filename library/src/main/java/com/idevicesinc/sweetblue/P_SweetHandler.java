package com.idevicesinc.sweetblue;


public interface P_SweetHandler
{

    void post(Runnable action);
    void postDelayed(Runnable action, long delay);
    void postDelayed(Runnable action, long delay, Object tag);
    void removeCallbacks(Runnable action);
    void removeCallbacks(Object tag);
    Thread getThread();

}
