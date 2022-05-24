package cn.edu.fudan.provider;

/**
 * @author fuwuchen
 * @date 2022/5/24 16:33
 */
public class ProviderConfig {
    public static final String TABLE_NAME = "providers";
    public static final String READ_SIDE_ID = "providerSummaryOffset";
    public static final String CREATE_TABLE_STATEMENT =
            String.format("CREATE TABLE IF NOT EXISTS %s ( " +
                    "id TEXT, name TEXT, mobile TEXT, since BIGINT, rating FLOAT," +
                    "PRIMARY KEY (id))", TABLE_NAME);
    public static final String INSERT_STATEMENT =
            String.format("INSERT INTO %s (id, name, mobile, since, rating) " +
                    "VALUES (?, ?, ?, ?, ?)", TABLE_NAME);
    public static final String UPDATE_STATEMENT =
            String.format("UPDATE %s " +
                    "SET name = ?, mobile = ?, since = ?, rating = ?" +
                    "WHERE id = ?", TABLE_NAME);
    public static final String DELETE_STATEMENT =
            String.format("DELETE FROM %s WHERE id = ?", TABLE_NAME);
}
