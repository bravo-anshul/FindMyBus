package com.example.bravo.findmybus;


public class busList {

    public String bus_no;
    public String[] stops = new String[10];


    public busList() {
        bus_no = "0";
        for(int i=0;i<10;i++){
            stops[i] = "zero";
        }

    }


    public busList(String bus_no){
        this.bus_no = bus_no;

    }

}
