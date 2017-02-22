package com.baway.donghao;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import java.util.ArrayList;
import java.util.List;

/**
 *  Created by Donghao on 2017/1/16.
 */
public class MainActivity extends AppCompatActivity {
    /**
     * 百度地图控件
     * 显示地图的View。
     * 一个显示地图的视图，当被焦点选中时，它能捕获按键事件和触摸手势去平移和缩放地图。
     */
    private MapView mMapView = null;
    // 百度地图对象,地图的总控制器
    private BaiduMap mBdMap;
    // 定位服务的客户端
    private LocationClient mLocationClient;
    // 封装当前位置的类
    private MyLocationData.Builder mLocationData;
    // TextView 控件
    private TextView mPositionText;
    // 定义标记，防止程序多次定位
    private boolean isFirstLocate = true;
    // 描述地图状态将要发生的变化
    private MapStatusUpdate update;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 得到服务端对象
        mLocationClient = new LocationClient(getApplicationContext());
        // 注册定位监听函数，会回调到实现  BDLocationListener 的类
        mLocationClient.registerLocationListener(new MyLocationListener());
        // 初始化地图，必须在setContentView之前，上下文必须传入getApplicationContext()
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        // 初始化视图
        initView();
        // 开启位置移动功能
        mBdMap.setMyLocationEnabled(true);
        // 存放权限集合
        List<String> permissionList = new ArrayList<String>();
        /**
         * 运行时权限，判断权限，如果没有获取到就存入集合，在最后统一请求
         */
        if (ContextCompat.checkSelfPermission(MainActivity.this,

                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this,

                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this,

                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(!permissionList.isEmpty()){
            // 将List集合转为数组
            String[] permissions = permissionList.toArray(new String[permissionList.size

                    ()]);
            // 申请权限
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        }else{
            // 开始定位
            requestLocation();
        }
    }
    /**
     * 移动到自身位置的方法
     */
    private void navigateTo(BDLocation location){
        // 判断是否第一次定位，只在程序第一次启动的时候将
        if (isFirstLocate){
            // 将BDLocationd的位置信息封装进latLng 作为中心点显示在地图
            LatLng ll = new LatLng(location.getLatitude(),location.getLongitude());
            // 设置地图新中心点
            update = MapStatusUpdateFactory.newLatLng(ll);
            // 将地图移动到当前所在的位置
            mBdMap.animateMapStatus(update);
            // 设置缩放级别
            // update = MapStatusUpdateFactory.zoomTo(16f);
            // 将地图移动到当前所在的位置
            mBdMap.animateMapStatus(update);
            // 改变标记状态
            isFirstLocate = false;
        }
        // 设置定位数据的纬度
        mLocationData.latitude(location.getLatitude());
        // 设置定位数据的经度
        mLocationData.longitude(location.getLongitude());
        // 构建生成定位数据对象
        MyLocationData locationData = mLocationData.build();
        // 设置定位数据, 只有先允许定位图层后设置数据才会生效
        mBdMap.setMyLocationData(locationData);
    }
    /**
     * 开始定位的方法
     */
    private void requestLocation(){
        // 设置跟新，每隔5秒跟新一次
        initLocation();
        // 启动定位sdk
        mLocationClient.start();
    }

    /**
     * 设置位置更新
     */
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(5000);
        mLocationClient.setLocOption(option);
    }

    /**
     * 通过Activity的生命周期方法，管理地图的生命周期
     */
    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }
    @Override
    protected void onDestroy() {
        // 停止定位sdk
        mLocationClient.stop();
        mMapView.onDestroy();
        mBdMap.setMyLocationEnabled(false);
        mMapView = null;
        super.onDestroy();
    }
    /**
     * 处理权限申请结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[]

            grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if (grantResults.length>0){
                    for (int result : grantResults){
                        if (result != PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this, "你必须同意所有权限",

                                    Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                }
                break;
        }
    }


    /**
     * 初始化视图
     */
    private void initView() {
        mPositionText = (TextView) findViewById(R.id.main_text);
        mMapView = (MapView) findViewById(R.id.bmapview);
        mLocationData = new MyLocationData.Builder();
        mBdMap = mMapView.getMap();
    }

    /**
     * 获得经纬度  定位方式
     * Created by Donghao on 2017/1/16.
     */
    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {

            if (bdLocation.getLocType() == BDLocation.TypeGpsLocation ||

                    bdLocation.getLocType() == BDLocation.TypeNetWorkLocation)
            {
                Log.e("TAG", "onReceiveLocation: " );
                // 将当前位置信息设置到地图
                navigateTo(bdLocation);
            }
            // 可变长字符串
            StringBuilder currentPosition = new StringBuilder();
            // bdLocation.getLatitude()  得到纬度
            currentPosition.append("纬度：").append(bdLocation.getLatitude());
            Log.e("ZHOUKAO","纬度为"+bdLocation.getLatitude());
            // 得到 经线  bdLocation.getLongitude()
            currentPosition.append("经线：").append(bdLocation.getLongitude());
            Log.e("ZHOUKAO","经度为"+bdLocation.getLongitude());
            currentPosition.append("定位方式：");
            // bdLocation.getLocType()  判断定位方式   BDLocation.TypeGpsLocation  GPS定位
            if (bdLocation.getLocType() == BDLocation.TypeGpsLocation){
                currentPosition.append("GPS");
                // BDLocation.TypeNetWorkLocation 网络定位
            }else if (bdLocation.getLocType() == BDLocation.TypeNetWorkLocation){
                currentPosition.append("网络：");
            }
            mPositionText.setText(currentPosition);
            Log.e("TAG", "onReceiveLocation: "+currentPosition);
        }
    }
}
