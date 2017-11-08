package com.idevicesinc.sweetblue.rx;


import com.idevicesinc.sweetblue.BleDevice;


public final class RxReadWriteEvent
{

    private final BleDevice.ReadWriteListener.ReadWriteEvent m_event;


    RxReadWriteEvent(BleDevice.ReadWriteListener.ReadWriteEvent event)
    {
        m_event = event;
    }


    public final BleDevice.ReadWriteListener.ReadWriteEvent event()
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
