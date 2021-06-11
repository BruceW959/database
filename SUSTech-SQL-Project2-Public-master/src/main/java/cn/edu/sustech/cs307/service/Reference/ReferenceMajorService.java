package cn.edu.sustech.cs307.service.Reference;

import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.Department;
import cn.edu.sustech.cs307.dto.Major;
import cn.edu.sustech.cs307.exception.EntityNotFoundException;
import cn.edu.sustech.cs307.service.MajorService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class ReferenceMajorService implements MajorService {
    @Override
    public int addMajor(String name, int departmentId) {
        ResultSet rst = null;
        int num = 0;
        try(Connection conn = SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO major(id, name, department_id) VALUES(DEFAULT,?,?)" +
                    "ON conflict(id)  DO NOTHING " +
                    "RETURNING id");
            ){
            stmt.setString(1, name);
            stmt.setInt(2, departmentId);
            rst = stmt.executeQuery();
            rst.next();
            num = rst.getInt(1);
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
        return num;
    }

    @Override
    public void removeMajor(int majorId) {
        try(Connection conn = SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmt = conn.prepareStatement("delete from major where id = ?")){

            stmt.setInt(1,majorId);
            stmt.execute();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    @Override
    public List<Major> getAllMajors() {
        ResultSet rst = null;
        ResultSet rst1 = null;
        List<Major> listmajor = new LinkedList<>();
        try(Connection conn = SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmt = conn.prepareStatement("select * from major");
            ){
            rst = stmt.executeQuery();
            while (rst.next()) {
                int id = rst.getInt(1);
                String name = rst.getString(2);
                int department_id = rst.getInt(3);
                String sql1 = "select * from department where id = ?";
                PreparedStatement stmt1 = conn.prepareStatement(sql1);
                stmt1.setInt(1,department_id);
                rst1 = stmt1.executeQuery();
                rst1.next();
                String department_name = rst1.getString(2);
                Department department = new Department();
                department.name = department_name;
                department.id = department_id;
                Major major = new Major();
                major.id = id;
                major.name = name;
                major.department = department;
                listmajor.add(major);
            }
        }catch(SQLException e){
            e.printStackTrace();
        }finally {
            try{
                assert rst != null;
                rst.close();
                assert rst1 != null;
                rst1.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return listmajor;
    }

    @Override
    public Major getMajor(int majorId) {
        Major major = new Major();
        ResultSet rst = null;
        ResultSet rst1 = null;
        try(Connection conn = SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmt = conn.prepareStatement("select * from major where id = ?");
            ){
            stmt.setInt(1, majorId);
            rst = stmt.executeQuery();
            rst.next();
            int id = rst.getInt(1);
            String name = rst.getString(2);
            int department_id = rst.getInt(3);
            String sql1 = "select * from department where id = ?";
            PreparedStatement stmt1 = conn.prepareStatement(sql1);
            stmt1.setInt(1,department_id);
            rst1 = stmt1.executeQuery();
            rst1.next();
            String department_name = rst1.getString(2);
            Department department = new Department();
            department.name = department_name;
            department.id = department_id;
            major.id = id;
            major.name = name;
            major.department = department;
        }catch(SQLException e){
            throw new EntityNotFoundException();
        }finally {
            try{
                assert rst != null;
                rst.close();
                assert rst1 != null;
                rst1.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return major;
    }

    @Override
    public void addMajorCompulsoryCourse(int majorId, String courseId) {
        try(Connection conn = SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO course_major(major_id, course_id, is_compulsory) VALUES(?,?,?)")){
            stmt.setInt(1,majorId);
            stmt.setString(2,courseId);
            stmt.setBoolean(3,true);
            stmt.execute();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    @Override
    public void addMajorElectiveCourse(int majorId, String courseId) {
        try(Connection conn = SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO course_major(major_id, course_id, is_compulsory) VALUES(?,?,?)")){
            stmt.setInt(1,majorId);
            stmt.setString(2,courseId);
            stmt.setBoolean(3,false);
            stmt.execute();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }
}
