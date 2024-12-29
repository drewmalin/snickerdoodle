package com.drewmalin.snickerdoodle.engine.ecs.entity;

import com.drewmalin.snickerdoodle.engine.ecs.component.Component;

import java.util.Optional;
import java.util.Set;

public interface EntityManager {

    Entity newEntity(String name);

    /**
     * Stores a component as being associated with the given entity. If a component of the same type exists
     * for this entity, it is replaced. If no such entity exists, an IllegalArgumentException is thrown.
     */
    <T extends Component> void putComponent(Entity entity, T component);

    /**
     * Gets a component for the given Component type for the given entity. If no such entity exists, an
     * IllegalArgumentException is thrown. If no entity is found for the provided type, returns Optional.empty().
     */
    <T extends Component> Optional<T> getComponent(Entity entity, Class<T> componentType);

    /**
     * Retrieves all entities mapped to a component of the given type.
     */
    <T extends Component> Set<Entity> getEntitiesWithComponent(Class<T> componentType);
}
