package com.frank.babymonitor.babyipcamera;

import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.frank.babymonitor.R;

/**
 * Parent Device Activity
 * Receive the image from Baby device
 */

public class ParentDeviceActivity extends AppCompatActivity {

    private final String TAG = "ParentDeviceActivity";

    private NsdManager nsdManager;
    private NsdManager discoveryNsdManager;
    private NsdManager.DiscoveryListener discoveryListener;
    private final String SERVICE_TYPE="_monitor._tcp.";
    private final String SERVICE_NAME = "BabyMon";

    private Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parent_search_devices);
        nsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);

        searchButton = (Button) findViewById(R.id.search_baby_device);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searching();
            }
        });
    }

    private void searching(){
        setContentView(R.layout.parent_select_devices);
        startServiceSearching();
    }

    private void startServiceSearching(){
        discoveryNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);

        final ListView serviceList = (ListView) findViewById(R.id.found_device_list);
        final ArrayAdapter<ServiceInformation> servicesAdapter = new ArrayAdapter<ServiceInformation>(this,
                R.layout.found_device_list);
        serviceList.setAdapter(servicesAdapter);
        serviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final ServiceInformation information = (ServiceInformation) parent.getItemAtPosition(position);
                connectToMonitor(information.getIP(),information.getPort(),information.getName());
            }
        });

        discoveryListener = new NsdManager.DiscoveryListener(){

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.d(TAG, "onStartDiscoveryFailed");
                discoveryNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.d(TAG, "onStopDiscoveryFailed");
                discoveryNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onDiscoveryStarted(String serviceType) {
                Log.d(TAG, "onDiscoveryStarted");
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.d(TAG, "onDiscoveryStopped");
            }

            @Override
            public void onServiceFound(NsdServiceInfo serviceInfo) {
                Log.d(TAG, "onServiceFound");
                if(serviceInfo.getServiceType().equals(SERVICE_TYPE) && serviceInfo.getServiceName().contains(SERVICE_NAME)){
                    NsdManager.ResolveListener resolver = new NsdManager.ResolveListener(){

                        @Override
                        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {

                        }

                        @Override
                        public void onServiceResolved(final NsdServiceInfo serviceInfo) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    servicesAdapter.add(new ServiceInformation(serviceInfo));
                                }
                            });
                        }
                    };

                    nsdManager.resolveService(serviceInfo,resolver);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo serviceInfo) {
                Log.d(TAG, "onServiceLost");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        servicesAdapter.clear();
                    }
                });
            }
        };

        discoveryNsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);

    }

    @Override
    protected void onDestroy() {
        if(discoveryListener != null){
            nsdManager.stopServiceDiscovery(discoveryListener);
            discoveryNsdManager.stopServiceDiscovery(discoveryListener);
            discoveryListener=null;
        }
        super.onDestroy();
    }

    private void connectToMonitor(String ip, int port, String name){
        Log.d(TAG, "connect to monitor is called");
        Intent i = new Intent(getApplicationContext(), ParentViewActivity.class); // connect to parentView
        Bundle b = new Bundle();
        b.putString("ip", ip);
        b.putInt("port", port);
        b.putString("name", name);
        i.putExtras(b);
        startActivity(i);
    }
}

class ServiceInformation{

    private NsdServiceInfo info;

    public ServiceInformation(NsdServiceInfo info) {this.info = info;}

    public String getIP(){return info.getHost().getHostAddress();}

    public int getPort(){return info.getPort();}

    public String getName(){
        String serviceName = info.getServiceName();
        serviceName = serviceName.replace("\\\\032", " ");
        serviceName = serviceName.replace("\\032", " ");
        return serviceName;
    }

    @Override
    public String toString() {
        return getName();
    }
}
