package db.migration;

import com.app.infrastructure.security.hasher.SpringBcryptHasher;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import java.sql.PreparedStatement;
import java.util.Random;

public class V3_0__Insert_super_user extends BaseJavaMigration {
    public void migrate(Context context) throws Exception {
        SpringBcryptHasher bcryptHasher = new SpringBcryptHasher();

        Random random = new Random();

        String randomPassword = random.ints(48, 123)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(16)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        String salt = bcryptHasher.getSalt();
        String superUserPassword = bcryptHasher.getHash(randomPassword, salt);

        try (PreparedStatement statement =
                     context
                             .getConnection()
                             .prepareStatement("INSERT INTO users (name, email, password) VALUES ('Super User', 'superuser@domain.com', '"+superUserPassword+"')")) {

            statement.execute();
        }

        System.out.println("\nSuper user generated password: " + randomPassword + "\n");
    }
}