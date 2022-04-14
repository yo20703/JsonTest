package com.example.jsontest;

public class AddressData {
    // example
    // {"city":"彭湖縣","cn":"彭湖重光漁港","addr":"彭湖縣吉林路23號","tel":"0933123456"}
    String city = "";
    String cn = "";
    String addr = "";
    String tel = "";

    public AddressData(String city, String cn, String addr, String tel){
        this.city = city;
        this.cn = cn;
        this.addr = addr;
        this.tel = tel;
    }
}
