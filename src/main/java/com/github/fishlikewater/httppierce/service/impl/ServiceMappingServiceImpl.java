package com.github.fishlikewater.httppierce.service.impl;

import com.github.fishlikewater.httppierce.entity.ConnectionStateInfo;
import com.github.fishlikewater.httppierce.kit.ChannelUtil;
import com.github.fishlikewater.httppierce.kit.ClientKit;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.github.fishlikewater.httppierce.entity.ServiceMapping;
import com.github.fishlikewater.httppierce.mapper.ServiceMappingMapper;
import com.github.fishlikewater.httppierce.service.ServiceMappingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import static com.github.fishlikewater.httppierce.entity.table.ServiceMappingTableDef.SERVICE_MAPPING;

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
            ConnectionStateInfo stateInfo = ChannelUtil.stateMap.get(mapping.getRegisterName());
            if (Objects.nonNull(stateInfo) && stateInfo.getState() == 1){
                ClientKit.cancelRegister(mapping.getRegisterName());
            }
        }
    }

    @Override
    public void enable(Integer id) {
        final ServiceMapping mapping = this.getById(id);
        if (mapping.getEnable() == 1){
            this.updateChain().set(SERVICE_MAPPING.ENABLE, 0).where(SERVICE_MAPPING.ID.eq(id)).update();
            ConnectionStateInfo stateInfo = ChannelUtil.stateMap.get(mapping.getRegisterName());
            if (stateInfo.getState() == 1){
                ClientKit.cancelRegister(mapping.getRegisterName());
            }
        }else {
            this.updateChain().set(SERVICE_MAPPING.ENABLE, 1).where(SERVICE_MAPPING.ID.eq(id)).update();
            mapping.setEnable(1);
            ClientKit.addMapping(mapping);
        }
    }
}
