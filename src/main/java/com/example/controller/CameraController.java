package com.example.controller;

import com.example.dto.response.DeviceInfoDto;
import com.example.dto.response.DeviceRule;
import com.example.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequiredArgsConstructor
public class CameraController {

    private final DeviceService deviceService;

//    @GetMapping("server/login")
//    public String login() {
//        sdkInit.login("192.168.1.101", 37777, "admin", "o858899789");
//        return "ok login";
//    }

    @GetMapping("/camera/all")
    public String getAllDevices() {
        HashMap<String, DeviceInfoDto> cameraInfoDtoList = deviceService.getListCameraCCTV();
        return cameraInfoDtoList.toString();
    }

    @GetMapping("/camera/config")
    public String getCameraConfig() {
        DeviceRule deviceRule = deviceService.getDeviceConfig();
        System.out.println("Finding config of camera:" + deviceRule);
        return "find config ok";
    }

}
