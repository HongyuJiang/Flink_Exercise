根据商品信息和商品点击事件，计算实时点击量TOP N的商品。

### 输入：

ClickEvent：通过命令行工具写入Kakfa，是实时的流数据，数据可能会迟到。包含itemId, userId, timestamp。本次练习可使用 avro-kafka-consumer-console 写入。
ItemData: 商品主数据。定期的更新CSV文件（全量数据）。包含 itemId, description。
输出：itemId, description, count, startTime, endTime

### 其他需求

Kafka中的数据使用avro编码，通过schema registry进行校验；当程序重启时，能从上次断点继续执行（容错）

### 创建数据表

```sql
CREATE TABLE avro_click_4 (
        itemId VARCHAR PRIMARY KEY,
        `description` VARCHAR,
        `count` BIGINT,
        startTime TIMESTAMP,
        endTime TIMESTAMP
);
```
