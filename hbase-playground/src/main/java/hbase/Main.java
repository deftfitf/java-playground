package hbase;

import com.google.protobuf.ServiceException;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.NamespaceDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.exceptions.DeserializationException;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

public final class Main {

    public static void main(String[] args) {
        final var config = HBaseConfiguration.create();
        config.set("hbase.zookeeper.quorum", "127.0.0.1");
        config.set("hbase.zookeeper.property.clientPort","2181");

        try (final var conn = ConnectionFactory.createConnection(config)) {
            HBaseAdmin.checkHBaseAvailable(config);
            final var admin = conn.getAdmin();
            admin.createNamespace(NamespaceDescriptor.create("ns").build());
            admin.createTable(HTableDescriptor.parseFrom(Bytes.toBytes("tbl")));

            final var put = new Put(Bytes.toBytes("row1"));
            put.add(Bytes.toBytes("fam"), Bytes.toBytes("col1"), Bytes.toBytes("value1"));
            put.add(Bytes.toBytes("fam"), Bytes.toBytes("col2"), 100L, Bytes.toBytes("value1"));

            final var tbl = conn.getTable(TableName.valueOf("tbl"));
            tbl.put(put);

            final var scan = new Scan();
            final var bldr = new StringBuilder();
            try (final var sc = tbl.getScanner(scan)) {
                for (final var row: sc) {
                    bldr.append(Bytes.toString(row.getRow())).append("\n");
                }
            }
            tbl.close();

            System.out.println(bldr.toString());
        } catch (IOException | DeserializationException | ServiceException e) {
            e.printStackTrace();
        }
    }

}
