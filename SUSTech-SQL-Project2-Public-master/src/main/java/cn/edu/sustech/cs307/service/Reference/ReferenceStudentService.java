package cn.edu.sustech.cs307.service.Reference;
import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.*;
import cn.edu.sustech.cs307.dto.grade.Grade;
import cn.edu.sustech.cs307.dto.grade.HundredMarkGrade;
import cn.edu.sustech.cs307.dto.grade.PassOrFailGrade;
import cn.edu.sustech.cs307.exception.EntityNotFoundException;
import cn.edu.sustech.cs307.exception.IntegrityViolationException;
import cn.edu.sustech.cs307.service.StudentService;

import javax.annotation.Nullable;
import java.sql.*;
import java.sql.Date;
import java.time.DayOfWeek;
import java.util.*;


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
        ResultSet rst ;
        try (Connection conn = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement stmt = conn.prepareStatement("select enrollcourse(?, ?)")){
            stmt.setInt(1,studentId);
            stmt.setInt(2,sectionId);
            rst = stmt.executeQuery();
            while(rst.next()){
                int judge = rst.getInt(1);
                switch (judge) {
                    case 0:
                        return EnrollResult.UNKNOWN_ERROR;
                    case 1:
                        return EnrollResult.COURSE_NOT_FOUND;
                    case 2:
                        return EnrollResult.COURSE_IS_FULL;
                    case 3:
                        return EnrollResult.ALREADY_ENROLLED;
                    case 4:
                        return EnrollResult.ALREADY_PASSED;
                    case 6:
                        return EnrollResult.COURSE_CONFLICT_FOUND;
                    default:

                        return EnrollResult.SUCCESS;
                }
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
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
        ResultSet rst = null;
        try(Connection conn = SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmt = conn.prepareStatement("select addEnrolledCourseWithGrade(?, ? ,?)")
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
            stmt.setInt(1, studentId);
            stmt.setInt(2, sectionId);
            stmt.setInt(3, grade.when(cases));
            rst = stmt.executeQuery();
            if(rst.next()){
                if(!rst.getBoolean(1)){
                    throw new IntegrityViolationException();
                }
            }
        }catch (SQLException e){
            e.printStackTrace();
        }finally {
            try{
                assert rst != null;
                rst.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    @Override
    public void setEnrolledCourseGrade(int studentId, int sectionId, Grade grade) {
        ResultSet rst = null;
        try(Connection conn = SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmt = conn.prepareStatement("select setEnrolledCourseWithGrade(?, ? ,?)")
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
            rst = stmt.executeQuery();
            if(rst.next()){
                if(!rst.getBoolean(1)){
                    throw new IntegrityViolationException();
                }
            }
        }catch (SQLException e){
            e.printStackTrace();
        }finally {
            try{
                assert rst != null;
                rst.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    @Override
    public Map<Course, Grade> getEnrolledCoursesAndGrades(int studentId,
                                                          @Nullable Integer semesterId) {
        Map<Course, Grade> map = new HashMap<>();
        ResultSet rst = null;
        PreparedStatement stmt = null;
        try(Connection conn = SQLDataSource.getInstance().getSQLConnection()){
            if(semesterId != null){
                stmt = conn.prepareStatement("select * from SearchCourse(? , ?) as(grade int , course_id varchar ,course_name varchar ,course_credit int,course_hour int,is_pf_grading boolean)");
                stmt.setInt(1,studentId);
                stmt.setInt(2,semesterId);
                rst = stmt.executeQuery();
                while(rst.next()){
                    Course course = new Course();
                    Grade course_grade;
                    int grade = rst.getInt(1);
                    course.id = rst.getString(2);
                    course.name = rst.getString(3);
                    course.credit = rst.getInt(4);
                    course.classHour = rst.getInt(5);
                    course.grading = rst.getBoolean(6)? Course.CourseGrading.PASS_OR_FAIL : Course.CourseGrading.HUNDRED_MARK_SCORE;
                    if (rst.wasNull()) {
                        throw new EntityNotFoundException();
                    }
                    switch(grade){
                        case -1:
                            course_grade = PassOrFailGrade.PASS;
                            break;
                        case -2:
                            course_grade = PassOrFailGrade.FAIL;
                            break;
                        default:
                            course_grade = new HundredMarkGrade((short) grade);
                            break;
                    }
                    map.put(course , course_grade);
                }


            }else{
                stmt = conn.prepareStatement("select * from SearchCoursenoSem(?) as (grade int , course_id varchar ,course_name varchar ,course_credit int,course_hour int,is_pf_grading boolean)");
                stmt.setInt(1,studentId);
                rst = stmt.executeQuery();
                while(rst.next()){
                    Course course = new Course();
                    Grade course_grade;
                    int grade = rst.getInt(1);
                    course.id = rst.getString(2);
                    course.name = rst.getString(3);
                    course.credit = rst.getInt(4);
                    course.classHour = rst.getInt(5);
                    course.grading = rst.getBoolean(6)? Course.CourseGrading.PASS_OR_FAIL : Course.CourseGrading.HUNDRED_MARK_SCORE;
                    if (rst.wasNull()) {
                        throw new EntityNotFoundException();
                    }
                    switch(grade){
                        case -1:
                            course_grade = PassOrFailGrade.PASS;
                            break;
                        case -2:
                            course_grade = PassOrFailGrade.FAIL;
                            break;
                        default:
                            course_grade = new HundredMarkGrade((short) grade);
                            break;
                    }
                    map.put(course , course_grade);
                }
            }
        }catch (SQLException e){
            e.printStackTrace();
        }finally {
            try{
                assert rst != null;
                rst.close();
                stmt.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return map;
    }

    @Override
    public CourseTable getCourseTable(int studentId, Date date) {
        ResultSet rst = null;
        CourseTable courseTable = new CourseTable();
        courseTable.table = new HashMap<>();
        courseTable.table.put(DayOfWeek.MONDAY, new HashSet<>());
        courseTable.table.put(DayOfWeek.TUESDAY, new HashSet<>());
        courseTable.table.put(DayOfWeek.WEDNESDAY, new HashSet<>());
        courseTable.table.put(DayOfWeek.THURSDAY, new HashSet<>());
        courseTable.table.put(DayOfWeek.FRIDAY, new HashSet<>());
        courseTable.table.put(DayOfWeek.SATURDAY, new HashSet<>());
        courseTable.table.put(DayOfWeek.SUNDAY, new HashSet<>());
        Instructor instructor = new Instructor();
        CourseTable.CourseTableEntry courseTableEntry = new CourseTable.CourseTableEntry();
        try(Connection conn = SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmt = conn.prepareStatement("select * from getCourseTable(? , ?) as (full_name varchar ,class_begin integer ,class_end integer ,location varchar ,instructor_id int ,instructor_name varchar ,day_of_week integer )")
        ){
            stmt.setInt(1,studentId);
            stmt.setDate(2,date);
            rst = stmt.executeQuery();
            while(rst.next()){
                courseTableEntry = new CourseTable.CourseTableEntry();
                instructor = new Instructor();
                courseTableEntry.courseFullName = rst.getString(1);;
                courseTableEntry.classBegin = rst.getShort(2);
                courseTableEntry.classEnd = rst.getShort(3);
                courseTableEntry.location = rst.getString(4);
                instructor.id = rst.getInt(5);
                instructor.fullName = rst.getString(6);
                courseTableEntry.instructor = instructor;
                int day_of_week = rst.getInt(7);
                courseTable.table.get(DayOfWeek.of(day_of_week)).add(courseTableEntry);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }finally {
            try{
                assert rst != null;
                rst.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return courseTable;
    }

    @Override
    public boolean passedPrerequisitesForCourse(int studentId, String courseId) {
        ResultSet rst;
        boolean judge = false;
        try(Connection conn = SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmt1 = conn.prepareStatement("select * from turn_prerequisite(? , ?) as (is_exist bool , section_id int)");
            ){
            String[] Prerequisites = courseId.split("\\|");
            Stack<Boolean> pass = new Stack<>();
            for (String prerequisite : Prerequisites) {
                if (!prerequisite.equals("AND") && !prerequisite.equals("OR")) {
                    stmt1.setInt(1, studentId);
                    stmt1.setString(2, prerequisite);
                    rst = stmt1.executeQuery();
                    if (!rst.getBoolean(1)) {
                        throw new IntegrityViolationException();
                    } else {
                        int grade = rst.getInt(2);
                        if (grade >= 60 || grade == -1) {
                            pass.add(true);
                        } else {
                            pass.add(false);
                        }
                    }
                } else if (prerequisite.equals("AND")) {
                    boolean b1 = pass.pop();
                    boolean b2 = pass.pop();
                    boolean b3 = b1 & b2;
                    pass.add(b3);
                } else {
                    boolean b1 = pass.pop();
                    boolean b2 = pass.pop();
                    boolean b3 = b1 | b2;
                    pass.add(b3);
                }
            }
            judge = pass.pop();
        }catch (SQLException e){
            e.printStackTrace();
        }
        return judge;
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
