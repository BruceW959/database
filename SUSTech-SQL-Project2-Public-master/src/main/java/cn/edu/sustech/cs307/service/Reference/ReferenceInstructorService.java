package cn.edu.sustech.cs307.service.Reference;

import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.CourseSection;
import cn.edu.sustech.cs307.service.InstructorService;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;

public class ReferenceInstructorService implements InstructorService {

    @Override
    public void addInstructor(int userId, String firstName, String lastName) {

        try(Connection conn = SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO instructor(id, first_name, last_name, full_name) VALUES(?,?,?,?)" +
                    "ON conflict(id)  DO NOTHING;")){
            String fullname = firstName + " " + lastName;
            stmt.setInt(1, userId);
            stmt.setString(2, firstName);
            stmt.setString(3, lastName);
            stmt.setString(4, fullname);
            stmt.execute();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    @Override
    public List<CourseSection> getInstructedCourseSections(int instructorId, int semesterId) {
        ResultSet rst = null;
        List<CourseSection> courseSections = new LinkedList<>();
        try(Connection conn = SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmt = conn.prepareStatement("select getInstructedCourseSections(?,?)")){
            stmt.setInt(1,instructorId);
            stmt.setInt(2,semesterId);
            rst = stmt.executeQuery();
            rst.next();
            int id = rst.getInt(1);
            String name = rst.getString(2);
            int total_capacity = rst.getInt(3);
            int left_capacity = rst.getInt(4);
            CourseSection courseSection = new CourseSection();
            courseSection.id = id;
            courseSection.name = name;
            courseSection.totalCapacity = total_capacity;
            courseSection.leftCapacity = left_capacity;
            courseSections.add(courseSection);
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
        return courseSections;
    }
}
