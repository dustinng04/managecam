package com.example.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceResponse {
    Integer id;
    String name;
    String ip;
    Integer port;
    String description;
    String status;
    Integer roomId;
    String roomName;
    String cameraType;
    String checkType;

}
