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
import com.idevicesinc.sweetblue.ManagerStateListener;
import com.idevicesinc.sweetblue.ScanOptions;
import com.idevicesinc.sweetblue.annotations.Nullable;
import com.idevicesinc.sweetblue.rx.annotations.SingleSubscriber;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;


public class RxBleManager
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



    private RxBleManager(BleManager manager)
    {
        m_manager = manager;
    }


    public final void setConfig(BleManagerConfig config)
    {
        m_manager.setConfig(config);
    }

    /**
     * Returns an {@link Observable} which kicks off a scan using the provided {@link ScanOptions} once subscribed to. The observable returns an {@link RxDiscoveryEvent}, so
     * that you can see if the device was {@link LifeCycle#DISCOVERED}, {@link LifeCycle#REDISCOVERED}, or {@link LifeCycle#UNDISCOVERED}. Be aware that the
     * {@link LifeCycle#REDISCOVERED} state can be emitted many times in a single scan. In most cases, {@link #scan_onlyNew(ScanOptions)} will suffice, as it only emits
     * when a {@link BleDevice} is {@link LifeCycle#DISCOVERED}.
     *
     * NOTE: You should make sure to only have ONE instance of this observable around, as you can only have 1 scan running at a time.
     * NOTE2: This will override any {@link DiscoveryListener} that is set within the {@link ScanOptions} instance passed into this method.
     */
    @SingleSubscriber(methods = {"RxBleManager.scan()", "RxBleManager.scan_onlyNew()"})
    public final Observable<RxDiscoveryEvent> scan(final ScanOptions options)
    {
        return Observable.create(new ObservableOnSubscribe<BleManager.DiscoveryListener.DiscoveryEvent>()
        {
            @Override
            public void subscribe(final ObservableEmitter<BleManager.DiscoveryListener.DiscoveryEvent> emitter) throws Exception
            {
                if (emitter.isDisposed())
                    return;

                options.withDiscoveryListener(new BleManager.DiscoveryListener()
                {
                    @Override
                    public void onEvent(DiscoveryEvent e)
                    {
                        if (!emitter.isDisposed())
                        {
                            emitter.onNext(e);
                        }
                    }
                });

                if (!options.isContinuous())
                {
                    m_manager.setListener_State(new ManagerStateListener()
                    {
                        @Override
                        public void onEvent(BleManager.StateListener.StateEvent e)
                        {
                            if (e.didExit(BleManagerState.SCANNING) && !m_manager.isScanning())
                            {
                                emitter.onComplete();
                            }
                        }
                    });
                }

                m_manager.startScan(options);
            }
        }).doOnDispose(new Action()
        {
            @Override
            public void run() throws Exception
            {
                m_manager.stopAllScanning();
            }
        }).map(new Function<BleManager.DiscoveryListener.DiscoveryEvent, RxDiscoveryEvent>()
        {
            @Override
            public RxDiscoveryEvent apply(BleManager.DiscoveryListener.DiscoveryEvent discoveryEvent) throws Exception
            {
                return new RxDiscoveryEvent(discoveryEvent);
            }
        });
    }

    /**
     * Returns an {@link Observable} which kicks off a scan using the provided {@link ScanOptions} once subscribed to. The observable returns a {@link BleDevice}, as this
     * method will only ever emit devices that were {@link LifeCycle#DISCOVERED}, as opposed to being {@link LifeCycle#REDISCOVERED}, or {@link LifeCycle#UNDISCOVERED}. If
     * you care about those other states, then you should use {@link #scan(ScanOptions)} instead.
     *
     * NOTE: You should make sure to only have ONE instance of this observable around, as you can only have 1 scan running at a time.
     * NOTE2: This will override any {@link DiscoveryListener} that is set within the {@link ScanOptions} instance passed into this method.
     */
    @SingleSubscriber(methods = {"RxBleManager.scan()", "RxBleManager.scan_onlyNew()"})
    public final Observable<RxBleDevice> scan_onlyNew(final ScanOptions options)
    {
        return Observable.create(new ObservableOnSubscribe<BleDevice>()
        {
            @Override
            public void subscribe(final ObservableEmitter<BleDevice> emitter) throws Exception
            {
                if (emitter.isDisposed())
                    return;

                options.withDiscoveryListener(new BleManager.DiscoveryListener()
                {
                    @Override
                    public void onEvent(DiscoveryEvent e)
                    {
                        if (emitter.isDisposed())
                            return;

                        if (e.was(LifeCycle.DISCOVERED))
                        {
                            emitter.onNext(e.device());
                        }
                    }
                });

                if (!options.isContinuous())
                {
                    m_manager.setListener_State(new ManagerStateListener()
                    {
                        @Override
                        public void onEvent(BleManager.StateListener.StateEvent e)
                        {
                            if (e.didExit(BleManagerState.SCANNING) && !m_manager.isScanning())
                            {
                                emitter.onComplete();
                            }
                        }
                    });
                }

                m_manager.startScan(options);
            }
        }).doOnDispose(new Action()
        {
            @Override
            public void run() throws Exception
            {
                m_manager.stopAllScanning();
            }
        }).map(BLE_DEVICE_TRANSFORMER);
    }

    /**
     * Rx-ified version of {@link BleManager#getDevice(BleDeviceState)}.
     *
     * Returns a {@link Single} which contains an instance of {@link RxBleDevice}.
     */
    public Single<RxBleDevice> getDevice(final BleDeviceState state)
    {
        return Single.create(new SingleOnSubscribe<BleDevice>()
        {
            @Override
            public void subscribe(SingleEmitter<BleDevice> emitter) throws Exception
            {
                if (emitter.isDisposed()) return;

                emitter.onSuccess(m_manager.getDevice(state));
            }
        }).map(BLE_DEVICE_TRANSFORMER);
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
        }).map(BLE_DEVICE_TRANSFORMER);
    }

    /**
     * Rx-ified version of {@link BleManager#newDevice(String)}.
     */
    public Single<RxBleDevice> newDevice(final String macAddress)
    {
        return newDevice(macAddress, null, null);
    }

    /**
     * Rx-ified version of {@link BleManager#newDevice(String, String)}.
     */
    public Single<RxBleDevice> newDevice(final String macAddress, final String name)
    {
        return newDevice(macAddress, name, null);
    }

    /**
     * Rx-ified version of {@link BleManager#newDevice(String, BleDeviceConfig)}.
     */
    public Single<RxBleDevice> newDevice(final String macAddress, final BleManagerConfig config)
    {
        return newDevice(macAddress, null, config);
    }

    /**
     * Rx-ified version of {@link BleManager#newDevice(String, String, BleDeviceConfig)}.
     */
    public Single<RxBleDevice> newDevice(final String macAddress, final String name, final BleDeviceConfig config)
    {
        return Single.create(new SingleOnSubscribe<BleDevice>()
        {
            @Override
            public void subscribe(SingleEmitter<BleDevice> emitter) throws Exception
            {
                if (emitter.isDisposed()) return;

                emitter.onSuccess(m_manager.newDevice(macAddress, name, config));
            }
        }).map(BLE_DEVICE_TRANSFORMER);
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
