package db.migration;

import java.sql.Connection;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V7__prevent_overlapping_class_assignments extends BaseJavaMigration {

	@Override
	public void migrate(Context context) throws Exception {
		Connection connection = context.getConnection();
		if (!connection.getMetaData().getDatabaseProductName().equalsIgnoreCase("PostgreSQL")) {
			return;
		}
		try (var statement = connection.createStatement()) {
			statement.execute("CREATE EXTENSION IF NOT EXISTS btree_gist");
			statement.execute("""
					ALTER TABLE student_class_assignments
					ADD CONSTRAINT ex_class_assignments_no_overlap
					EXCLUDE USING gist (
					    student_id WITH =,
					    daterange(start_date, COALESCE(end_date + 1, 'infinity'::date), '[)') WITH &&
					)
					""");
		}
	}
}
