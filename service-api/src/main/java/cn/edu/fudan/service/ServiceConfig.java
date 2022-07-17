package cn.edu.fudan.service;

/**
 * @author fuwuchen
 */
public class ServiceConfig {
    public static final String TABLE_NAME = "services";
    public static final String READ_SIDE_ID = "serviceSummaryOffset";
    public static final String CREATE_TABLE_STATEMENT =
            String.format("CREATE TABLE IF NOT EXISTS %s ( " +
                    "id TEXT, type TEXT, providerId TEXT, area TEXT, cost INT, rating INT, status TEXT," +
                    "PRIMARY KEY (id))", TABLE_NAME);
    public static final String INSERT_STATEMENT =
            String.format("INSERT INTO %s (id, type, providerId, area, cost, rating, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)", TABLE_NAME);
    public static final String UPDATE_STATEMENT =
            String.format("UPDATE %s " +
                    "SET type = ?, providerId = ?, area = ?, cost = ?, rating = ?, status = ?" +
                    "WHERE id = ?", TABLE_NAME);
    public static final String DELETE_STATEMENT =
            String.format("DELETE FROM %s WHERE id = ?", TABLE_NAME);
}
