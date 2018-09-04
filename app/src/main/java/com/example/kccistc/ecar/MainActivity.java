package com.example.kccistc.ecar;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Network;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import android.content.pm.Signature;
import android.view.ViewGroup;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MapView.OpenAPIKeyAuthenticationResultListener,
    MapView.MapViewEventListener{

    String name;
    String name2;
    double lat;
    double lng;
    double lat2;
    double lng2;
    ArrayList<DataVO> list;
    LocationListener locationListener = new LocationListener() {
        @Override

        public void onLocationChanged(Location location) { // 위치 변경
            lat = location.getLatitude();
            lng = location.getLongitude();
            //Log.e("provider", location.getProvider()); // 위치 제공자
            //Log.e("lat", lat + ""); // 변경된 위도
            //Log.e("lng", lng + ""); // 변경된 경도

            MapPoint mp = MapPoint.mapPointWithGeoCoord(lat, lng);

            //mapView.setMapCenterPoint(mp, true); //생성될때 가운데로 위치시킨다


           // mapView.removeAllPOIItems();


            MapPoint.mapPointWithGeoCoord(lat, lng);
            MapPOIItem marker = new MapPOIItem();
            marker.setItemName("내 위치");
            marker.setTag(0);
            marker.setMapPoint(MapPoint.mapPointWithGeoCoord(lat, lng));
            marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
            marker.setSelectedMarkerType(MapPOIItem.MarkerType.YellowPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.

            mapView.addPOIItem(marker);

            //각 매장별 마커

        }
        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {} // 위치 제공자 상태 변경
        @Override
        public void onProviderEnabled(String s) {} // 위치 제공자 활성화
        @Override
        public void onProviderDisabled(String s) {} // 위치 제공자 비활성화
    };
    MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isGps = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetwork = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        //Log.e("isGps", isGps + "");
        //Log.e("isNetwork", isNetwork + "");

        if(isGps) {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, locationListener);
        }
        if(isNetwork) {
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 1, locationListener);
        }

        mapView = new MapView(this);
        mapView.setMapViewEventListener(this);
        mapView.setOpenAPIKeyAuthenticationResultListener(this);
        ViewGroup mapViewContainer = findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);






        //-----------json 파싱--------
        AssetManager manager = getAssets();
        try {
            InputStream in = manager.open("data.json");
            InputStreamReader isr = new InputStreamReader(in);
            BufferedReader reader = new BufferedReader(isr);

            String result = "";
            while(true) {
                String data = reader.readLine();
                if(data == null) {
                    break;
                }
                result += data;
            }
            //여기까지 json 파일 불러왔음, result에 저장
            JSONObject obj = new JSONObject(result);
            JSONArray rec = obj.getJSONArray("records");


            list = new ArrayList<>();
            for(int i = 0; i < rec.length() ; i++){
                JSONObject re = rec.getJSONObject(i);

                name = re.getString("충전소명");
                lat = re.getDouble("위도");
                lng = re.getDouble("경도");
                DataVO vo = new DataVO(name, lat ,lng);
                list.add(vo);


                lat2 = list.get(i).getLat();
                lng2 = list.get(i).getLng();

                double dist = LocationUtil.distance(lat, lng, lat2, lng2, LocationUtil.METER);
                MapPOIItem marker2 = new MapPOIItem();
                //Log.e("거리", dist + list.get(i).getName()+"까지의 거리");
                marker2.setItemName(list.get(i).getName());
                marker2.setTag(0);
                marker2.setMapPoint(MapPoint.mapPointWithGeoCoord(lat2, lng2));
                marker2.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
                marker2.setSelectedMarkerType(MapPOIItem.MarkerType.YellowPin);

                mapView.addPOIItem(marker2);

                if(i== rec.length() - 1){
                    Log.e("로그", String.valueOf(list.get(i).getName()));
                }
            }

            //Log.e("위도", String.valueOf(lat));
            //Log.e("경도", String.valueOf(lng));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //getAppKeyHash();

    }
    /*private void getAppKeyHash() {
        try {
            PackageInfo info =
                    getPackageManager().getPackageInfo(
                            getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String something = new String(Base64.encode(md.digest(), 0));
                Log.e("Hash key", something);
            }
        } catch (Exception e) {
            Log.e("name not found", e.toString());
        }
    }*/

    @Override
    public void onMapViewInitialized(MapView mapView) {


        mapView.setMapCenterPointAndZoomLevel(
                MapPoint.mapPointWithGeoCoord(lat, lng), 2, true); // 위치좌표

        MapPOIItem marker = new MapPOIItem();
        marker.setItemName("Default Marker");
        marker.setTag(0);

        MapPoint.mapPointWithGeoCoord(lat, lng);
        marker.setMapPoint(
                MapPoint.mapPointWithGeoCoord(lat, lng));

        marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
        marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.

        mapView.addPOIItem(marker);



    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {

    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onDaumMapOpenAPIKeyAuthenticationResult(MapView mapView, int i, String s) {
        Log.e("apikey_authentication",
                String.format("Open API Key Authentication Result : code=%d, message=%s", i, s));
    }
}
