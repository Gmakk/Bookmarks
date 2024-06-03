package org.example.database;

import org.example.formula.Formula;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class Access {
    
    private final Map<Formula, Connection> connections = new HashMap<Formula, Connection>();


    /**
     * Метод для получения объекта Connection необходимого для подстановки в конкретную закладку
     * @param formula формула, в которой указаны параметры подключения
     * @return Connection соответствующий формуле
     */
    public Connection getConnection(Formula formula) throws SQLException {
        Connection connection;
        //проходимся по существующим Connection
        for(Formula existFormulaConnection : connections.keySet()) {
            //Если находим существующий Connection для этой или аналогичной формулы, возвращаем ее
            if(formula.hasEqualDatabaseConnection(existFormulaConnection))
                return connections.get(existFormulaConnection);
        }
        //если нужного Connection нет, то создаем новый
        connection = DriverManager.getConnection(formula.getUrl(), formula.getUsername(), formula.getPassword());
        connections.put(formula,connection);
        return connection;
    }

    public String getData(Formula formula) throws SQLException {
        //получение connection
        Connection connection = getConnection(formula);
        //подготовка запроса
//        String sqlCommand = "SELECT ? FROM ? WHERE ? = ?";
//        PreparedStatement preparedStatement = connection.prepareStatement(sqlCommand);
//        preparedStatement.setString(1, formula.getColumn());
//        preparedStatement.setString(2, formula.getTable());
//        preparedStatement.setString(3, formula.getPrimaryKey());
//        preparedStatement.setString(4, formula.getPrimaryKeyValue());//TODO: проверить на числах

        String sqlCommand = "SELECT " + formula.getColumn() + " FROM " + formula.getTable() + " WHERE " + formula.getPrimaryKey() + " = " + formula.getPrimaryKeyValue();
        //получение данных из бд
        try{
            //ResultSet resultSet = preparedStatement.executeQuery();
            ResultSet resultSet = connection.createStatement().executeQuery(sqlCommand);
        if(resultSet.next())
            return resultSet.getString(1);
        } catch(Exception ex){
            System.out.println(ex);
        }
        return null;
    }

    public void closeAllConnections() throws SQLException {
        for(Formula formula : connections.keySet()) {
            connections.get(formula).close();
            //TODO: что делать с загруженным драйвером
        }
    }
}
