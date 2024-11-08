package com.example.controller;

import com.example.SDKInitService;
import com.example.service.CameraInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequiredArgsConstructor
public class CameraController {

    private final SDKInitService sdkInit;

//    @GetMapping("server/login")
//    public String login() {
//        sdkInit.login("192.168.1.101", 37777, "admin", "o858899789");
//        return "ok login";
//    }

    @GetMapping("/camera/all")
    public String getAllDevices() {
        HashMap<String, CameraInfoDto> cameraInfoDtoList = sdkInit.getListCameraCCTV();
        System.out.println("Finding camera list: " + cameraInfoDtoList);
        return "ok";
    }
}
