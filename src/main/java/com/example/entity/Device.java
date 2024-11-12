package com.example.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Device {
    Integer id;
    String tcpIp;
    String username;
    String password;
    String port;
    String description;
    boolean status;
    String type;
    Integer channelId;
}
