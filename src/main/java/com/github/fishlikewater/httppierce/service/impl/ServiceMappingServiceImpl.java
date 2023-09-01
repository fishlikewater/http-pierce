package com.github.fishlikewater.httppierce.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.github.fishlikewater.httppierce.entity.ServiceMapping;
import com.github.fishlikewater.httppierce.mapper.ServiceMappingMapper;
import com.github.fishlikewater.httppierce.service.ServiceMappingService;
import org.springframework.stereotype.Service;

/**
 *  服务层实现。
 *
 * @author fishl
 * @since 2023-09-01
 */
@Service
public class ServiceMappingServiceImpl extends ServiceImpl<ServiceMappingMapper, ServiceMapping> implements ServiceMappingService {

}
