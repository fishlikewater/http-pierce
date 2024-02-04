package com.github.fishlikewater.httppierce.api;

import com.github.fishlikewater.httppierce.api.model.ServiceMappingBo;
import com.github.fishlikewater.httppierce.entity.ServiceMapping;
import com.github.fishlikewater.httppierce.kit.ClientKit;
import com.github.fishlikewater.httppierce.service.ServiceMappingService;
import io.github.linpeilie.Converter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 客户端数据接口
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年09月01日 13:00
 **/
@RestController
@RequiredArgsConstructor
@RequestMapping("/client/api")
@ConditionalOnProperty(prefix = "http.pierce", name = "boot-type", havingValue = "${http.pierce.boot-type:client}")
public class ClientConfigApi {

    private final ServiceMappingService serviceMappingService;
    private final Converter converter;

    @GetMapping
    public Result<List<ServiceMapping>> getList() {
        final List<ServiceMapping> list = serviceMappingService.querylist();
        return Result.of(list);
    }

    /**
     * 启用
     *
     * @param id id
     * @return {@link Result<String>}
     */
    @PutMapping("/{id}")
    public Result<String> enable(@PathVariable("id") Integer id) {
        serviceMappingService.enable(id);
        return Result.of("ok");
    }

    @PostMapping
    public Result<String> edit(@RequestBody @Validated ServiceMappingBo serviceMappingBo) {
        serviceMappingService.edit(converter.convert(serviceMappingBo, ServiceMapping.class));
        return Result.of("ok");
    }

    @DeleteMapping("/{id}")
    public Result<String> del(@PathVariable("id") Integer id) {
        serviceMappingService.delById(id);
        return Result.of("ok");
    }

    @PostMapping("/reboot")
    public Result<String> reboot() {
        ClientKit.getClientBoot().stop();
        ClientKit.getClientBoot().start();
        return Result.of("ok");
    }


}

