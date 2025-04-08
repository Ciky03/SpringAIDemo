package cloud.ciky.service.impl;

import cloud.ciky.entity.po.Course;
import cloud.ciky.mapper.CourseMapper;
import cloud.ciky.service.ICourseService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 学科表 服务实现类
 * </p>
 *
 * @author ciky
 * @since 2025-04-08
 */
@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements ICourseService {

}
