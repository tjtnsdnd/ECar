package com.example.kccistc.ecar;

public class DataVO {
    private String name; // 충전소명
    private  double lat; // 위도
    private  double lng; // 경도
    private String addr; // 주소

    public DataVO(){}
    public DataVO(String name, double lat, double lng){
        this.name = name;
        this.lat = lat;
        this.lng = lng;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
