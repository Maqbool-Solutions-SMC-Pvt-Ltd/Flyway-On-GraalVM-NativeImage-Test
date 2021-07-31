package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V1_0_2__Insert_Data extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        for (int i = 0; i < 5; i++) {
            context.getConnection().createStatement().executeUpdate(
                    "INSERT INTO tbl_message VALUES NOW(), 'Admin" + i + "'"
            );
        }
    }

}
