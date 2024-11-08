package com.example;

import com.example.service.CameraInfoDto;
import com.example.service.ServerInstance;
import com.example.service.response.CameraResponse;
import com.netsdk.lib.NetSDKLib;
import com.netsdk.lib.NetSDKLib.LLong;
import com.netsdk.lib.NetSDKLib.fDisConnect;
import com.netsdk.lib.NetSDKLib.fHaveReConnect;
import com.netsdk.lib.ToolKits;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SDKInitService {

    private final ServerInstance serverInstance;
    public static NetSDKLib.NET_DEVICEINFO_Ex m_stDeviceInfo = new NetSDKLib.NET_DEVICEINFO_Ex();

//    @Value("${device.ip}")
    private String deviceIp = "192.168.1.101";

//    @Value("${device.port}")
    private Integer devicePort = 37777;

//    @Value("${device.username}")
    private String deviceUsername = "admin";

//    @Value("${device.password}")
    private String devicePassword = "o858899789";

    List<String> deviceIds = new ArrayList<>();

    // Disconnect callback
    private final fDisConnect disconnectCallback = new fDisConnect() {
        @Override
        public void invoke(LLong lLoginID, String pchDVRIP, int nDVRPort, Pointer dwUser) {
            System.out.println("Device disconnected. LoginID: " + lLoginID + ", IP: " + pchDVRIP + ", Port: " + nDVRPort);
        }
    };

    // Reconnect callback
    private final fHaveReConnect reconnectCallback = (loginID, ip, port, user) ->
            System.out.println("Device reconnected, loginID: " + loginID + ", IP: " + ip + ", Port: " + port);

    @PostConstruct
    public void initSDK() {
        boolean initResult = serverInstance.getNetSdk().CLIENT_Init(disconnectCallback, null);
        if (!initResult) {
            throw new IllegalStateException("Failed to initialize SDK.");
        }
        System.out.println("SDK initialized successfully.");
        login(deviceIp, devicePort, deviceUsername, devicePassword);
    }

    public void login(String deviceIp, int devicePort, String deviceUsername, String devicePassword) {
        logout();

        NetSDKLib.NET_IN_LOGIN_WITH_HIGHLEVEL_SECURITY pstlnParam = new NetSDKLib.NET_IN_LOGIN_WITH_HIGHLEVEL_SECURITY() {
            {
                szIP = deviceIp.getBytes();
                nPort = devicePort;
                szUserName = deviceUsername.getBytes();
                szPassword = devicePassword.getBytes();
            }
        };

        NetSDKLib.NET_OUT_LOGIN_WITH_HIGHLEVEL_SECURITY pstOutParam = new NetSDKLib.NET_OUT_LOGIN_WITH_HIGHLEVEL_SECURITY();

        pstOutParam.stuDeviceInfo =m_stDeviceInfo;
        NetSDKLib.LLong loginHandle = serverInstance.getNetSdk().CLIENT_LoginWithHighLevelSecurity(pstlnParam, pstOutParam);
        System.out.println(loginHandle);
        if (loginHandle.longValue() != 0) {
            System.out.println("Login Success");
            serverInstance.setLoginHandle(loginHandle);
            serverInstance.setBConnect(true);
            serverInstance.setDeviceInfo(pstOutParam.stuDeviceInfo);
            CameraResponse device = new CameraResponse();
            device.setCameraType("IVSS");
            device.setIp(deviceIp);
            device.setPort(devicePort);

            serverInstance.setServerInfo(device);
        } else{
            System.err.printf("CLIENT_LoginWithHighLevelSecurity Failed!LastError = %s\n", ToolKits.getErrorCode());
        }
    }

    public HashMap<String, CameraInfoDto> getListCameraCCTV() {
        HashMap<String, CameraInfoDto> cameraInfoDtoList = new HashMap<>();
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
                CameraInfoDto cameraInfoDto = new CameraInfoDto();
                cameraInfoDto.setChannelId(cameraInfo[j].nUniqueChannel);
                cameraInfoDto.setIp( new String(cameraInfo[j].stuRemoteDevice.szIp).trim());
                cameraInfoDto.setPort( cameraInfo[j].stuRemoteDevice.nPort);

                cameraInfoDtoList.put(cameraInfoDto.getIp(),cameraInfoDto);
            }
        }
        return cameraInfoDtoList;
    }


    public void logout() {
        if (serverInstance.isConnect()) {
            serverInstance.getNetSdk().CLIENT_Logout(serverInstance.getLoginHandle());
            serverInstance.setBConnect(false);
        }
    }

    @PreDestroy
    public void cleanup() {
        logout();
        serverInstance.getNetSdk().CLIENT_Cleanup();
        System.out.println("See You...");
    }

}