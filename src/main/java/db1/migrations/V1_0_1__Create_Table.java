package db1.migrations;

import java.sql.PreparedStatement;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V1_0_1__Create_Table extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        PreparedStatement ps = context.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS tbl_message (msg VARCHAR(100), sendby VARCHAR(100))");
        ps.executeUpdate();
    }

}
