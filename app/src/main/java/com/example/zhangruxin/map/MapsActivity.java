package com.example.zhangruxin.map;

import android.Manifest;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.Manifest;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import static java.lang.Math.PI;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private String provider;
    public double x;
    public double y;
    //    private String addrs = "";
//    private String address = null;
    private String city;

    //    private LatLng currentPt;
    private EditText editText;
    public SharedPreferences settings;
    public int count = 0;
    public TextView textView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
       // ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
       // onRequestPermissionsResult (1, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, new int[]{1});


        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        editText = (EditText) findViewById(R.id.input);

        //获取定位服务
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //获取当前可用的位置控制器
        List<String> list = locationManager.getProviders(true);

        if (list.contains(LocationManager.GPS_PROVIDER)) {
            //是否为GPS位置控制器
            provider = LocationManager.GPS_PROVIDER;
        } else if (list.contains(LocationManager.NETWORK_PROVIDER)) {
            //是否为网络位置控制器
            provider = LocationManager.NETWORK_PROVIDER;

        } else {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            onCreate(savedInstanceState);
            //onRequestPermissionsResult (1, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, new int[]{1});


//            Toast.makeText(this, "please check the GPS",
//                    Toast.LENGTH_LONG).show();
//            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);


        while (location == null) {
            locationManager.requestLocationUpdates("gps", 60000, 1, locationListener);
        }

        x = location.getLatitude();
        y = location.getLongitude();
        //initListener();

        textView = (TextView) findViewById(R.id.area);
        final Button button = (Button) findViewById(R.id.addpiont);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //System.out.println("0000000000");
                creatpolygon();
                button.setText("End Polygon");

            }
        });


    }



    public void creatpolygon() {
        double area = 0;
        if (count < 3) {
            textView.setText("Cannot creat a polygon!");
        } else {
            double point[][] = getpoint();
//            System.out.println("00000");

            for (int i = 1; i <= count - 2; i++) {
                double a = getdis(point[0][1], point[i][1], point[0][0], point[i][0]);
                double b = getdis(point[0][1], point[i + 1][1], point[0][0], point[i + 1][0]);
                double c = getdis(point[i + 1][1], point[i][1], point[i + 1][0], point[i][0]);
//                System.out.println(a);
//                System.out.println(b);
//                System.out.println(c);
                double curarea = getTRianglearea(a, b, c);
                area = area + curarea;
//                System.out.println(area);
//                System.out.println("==============");

            }
            if (area > 10000000) {
                textView.setText("Area: " + area / 1000000 + "km2");
            } else {
                textView.setText("Area: " + area + "m2");
            }
            double[] cen = new double[2];
            cen = getcentroid(point);
//            double r = getarea(point);

            LatLng center = new LatLng(cen[0], cen[1]);
            mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)).position(center).title("center"));
            startPolygon(point);

        }


    }

    private void startPolygon(double point[][]) {

        mMap.setMinZoomPreference(10);


        String unit = "m<sup><small>2</small></sup>";

        ArrayList<Double> lats = new ArrayList<>();

        ArrayList<Double> longs = new ArrayList<>();

        List<LatLng> latLngs = new ArrayList<>();

        LatLng centroid;

        for (int i = 0; i < count; i++) {

            double lat = point[i][0];
            double lng = point[i][1];

            latLngs.add(new LatLng(lat, lng));

            lats.add(lat);

            longs.add(lng);

        }

        PolygonOptions polygonOptions = new PolygonOptions();


        polygonOptions.addAll(latLngs);

        polygonOptions.fillColor(0x00ff00);
        Polygon polygon = mMap.addPolygon(polygonOptions);


    }

    public double getarea(double point[][]) {
        double sum = 0;
        double term1 = 0;
        double term2 = 0;
        double term = 0;
        //[][0]->x
        //[][1]->y
        for (int i = 1; i < count; i++) {
            System.out.println("lllll");
            System.out.println(point[i - 1][0]);
            System.out.println(point[i][1]);
            term1 = point[i - 1][0] * point[i][1];//x1*y2
            term2 = point[i - 1][1] * point[i][0];//x2*y1
            term = term1 - term2;
            sum = sum + term;

        }
        term1 = point[count - 1][0] * point[0][1];//xn*y1
        term2 = point[0][0] * point[count - 1][1];//x1*yn
        term = term1 - term2;
        sum = sum + term;
        double area = sum / 2;
//        System.out.println("fffffffff");
//        System.out.println(area/1000000);
        return area;

    }

    public double[][] getpoint() {


        SharedPreferences sharedPreferences = getSharedPreferences("point", MODE_PRIVATE);

        double[][] point = new double[count][2];
        for (int i = 1; i <= count; i++) {
            String po = sharedPreferences.getString("" + i, "null");

            point[i - 1] = valueSplit(po);


        }
        return point;
    }


    //This function changes the latLng from Sting to double
    public double[] valueSplit(String po) {
        String a[] = po.split("\\(");
        String b[] = a[1].split("\\)");
        String c[] = b[0].split(",");

//        System.out.println("00000");
//        System.out.println(c[0]+c[1]);
        double[] d = new double[2];
        d[0] = Double.valueOf(c[0].toString());
//        System.out.println(d[0]);
        d[1] = Double.valueOf(c[1].toString());
//        System.out.println(d[1]);
        return d;
    }

    //This function returns the centroid based on points of polygon
    public double[] getcentroid(double point[][]) {
        double[][] p = new double[3][2];
        double[] cen = new double[2];
        if (point.length == 2) {
            cen[0] = (point[0][0] + point[1][0]) / 2;
            cen[1] = (point[0][1] + point[1][1]) / 2;
        }
        if (point.length == 3) {
            cen[0] = (point[0][0] + point[1][0] + point[2][0]) / 3;
            cen[1] = (point[0][1] + point[1][1] + point[2][1]) / 3;
        }
        if (point.length > 3) {
            double[][] newpoint = new double[point.length - 2][2];
            for (int i = 1; i < point.length - 1; i++) {

                p[0] = point[0];
                p[1] = point[i];
                p[2] = point[i + 1];
                newpoint[i - 1] = getcentroid(p);
//                System.out.println(point.length);
//                System.out.println(i);
//                System.out.println("====");
            }
            cen = getcentroid(newpoint);
        }
        return cen;

    }


    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {

        }

        public void onProviderDisabled(String arg0) {

        }

        public void onProviderEnabled(String arg0) {

        }

        public void onStatusChanged(String arg0, int arg1, Bundle arg2) {

        }
    };


    public double getdis(double lng1, double lng2, double lat1, double lat2) {

        double R = 6370996.81;
        double distance = R * Math.acos(Math.cos(lat1 * PI / 180) * Math.cos(lat2 * PI / 180) *
                Math.cos(lng1 * PI / 180 - lng2 * PI / 180) + Math.sin(lat1 * PI / 180)
                * Math.sin(lat2 * PI / 180));

        return distance;

    }

    public double getTRianglearea(double a, double b, double c) {

        double p = (a + b + c) * 0.5;
        double s = Math.sqrt(p * (p - a) * (p - b) * (p - c));
        return s;
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

//        System.out.println(x+"000000" + y);


        // Add a marker in Sydney and move the camera
        LatLng loca = new LatLng(x, y);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);


        Geocoder geoCoder = new Geocoder(getBaseContext(), Locale.getDefault());
        List <android.location.Address> addr = null;
        try {
            addr = geoCoder.getFromLocation(x,y,3);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //String addr = geocodeAddr("31.71099194", "120.4019789");// (38.9146943,121.612382);
        //System.out.println("0000000000");

        if (addr.size() > 0) {
            Address address = addr.get(0);

//            System.out.println(addr.get(0).getLocality());
            city = addr.get(1).getLocality();

        }

        //System.out.println(addr.get(1));

        //mMap.addMarker(new MarkerOptions().position(loca).title("Marker in "+ city));

        //mMap.moveCamera(CameraUpdateFactory.newLatLng(loca));

        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                count = count +1;
                String str = ""+count + ". " + editText.getText().toString();
//                System.out.print(str);

                mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)).position(latLng).title(str));

                SharedPreferences settings = getSharedPreferences("point", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor=settings.edit();
                editor.putString(""+count,latLng.toString());
//                System.out.println(latLng.toString());
                editor.commit();


            }
        });


    }
}
