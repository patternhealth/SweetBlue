package com.idevicesinc.sweetblue.rx;


import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.BleDevice.BondListener;
import com.idevicesinc.sweetblue.BleDevice.ReadWriteListener;
import com.idevicesinc.sweetblue.BleDevice.ReadWriteListener.ReadWriteEvent;
import com.idevicesinc.sweetblue.BleDeviceState;
import com.idevicesinc.sweetblue.BleNode;
import com.idevicesinc.sweetblue.DeviceStateListener;
import com.idevicesinc.sweetblue.NotificationListener;
import com.idevicesinc.sweetblue.rx.annotations.HotObservable;
import com.idevicesinc.sweetblue.rx.exception.BondException;
import com.idevicesinc.sweetblue.rx.exception.ConnectException;
import com.idevicesinc.sweetblue.BleNode.ConnectionFailListener.ConnectionFailEvent;
import com.idevicesinc.sweetblue.rx.exception.ReadWriteException;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Function;


public class RxBleDevice
{

    private final BleDevice m_device;

    private Flowable<BleDevice.StateListener.StateEvent> m_stateFlowable;
    private Flowable<NotificationListener.NotificationEvent> m_notifyFlowable;
    private Flowable<BondListener.BondEvent> m_bondFlowable;
    private Flowable<ReadWriteEvent> m_readWriteFlowable;
    private Flowable<BleNode.HistoricalDataLoadListener.HistoricalDataLoadEvent> m_historicalDataLoadFlowable;


    private RxBleDevice(BleDevice device)
    {
        m_device = device;
    }


    public final @HotObservable Flowable<BleDevice.StateListener.StateEvent> observeStateEvents()
    {
        if (m_stateFlowable == null)
        {
            m_stateFlowable = Flowable.create(new FlowableOnSubscribe<BleDevice.StateListener.StateEvent>()
            {
                @Override
                public void subscribe(final FlowableEmitter<BleDevice.StateListener.StateEvent> emitter) throws Exception
                {
                    if (emitter.isCancelled()) return;

                    m_device.setListener_State(new DeviceStateListener()
                    {
                        @Override
                        public void onEvent(BleDevice.StateListener.StateEvent e)
                        {
                            if (emitter.isCancelled()) return;

                            emitter.onNext(e);
                        }
                    });

                    emitter.setCancellable(new Cancellable()
                    {
                        @Override
                        public void cancel() throws Exception
                        {
                            m_device.setListener_State((DeviceStateListener) null);
                            m_stateFlowable = null;
                        }
                    });
                }
            }, BackpressureStrategy.BUFFER).share();
        }

        return m_stateFlowable.share();
    }

    public final @HotObservable Flowable<NotificationListener.NotificationEvent> observeNotifyEvents()
    {
        if (m_notifyFlowable == null)
        {
            m_notifyFlowable = Flowable.create(new FlowableOnSubscribe<NotificationListener.NotificationEvent>()
            {
                @Override
                public void subscribe(final FlowableEmitter<NotificationListener.NotificationEvent> emitter) throws Exception
                {
                    if (emitter.isCancelled()) return;

                    m_device.setListener_Notification(new NotificationListener()
                    {
                        @Override
                        public void onEvent(NotificationEvent e)
                        {
                            if (emitter.isCancelled()) return;

                            emitter.onNext(e);
                        }
                    });

                    emitter.setCancellable(new Cancellable()
                    {
                        @Override
                        public void cancel() throws Exception
                        {
                            m_device.setListener_Notification(null);
                            m_notifyFlowable = null;
                        }
                    });
                }
            }, BackpressureStrategy.BUFFER).share();
        }

        return m_notifyFlowable.share();
    }

    public final @HotObservable Flowable<BondListener.BondEvent> observeBondEvents()
    {
        if (m_bondFlowable == null)
        {
            m_bondFlowable = Flowable.create(new FlowableOnSubscribe<BondListener.BondEvent>()
            {
                @Override
                public void subscribe(final FlowableEmitter<BondListener.BondEvent> emitter) throws Exception
                {
                    if (emitter.isCancelled()) return;

                    m_device.setListener_Bond(new BondListener()
                    {
                        @Override
                        public void onEvent(BondEvent e)
                        {
                            if (emitter.isCancelled()) return;

                            emitter.onNext(e);
                        }
                    });

                    emitter.setCancellable(new Cancellable()
                    {
                        @Override
                        public void cancel() throws Exception
                        {
                            m_device.setListener_Bond(null);
                            m_bondFlowable = null;
                        }
                    });
                }
            }, BackpressureStrategy.BUFFER).share();
        }

        return m_bondFlowable.share();
    }

    public final @HotObservable Flowable<ReadWriteEvent> observeReadWriteEvents()
    {
        if (m_readWriteFlowable == null)
        {
            m_readWriteFlowable = Flowable.create(new FlowableOnSubscribe<ReadWriteEvent>()
            {
                @Override
                public void subscribe(final FlowableEmitter<ReadWriteEvent> emitter) throws Exception
                {
                    if (emitter.isCancelled()) return;

                    m_device.setListener_ReadWrite(new ReadWriteListener()
                    {
                        @Override
                        public void onEvent(ReadWriteEvent e)
                        {
                            if (emitter.isCancelled()) return;

                            emitter.onNext(e);
                        }
                    });

                    emitter.setCancellable(new Cancellable()
                    {
                        @Override
                        public void cancel() throws Exception
                        {
                            m_device.setListener_ReadWrite((ReadWriteListener) null);
                            m_readWriteFlowable = null;
                        }
                    });
                }
            }, BackpressureStrategy.BUFFER).share();
        }

        return m_readWriteFlowable.share();
    }

    public final @HotObservable Flowable<BleNode.HistoricalDataLoadListener.HistoricalDataLoadEvent> observeHistoricalDataLoadEvents()
    {
        if (m_historicalDataLoadFlowable == null)
        {
            m_historicalDataLoadFlowable = Flowable.create(new FlowableOnSubscribe<BleNode.HistoricalDataLoadListener.HistoricalDataLoadEvent>()
            {
                @Override
                public void subscribe(final FlowableEmitter<BleNode.HistoricalDataLoadListener.HistoricalDataLoadEvent> emitter) throws Exception
                {
                    if (emitter.isCancelled()) return;

                    m_device.setListener_HistoricalDataLoad(new BleNode.HistoricalDataLoadListener()
                    {
                        @Override
                        public void onEvent(HistoricalDataLoadEvent e)
                        {
                            if (emitter.isCancelled()) return;

                            emitter.onNext(e);
                        }
                    });

                    emitter.setCancellable(new Cancellable()
                    {
                        @Override
                        public void cancel() throws Exception
                        {
                            m_device.setListener_HistoricalDataLoad(null);
                            m_historicalDataLoadFlowable = null;
                        }
                    });
                }
            }, BackpressureStrategy.BUFFER).share();
        }

        return m_historicalDataLoadFlowable.share();
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
    public Single<RxBondEvent> bond()
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
        }).map(new Function<BondListener.BondEvent, RxBondEvent>()
        {
            @Override
            public RxBondEvent apply(BondListener.BondEvent bondEvent) throws Exception
            {
                return new RxBondEvent(bondEvent);
            }
        });
    }

    /**
     * Forwards {@link BleDevice#unbond()}.
     */
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
    public Single<RxReadWriteEvent> read(final BleRead read)
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
        }).map(new Function<ReadWriteEvent, RxReadWriteEvent>()
        {
            @Override
            public RxReadWriteEvent apply(ReadWriteEvent event) throws Exception
            {
                return new RxReadWriteEvent(event);
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
    public Single<RxReadWriteEvent> write(final BleWrite write)
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
        }).map(new Function<ReadWriteEvent, RxReadWriteEvent>()
        {
            @Override
            public RxReadWriteEvent apply(ReadWriteEvent event) throws Exception
            {
                return new RxReadWriteEvent(event);
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
    public Single<RxReadWriteEvent> enableNotify(final BleNotify notify)
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
        }).map(new Function<ReadWriteEvent, RxReadWriteEvent>()
        {
            @Override
            public RxReadWriteEvent apply(ReadWriteEvent event) throws Exception
            {
                return new RxReadWriteEvent(event);
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
    public Single<RxReadWriteEvent> disableNotify(final BleNotify notify)
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
        }).map(new Function<ReadWriteEvent, RxReadWriteEvent>()
        {
            @Override
            public RxReadWriteEvent apply(ReadWriteEvent event) throws Exception
            {
                return new RxReadWriteEvent(event);
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
