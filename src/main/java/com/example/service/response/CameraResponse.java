package com.example.service.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CameraResponse {
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
