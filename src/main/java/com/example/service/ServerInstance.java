package com.example.service;

import com.example.service.response.CameraResponse;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import com.netsdk.lib.NetSDKLib;

@Getter
@Setter
@Component
public class ServerInstance {

    // SDK instance
    private final NetSDKLib netSdk = NetSDKLib.NETSDK_INSTANCE;
    private final NetSDKLib configsdk = NetSDKLib.CONFIG_INSTANCE;
    private NetSDKLib.LLong loginHandle = new NetSDKLib.LLong(0);
    private NetSDKLib.NET_DEVICEINFO_Ex deviceInfo = new NetSDKLib.NET_DEVICEINFO_Ex();
    private NetSDKLib.LLong cameraHandle = new NetSDKLib.LLong(0);
    private NetSDKLib.LLong realLoadHandle = new NetSDKLib.LLong(0);
    private boolean bConnect = false;
    private CameraResponse serverInfo;


    public boolean isConnect(){
        return loginHandle.longValue()!=0&&bConnect;
    }

}
