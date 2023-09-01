package com.github.fishlikewater.httppierce.api.model;

import com.github.fishlikewater.httppierce.entity.ServiceMapping;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 *
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年09月01日 15:25
 **/
@Data
@Accessors(chain = true)
@EqualsAndHashCode
@AutoMapper(target = ServiceMapping.class, reverseConvertGenerate = false)
public class ServiceMappingBo {

    private Integer id;

    @NotBlank(message = "名称不能为空")
    private String name;

    @NotBlank(message = "地址不能为空")
    private String address;

    @NotNull(message = "本地映射端口不能为空")
    private Integer localPort;

    @NotBlank(message = "注册名不能为空")
    private String registerName;

    @NotNull(message = "是否删除路径中得注册名")
    private Integer delRegisterName;

    @NotNull(message = "是否单独开启新端口映射")
    private Integer newServerPort;

    @NotNull(message = "服务端需要开启得新端口")
    private Integer newPort;

    @NotBlank(message = "协议不能为空")
    private String protocol;


}
