package streamingdatapipelineexercise.examples.click.v6;

import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import streamingdatapipelineexercise.examples.click.shared.Click;
import streamingdatapipelineexercise.examples.click.shared.Config;
import streamingdatapipelineexercise.examples.click.shared.StreamBuilder;
import java.util.Properties;


public class AvroClick {
    public static void main(String[] args) throws Exception {
        new AvroClick().execute();
    }

    private static SinkFunction<Click> buildDatabaseSink(String jdbcURL, String username, String password, String tableName) {
        return JdbcSink.sink(
                "INSERT INTO " + tableName + " (itemId, description, \"count\", \"startTime\", \"endTime\") values (?, ?, ?, ?, ?)\n" +
                        "ON conflict(itemId, \"startTime\") DO\n" +
                        "UPDATE\n" +
                        "SET \"count\" = ?, \"endTime\" = ?",
                (preparedStatement, click) -> {
                    preparedStatement.setString(1, click.getItemId());
                    preparedStatement.setString(2, click.getDescription());
                    preparedStatement.setLong(3, click.getCount());
                    preparedStatement.setLong(4, click.getStartTime());
                    preparedStatement.setLong(5, click.getEndTime());

                    preparedStatement.setLong(6, click.getCount());
                    preparedStatement.setLong(7, click.getEndTime());
                },
                JdbcExecutionOptions.builder()
                        .withBatchSize(1000)
                        .withBatchIntervalMs(200)
                        .withMaxRetries(5)
                        .build(),
                new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                        .withUrl(jdbcURL)
                        .withDriverName("org.postgresql.Driver")
                        .withUsername(username)
                        .withPassword(password)
                        .build()
        );
    }

    private void execute() throws Exception {
        Properties properties = new Properties();
        String kafkaBoostrapServers = Config.KAFKA_BOOTSTRAP_SERVERS;
        properties.setProperty("bootstrap.servers", kafkaBoostrapServers);
        String groupId = "AvroClickAllTop3";
        properties.setProperty("group.id", groupId);
        String kafkaTopic = "item_v1";

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //env.enableCheckpointing(5000);

        SingleOutputStreamOperator<Click> map = StreamBuilder.getClickStreamOperator(properties, kafkaTopic, env);

        //createTable("avro_click", env);

        map.addSink(buildDatabaseSink(
                Config.JDBC_URL,
                Config.JDBC_USERNAME,
                Config.JDBC_PASSWORD, "avro_click_4"));
        env.execute("AvroClick");

    }

    private void createTable(String tablePath, StreamExecutionEnvironment env){

        String createTableStatement = "CREATE TABLE " + tablePath + " (\n" +
                "  itemId VARCHAR PRIMARY KEY,\n" +
                "  `description` VARCHAR,\n" +
                "  `count` BIGINT,\n" +
                "  startTime TIMESTAMP,\n" +
                "  endTime TIMESTAMP\n" +
                ")";

        String jdbcURL = Config.JDBC_URL;
        String username = Config.JDBC_USERNAME;
        String password = Config.JDBC_PASSWORD;
        String dbTableName = "avro_click_4";

        String statement = createTableStatement +
                " WITH (\n" +
                "   'connector' = 'jdbc',\n" +
                "   'url' = '" + jdbcURL + "',\n" +
                "   'table-name' = '" + dbTableName + "',\n" +
                "   'username' = '" + username + "',\n" +
                "   'password' = '" + password + "'\n" +
                ")";
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);
        tableEnv.executeSql(statement);
    }
}
