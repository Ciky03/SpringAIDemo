package cloud.ciky.service.impl;

import cloud.ciky.entity.po.School;
import cloud.ciky.mapper.SchoolMapper;
import cloud.ciky.service.ISchoolService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 校区表 服务实现类
 * </p>
 *
 * @author ciky
 * @since 2025-04-08
 */
@Service
public class SchoolServiceImpl extends ServiceImpl<SchoolMapper, School> implements ISchoolService {

}
