package com.idevicesinc.sweetblue.rx;


import com.idevicesinc.sweetblue.BleManager;
import com.idevicesinc.sweetblue.ScanOptions;
import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.BleManager.DiscoveryListener.DiscoveryEvent;

/**
 * Convenience class used when scanning with {@link RxBleManager#scan(ScanOptions)}. This simply holds the {@link DiscoveryEvent}
 * returned from the scan with some convenience methods.
 */
public final class RxDiscoveryEvent
{

    private final DiscoveryEvent m_event;


    RxDiscoveryEvent(DiscoveryEvent event)
    {
        m_event = event;
    }


    public final DiscoveryEvent event()
    {
        return m_event;
    }

    /**
     * Returns <code>true</code> if the {@link DiscoveryEvent} was for the {@link BleDevice} being discovered for the first time.
     * This just calls {@link DiscoveryEvent#was(BleManager.DiscoveryListener.LifeCycle)} using {@link com.idevicesinc.sweetblue.BleManager.DiscoveryListener.LifeCycle#DISCOVERED}
     */
    public final boolean wasDiscovered()
    {
        return m_event.was(BleManager.DiscoveryListener.LifeCycle.DISCOVERED);
    }

    /**
     * Returns <code>true</code> if the {@link DiscoveryEvent} was for the {@link BleDevice} getting re-discovered.
     * This just calls {@link DiscoveryEvent#was(BleManager.DiscoveryListener.LifeCycle)} using {@link com.idevicesinc.sweetblue.BleManager.DiscoveryListener.LifeCycle#REDISCOVERED}
     */
    public final boolean wasRediscovered()
    {
        return m_event.was(BleManager.DiscoveryListener.LifeCycle.REDISCOVERED);
    }

    /**
     *
     * This just calls {@link DiscoveryEvent#was(BleManager.DiscoveryListener.LifeCycle)} using {@link com.idevicesinc.sweetblue.BleManager.DiscoveryListener.LifeCycle#UNDISCOVERED}
     */
    public final boolean wasUndiscovered()
    {
        return m_event.was(BleManager.DiscoveryListener.LifeCycle.UNDISCOVERED);
    }

    /**
     * Returns an instance of {@link RxBleDevice}
     */
    public final RxBleDevice getDevice()
    {
        return RxBleManager.getOrCreateDevice(m_event.device());
    }
}
