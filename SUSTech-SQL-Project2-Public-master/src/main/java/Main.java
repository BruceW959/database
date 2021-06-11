import cn.edu.sustech.cs307.config.Config;
import cn.edu.sustech.cs307.dto.*;
import cn.edu.sustech.cs307.dto.prerequisite.AndPrerequisite;
import cn.edu.sustech.cs307.dto.prerequisite.CoursePrerequisite;
import cn.edu.sustech.cs307.dto.prerequisite.OrPrerequisite;
import cn.edu.sustech.cs307.dto.prerequisite.Prerequisite;
import cn.edu.sustech.cs307.dto.Department;
import cn.edu.sustech.cs307.dto.Major;
import cn.edu.sustech.cs307.dto.Student;
import cn.edu.sustech.cs307.dto.User;
import cn.edu.sustech.cs307.dto.prerequisite.AndPrerequisite;
import cn.edu.sustech.cs307.dto.prerequisite.CoursePrerequisite;
import cn.edu.sustech.cs307.dto.prerequisite.OrPrerequisite;
import cn.edu.sustech.cs307.dto.prerequisite.Prerequisite;
import cn.edu.sustech.cs307.factory.ServiceFactory;
import cn.edu.sustech.cs307.service.*;
import io.netty.util.internal.shaded.org.jctools.queues.atomic.LinkedQueueAtomicNode;
import cn.edu.sustech.cs307.service.Reference.ReferenceCourseService;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.util.*;

public class Main {
    public static void main(String[] args) throws ParseException {
        ServiceFactory serviceFactory = Config.getServiceFactory();
        assert serviceFactory != null;
/*        InstructorService instructorService = serviceFactory.createService(InstructorService.class);
        MajorService majorService = serviceFactory.createService(MajorService.class);
        DepartmentService departmentService = serviceFactory.createService(DepartmentService.class);
        SemesterService semesterService = serviceFactory.createService(SemesterService.class);
        UserService userService = serviceFactory.createService(UserService.class);
        CourseService courseService=serviceFactory.createService(CourseService.class);

        //List<User> users = new LinkedList<>();
        //users = userService.getAllUsers();
        test(courseService);

    }
    public static void test(CourseService courseService){
        Prerequisite calculus = new OrPrerequisite(List.of(
                new CoursePrerequisite("MA101A"),
                new CoursePrerequisite("MA101B")
        ));
        Prerequisite algebra = new CoursePrerequisite("MA103A");
        Prerequisite prerequisite = new AndPrerequisite(List.of(calculus, algebra));
        //courseService.addCourse("CS205","c++",3,64, Course.CourseGrading.HUNDRED_MARK_SCORE,prerequisite);
        //courseService.addCourseSection("CS205",1,"中文一班",120);
        List<Short> weekList=new LinkedList<>();
        weekList.add((short)1);
        weekList.add((short)3);
        weekList.add((short)5);
        weekList.add((short)6);
        weekList.add((short)7);
        int wl=0;
        for (short s:weekList){
            wl+=(int)Math.pow(2,s);
        }
        //courseService.addCourseSectionClass(9,1,DayOfWeek.MONDAY,weekList,(short) 1,(short)2,"teaching building 1");
        //courseService.removeCourse("CS205");
        //courseService.removeCourseSection(7);
        //courseService.removeCourseSectionClass(1);
        List<Course> cl=courseService.getAllCourses();
        for(Course c:cl){
            System.out.println(c.name+" "+c.id);
        }
        List<CourseSection> csl=courseService.getCourseSectionsInSemester("CS205",1);
        for(CourseSection c:csl){
            System.out.println(c.name+" "+c.id);
        }
        System.out.println(courseService.getCourseBySection(9).name);
        System.out.println(courseService.getCourseSectionByClass(3).name);
        List<User> users = new LinkedList<>();
        users = userService.getAllUsers();*/
        Prerequisite calculus = new OrPrerequisite(List.of(
                new CoursePrerequisite("MA101A"),
                new CoursePrerequisite("MA101B")
        ));
        Prerequisite algebra = new CoursePrerequisite("MA103A");
        Prerequisite prerequisite = new AndPrerequisite(List.of(calculus, algebra));
        ReferenceCourseService r=new ReferenceCourseService();
        System.out.println(r.addPre(prerequisite));
    }

}
