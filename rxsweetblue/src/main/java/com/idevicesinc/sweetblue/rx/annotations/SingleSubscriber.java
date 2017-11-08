package com.idevicesinc.sweetblue.rx.annotations;

import com.idevicesinc.sweetblue.ScanOptions;
import com.idevicesinc.sweetblue.rx.RxBleManager;
import io.reactivex.Observable;


/**
 * Annotation that dictates that an {@link Observable} should only ever have a single subscription.
 * For example, {@link RxBleManager#scan(ScanOptions)}, or {@link RxBleManager#scan_onlyNew(ScanOptions)}.
 *
 * The methods String array contains the methods that should be considered mutually exclusive.
 */
public @interface SingleSubscriber
{

    String[] methods();

}
