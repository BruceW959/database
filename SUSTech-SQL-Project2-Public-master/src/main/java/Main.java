import cn.edu.sustech.cs307.config.Config;
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
import cn.edu.sustech.cs307.service.Reference.ReferenceCourseService;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
