package com.drewmalin.snickerdoodle.engine.script;

import com.drewmalin.snickerdoodle.engine.ecs.component.Component;
import com.drewmalin.snickerdoodle.engine.ecs.entity.Entity;
import com.drewmalin.snickerdoodle.engine.ecs.entity.EntityManager;

public interface Script extends Component {

    void invoke(Entity entity, EntityManager entityManager, double dt);
}
