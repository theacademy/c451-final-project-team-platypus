/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mthree.stocksimulator.dao.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import mthree.stocksimulator.model.User;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author gabri
 */
public class UserMapper implements RowMapper<User> {
    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        
        //uid, userName, accountBal
        User user = new User();
        user.setUid(rs.getInt("uid"));
        user.setUserName(rs.getString("userName"));
        //getBigDecimal preserves database format
        user.setAccountBal(rs.getBigDecimal("accountBal"));
        
        return user;
    }
}
