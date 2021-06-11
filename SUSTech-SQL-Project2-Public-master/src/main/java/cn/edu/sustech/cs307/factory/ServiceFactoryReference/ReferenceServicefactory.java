package cn.edu.sustech.cs307.factory.ServiceFactoryReference;

import cn.edu.sustech.cs307.factory.ServiceFactory;
import cn.edu.sustech.cs307.service.*;
import cn.edu.sustech.cs307.service.Reference.*;

public class ReferenceServicefactory extends ServiceFactory {
    public ReferenceServicefactory(){
        registerService(StudentService.class, new ReferenceStudentService());
        registerService(CourseService.class, new ReferenceCourseService());
        registerService(DepartmentService.class, new ReferenceDepartmentService());
        registerService(InstructorService.class, new ReferenceInstructorService());
        registerService(MajorService.class, new ReferenceMajorService());
        registerService(SemesterService.class, new ReferenceSemesterService());
        registerService(UserService.class, new ReferenceUserServices());
    }

}
