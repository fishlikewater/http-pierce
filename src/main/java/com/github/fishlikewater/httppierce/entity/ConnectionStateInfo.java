package com.github.fishlikewater.httppierce.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 连接状态
 *
 * @author fishlikewater@126.com
 * @since 2023年09月12日 9:21
 **/
@Data
@EqualsAndHashCode
public class ConnectionStateInfo implements Serializable {

    private String registerName;

    private int servicePort;

    private int state;
}
