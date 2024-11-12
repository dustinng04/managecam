package com.example.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class DeviceRule {
    int cameraId;
    boolean enable;
    List<LinkGroup> groups;
    boolean strangerMode;
}
