package loginapp;

import dbUtil.dbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginModel {
    Connection connection;

    public LoginModel(){
        try {
            this.connection = dbConnection.getConnection();
        }catch (SQLException ex){
            ex.printStackTrace();
        }
        if(this.connection == null){
            System.exit(1);
        }
    }

    public boolean isDatabaseConnected(){
        return this.connection != null;
    }

    public boolean isLogin(String pass) throws SQLException {

        PreparedStatement pr = null;
        ResultSet rs = null;

        String sql = "SELECT * FROM login where password = ?";
        try {
            pr = this.connection.prepareStatement(sql);
            pr.setString(1, pass);
            rs = pr.executeQuery();

            if(rs.next()){
                return true;
            }
            return false;
        }catch (SQLException ex){
            return false;
        }
        finally {
            {
                pr.close();
                rs.close();
            }
        }
    }
}
