package cloud.ciky.tools;

import cloud.ciky.entity.dto.CourseQueryDTO;
import cloud.ciky.entity.po.Course;
import cloud.ciky.entity.po.CourseReservation;
import cloud.ciky.entity.po.School;
import cloud.ciky.service.ICourseReservationService;
import cloud.ciky.service.ICourseService;
import cloud.ciky.service.ISchoolService;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: ciky
 * @Description: 课程Function类
 * @DateTime: 2025/4/9 0:21
 **/
@Component
@RequiredArgsConstructor
public class CourseTools {

    private final ICourseService courseService;
    private final ISchoolService schoolService;
    private final ICourseReservationService courseReservationService;

    @Tool(description = "根据条件查询课程")
    public List<Course> queryCourse(@ToolParam(description = "查询的条件",required = false) CourseQueryDTO query){
        //如果条件为空,则全返回
        if(query == null){
            return courseService.list();
        }

        //根据条件返回
        //1.根据type和edu
        QueryChainWrapper<Course> wrapper = courseService.query()
                .eq(query.getType() != null, "type", query.getType())
                .le(query.getEdu() != null, "edu", query.getEdu());
        //2.按照指定字段排序
        if(query.getSorts() != null) {
            for (CourseQueryDTO.Sort sort : query.getSorts()) {
                wrapper.orderBy(true, sort.getAsc(), sort.getField());
            }
        }
        return wrapper.list();
    }

    @Tool(description = "查询所有学校")
    public List<School> querySchool(){
        return schoolService.list();
    }

    @Tool(description = "生成预约单,返回预约单号")
    public Integer createCourseReservation(
            @ToolParam(description = "预约课程") String course,
            @ToolParam(description = "预约校区") String school,
            @ToolParam(description = "学生姓名") String studentName,
            @ToolParam(description = "联系方式") String contactInfo,
            @ToolParam(description = "备注",required = false) String remark){
        CourseReservation courseReservation = new CourseReservation();
        courseReservation.setCourse(course);
        courseReservation.setSchool(school);
        courseReservation.setStudentName(studentName);
        courseReservation.setContactInfo(contactInfo);
        courseReservation.setRemark(remark);
        courseReservationService.save(courseReservation);
       return courseReservation.getId();
    }
}
