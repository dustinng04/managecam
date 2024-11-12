package com.example.dto.response;

import lombok.Data;

@Data
public class DeviceInfoDto {
    String ip;
    Integer port;
    Integer channelId;
}