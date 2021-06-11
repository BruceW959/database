package cn.edu.sustech.cs307.service.Reference;

import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.*;
import cn.edu.sustech.cs307.service.UserService;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;


public class ReferenceUserServices implements UserService {

    @Override
    public void removeUser(int userId) {
        try (Connection conn = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement stmt = conn.prepareStatement("delete_user(?)")){
            stmt.setInt(1, userId);
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    @Override
    public List<User> getAllUsers(){
        List<User> users = new LinkedList<>();
        try (Connection conn = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement stmt = conn.prepareStatement("select * from getusers() as ( is_student bool, id int , full_name varchar , enroll_date date , major_id int , major_name varchar , department_id int , department_name varchar )");
             ResultSet rst = stmt.executeQuery()){
            while(rst.next()){
                if(rst.getBoolean(1)){
                    Student user = new Student();
                    int id = rst.getInt(2);
                    String full_name = rst.getString(3);
                    Date enroll_date = rst.getDate(4);
                    int major_id = rst.getInt(5);
                    String major_name = rst.getString(6);
                    int department_id = rst.getInt(7);
                    String department_name = rst.getString(8);
                    user.id = id;
                    user.fullName = full_name;
                    user.enrolledDate = enroll_date;
                    Major major = new Major();
                    major.name = major_name;
                    major.id = major_id;
                    Department department = new Department();
                    department.id = department_id;
                    department.name = department_name;
                    major.department = department;
                    user.major = major;
                    users.add(user);
                }else{
                    Instructor user = new Instructor();
                    int id = rst.getInt(2);
                    String full_name = rst.getString(3);
                    user.id = id;
                    user.fullName = full_name;
                    users.add(user);
                }
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return users;
    }

    @Override
    public User getUser(int userId){
        User user = null;
        ResultSet rst = null;
        try (Connection conn = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement stmt = conn.prepareStatement("select * from searchusers(?) as (is_student bool , id int, fullname varchar , enroll_date date , major_id int , major_name int , department_id int , department_name varchar)"))
             {
                 stmt.setInt(1,userId);
                 rst = stmt.executeQuery();
                 while(rst.next()){
                     if(rst.getBoolean(1)){
                         Student user_S = new Student();
                         int id = rst.getInt(2);
                         String full_name = rst.getString(3);
                         Date enroll_date = rst.getDate(4);
                         int major_id = rst.getInt(5);
                         String major_name = rst.getString(6);
                         int department_id = rst.getInt(7);
                         String department_name = rst.getString(8);
                         user_S.id = id;
                         user_S.fullName = full_name;
                         user_S.enrolledDate = enroll_date;
                         Major major = new Major();
                         major.name = major_name;
                         major.id = major_id;
                         Department department = new Department();
                         department.id = department_id;
                         department.name = department_name;
                         major.department = department;
                         user_S.major = major;
                         user = user_S;
                     }else{
                         Instructor user_i = new Instructor();
                         int id = rst.getInt(2);
                         String full_name = rst.getString(3);
                         user_i.id = id;
                         user_i.fullName = full_name;
                         user = user_i;
                     }
                 }

        }catch(SQLException e){
            e.printStackTrace();
        }finally {
            try{
                assert rst != null;
                rst.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return user;
    }
}
