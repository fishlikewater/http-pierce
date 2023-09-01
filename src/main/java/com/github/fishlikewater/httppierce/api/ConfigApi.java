package com.github.fishlikewater.httppierce.api;

import com.github.fishlikewater.httppierce.api.model.ServiceMappingBo;
import com.github.fishlikewater.httppierce.entity.ServiceMapping;
import com.github.fishlikewater.httppierce.kit.ChannelUtil;
import com.github.fishlikewater.httppierce.service.ServiceMappingService;
import io.github.linpeilie.Converter;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * <p>
 *
 * </p>
 *
 * @author fishlikewater@126.com
 * @since 2023年09月01日 13:00
 **/
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ConfigApi {

    private final ServiceMappingService serviceMappingService;
    private final Converter converter;

    @GetMapping()
    public Result<List<ServiceMapping>> getList(){
        final List<ServiceMapping> list = serviceMappingService.list();
        for (ServiceMapping serviceMapping : list) {
            final Integer id = serviceMapping.getId();
            final Integer state = ChannelUtil.stateMap.get((long) id);
            if (Objects.nonNull(state)){
                serviceMapping.setState(state);
            }
        }
        return Result.of(list, CodeEnum.SUCCESS);
    }


    @PostMapping()
    public Result<?> edit(@RequestBody @Validated ServiceMappingBo serviceMappingBo){
        serviceMappingService.saveOrUpdate(converter.convert(serviceMappingBo, ServiceMapping.class));
        return Result.of("ok");
    }



}
