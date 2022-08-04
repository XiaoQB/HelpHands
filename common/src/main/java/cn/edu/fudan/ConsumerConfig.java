package cn.edu.fudan;

/**
 * @author XiaoQuanbin
 * @date 2022/7/26
 */
public class ConsumerConfig {
    public static final String TABLE_NAME = "consumer";
    public static final String READ_SIDE_ID = "consumerSummaryOffset";
    public static final String SELECT_ALL_STATEMENT =
            String.format("SELECT id, name, address, mobile, email, geo FROM %s ", TABLE_NAME);
    public static final String SELECT_ONE_STATEMENT =
            String.format("SELECT id, name, address, mobile, email, geo FROM %s WHERE id = ?", TABLE_NAME);
    public static final String CREATE_TABLE_STATEMENT =
            String.format("CREATE TABLE IF NOT EXISTS %s ( " +
                    "id TEXT, name TEXT, address TEXT, mobile TEXT, email TEXT, geo TEXT," +
                    "PRIMARY KEY (id))", TABLE_NAME);
    public static final String INSERT_STATEMENT =
            String.format("INSERT INTO %s (id, name, address, mobile, email, geo) " +
                    "VALUES (?, ?, ?, ?, ?, ?)", TABLE_NAME);
    public static final String UPDATE_STATEMENT =
            String.format("UPDATE %s " +
                    "SET name = ?, address= ?, mobile = ?, email = ?, geo= ?" +
                    "WHERE id = ?", TABLE_NAME);
    public static final String DELETE_STATEMENT =
            String.format("DELETE FROM %s WHERE id = ?", TABLE_NAME);
}
