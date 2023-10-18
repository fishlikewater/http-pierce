package com.github.fishlikewater.httppierce.api;

import com.github.fishlikewater.httppierce.api.model.ServiceMappingBo;
import com.github.fishlikewater.httppierce.config.HttpPierceClientConfig;
import com.github.fishlikewater.httppierce.entity.ConnectionStateInfo;
import com.github.fishlikewater.httppierce.entity.ServiceMapping;
import com.github.fishlikewater.httppierce.kit.ChannelUtil;
import com.github.fishlikewater.httppierce.kit.ClientKit;
import com.github.fishlikewater.httppierce.service.ServiceMappingService;
import io.github.linpeilie.Converter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 客户端数据接口
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年09月01日 13:00
 **/
@RestController
@ConditionalOnProperty(prefix = "http.pierce", name = "boot-type", havingValue = "client")
@RequiredArgsConstructor
@RequestMapping("/client/api")
public class ClientConfigApi {

    private final ServiceMappingService serviceMappingService;
    private final Converter converter;
    private final HttpPierceClientConfig httpPierceClientConfig;

    @GetMapping
    public Result<List<ServiceMapping>> getList(){
        final List<ServiceMapping> list = serviceMappingService.list();
        for (ServiceMapping serviceMapping : list) {
            final ConnectionStateInfo stateInfo = ChannelUtil.stateMap.get(serviceMapping.getRegisterName());
            if (Objects.nonNull(stateInfo)){
                serviceMapping.setState(stateInfo.getState());
                if (stateInfo.getState() == 1){
                    serviceMapping.setRemoteAddress(httpPierceClientConfig.getServerAddress() + ":" + stateInfo.getServicePort());
                }
            }
        }
        return Result.of(list, CodeEnum.SUCCESS);
    }

    @PutMapping("/{id}")
    public Result<?> enable(@PathVariable("id")Integer id){
        serviceMappingService.enable(id);
        return Result.of("ok");
    }

    @PostMapping
    public Result<?> edit(@RequestBody @Validated ServiceMappingBo serviceMappingBo){
        serviceMappingService.edit(converter.convert(serviceMappingBo, ServiceMapping.class));
        return Result.of("ok");
    }

    @DeleteMapping("/{id}")
    public Result<?> del(@PathVariable("id")Integer id){
        serviceMappingService.delById(id);
        return Result.of("ok");
    }

    @PostMapping("/reboot")
    public Result<?> reboot(){
        ClientKit.getClientBoot().stop();
        ClientKit.getClientBoot().start();
        return Result.of("ok");
    }



}
