package ${packageName};

import jp.co.worksap.timachine.dynamo.DynamoMigration;
import jp.co.worksap.timachine.model.Down;
import jp.co.worksap.timachine.model.Migration;
import jp.co.worksap.timachine.model.Up;

@Migration
public class ${className} extends DynamoMigration {
    @Up
    public void up() {

    }

    @Down
    public void down() {

    }
}