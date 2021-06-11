package cn.edu.sustech.cs307.service.Reference;
import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.*;
import cn.edu.sustech.cs307.dto.grade.Grade;
import cn.edu.sustech.cs307.dto.grade.HundredMarkGrade;
import cn.edu.sustech.cs307.dto.grade.PassOrFailGrade;
import cn.edu.sustech.cs307.exception.IntegrityViolationException;
import cn.edu.sustech.cs307.service.StudentService;

import javax.annotation.Nullable;
import java.sql.*;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;


public class ReferenceStudentService implements StudentService {

    @Override
    public void addStudent(int userId, int majorId, String firstName, String lastName, Date enrolledDate) {
        try(Connection conn = SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO student(id, first_name, last_name, full_name, enrolled_date, major_id ) VALUES(?,?,?,?,?,?)" +
                    "ON conflict(id)  DO NOTHING;")){
            String full_name = firstName + " " + lastName;
            stmt.setInt(1, userId);
            stmt.setString(2, firstName);
            stmt.setString(3, lastName);
            stmt.setString(4, full_name);
            stmt.setDate(5, enrolledDate);
            stmt.setInt(6, majorId);
            stmt.execute();
        }catch (SQLException e){
            throw new IntegrityViolationException();
        }
    }

    @Override
    public List<CourseSearchEntry> searchCourse(int studentId, int semesterId, @Nullable String searchCid,
                                                @Nullable String searchName, @Nullable String searchInstructor,
                                                @Nullable DayOfWeek searchDayOfWeek, @Nullable Short searchClassTime,
                                                @Nullable List<String> searchClassLocations, CourseType searchCourseType,
                                                boolean ignoreFull, boolean ignoreConflict, boolean ignorePassed,
                                                boolean ignoreMissingPrerequisites, int pageSize, int pageIndex) {
        return null;
    }

    @Override
    public EnrollResult enrollCourse(int studentId, int sectionId) {
        return null;
    }

    @Override
    public void dropCourse(int studentId, int sectionId) throws IllegalStateException {
        ResultSet rst = null;
        try (Connection conn = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement stmt = conn.prepareStatement("select drop_course(?, ?)")){
            stmt.setInt(1,studentId);
            stmt.setInt(2,sectionId);
            rst = stmt.executeQuery();
            if(rst.next()){
                if(!rst.getBoolean(1)){
                    throw new IllegalStateException();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            try {
                assert rst != null;
                rst.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void addEnrolledCourseWithGrade(int studentId, int sectionId, @Nullable Grade grade) {
        try(Connection conn = SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmt = conn.prepareStatement("insert into course_student_grade(student_id , course_section_id , grade)value(?,?,?)"
                    +"ON conflict(student_id , course_section_id , grade)  DO NOTHING;")
        ){
            Grade.Cases<Short> cases = new Grade.Cases<Short>() {
                @Override
                public Short match(PassOrFailGrade self) {
                    if(self == PassOrFailGrade.PASS){
                        return -1;
                    }else {
                        return -2;
                    }
                }
                @Override
                public Short match(HundredMarkGrade self) {
                    return self.mark;
                }
            };
            if()
            stmt.setInt(1, studentId);
            stmt.setInt(2, sectionId);
            stmt.setInt(3, grade.when(cases));
            stmt.execute();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    @Override
    public void setEnrolledCourseGrade(int studentId, int sectionId, Grade grade) {
        try(Connection conn = SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmt = conn.prepareStatement("update course_student_grade set grade = ? where student_id = ? and course_section_id = ?")
            ){
            Grade.Cases<Short> cases = new Grade.Cases<Short>() {
                @Override
                public Short match(PassOrFailGrade self) {
                    if(self == PassOrFailGrade.PASS){
                        return -1;
                    }else {
                        return -2;
                    }
                }
                @Override
                public Short match(HundredMarkGrade self) {
                    return self.mark;
                }
            };
            stmt.setInt(2, studentId);
            stmt.setInt(3, sectionId);
            stmt.setInt(1, grade.when(cases));
            stmt.execute();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    @Override
    public Map<Course, Grade> getEnrolledCoursesAndGrades(int studentId,
                                                          @Nullable Integer semesterId) {

        return null;
    }

    @Override
    public CourseTable getCourseTable(int studentId, Date date) {

        return null;
    }

    @Override
    public boolean passedPrerequisitesForCourse(int studentId, String courseId) {
        return false;
    }

    @Override
    public Major getStudentMajor(int studentId) {
        Major major = new Major();
        ResultSet rst1 = null;
        ResultSet rst2 = null;
        ResultSet rst3 = null;
        try(Connection conn = SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmt1 = conn.prepareStatement("select major_id from student where id = ?");
            PreparedStatement stmt2 = conn.prepareStatement("select * from major where id = ?");
            PreparedStatement stmt3 = conn.prepareStatement("select * from department where id = ?");
            ){
            stmt1.setInt(1,studentId);
            rst1 = stmt1.executeQuery();
            rst1.next();
            int id1 = rst1.getInt(1);
            stmt2.setInt(1,id1);
            rst2 = stmt2.executeQuery();
            rst2.next();
            int id2 = rst2.getInt(1);
            String name = rst2.getString(2);
            int department_id = rst2.getInt(3);
            stmt3.setInt(1,department_id);
            rst3 = stmt3.executeQuery();
            rst3.next();
            String department_name = rst3.getString(2);
            Department department = new Department();
            department.id = department_id;
            department.name = department_name;
            major.id = id2;
            major.name = name;
            major.department = department;
        }catch (SQLException e){
            e.printStackTrace();
        }finally {
            try{
                assert rst1 != null;
                rst1.close();
                assert rst2 != null;
                rst2.close();
                assert rst3 != null;
                rst3.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return major;
    }
}
