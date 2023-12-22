package com.github.fishlikewater.httppierce.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;

import java.io.Serializable;

import com.mybatisflex.core.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 实体类。
 *
 * @author fishl
 * @since 2023-09-01
 */
@Accessors(chain = true)
@Data()
@EqualsAndHashCode(callSuper = true)
@Table(value = "service_mapping")
public class ServiceMapping extends Model<ServiceMapping> {

    @Id(keyType = KeyType.Auto)
    private Integer id;

    private String name;

    private String address;

    private Integer localPort;

    private String registerName;

    private Integer delRegisterName;

    private Integer newServerPort;

    private Integer newPort;

    private String protocol;

    private Integer enable;

    @Column(ignore = true)
    private Integer state;

    @Column(ignore = true)
    private String remoteAddress;

}
