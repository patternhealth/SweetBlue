package com.idevicesinc.sweetblue.rx.exception;


import com.idevicesinc.sweetblue.BleNode.ConnectionFailListener.ConnectionFailEvent;
import com.idevicesinc.sweetblue.rx.RxBleDevice;

/**
 * Exception class used to indicate a connection failure, which holds an instance of {@link ConnectionFailEvent},
 * when using {@link RxBleDevice#connect()}.
 */
public class ConnectException extends EventException
{
    public ConnectException(ConnectionFailEvent event)
    {
        super(event);
    }

    @Override
    public ConnectionFailEvent getEvent()
    {
        return super.getEvent();
    }
}
