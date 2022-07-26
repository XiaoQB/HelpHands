package cn.edu.fudan;

/**
 * @author XiaoQuanbin
 * @date 2022/7/26
 */
public class OrderConfig {
    public static final String TABLE_NAME = "orders";
    public static final String READ_SIDE_ID = "orderSummaryOffset";
    public static final String SELECT_ALL_STATEMENT =
            String.format("SELECT id, type, provider, consumer, cost, start, end, rating, status FROM %s ", TABLE_NAME);
    public static final String CREATE_TABLE_STATEMENT =
            String.format("CREATE TABLE IF NOT EXISTS %s ( " +
                    "id TEXT, type TEXT, provider TEXT, consumer TEXT, cost INT, start INT, end INT, rating INT, status TEXT," +
                    "PRIMARY KEY (id, consumer))", TABLE_NAME);
    public static final String INSERT_STATEMENT =
            String.format("INSERT INTO %s (id, type, provider, consumer, cost, start, end, rating, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", TABLE_NAME);
    public static final String UPDATE_STATEMENT =
            String.format("UPDATE %s " +
                    "SET provider = ?, start = ?, end = ?, cost = ?, rating = ?, status = ? " +
                    "WHERE id = ? and consumer = ?", TABLE_NAME);
    public static final String DELETE_STATEMENT =
            String.format("DELETE FROM %s WHERE id = ?", TABLE_NAME);
}
