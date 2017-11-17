package com.idevicesinc.sweetblue;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.idevicesinc.sweetblue.rx.BleRead;
import com.idevicesinc.sweetblue.rx.RxBleDevice;
import com.idevicesinc.sweetblue.rx.RxBleManager;
import com.idevicesinc.sweetblue.utils.BluetoothEnabler;
import com.idevicesinc.sweetblue.utils.DebugLogger;
import com.idevicesinc.sweetblue.utils.Interval;
import com.idevicesinc.sweetblue.utils.Utils_String;
import com.idevicesinc.sweetblue.utils.Uuids;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.idevicesinc.sweetblue.tester.R;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;


public class RxMainActivity extends Activity
{

    private final static int STATE_CHANGE_MIN_TIME = 50;

    RxBleManager mgr;
    private ListView mListView;
    private Button mStartScan;
    private Button mStopScan;
    private ScanAdaptor mAdaptor;
    private ArrayList<RxBleDevice> mDevices;
    private DebugLogger mLogger;
    private long mLastStateChange;
    private Disposable scanDisposable;
    private Disposable mgrStateDisposable;
    private Disposable deviceStateDisposable;
    private Disposable uhOhDisposable;
    private CompositeDisposable mDisposables;


    private final static UUID tempUuid = UUID.fromString("47495078-0002-491E-B9A4-F85CD01C3698");
//    private final static UUID tempUuid = UUID.fromString("1234666b-1000-2000-8000-001199334455");

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(com.idevicesinc.sweetblue.tester.R.layout.activity_main);

        mDisposables = new CompositeDisposable();

        mListView = findViewById(com.idevicesinc.sweetblue.tester.R.id.listView);
        mDevices = new ArrayList<>(0);
        mAdaptor = new ScanAdaptor(this, mDevices);
        mListView.setAdapter(mAdaptor);
        mListView.setOnItemClickListener((parent, view, position, id) ->
        {
            final RxBleDevice device = mDevices.get(position);
            device.connect().subscribe(() -> device.read(new BleRead(Uuids.BATTERY_LEVEL)), t -> {});
        });

        registerForContextMenu(mListView);

        mStartScan = findViewById(R.id.startScan);
        mStartScan.setOnClickListener(v -> {
            scanDisposable = mgr.scan_onlyNew(new ScanOptions().scanPeriodically(Interval.TEN_SECS, Interval.ONE_SEC)).
                    observeOn(AndroidSchedulers.mainThread()).
                    subscribe(rxBleDevice -> {
                        if (!mDevices.contains(rxBleDevice))
                        {
                            mDevices.add(rxBleDevice);
                            mAdaptor.notifyDataSetChanged();
                        }
                    });
            mDisposables.add(scanDisposable);
        });

        mStopScan = findViewById(R.id.stopScan);
        mStopScan.setOnClickListener(v -> {
            if (scanDisposable != null)
                scanDisposable.dispose();
        });

        mLogger = new DebugLogger(250);

        BleManagerConfig config = new BleManagerConfig();
        config.loggingEnabled = true;
        config.logger = mLogger;
        config.bondRetryFilter = new BondRetryFilter.DefaultBondRetryFilter(5);
        config.scanApi = BleScanApi.AUTO;
        config.runOnMainThread = false;
        config.forceBondDialog = true;
        config.reconnectFilter = new BleNodeConfig.DefaultReconnectFilter(Interval.ONE_SEC, Interval.secs(3.0), Interval.FIVE_SECS, Interval.secs(45));
        config.uhOhCallbackThrottle = Interval.secs(60.0);

        config.defaultScanFilter = e -> BleManagerConfig.ScanFilter.Please.acknowledgeIf(e.name_normalized().contains("wall"));

        mgr = RxBleManager.get(this, config);

//        mDisposables.add(uhOhDisposable = mgr.observeUhOhEvents().subscribe(uhOhEvent ->
//                Log.e("UhOhs", "Got " + uhOhEvent.uhOh() + " with remedy " + uhOhEvent.remedy()))
//        );
//
//        mDisposables.add(mgrStateDisposable = mgr.mgrStatePublisher().subscribe(stateEvent -> {
//            boolean scanning = mgr.getBleManager().isScanning();
//            mStartScan.setEnabled(!scanning);
//        }));
//
//        mDisposables.add(deviceStateDisposable = mgr.observeDeviceStateEvents().subscribe(stateEvent -> {
//            if (System.currentTimeMillis() - mLastStateChange > STATE_CHANGE_MIN_TIME)
//            {
//                mLastStateChange = System.currentTimeMillis();
//                mAdaptor.notifyDataSetChanged();
//            }
//        }));

        mDisposables.add(mgr.observeMgrStateEvents().subscribe((e) -> Log.e("State*", "State event: " + e.toString())));

        mStartScan.setEnabled(false);

        BluetoothEnabler.start(this, new BluetoothEnabler.DefaultBluetoothEnablerFilter()
                {
                    @Override
                    public Please onEvent(BluetoothEnablerEvent e)
                    {
                        if (e.isDone())
                        {
                            mStartScan.setEnabled(true);
                        }
                        return super.onEvent(e);
                    }
                }
        );

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.print_pretty_log:
                Log.e("Logs!", Utils_String.prettyFormatLogList(mLogger.getLogList()));
                return true;
            case R.id.nukeBle:
                mgr.getBleManager().nukeBle();
                return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(com.idevicesinc.sweetblue.tester.R.menu.main, menu);
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == com.idevicesinc.sweetblue.tester.R.id.listView)
        {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            boolean isBonded = mDevices.get(info.position).getBleDevice().is(BleDeviceState.BONDED);
            boolean connected = mDevices.get(info.position).getBleDevice().is(BleDeviceState.CONNECTED);

            menu.add(0, 0, 0, "Remove Bond");

            if (!isBonded)
            {
                menu.add(1, 1, 0, "Bond");
            }
            if (connected)
            {
                menu.add(2, 2, 0, "Disconnect");
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (item.getItemId() == 0)
        {
            mDevices.get(info.position).unbond();
            return true;
        }
        else if (item.getItemId() == 1)
        {
            mDevices.get(info.position).getBleDevice().bond(e -> Log.e("Bonding Event", e.toString()));
            return true;
        }
        else if (item.getItemId() == 2)
        {
            mDevices.get(info.position).getBleDevice().disconnect();
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private class ScanAdaptor extends ArrayAdapter<RxBleDevice>
    {

        private List<RxBleDevice> mDevices;


        public ScanAdaptor(Context context, List<RxBleDevice> objects)
        {
            super(context, R.layout.scan_listitem_layout, objects);
            mDevices = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            ViewHolder v;
            final RxBleDevice device = mDevices.get(position);
            if (convertView == null)
            {
                convertView = View.inflate(getContext(), R.layout.scan_listitem_layout, null);
                v = new ViewHolder();
                v.name = convertView.findViewById(R.id.name);
                v.rssi = convertView.findViewById(R.id.rssi);
                convertView.setTag(v);
            }
            else
            {
                v = (ViewHolder) convertView.getTag();
            }
            v.name.setText(Utils_String.concatStrings(device.toString(), "\nNative Name: ", device.getBleDevice().getName_native()));
            //v.rssi.setText(String.valueOf(mDevices.get(position).getRssi()));
            return convertView;
        }

    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mDisposables.dispose();
        mgr.shutdown();
    }

    private static class ViewHolder
    {
        private TextView name;
        private TextView rssi;
    }

}
