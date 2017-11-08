package com.idevicesinc.sweetblue.rx;


import com.idevicesinc.sweetblue.utils.Uuids;
import java.util.UUID;

/**
 * Builder-type class used when performing reads on BLE devices.
 *
 * @deprecated - This is marked as deprecated only because it will be a part of the core SweetBlue library in version 3 (the name and function will remain the same).
 */
@Deprecated
public class BleRead extends BleOp<BleRead>
{

    /**
     * "Invalid" static instance used when reading things like RSSI, or setting connection parameters
     */
    final static BleRead INVALID = new BleRead(Uuids.INVALID, Uuids.INVALID);


    public BleRead()
    {
    }

    public BleRead(UUID serviceUuid, UUID characteristicUuid)
    {
        super(serviceUuid, characteristicUuid);
    }

    public BleRead(UUID characteristicUuid)
    {
        super(characteristicUuid);
    }

    @Override
    public final boolean isValid()
    {
        return charUuid != null;
    }

    @Override
    final BleRead createDuplicate()
    {
        return getDuplicateOp();
    }

    @Override
    final BleRead createNewOp()
    {
        return new BleRead();
    }



    /**
     * Builder class to build out a list (or array) of {@link BleRead} instances.
     */
    public final static class Builder extends BleOp.Builder<Builder, BleRead>
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
            currentOp = new BleRead(serviceUuid, characteristicUuid);
        }
    }
}
