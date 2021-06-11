package cn.edu.sustech.cs307.service.Reference;

import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.Department;
import cn.edu.sustech.cs307.exception.EntityNotFoundException;
import cn.edu.sustech.cs307.exception.IntegrityViolationException;
import cn.edu.sustech.cs307.service.DepartmentService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class ReferenceDepartmentService implements DepartmentService {
    @Override
    public int addDepartment(String name) {
        ResultSet rst = null;
        int num = 0;
        try(Connection conn = SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO department(id, name) VALUES(DEFAULT,?)" +
                    "RETURNING id")){
            stmt.setString(1, name);
            rst = stmt.executeQuery();
            rst.next();
            num = rst.getInt(1);
        }catch (SQLException e){
            throw new IntegrityViolationException();
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
    public void removeDepartment(int departmentId) {
        try (Connection conn = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement stmt = conn.prepareStatement("delete from department where id = ?")){
            stmt.setInt(1, departmentId);
            stmt.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    @Override
    public List<Department> getAllDepartments() {
        List<Department> dauser = new LinkedList<>();
        try (Connection conn = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement stmt = conn.prepareStatement("select * from department");
             ResultSet rst = stmt.executeQuery()){
            while (rst.next()) {
                int id = rst.getInt(1);
                String name = rst.getString(2);
                Department department = new Department();
                department.id = id;
                department.name = name;
                dauser.add(department);
            }
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        return dauser;
    }

    @Override
    public Department getDepartment(int departmentId) {
        ResultSet rst = null;
        Department department = new Department();
        try (Connection conn = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement stmt = conn.prepareStatement("select * from department where id = ?");){
            stmt.setInt(1,departmentId);
            rst = stmt.executeQuery();
            rst.next();
            int id = rst.getInt(1);
            String name = rst.getString(2);
            department.id = id;
            department.name = name;
        }
        catch (SQLException e){
            throw new EntityNotFoundException();
        }finally {
            try{
                assert rst != null;
                rst.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return department;
    }
}
