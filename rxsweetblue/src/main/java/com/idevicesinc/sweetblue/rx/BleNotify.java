package com.idevicesinc.sweetblue.rx;

import com.idevicesinc.sweetblue.utils.Interval;
import com.idevicesinc.sweetblue.NotificationListener;
import java.util.UUID;


/**
 *
 * @deprecated - This is marked as deprecated only because it will be a part of the core SweetBlue library in version 3 (the name and function will remain the same).
 */
@Deprecated
public class BleNotify extends BleOp<BleNotify>
{

    Interval m_forceReadTimeout = Interval.INFINITE;
    NotificationListener m_notificationListener;


    public BleNotify()
    {
    }

    public BleNotify(UUID serviceUuid, UUID characteristicUuid)
    {
        super(serviceUuid, characteristicUuid);
    }

    public BleNotify(UUID characteristicUuid)
    {
        super(characteristicUuid);
    }


    @Override
    public final boolean isValid()
    {
        return charUuid != null;
    }

    @Override
    final BleNotify createDuplicate()
    {
        BleNotify notify = getDuplicateOp();
        notify.m_forceReadTimeout = m_forceReadTimeout;
        return notify;
    }

    @Override
    final BleNotify createNewOp()
    {
        return new BleNotify();
    }

    public final BleNotify setForceReadTimeout(Interval timeout)
    {
        m_forceReadTimeout = timeout;
        return this;
    }

    public final BleNotify setNotificationListener(NotificationListener listener)
    {
        m_notificationListener = listener;
        return this;
    }


    /**
     * Builder class to build out a list (or array) of {@link BleNotify} instances.
     */
    public final static class Builder extends BleOp.Builder<Builder, BleNotify>
    {

        public Builder()
        {
            this(null, null);
        }

        public Builder(UUID characteristicUuid)
        {
            this(null, characteristicUuid);
        }

        public Builder(UUID serviceUuid, UUID characteristicUuid)
        {
            currentOp = new BleNotify(serviceUuid, characteristicUuid);
        }


        public final Builder setForceReadTimeout(Interval timeout)
        {
            currentOp.setForceReadTimeout(timeout);
            return this;
        }

        public final Builder setNotificationListener(NotificationListener listener)
        {
            currentOp.setNotificationListener(listener);
            return this;
        }

    }
}