package cn.edu.sustech.cs307.service.Reference;

import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.Semester;
import cn.edu.sustech.cs307.exception.EntityNotFoundException;
import cn.edu.sustech.cs307.exception.IntegrityViolationException;
import cn.edu.sustech.cs307.service.SemesterService;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;

public class ReferenceSemesterService implements SemesterService {
    @Override
    public int addSemester(String name, Date begin, Date end) {
        ResultSet rst = null;
        int id = 0;
        try (Connection conn = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement stmt = conn.prepareStatement("insert into semester(id , name , begin_date , end_date)values(default , ? , ? , ? )" +
                     "ON conflict(id)  DO NOTHING " + "RETURNING id");
             ){
            stmt.setString(1, name);
            stmt.setDate(2, begin);
            stmt.setDate(3, end);
            rst = stmt.executeQuery();
            rst.next();
            id = rst.getInt(1);
        } catch (SQLException e) {
            throw new IntegrityViolationException();
        }finally {
            try {
                assert rst != null;
                rst.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return id;
    }

    @Override
    public void removeSemester(int semesterId) {
        try (Connection conn = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement stmt = conn.prepareStatement("delete from semester where id = ?")){
            stmt.setInt(1, semesterId);
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Semester> getAllSemesters() {
        List<Semester> listofsemester = new LinkedList<>();
        try (Connection conn = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement stmt = conn.prepareStatement("select * from semester");
             ResultSet  rst = stmt.executeQuery()){
            while (rst.next()) {
                Semester semester = new Semester();
                semester.id = rst.getInt(1);
                semester.name = rst.getString(2);
                semester.begin = rst.getDate(3);
                semester.end = rst.getDate(4);
                listofsemester.add(semester);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listofsemester;
    }

    @Override
    public Semester getSemester(int semesterId) {
        ResultSet rst = null;
        Semester semester = new Semester();
        try (Connection conn = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement stmt = conn.prepareStatement("select * from semester where id = ?")
             ){
            stmt.setInt(1, semesterId);
            rst = stmt.executeQuery();
            if (rst.next()) {
                semester.id = rst.getInt(1);
                semester.name = rst.getString(2);
                semester.begin = rst.getDate(3);
                semester.end = rst.getDate(4);
            } else {
                throw new EntityNotFoundException();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            try{
                assert rst != null;
                rst.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return semester;
    }
}
