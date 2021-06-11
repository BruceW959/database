package cn.edu.sustech.cs307.service.Reference;

import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.*;
import cn.edu.sustech.cs307.dto.prerequisite.AndPrerequisite;
import cn.edu.sustech.cs307.dto.prerequisite.CoursePrerequisite;
import cn.edu.sustech.cs307.dto.prerequisite.OrPrerequisite;
import cn.edu.sustech.cs307.dto.prerequisite.Prerequisite;
import cn.edu.sustech.cs307.service.CourseService;

import javax.annotation.Nullable;
import java.sql.*;
import java.time.DayOfWeek;
import java.util.*;

public class ReferenceCourseService implements CourseService {
    public String addPre(Prerequisite p){
        if(p==null){
            return null;
        }
        String pre=p.when(new Prerequisite.Cases<>() {
            StringBuilder temp;

            @Override
            public String match(AndPrerequisite self) {
                String[] children = self.terms.stream()
                        .map(term -> term.when(this))
                        .toArray(String[]::new);
                temp = new StringBuilder(children[0]);
                for (int i = 1; i < children.length; i++) {
                    temp.append("|").append(children[i]).append("|AND");
                }
                return temp.toString();
            }

            @Override
            public String match(OrPrerequisite self) {
                String[] children = self.terms.stream()
                        .map(term -> term.when(this))
                        .toArray(String[]::new);
                temp = new StringBuilder(children[0]);
                for (int i = 1; i < children.length; i++) {
                    temp.append("|").append(children[i]).append("|OR");
                }
                return temp.toString();
            }

            @Override
            public String match(CoursePrerequisite self) {
                return self.courseID;
            }
        });
        return pre;
    }
    @Override
    public void addCourse(String courseId, String courseName, int credit, int classHour,
                          Course.CourseGrading grading,
                          @Nullable Prerequisite prerequisite) {
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement stmt = connection.prepareStatement("insert into course (id,name, credit, class_hour, is_pf_grading)" +
                     " VALUES(?,?,?,?,?)" +
                     "ON conflict(id)  DO NOTHING;" , Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, courseId);
            stmt.setString(2, courseName);
            stmt.setInt(3, credit);
            stmt.setInt(4, classHour);
            stmt.setBoolean(5, grading==Course.CourseGrading.PASS_OR_FAIL);
            PreparedStatement stmt1=connection.prepareStatement("insert into prerequisite (course_id, prerequisite) values (?,?)");
            stmt1.setString(1,courseId);
            stmt1.setString(2, addPre(prerequisite));
            stmt.executeUpdate();
            stmt1.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int addCourseSection(String courseId, int semesterId, String sectionName, int totalCapacity) {
        ResultSet rs = null;
        int sectionId=-1;
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement stmt = connection.prepareStatement("insert into course_section (id ,course_id, semester_id, name, total_capacity, left_capacity) " +
                     " VALUES(DEFAULT ,?,?,?,?,?)" , Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, courseId);
            stmt.setInt(2, semesterId);
            stmt.setString(3, sectionName);
            stmt.setInt(4, totalCapacity);
            stmt.setInt(5, totalCapacity);
            stmt.executeUpdate();

            PreparedStatement stmt2=connection.prepareStatement("select currval('course_section_id_seq')");
            rs = stmt2.executeQuery();
            rs.next();

            sectionId = rs.getInt("currval");
        } catch (SQLException e) {
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        } finally{

            try {
                if (rs != null)
                    rs.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return sectionId;
    }

    @Override
    public int addCourseSectionClass(int sectionId, int instructorId, DayOfWeek dayOfWeek, Set<Short> weekList, short classStart, short classEnd, String location) {
        ResultSet rs = null;
        int sectionClassId=-1;
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement stmt = connection.prepareStatement("insert into course_section_class (section_id, instructor_id, day_of_week, week_list, class_begin, class_end, location) " +
                     " VALUES(?,?,?,?,?,?,?)" , Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, sectionId);
            stmt.setInt(2, instructorId);
            stmt.setInt(3, dayOfWeek.getValue());
            int wl=0;
            for (short s:weekList){
                wl+=(int)Math.pow(2,s);
            }
            stmt.setInt(4,wl);
            stmt.setShort(5, classStart);
            stmt.setShort(6, classEnd);
            stmt.setString(7,location);
            stmt.execute();

            PreparedStatement stmt2=connection.prepareStatement("select currval('course_section_class_id_seq')");
            rs = stmt2.executeQuery();
            rs.next();

            sectionClassId = rs.getInt("currval");
        } catch (SQLException e) {
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        } finally{

            try {
                if (rs != null)
                    rs.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return sectionClassId;
    }


    @Override
    public void removeCourse(String courseId) {
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement stmt = connection.prepareStatement("delete from  course where id = ?", Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, courseId);

            stmt.execute();
            System.out.println("delete complete.");
        } catch (SQLException e) {
            System.out.println("course_delete SQLException");
            e.printStackTrace();
        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    @Override
    public void removeCourseSection(int sectionId) {
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement stmt = connection.prepareStatement("delete from course_section where id = ?", Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, sectionId);

            stmt.execute();
            System.out.println("delete complete.");
        } catch (SQLException e) {
            System.out.println("course_delete SQLException");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeCourseSectionClass(int classId) {

        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement stmt = connection.prepareStatement("delete from course_section_class where id = ?", Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, classId);

            stmt.execute();
            System.out.println("delete complete.");
        } catch (SQLException e) {
            System.out.println("course_delete SQLException");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Course> getAllCourses() {
        List<Course> courseList=new LinkedList<>();
        try{
            Connection connection = SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmt = connection.prepareStatement("select * from course", Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = null;
            rs=stmt.executeQuery();

            while(rs.next()){
                Course c=new Course();
                c.id=rs.getString(1);
                c.name=rs.getString(2);
                c.credit=rs.getInt(3);
                c.classHour=rs.getInt(4);
                c.grading=rs.getBoolean(5)?Course.CourseGrading.PASS_OR_FAIL:Course.CourseGrading.HUNDRED_MARK_SCORE;
                courseList.add(c);
            }

        }catch (SQLException e) {
            e.printStackTrace();
        }
        return courseList;
    }

    @Override
    public List<CourseSection> getCourseSectionsInSemester(String courseId, int semesterId) {
        List<CourseSection> courseSectionList=new LinkedList<>();
        ResultSet rs = null;
        try{
            Connection connection = SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmt = connection.prepareStatement("select id,name,total_capacity,left_capacity  from course_section where course_id= ? and semester_id=?", Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1,courseId);
            stmt.setInt(2,semesterId);
            rs=stmt.executeQuery();

            while(rs.next()){
                CourseSection cs=new CourseSection();
                cs.id=rs.getInt(1);
                cs.name=rs.getString(2);
                cs.totalCapacity=rs.getInt(3);
                cs.leftCapacity=rs.getInt(4);
                courseSectionList.add(cs);
            }

        }catch (SQLException e) {
            e.printStackTrace();
        }
        return courseSectionList;

    }

    @Override
    public Course getCourseBySection(int sectionId) {
        ResultSet rs = null;
        try{
            Connection connection = SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmt = connection.prepareStatement("select * from course where id=(select course_id from course_section where course_section.id=?)", Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1,sectionId);

            rs=stmt.executeQuery();

            if(rs.next()){
                Course c=new Course();
                c.id=rs.getString(1);
                c.name=rs.getString(2);
                c.credit=rs.getInt(3);
                c.classHour=rs.getInt(4);
                c.grading=rs.getBoolean(5)?Course.CourseGrading.PASS_OR_FAIL:Course.CourseGrading.HUNDRED_MARK_SCORE;
                return c;
            }

        }catch (SQLException e) {
            e.printStackTrace();
        }finally{
            try {
                if (rs != null)
                    rs.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public List<CourseSectionClass> getCourseSectionClasses(int sectionId) {
        List<CourseSectionClass> cscList=new LinkedList<>();
        ResultSet rs = null;
        try{
            Connection connection = SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmt = connection.prepareStatement("select * from (select * from course_section_class where section_id = ?) x join instructor y on x.instructor_id=y.id;", Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1,sectionId);

            rs=stmt.executeQuery();

            while(rs.next()){
                CourseSectionClass csc=new CourseSectionClass();
                csc.id=rs.getInt(1);//1
                Instructor i=new Instructor();
                i.id=rs.getInt(9);
                i.fullName=rs.getString(12);
                csc.instructor=i;//2
                csc.dayOfWeek=DayOfWeek.of(rs.getInt(4));//3
                Set<Short> weekList=new HashSet<>();
                int wl=rs.getInt(5);
                int index=0;
                while(wl>0){
                    index++;
                    wl/=2;
                    if (wl%2==1){
                        weekList.add((short)index);
                    }
                }
                csc.weekList=weekList;//4
                csc.classBegin=(short)rs.getInt(6);
                csc.classEnd=(short)rs.getInt(7);
                csc.location=rs.getString(8);
                cscList.add(csc);
            }
            return cscList;
        }catch (SQLException e) {
            e.printStackTrace();
        }finally{
            try {
                if (rs != null)
                    rs.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

        return null;
    }

    @Override
    public CourseSection getCourseSectionByClass(int classId) {
        ResultSet rs = null;
        try{
            Connection connection = SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmt = connection.prepareStatement("select * from course_section where id=(select distinct section_id from course_section_class where id=?)", Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1,classId);

            rs=stmt.executeQuery();

            if(rs.next()){
                CourseSection cs=new CourseSection();
                cs.id=rs.getInt(1);
                cs.name=rs.getString(4);
                cs.totalCapacity=rs.getInt(5);
                cs.leftCapacity=rs.getInt(6);
                return cs;
            }

        }catch (SQLException e) {
            e.printStackTrace();
        }finally{
            try {
                if (rs != null)
                    rs.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return null;
    }
    @Override
    public List<Student> getEnrolledStudentsInSemester(String courseId, int semesterId) {
        List<Student> studentList=new LinkedList<>();
        ResultSet rs = null;
        try{
            Connection connection = SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmt = connection.prepareStatement("select d.id,full_name,enrolled_date,major_id , name, d_id, d_name from\n" +
                    "              (((select distinct student_id from course_student_grade a join (select * from course_section where course_id = ? and semester_id = ?) b\n" +
                    "    on a.course_section_id=b.id) c join student on c.student_id=student.id )d join (select major.id ,major.name,d2.id as d_id ,d2.name as d_name from major join department d2 on major.department_id = d2.id)e on d.major_id=e.id);", Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1,courseId);
            stmt.setInt(2,semesterId);

            rs=stmt.executeQuery();

            HashMap<Integer,Major> majors=new HashMap<>();
            HashMap<Integer,Department> departments=new HashMap<>();

            while(rs.next()){
                Student s=new Student();
                s.id=rs.getInt(1);//1
                s.fullName=rs.getString(2);//2
                s.enrolledDate=rs.getDate(3);//3
                if(!majors.containsKey(rs.getInt(4))){
                    Major m=new Major();
                    m.id=rs.getInt(4);
                    m.name=rs.getString(5);
                    if(!departments.containsKey(rs.getInt(7))){
                        Department d=new Department();
                        d.id=rs.getInt(7);
                        d.name=rs.getString(8);
                        m.department=d;
                        departments.put(d.id,d);
                    }else{
                        m.department=departments.get(rs.getInt(7));
                    }
                    majors.put(m.id,m);
                    s.major=m;//4
                }else{
                    s.major=majors.get(rs.getInt(4));//4
                }
                studentList.add(s);
            }
            return studentList;

        }catch (SQLException e) {
            e.printStackTrace();
        }finally{
            try {
                if (rs != null)
                    rs.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return null;
    }

}
