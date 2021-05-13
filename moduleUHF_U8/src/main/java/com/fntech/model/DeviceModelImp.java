package com.fntech.model;

import java.util.List;

import com.fntech.entity.Device;

public interface DeviceModelImp {
    //将数据用序列化写入xml文件中
    void writeXML(List<Device> device, int gpioLength);

    //将数据从xml文件中读出来
    List<Device> readXML();


    //供内部调用,获取list后根据model获取device
    Device reLoadDevice(String model);

    //供外界调用,获取device信息
    Device getDeviceFromModel(String model);
}
