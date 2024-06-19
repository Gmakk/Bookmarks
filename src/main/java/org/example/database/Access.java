package org.example.database;

import org.example.formula.Formula;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class Access {
    
    private final Map<Formula, Connection> connections = new HashMap<Formula, Connection>();


    /**
     * Метод для получения объекта Connection по параметрам из формулы
     * @param formula формула, в которой указаны параметры подключения
     * @return Connection соответствующий формуле
     * @throws SQLException
     */
    public Connection getConnection(Formula formula) throws SQLException {
        //проходимся по существующим Connection
        for(Formula existFormulaConnection : connections.keySet()) {
            //Если находим существующий Connection для этой или аналогичной формулы, возвращаем ее
            if(formula.hasEqualDatabaseConnection(existFormulaConnection))
                return connections.get(existFormulaConnection);
        }
        //если нужного Connection нет, то создаем новый
        Connection connection;
        if(formula.getUsername().isEmpty() || formula.getPassword().isEmpty()){
            connection = DriverManager.getConnection(formula.getUrl());
        }else
            connection = DriverManager.getConnection(formula.getUrl(), formula.getUsername(), formula.getPassword());
        connections.put(formula,connection);
        return connection;
    }

    /**
     * Метод для получения данных по формуле
     * @param formula формула с параметрами подключения и источником информации
     * @return строка, получаемого в результате запроса к бд
     * @throws SQLException
     */
    public String getData(Formula formula) throws SQLException {
        //получение connection
        Connection connection = getConnection(formula);
        //подготовка запроса
        String sqlCommand = "SELECT " + formula.getColumn() + " FROM " + formula.getTable() + " WHERE " + formula.getPrimaryKey() + " = " + formula.getPrimaryKeyValue();
        //получение данных из бд
        ResultSet resultSet = connection.createStatement().executeQuery(sqlCommand);
        if(resultSet.next())
            return resultSet.getString(1);
        throw new SQLException("No data found");
    }

    public void closeAllConnections() throws SQLException {
        for(Formula formula : connections.keySet()) {
            connections.get(formula).close();
        }
    }
}
