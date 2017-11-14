package com.idevicesinc.sweetblue.rx;


import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.BleDevice.BondListener;
import com.idevicesinc.sweetblue.BleDevice.ReadWriteListener;
import com.idevicesinc.sweetblue.BleDevice.ReadWriteListener.ReadWriteEvent;
import com.idevicesinc.sweetblue.BleDeviceState;
import com.idevicesinc.sweetblue.DeviceStateListener;
import com.idevicesinc.sweetblue.NotificationListener;
import com.idevicesinc.sweetblue.rx.exception.BondException;
import com.idevicesinc.sweetblue.rx.exception.ConnectException;
import com.idevicesinc.sweetblue.BleNode.ConnectionFailListener.ConnectionFailEvent;
import com.idevicesinc.sweetblue.rx.exception.ReadWriteException;

import org.reactivestreams.Subscriber;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.processors.PublishProcessor;


public class RxBleDevice
{

    private final BleDevice m_device;
    private final PublishProcessor<BleDevice.StateListener.StateEvent> m_StatePublisher;
    private final PublishProcessor<NotificationListener.NotificationEvent> m_notifyPublisher;


    private RxBleDevice(BleDevice device)
    {
        m_device = device;
        m_StatePublisher = PublishProcessor.create();
        m_notifyPublisher = PublishProcessor.create();
        setListeners();
    }

    private void setListeners()
    {
        m_device.setListener_State(new DeviceStateListener()
        {
            @Override
            public void onEvent(BleDevice.StateListener.StateEvent e)
            {
                if (m_StatePublisher.hasSubscribers())
                    m_StatePublisher.onNext(e);
            }
        });
        m_device.setListener_Notification(new NotificationListener()
        {
            @Override
            public void onEvent(NotificationEvent e)
            {
                if (m_notifyPublisher.hasSubscribers())
                    m_notifyPublisher.onNext(e);
            }
        });
    }

    public final PublishProcessor getStatePublisher()
    {
        return m_StatePublisher;
    }

    public final BleDevice getBleDevice()
    {
        return m_device;
    }


    /**
     * Connect to this BLE device.
     *
     * Returns a {@link Single} which holds nothing. {@link SingleEmitter#onSuccess(Object)} will be called when connected, otherwise
     * a {@link ConnectException} will be returned in {@link SingleEmitter#onError(Throwable)}, which contains the
     * {@link ConnectionFailEvent}.
     */
    public Completable connect()
    {
        return Completable.create(new CompletableOnSubscribe()
        {
            @Override
            public void subscribe(final CompletableEmitter emitter) throws Exception
            {
                if (emitter.isDisposed())  return;

                m_device.connect(new BleDevice.StateListener()
                {
                    @Override
                    public void onEvent(StateEvent e)
                    {
                        if (e.didEnter(BleDeviceState.INITIALIZED))
                            emitter.onComplete();

                    }
                }, new BleDevice.DefaultConnectionFailListener() {
                    @Override
                    public Please onEvent(ConnectionFailEvent e)
                    {
                        Please please = super.onEvent(e);

                        if (!please.isRetry() && !emitter.isDisposed())
                            emitter.onError(new ConnectException(e));

                        return please;
                    }
                });
            }
        });
    }

    /**
     * Bonds this device.
     *
     * Returns a {@link Single} which holds an instance of {@link BondListener.BondEvent}. If the bond fails,
     * {@link SingleEmitter#onError(Throwable)} will be called which holds an instance of {@link BondException}, which also holds an instance
     * of {@link BondListener.BondEvent}, so you can get more information on what went wrong.
     */
    public Single<BleDevice.BondListener.BondEvent> bond()
    {
        return Single.create(new SingleOnSubscribe<BondListener.BondEvent>()
        {
            @Override
            public void subscribe(final SingleEmitter<BondListener.BondEvent> emitter) throws Exception
            {
                if (emitter.isDisposed()) return;

                m_device.bond(new BondListener()
                {
                    @Override
                    public void onEvent(BondEvent e)
                    {
                        if (emitter.isDisposed()) return;

                        if (e.wasSuccess())
                            emitter.onSuccess(e);
                        else
                            emitter.onError(new BondException(e));
                    }
                });
            }
        });
    }

    public void unbond()
    {
        m_device.unbond();
    }

    /**
     * Perform a BLE read on this device.
     *
     * Returns a {@link Single} which holds an instance of {@link ReadWriteEvent}. If the bond fails,
     * {@link SingleEmitter#onError(Throwable)} will be called which holds an instance of {@link ReadWriteException}, which also holds an instance
     * of {@link ReadWriteEvent}, so you can get more information on what went wrong.
     */
    public Single<BleDevice.ReadWriteListener.ReadWriteEvent> read(final BleRead read)
    {
        return Single.create(new SingleOnSubscribe<ReadWriteEvent>()
        {
            @Override
            public void subscribe(final SingleEmitter<ReadWriteEvent> emitter) throws Exception
            {
                if (emitter.isDisposed()) return;

                m_device.read(read.serviceUuid, read.charUuid, read.descriptorFilter, new ReadWriteListener()
                {
                    @Override
                    public void onEvent(ReadWriteEvent e)
                    {
                        if (emitter.isDisposed()) return;

                        if (e.wasSuccess())
                            emitter.onSuccess(e);
                        else
                            emitter.onError(new ReadWriteException(e));
                    }
                });
            }
        });
    }

    /**
     * Performs a BLE write on this device.
     *
     * Returns a {@link Single} which holds an instance of {@link ReadWriteEvent}. If the bond fails,
     * {@link SingleEmitter#onError(Throwable)} will be called which holds an instance of {@link ReadWriteException}, which also holds an instance
     * of {@link ReadWriteEvent}, so you can get more information on what went wrong.
     */
    public Single<ReadWriteListener.ReadWriteEvent> write(final BleWrite write)
    {
        return Single.create(new SingleOnSubscribe<ReadWriteEvent>()
        {
            @Override
            public void subscribe(final SingleEmitter<ReadWriteEvent> emitter) throws Exception
            {
                if (emitter.isDisposed()) return;

                m_device.write(write.serviceUuid, write.charUuid, write.m_data, write.descriptorFilter, new ReadWriteListener()
                {
                    @Override
                    public void onEvent(ReadWriteEvent e)
                    {
                        if (emitter.isDisposed()) return;

                        if (e.wasSuccess())
                            emitter.onSuccess(e);
                        else
                            emitter.onError(new ReadWriteException(e));
                    }
                });
            }
        });
    }

    /**
     * Enable notifications for a characteristic on this BLE device.
     *
     * Returns a {@link Single} which holds an instance of {@link ReadWriteEvent}. If the bond fails,
     * {@link SingleEmitter#onError(Throwable)} will be called which holds an instance of {@link ReadWriteException}, which also holds an instance
     * of {@link ReadWriteEvent}, so you can get more information on what went wrong.
     */
    public Single<ReadWriteEvent> enableNotify(final BleNotify notify)
    {
        return Single.create(new SingleOnSubscribe<ReadWriteEvent>()
        {
            @Override
            public void subscribe(final SingleEmitter<ReadWriteEvent> emitter) throws Exception
            {
                if (emitter.isDisposed()) return;

                m_device.enableNotify(notify.serviceUuid, notify.charUuid, notify.m_forceReadTimeout, notify.descriptorFilter, new ReadWriteListener()
                {
                    @Override
                    public void onEvent(ReadWriteEvent e)
                    {
                        if (emitter.isDisposed()) return;

                        if (e.wasSuccess())
                            emitter.onSuccess(e);
                        else
                            emitter.onError(new ReadWriteException(e));
                    }
                });
            }
        });
    }

    /**
     * Disable notifications for a characteristic on this BLE device.
     *
     * Returns a {@link Single} which holds an instance of {@link ReadWriteEvent}. If the bond fails,
     * {@link SingleEmitter#onError(Throwable)} will be called which holds an instance of {@link ReadWriteException}, which also holds an instance
     * of {@link ReadWriteEvent}, so you can get more information on what went wrong.
     */
    public Single<ReadWriteEvent> disableNotify(final BleNotify notify)
    {
        return Single.create(new SingleOnSubscribe<ReadWriteEvent>()
        {
            @Override
            public void subscribe(final SingleEmitter<ReadWriteEvent> emitter) throws Exception
            {
                if (emitter.isDisposed()) return;

                m_device.disableNotify(notify.serviceUuid, notify.charUuid, notify.m_forceReadTimeout, new ReadWriteListener()
                {
                    @Override
                    public void onEvent(ReadWriteEvent e)
                    {
                        if (emitter.isDisposed()) return;

                        if (e.wasSuccess())
                            emitter.onSuccess(e);
                        else
                            emitter.onError(new ReadWriteException(e));
                    }
                });
            }
        });
    }

    /**
     * As you should never get an instance of this class which is <code>null</code>, use this method to see if the device is considered to be
     * <code>null</code> or not.
     */
    public boolean isNull()
    {
        return m_device == null || m_device.isNull();
    }


    @Override
    public int hashCode()
    {
        return m_device.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof RxBleDevice))
            return false;
        return equals((RxBleDevice) obj);
    }

    public boolean equals(RxBleDevice device)
    {
        return m_device.equals(device.m_device);
    }

    @Override
    public String toString()
    {
        return m_device.toString();
    }

    static RxBleDevice create(BleDevice device)
    {
        return new RxBleDevice(device);
    }

}
