package com.github.fishlikewater.httppierce.service.impl;

import com.github.fishlikewater.httppierce.kit.ChannelUtil;
import com.github.fishlikewater.httppierce.kit.ClientKit;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.github.fishlikewater.httppierce.entity.ServiceMapping;
import com.github.fishlikewater.httppierce.mapper.ServiceMappingMapper;
import com.github.fishlikewater.httppierce.service.ServiceMappingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 *  服务层实现。
 *
 * @author fishl
 * @since 2023-09-01
 */
@Service
public class ServiceMappingServiceImpl extends ServiceImpl<ServiceMappingMapper, ServiceMapping> implements ServiceMappingService {

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void edit(ServiceMapping serviceMapping) {
        if (Objects.isNull(serviceMapping.getId())){
            this.save(serviceMapping);
            if (serviceMapping.getEnable() == 1){
                ClientKit.addMapping(serviceMapping);
            }
        }else {
            final ServiceMapping mapping = this.getById(serviceMapping.getId());
            if (!Objects.equals(mapping.getEnable(), serviceMapping.getEnable())){
                if (serviceMapping.getEnable() == 1){
                    ClientKit.addMapping(serviceMapping);
                }else {
                    ClientKit.cancelRegister(serviceMapping.getRegisterName());
                }
            }
        }

    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void delById(Integer id) {
        final ServiceMapping mapping = this.getById(id);
        if (Objects.nonNull(mapping)){
            this.removeById(id);
            final Integer state = ChannelUtil.stateMap.get(mapping.getRegisterName());
            if (Objects.nonNull(state) && state == 1){
                ClientKit.cancelRegister(mapping.getRegisterName());
            }
        }
    }
}
