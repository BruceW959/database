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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ReferenceCourseService implements CourseService {
    public String addPre(Prerequisite p){
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
             PreparedStatement stmt = connection.prepareStatement("INSERT INTO course(courseId, courseCredit, courseHour, courseName, courseDept)" +
                     " VALUES(DEFAULT,?,?,?,?,?)" +
                     "ON conflict(courseId)  DO NOTHING;" , Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, courseId);
            stmt.setString(2, courseName);
            stmt.setInt(3, credit);
            stmt.setInt(4, classHour);
            stmt.setString(5, grading.toString());
            stmt.setString(6, addPre(prerequisite));
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int addCourseSection(String courseId, int semesterId, String sectionName, int totalCapacity) {
        ResultSet rs = null;
        int sectionId=-1;
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement stmt = connection.prepareStatement("insert into course_section (course_id, semester_id, name, total_capacity, left_capacity) " +
                     " VALUES(?,?,?,?,?)" +
                     "ON conflict(courseId)  DO NOTHING;" , Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, courseId);
            stmt.setInt(2, semesterId);
            stmt.setString(3, sectionName);
            stmt.setInt(4, totalCapacity);
            stmt.setInt(5, totalCapacity);
            stmt.execute();

            PreparedStatement stmt2=connection.prepareStatement("select currval('course_section_id_seq')");
            rs = stmt2.executeQuery();
            rs.next();

            sectionId = rs.getInt("currval");
        } catch (SQLException e) {
            e.printStackTrace();
        }catch (Exception e) {
            System.out.println("section_Exception异常↓");
            e.printStackTrace();
            System.out.println("section_Exception异常↑");
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
    public int addCourseSectionClass(int sectionId, int instructorId, DayOfWeek dayOfWeek, List<Short> weekList, short classStart, short classEnd, String location) {
        ResultSet rs = null;
        int sectionClassId=-1;
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement stmt = connection.prepareStatement("course_section_class (section_id, instructor_id, day_of_week, week_list, class_begin, class_end, location) " +
                     " VALUES(?,?,?,?,?,?,?)" +
                     "ON conflict(courseId)  DO NOTHING;" , Statement.RETURN_GENERATED_KEYS)) {
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
            System.out.println("section_Exception异常↓");
            e.printStackTrace();
            System.out.println("section_Exception异常↑");
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
             PreparedStatement stmt = connection.prepareStatement("delete from table course where id = ?", Statement.RETURN_GENERATED_KEYS)) {
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
             PreparedStatement stmt = connection.prepareStatement("delete from table course_section where id = ?", Statement.RETURN_GENERATED_KEYS)) {
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
             PreparedStatement stmt = connection.prepareStatement("delete from table course_section where id = ?", Statement.RETURN_GENERATED_KEYS)) {
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
        } catch (Exception e) {
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
        } catch (Exception e) {
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
        } catch (Exception e) {
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
                csc.id=rs.getInt(1);
                Instructor i=new Instructor();
                i.id=rs.getInt(9);
                i.fullName=rs.getString(12);
                csc.instructor=i;
                csc.dayOfWeek=DayOfWeek.of(rs.getInt(4));
                List<Short> weekList=new LinkedList<>();
                int wl=rs.getInt(5);
                int index=0;

                while(wl>0){
                    index++;
                    wl/=2;
                    if (wl%2==1){
                        weekList.add((short)index);
                    }
                }
                csc.weekList=weekList;

            }

        }catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
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
        return null;
    }

    @Override
    public List<Student> getEnrolledStudentsInSemester(String courseId, int semesterId) {
        return null;
    }


}
