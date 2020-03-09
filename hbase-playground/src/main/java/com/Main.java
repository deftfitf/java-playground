package com;

import java.io.IOException;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import com.google.protobuf.ServiceException;

public class Main {

    public static void main(String[] args) {
        final var config = HBaseConfiguration.create();
        config.set("hbase.zookeeper.quorum", "localhost");
        config.set("hbase.zookeeper.property.clientPort","2181");

        try (final var conn = ConnectionFactory.createConnection(config)) {
            HBaseAdmin.checkHBaseAvailable(config);
            final var sourceTableName = Bytes.toBytes("ns:ag");
            final var sourceTable = conn.getTable(TableName.valueOf(sourceTableName));

            final var audienceGroup = Bytes.toBytes("aid");
            final var aidFamily = Bytes.toBytes("f");
            final var agCountFamily = Bytes.toBytes("c");
            final var emptyValue = Bytes.toBytes("e");

            final var put = new Put(audienceGroup);
            put.addColumn(aidFamily, Bytes.toBytes("cellv"), emptyValue);

            final var result =
                    sourceTable.checkAndPut(audienceGroup, aidFamily, Bytes.toBytes("cellv"), null, put);
            System.out.println(result);

            sourceTable.close();
        } catch (IOException | ServiceException e) {
            System.out.println(e);
        }
    }

}
