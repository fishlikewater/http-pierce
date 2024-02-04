package com.github.fishlikewater.httppierce.service;

import com.github.fishlikewater.httppierce.entity.ServiceMapping;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * 服务层。
 *
 * @author fishl
 * @since 2023-09-01
 */
public interface ServiceMappingService extends IService<ServiceMapping> {

    void edit(ServiceMapping serviceMapping);

    void delById(Integer id);

    void enable(Integer id);

    List<ServiceMapping> querylist();
}
