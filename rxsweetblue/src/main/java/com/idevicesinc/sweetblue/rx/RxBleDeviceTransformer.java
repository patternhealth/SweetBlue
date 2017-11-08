package com.idevicesinc.sweetblue.rx;


import com.idevicesinc.sweetblue.BleDevice;
import io.reactivex.functions.Function;


/**
 * Transformation class to convert a {@link BleDevice} to an {@link RxBleDevice}.
 */
public final class RxBleDeviceTransformer implements Function<BleDevice, RxBleDevice>
{
    @Override
    public RxBleDevice apply(BleDevice bleDevice) throws Exception
    {
        return RxBleManager.getOrCreateDevice(bleDevice);
    }
}
