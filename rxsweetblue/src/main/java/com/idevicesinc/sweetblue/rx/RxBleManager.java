package com.idevicesinc.sweetblue.rx;


import android.content.Context;
import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.BleDeviceConfig;
import com.idevicesinc.sweetblue.BleDeviceState;
import com.idevicesinc.sweetblue.BleManager;
import com.idevicesinc.sweetblue.BleManager.DiscoveryListener.LifeCycle;
import com.idevicesinc.sweetblue.BleManager.DiscoveryListener;
import com.idevicesinc.sweetblue.BleManagerConfig;
import com.idevicesinc.sweetblue.BleManagerState;
import com.idevicesinc.sweetblue.BleNode;
import com.idevicesinc.sweetblue.BleServer;
import com.idevicesinc.sweetblue.DeviceStateListener;
import com.idevicesinc.sweetblue.ManagerStateListener;
import com.idevicesinc.sweetblue.NotificationListener;
import com.idevicesinc.sweetblue.ReadWriteListener;
import com.idevicesinc.sweetblue.ScanOptions;
import com.idevicesinc.sweetblue.annotations.Nullable;
import com.idevicesinc.sweetblue.rx.annotations.HotObservable;
import com.idevicesinc.sweetblue.rx.schedulers.SweetBlueSchedulers;
import com.idevicesinc.sweetblue.utils.Pointer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;


public final class RxBleManager
{

    public static RxBleManager get(Context context, BleManagerConfig config)
    {
        if (s_instance == null)
        {
            BleManager mgr = BleManager.get(context, config);
            s_instance = new RxBleManager(mgr);
        }
        else
            s_instance.setConfig(config);

        return s_instance;
    }

    public static RxBleManager get(Context context)
    {
        if (s_instance == null)
            s_instance = get(context, new BleManagerConfig());

        return s_instance;
    }


    public static final RxBleDeviceTransformer BLE_DEVICE_TRANSFORMER = new RxBleDeviceTransformer();

    // Map to hold instances of RxBleDevice. This is to avoid creating multiple instances of RxBleDevice for a single instance of BleDevice
    private static final Map<String, RxBleDevice> m_deviceMap = new HashMap<>();

    private static RxBleManager s_instance;

    private final BleManager m_manager;


    private Flowable<BleManager.UhOhListener.UhOhEvent> m_uhOhFlowable;
    private Flowable<BleDevice.StateListener.StateEvent> m_deviceStateFlowable;
    private Flowable<BleManager.StateListener.StateEvent> m_mgrStateFlowable;
    private Flowable<BleManager.AssertListener.AssertEvent> m_assertFlowable;
    private Flowable<BleServer.StateListener.StateEvent> m_serverStateFlowable;
    private Flowable<BleDevice.BondListener.BondEvent> m_bondFlowable;
    private Flowable<BleDevice.ReadWriteListener.ReadWriteEvent> m_readWriteFlowable;
    private Flowable<NotificationListener.NotificationEvent> m_notifyEventFlowable;
    private Flowable<BleNode.HistoricalDataLoadListener.HistoricalDataLoadEvent> m_historicalDataLoadFlowable;
    private Flowable<RxDiscoveryEvent> m_discoveryFlowable;
    private Flowable<BleServer.OutgoingListener.OutgoingEvent> m_outgoingEventFlowable;
    private Flowable<BleServer.ServiceAddListener.ServiceAddEvent> m_serviceAddEventFlowable;
    private Flowable<BleServer.AdvertisingListener.AdvertisingEvent> m_advertisingEventFlowable;



    private RxBleManager(BleManager manager)
    {
        m_manager = manager;
    }


    public final void setConfig(BleManagerConfig config)
    {
        m_manager.setConfig(config);
    }

    public final BleManager getBleManager()
    {
        return m_manager;
    }

    /**
     * Returns an {@link Observable} which kicks off a scan using the provided {@link ScanOptions} once subscribed to. The observable returns an {@link RxDiscoveryEvent}, so
     * that you can see if the device was {@link LifeCycle#DISCOVERED}, {@link LifeCycle#REDISCOVERED}, or {@link LifeCycle#UNDISCOVERED}. Be aware that the
     * {@link LifeCycle#REDISCOVERED} state can be emitted many times in a single scan. In most cases, {@link #scan_onlyNew(ScanOptions)} will suffice, as it only emits
     * when a {@link BleDevice} is {@link LifeCycle#DISCOVERED}.
     *
     * NOTE: This ignores any {@link DiscoveryListener} that is set within the {@link ScanOptions} instance passed into this method.
     */
    public final @HotObservable Flowable<RxDiscoveryEvent> scan(final ScanOptions options)
    {
        checkDiscoveryFlowable(options);
        m_manager.startScan(options);
        return m_discoveryFlowable.share();
    }

    /**
     * Returns an {@link Observable} which kicks off a scan using the provided {@link ScanOptions} once subscribed to. The observable returns a {@link BleDevice}, as this
     * method will only ever emit devices that were {@link LifeCycle#DISCOVERED}, as opposed to being {@link LifeCycle#REDISCOVERED}, or {@link LifeCycle#UNDISCOVERED}. If
     * you care about those other states, then you should use {@link #scan(ScanOptions)} instead.
     *
     * NOTE: This ignores any {@link DiscoveryListener} that is set within the {@link ScanOptions} instance passed into this method.
     */
    public final @HotObservable Flowable<RxBleDevice> scan_onlyNew(@Nullable(Nullable.Prevalence.NORMAL) ScanOptions options)
    {
        return scan(options).filter(new Predicate<RxDiscoveryEvent>()
        {
            @Override
            public boolean test(RxDiscoveryEvent rxDiscoveryEvent) throws Exception
            {
                // Filter out anything other than DISCOVERED devices (ignoring rediscovery, and undiscovery)
                return rxDiscoveryEvent.wasDiscovered();
            }
        }).map(new Function<RxDiscoveryEvent, RxBleDevice>()
        {
            @Override
            public RxBleDevice apply(RxDiscoveryEvent rxDiscoveryEvent) throws Exception
            {
                return rxDiscoveryEvent.getDevice();
            }
        });
    }

    /**
     * Rx-ified version of {@link BleManager#getDevice(BleDeviceState)}.
     *
     * Returns an instance of {@link RxBleDevice}.
     */
    public RxBleDevice getDevice(final BleDeviceState state)
    {
        return Single.create(new SingleOnSubscribe<BleDevice>()
        {
            @Override
            public void subscribe(SingleEmitter<BleDevice> emitter) throws Exception
            {
                if (emitter.isDisposed()) return;

                emitter.onSuccess(m_manager.getDevice(state));
            }
        }).subscribeOn(SweetBlueSchedulers.sweetBlueThread()).map(BLE_DEVICE_TRANSFORMER).blockingGet();
    }

    /**
     * Rx-ified version of {@link BleManager#getDevices_bonded()}.
     *
     * Returns an {@link Observable} to cycle through all the devices returned. They will all be instances of {@link RxBleDevice},
     */
    public Observable<RxBleDevice> getDevices_bonded()
    {
        return Observable.create(new ObservableOnSubscribe<BleDevice>()
        {
            @Override
            public void subscribe(ObservableEmitter<BleDevice> emitter) throws Exception
            {
                if (emitter.isDisposed()) return;

                Set<BleDevice> devices = m_manager.getDevices_bonded();

                for (BleDevice device : devices)
                {
                    if (emitter.isDisposed()) return;

                    emitter.onNext(device);
                }

                emitter.onComplete();
            }
        }).subscribeOn(SweetBlueSchedulers.sweetBlueThread()).map(BLE_DEVICE_TRANSFORMER);
    }

    /**
     * Returns a {@link Flowable} which emits {@link com.idevicesinc.sweetblue.BleManager.StateListener.StateEvent} when {@link BleManager}'s state
     * changes.
     */
    public @HotObservable Flowable<BleManager.StateListener.StateEvent> observeMgrStateEvents()
    {
        if (m_mgrStateFlowable == null)
        {
            m_mgrStateFlowable = Flowable.create(new FlowableOnSubscribe<BleManager.StateListener.StateEvent>()
            {
                @Override
                public void subscribe(final FlowableEmitter<BleManager.StateListener.StateEvent> emitter) throws Exception
                {
                    if (emitter.isCancelled()) return;

                    m_manager.setListener_State(new ManagerStateListener()
                    {
                        @Override
                        public void onEvent(BleManager.StateListener.StateEvent e)
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
                            m_manager.setListener_State((ManagerStateListener) null);
                            m_mgrStateFlowable = null;
                        }
                    });
                }
            }, BackpressureStrategy.BUFFER).share();
        }

        return m_mgrStateFlowable.share();
    }

    /**
     * Returns a {@link Flowable} which emits {@link com.idevicesinc.sweetblue.BleDevice.StateListener.StateEvent} when any {@link BleDevice}'s state
     * changes.
     */
    public @HotObservable Flowable<BleDevice.StateListener.StateEvent> observeDeviceStateEvents()
    {
        if (m_deviceStateFlowable == null)
        {
            m_deviceStateFlowable = Flowable.create(new FlowableOnSubscribe<BleDevice.StateListener.StateEvent>()
            {
                @Override
                public void subscribe(final FlowableEmitter<BleDevice.StateListener.StateEvent> emitter) throws Exception
                {
                    if (emitter.isCancelled()) return;

                    m_manager.setListener_DeviceState(new DeviceStateListener()
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
                            m_manager.setListener_DeviceState((DeviceStateListener) null);
                            m_deviceStateFlowable = null;
                        }
                    });
                }
            }, BackpressureStrategy.BUFFER).share();
        }

        return m_deviceStateFlowable.share();
    }

    /**
     * Returns a {@link Flowable} which emits {@link com.idevicesinc.sweetblue.BleManager.UhOhListener.UhOhEvent} when any {@link com.idevicesinc.sweetblue.BleManager.UhOhListener.UhOh}s
     * are posted by the library.
     */
    public @HotObservable Flowable<BleManager.UhOhListener.UhOhEvent> observeUhOhEvents()
    {
        if (m_uhOhFlowable == null)
        {
            m_uhOhFlowable = Flowable.create(new FlowableOnSubscribe<BleManager.UhOhListener.UhOhEvent>()
            {
                @Override
                public void subscribe(final FlowableEmitter<BleManager.UhOhListener.UhOhEvent> emitter) throws Exception
                {
                    if (emitter.isCancelled()) return;

                    m_manager.setListener_UhOh(new BleManager.UhOhListener()
                    {
                        @Override
                        public void onEvent(UhOhEvent e)
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
                            m_manager.setListener_UhOh(null);
                            m_uhOhFlowable = null;
                        }
                    });
                }
            }, BackpressureStrategy.BUFFER).share();
        }

        return m_uhOhFlowable.share();
    }

    public final @HotObservable Flowable<BleManager.AssertListener.AssertEvent> observeAssertEvents()
    {
        if (m_assertFlowable == null)
        {
            m_assertFlowable = Flowable.create(new FlowableOnSubscribe<BleManager.AssertListener.AssertEvent>()
            {
                @Override
                public void subscribe(final FlowableEmitter<BleManager.AssertListener.AssertEvent> emitter) throws Exception
                {
                    if (emitter.isCancelled()) return;

                    m_manager.setListener_Assert(new BleManager.AssertListener()
                    {
                        @Override
                        public void onEvent(AssertEvent e)
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
                            m_manager.setListener_Assert(null);
                            m_assertFlowable = null;
                        }
                    });
                }
            }, BackpressureStrategy.BUFFER).share();
        }

        return m_assertFlowable.share();
    }

    public final @HotObservable Flowable<BleServer.StateListener.StateEvent> observeServerStateEvents()
    {
        if (m_serverStateFlowable == null)
        {
            m_serverStateFlowable = Flowable.create(new FlowableOnSubscribe<BleServer.StateListener.StateEvent>()
            {
                @Override
                public void subscribe(final FlowableEmitter<BleServer.StateListener.StateEvent> emitter) throws Exception
                {
                    if (emitter.isCancelled()) return;

                    m_manager.setListener_ServerState(new BleServer.StateListener()
                    {
                        @Override
                        public void onEvent(StateEvent e)
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
                            m_manager.setListener_ServerState(null);
                            m_serverStateFlowable = null;
                        }
                    });
                }
            }, BackpressureStrategy.BUFFER).share();
        }

        return m_serverStateFlowable.share();
    }

    public final @HotObservable Flowable<BleDevice.BondListener.BondEvent> observeBondEvents()
    {
        if (m_bondFlowable == null)
        {
            m_bondFlowable = Flowable.create(new FlowableOnSubscribe<BleDevice.BondListener.BondEvent>()
            {
                @Override
                public void subscribe(final FlowableEmitter<BleDevice.BondListener.BondEvent> emitter) throws Exception
                {
                    if (emitter.isCancelled()) return;

                    m_manager.setListener_Bond(new BleDevice.BondListener()
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
                            m_manager.setListener_Bond(null);
                            m_bondFlowable = null;
                        }
                    });
                }
            }, BackpressureStrategy.BUFFER).share();
        }

        return m_bondFlowable.share();
    }

    public final @HotObservable Flowable<BleDevice.ReadWriteListener.ReadWriteEvent> observeReadWriteEvents()
    {
        if (m_readWriteFlowable == null)
        {
            m_readWriteFlowable = Flowable.create(new FlowableOnSubscribe<BleDevice.ReadWriteListener.ReadWriteEvent>()
            {
                @Override
                public void subscribe(final FlowableEmitter<BleDevice.ReadWriteListener.ReadWriteEvent> emitter) throws Exception
                {
                    if (emitter.isCancelled()) return;

                    m_manager.setListener_Read_Write(new ReadWriteListener()
                    {
                        @Override
                        public void onEvent(BleDevice.ReadWriteListener.ReadWriteEvent e)
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
                            m_manager.setListener_Read_Write(null);
                            m_readWriteFlowable = null;
                        }
                    });
                }
            }, BackpressureStrategy.BUFFER).share();
        }

        return m_readWriteFlowable.share();
    }

    public final @HotObservable Flowable<NotificationListener.NotificationEvent> observeNotificationEvents()
    {
        if (m_notifyEventFlowable == null)
        {
            m_notifyEventFlowable = Flowable.create(new FlowableOnSubscribe<NotificationListener.NotificationEvent>()
            {
                @Override
                public void subscribe(final FlowableEmitter<NotificationListener.NotificationEvent> emitter) throws Exception
                {
                    if (emitter.isCancelled()) return;

                    m_manager.setListener_Notification(new NotificationListener()
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
                            m_manager.setListener_Notification(null);
                            m_notifyEventFlowable = null;
                        }
                    });
                }
            }, BackpressureStrategy.BUFFER).share();
        }

        return m_notifyEventFlowable.share();
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

                    m_manager.setListener_HistoricalDataLoad(new BleNode.HistoricalDataLoadListener()
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
                            m_manager.setListener_HistoricalDataLoad(null);
                            m_historicalDataLoadFlowable = null;
                        }
                    });
                }
            }, BackpressureStrategy.BUFFER).share();
        }

        return m_historicalDataLoadFlowable.share();
    }

    public final @HotObservable Flowable<BleServer.OutgoingListener.OutgoingEvent> observeOutgoingEvents()
    {
        if (m_outgoingEventFlowable == null)
        {
            m_outgoingEventFlowable = Flowable.create(new FlowableOnSubscribe<BleServer.OutgoingListener.OutgoingEvent>()
            {
                @Override
                public void subscribe(final FlowableEmitter<BleServer.OutgoingListener.OutgoingEvent> emitter) throws Exception
                {
                    if (emitter.isCancelled()) return;

                    m_manager.setListener_Outgoing(new BleServer.OutgoingListener()
                    {
                        @Override
                        public void onEvent(OutgoingEvent e)
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
                            m_manager.setListener_Outgoing(null);
                            m_outgoingEventFlowable = null;
                        }
                    });
                }
            }, BackpressureStrategy.BUFFER).share();
        }

        return m_outgoingEventFlowable.share();
    }

    public final @HotObservable Flowable<BleServer.ServiceAddListener.ServiceAddEvent> observeServiceAddEvents()
    {
        if (m_serviceAddEventFlowable == null)
        {
            m_serviceAddEventFlowable = Flowable.create(new FlowableOnSubscribe<BleServer.ServiceAddListener.ServiceAddEvent>()
            {
                @Override
                public void subscribe(final FlowableEmitter<BleServer.ServiceAddListener.ServiceAddEvent> emitter) throws Exception
                {
                    if (emitter.isCancelled()) return;

                    m_manager.setListener_ServiceAdd(new BleServer.ServiceAddListener()
                    {
                        @Override
                        public void onEvent(ServiceAddEvent e)
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
                            m_manager.setListener_ServiceAdd(null);
                            m_serviceAddEventFlowable = null;
                        }
                    });
                }
            }, BackpressureStrategy.BUFFER).share();
        }

        return m_serviceAddEventFlowable.share();
    }

    public final @HotObservable Flowable<BleServer.AdvertisingListener.AdvertisingEvent> observeAdvertisingEvents()
    {
        if (m_advertisingEventFlowable == null)
        {
            m_advertisingEventFlowable = Flowable.create(new FlowableOnSubscribe<BleServer.AdvertisingListener.AdvertisingEvent>()
            {
                @Override
                public void subscribe(final FlowableEmitter<BleServer.AdvertisingListener.AdvertisingEvent> emitter) throws Exception
                {
                    if (emitter.isCancelled()) return;

                    m_manager.setListener_Advertising(new BleServer.AdvertisingListener()
                    {
                        @Override
                        public void onEvent(AdvertisingEvent e)
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
                            m_manager.setListener_Advertising(null);
                            m_advertisingEventFlowable = null;
                        }
                    });
                }
            }, BackpressureStrategy.BUFFER).share();
        }

        return m_advertisingEventFlowable.share();
    }

    /**
     * Rx-ified version of {@link BleManager#newDevice(String)}.
     *
     * NOTE: The device creation is performed on the thread which SweetBlue is using, and this method is blocking.
     */
    public RxBleDevice newDevice(final String macAddress)
    {
        return newDevice(macAddress, null, null);
    }

    /**
     * Rx-ified version of {@link BleManager#newDevice(String, String)}.
     *
     * NOTE: The device creation is performed on the thread which SweetBlue is using, and this method is blocking.
     */
    public RxBleDevice newDevice(final String macAddress, final String name)
    {
        return newDevice(macAddress, name, null);
    }

    /**
     * Rx-ified version of {@link BleManager#newDevice(String, BleDeviceConfig)}.
     *
     * NOTE: The device creation is performed on the thread which SweetBlue is using, and this method is blocking.
     */
    public RxBleDevice newDevice(final String macAddress, final BleManagerConfig config)
    {
        return newDevice(macAddress, null, config);
    }

    /**
     * Rx-ified version of {@link BleManager#newDevice(String, String, BleDeviceConfig)}.
     *
     * NOTE: The device creation is performed on the thread which SweetBlue is using, and this method is blocking.
     */
    public RxBleDevice newDevice(final String macAddress, final String name, final BleDeviceConfig config)
    {
        return Single.create(new SingleOnSubscribe<BleDevice>()
        {
            @Override
            public void subscribe(SingleEmitter<BleDevice> emitter) throws Exception
            {
                if (emitter.isDisposed()) return;

                emitter.onSuccess(m_manager.newDevice(macAddress, name, config));
            }
        }).map(BLE_DEVICE_TRANSFORMER).subscribeOn(SweetBlueSchedulers.sweetBlueThread()).blockingGet();
    }

    /**
     * Disconnects all devices, shuts down the BleManager, and it's backing thread, and unregisters any receivers that may be in use.
     * This also clears out the {@link BleManager}, and {@link RxBleManager} static instances. This is meant to be called upon application exit. However, to use it again,
     * just call {@link BleManager#get(Context)}, or {@link BleManager#get(Context, BleManagerConfig)} again.
     */
    public final void shutdown()
    {
        m_manager.shutdown();
        m_deviceMap.clear();
        s_instance = null;
    }



    private void checkDiscoveryFlowable(ScanOptions scanoptions)
    {
        final Pointer<ScanOptions> options = new Pointer<>(scanoptions == null ? new ScanOptions() : scanoptions);
        if (m_discoveryFlowable == null)
        {
            m_discoveryFlowable = Flowable.create(new FlowableOnSubscribe<DiscoveryListener.DiscoveryEvent>()
            {
                private Disposable stateDisposable;

                @Override
                public void subscribe(final FlowableEmitter<DiscoveryListener.DiscoveryEvent> emitter) throws Exception
                {
                    if (emitter.isCancelled()) return;

                    m_manager.setListener_Discovery(new DiscoveryListener()
                    {
                        @Override
                        public void onEvent(DiscoveryEvent e)
                        {
                            if (emitter.isCancelled()) return;

                            emitter.onNext(e);
                        }
                    });

                    if (!options.value.isContinuous())
                    {
                        stateDisposable = observeMgrStateEvents().subscribe(new Consumer<BleManager.StateListener.StateEvent>()
                        {
                            @Override
                            public void accept(BleManager.StateListener.StateEvent stateEvent) throws Exception
                            {
                                if (stateEvent.didExit(BleManagerState.SCANNING) && !m_manager.isScanning())
                                {
                                    emitter.onComplete();
                                }
                            }
                        });
                    }

                    emitter.setCancellable(new Cancellable()
                    {
                        @Override
                        public void cancel() throws Exception
                        {
                            m_manager.setListener_Discovery(null);
                            m_discoveryFlowable = null;
                            // Clean up the state disposable, if we have one, and it's not already disposed
                            if (stateDisposable != null && !stateDisposable.isDisposed())
                                stateDisposable.dispose();

                            // Stop all scanning
                            m_manager.stopAllScanning();
                        }
                    });
                }
            }, BackpressureStrategy.BUFFER).map(new Function<DiscoveryListener.DiscoveryEvent, RxDiscoveryEvent>()
            {
                @Override
                public RxDiscoveryEvent apply(DiscoveryListener.DiscoveryEvent discoveryEvent) throws Exception
                {
                    return new RxDiscoveryEvent(discoveryEvent);
                }
            }).share();
        }
    }


    static RxBleDevice getOrCreateDevice(@Nullable(Nullable.Prevalence.NEVER) BleDevice device)
    {
        RxBleDevice rxDevice = m_deviceMap.get(device.getMacAddress());
        if (rxDevice == null)
        {
            rxDevice = RxBleDevice.create(device);
            m_deviceMap.put(device.getMacAddress(), rxDevice);
        }
        return rxDevice;
    }

}
