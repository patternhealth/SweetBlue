package com.idevicesinc.sweetblue.rx.exception;


import com.idevicesinc.sweetblue.BleDevice.ReadWriteListener;


/**
 * Exception class which holds an instance of {@link ReadWriteListener.ReadWriteEvent}, which gives more information about what went wrong
 * with a read/write. This gets passed into the onError method when using {@link RxBleDevice#read(BleRead)}, or {@link RxBleDevice#write(BleWrite)}.
 */
public class ReadWriteException extends EventException
{

    public ReadWriteException(ReadWriteListener.ReadWriteEvent event)
    {
        super(event);
    }

    @Override
    public ReadWriteListener.ReadWriteEvent getEvent()
    {
        return super.getEvent();
    }
}