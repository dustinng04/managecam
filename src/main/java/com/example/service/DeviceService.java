package com.example.service;

import com.example.dto.response.DeviceInfoDto;
import com.example.dto.response.DeviceRule;
import com.example.dto.response.LinkGroup;
import com.example.entity.Device;
import com.netsdk.lib.NetSDKLib;
import com.netsdk.lib.ToolKits;
import com.sun.jna.Memory;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.var;
import org.springframework.stereotype.Service;
import com.netsdk.lib.NetSDKLib.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceService {
    private final ServerInstance serverInstance;
    public static NetSDKLib.NET_DEVICEINFO_Ex m_stDeviceInfo = new NetSDKLib.NET_DEVICEINFO_Ex();

    public HashMap<String, DeviceInfoDto> getListCameraCCTV() {
        HashMap<String, DeviceInfoDto> cameraInfoDtoList = new HashMap<>();
        if(!serverInstance.isConnect()){
            return cameraInfoDtoList;
        }
        int cameraCount = serverInstance.getDeviceInfo().byChanNum;
        NetSDKLib.NET_MATRIX_CAMERA_INFO[]  cameraInfo = new NetSDKLib.NET_MATRIX_CAMERA_INFO[cameraCount];
        for(int i = 0; i < cameraCount; i++) {
            cameraInfo[i] = new NetSDKLib.NET_MATRIX_CAMERA_INFO();
        }

        NetSDKLib.NET_IN_MATRIX_GET_CAMERAS inMatrix = new NetSDKLib.NET_IN_MATRIX_GET_CAMERAS();

        NetSDKLib.NET_OUT_MATRIX_GET_CAMERAS outMatrix = new NetSDKLib.NET_OUT_MATRIX_GET_CAMERAS();
        outMatrix.nMaxCameraCount = cameraCount;
        outMatrix.pstuCameras = new Memory((long) cameraInfo[0].size() * cameraCount);
        outMatrix.pstuCameras.clear((long) cameraInfo[0].size() * cameraCount);

        ToolKits.SetStructArrToPointerData(cameraInfo, outMatrix.pstuCameras);

        if(serverInstance.getNetSdk().CLIENT_MatrixGetCameras(serverInstance.getLoginHandle(), inMatrix, outMatrix, 3000)) {
            ToolKits.GetPointerDataToStructArr(outMatrix.pstuCameras, cameraInfo);
            for(int j = 0; j < outMatrix.nRetCameraCount; j++) {
                if(new String(cameraInfo[j].stuRemoteDevice.szIp).trim().isEmpty() || cameraInfo[j].bRemoteDevice == 0 || cameraInfo[j].stuRemoteDevice.szIp.toString().isEmpty()) {   // 过滤远程设备
                    continue;
                }
                DeviceInfoDto deviceInfoDto = new DeviceInfoDto();
                deviceInfoDto.setChannelId(cameraInfo[j].nUniqueChannel);
                deviceInfoDto.setIp( new String(cameraInfo[j].stuRemoteDevice.szIp).trim());
                deviceInfoDto.setPort( cameraInfo[j].stuRemoteDevice.nPort);

                cameraInfoDtoList.put(deviceInfoDto.getIp(), deviceInfoDto);
            }
        }
        return cameraInfoDtoList;
    }

    public DeviceRule getDeviceConfig() {
        Device device = new Device(1, "192.168.1.103", "admin", "o858899789", "37777", "Camera", true, "CCTV", 1);
        int channel = device.getChannelId();
        int countGroup = 0;
        String command = NetSDKLib.CFG_CMD_ANALYSERULE; // Lệnh truy vấn cấu hình quy tắc

        int ruleCount = 10; // Số lượng quy tắc sự kiện
        CFG_RULE_INFO[] ruleInfo = new CFG_RULE_INFO[ruleCount];
        for (int i = 0; i < ruleCount; i++) {
            ruleInfo[i] = new CFG_RULE_INFO();
        }

        CFG_ANALYSERULES_INFO analyse = new CFG_ANALYSERULES_INFO();
        analyse.nRuleLen = 1024 * 1024 * 40;
        analyse.pRuleBuf = new Memory(1024 * 1024 * 40); // Provide memory
        analyse.pRuleBuf.clear(1024 * 1024 * 40);

//        List<DeviceRule> allRules = new ArrayList<>(); // List to hold all configured rules

        if (ToolKits.GetDevConfig(serverInstance.getLoginHandle(), channel, command, analyse)) {
            int offset = 0;
            int count = Math.min(analyse.nRuleCount, ruleCount);
            System.out.println(count);
            for (int i = 0; i < count; i++) {
                ToolKits.GetPointerDataToStruct(analyse.pRuleBuf, offset, ruleInfo[i]);
                offset += ruleInfo[0].size(); // Bước đệm quy tắc thông minh
                System.out.println(ruleInfo[i].nRuleSize + " ");
                switch (ruleInfo[i].dwRuleType) {
                    case NetSDKLib.EVENT_IVSS_FACECOMPARE: {
                        NetSDKLib.CFG_FACECOMPARE_INFO msg = new NetSDKLib.CFG_FACECOMPARE_INFO();
                        ToolKits.GetPointerDataToStruct(analyse.pRuleBuf, offset, msg);
                        var rule = new DeviceRule();
                        rule.setCameraId(device.getId());
                        rule.setEnable(msg.bRuleEnable == 1);
                        List<LinkGroup> groups = new ArrayList<>();

                        for (NetSDKLib.CFG_LINKGROUP_INFO group: msg.stuLinkGroupArr) {
//                            if(group.bEnable!=1) continue;
                            LinkGroup linkGroup = new LinkGroup();
                            linkGroup.setGroupName(String.valueOf(countGroup));
                            countGroup++;
                            linkGroup.setEnabled(group.bEnable == 1);
                            groups.add(linkGroup);
                        }
                        rule.setGroups(groups);
                        rule.setStrangerMode(msg.stuStrangerMode.bEnable==1);
                        return rule;
                    }
                    default: break;
                }
                offset += ruleInfo[i].nRuleSize; // Tăng bước đệm
            }
        }

        return null;
    }

//    private String getRuleTypeName(int ruleTypeId) {
//        switch (ruleTypeId) {
//            case NetSDKLib.EVENT_IVSS_FACECOMPARE: return "FaceCompare";
//            case NetSDKLib.EVENT_IVS_PEOPLE_COUNTING: return "PeopleCounting";
//            case NetSDKLib.EVENT_IVS_FACEDETECTION: return "FaceDetection";
//            // Add additional cases for each supported rule type if necessary
//            default: return "Unknown";
//        }
//    }
}
