package com.idevicesinc.sweetblue.rx;


import com.idevicesinc.sweetblue.BleDevice;


public class RxBondEvent
{

    private final BleDevice.BondListener.BondEvent m_event;


    RxBondEvent(BleDevice.BondListener.BondEvent event)
    {
        m_event = event;
    }


    public final BleDevice.BondListener.BondEvent event()
    {
        return m_event;
    }

    public final boolean wasSuccess()
    {
        return m_event.wasSuccess();
    }

    public final RxBleDevice device()
    {
        return RxBleManager.getOrCreateDevice(m_event.device());
    }

}
