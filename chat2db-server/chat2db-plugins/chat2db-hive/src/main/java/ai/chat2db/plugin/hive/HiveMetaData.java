package ai.chat2db.plugin.hive;

import ai.chat2db.spi.MetaData;
import ai.chat2db.spi.jdbc.DefaultMetaService;
import ai.chat2db.spi.model.Database;
import ai.chat2db.spi.model.Schema;
import ai.chat2db.spi.sql.SQLExecutor;
import jakarta.validation.constraints.NotEmpty;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class HiveMetaData extends DefaultMetaService implements MetaData {

    @Override
    public List<Database> databases(Connection connection) {
        List<Database> databases = new ArrayList<>();
        return SQLExecutor.getInstance().execute(connection,"show databases", resultSet -> {
            try {
                if (resultSet.next()) {
                    String databaseName = resultSet.getString("database_name");
                    Database database = new Database();
                    database.setName(databaseName);
                    databases.add(database);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return databases;
        });
    }

    @Override
    public List<Schema> schemas(Connection connection, String databaseName) {
        List<Schema> schemas = new ArrayList<>();
        return schemas;
    }

    @Override
    public String tableDDL(Connection connection, @NotEmpty String databaseName, String schemaName,
                           @NotEmpty String tableName) {
        String sql = "SHOW CREATE TABLE " + format(databaseName) + "."
                + format(tableName);
        return SQLExecutor.getInstance().execute(connection, sql, resultSet -> {
            StringBuilder sb = new StringBuilder();
            while (resultSet.next()) {
                // 拼接建表语句
                sb.append(resultSet.getString("createtab_stmt"));
                sb.append("\r\n");
            }
            if (sb.length() > 0) {
                sb = sb.delete(sb.length() - 2, sb.length());
                sb.append(";");
                return sb.toString();
            }
            return null;
        });
    }

    public static String format(String name) {
        return "`" + name + "`";
    }
}

