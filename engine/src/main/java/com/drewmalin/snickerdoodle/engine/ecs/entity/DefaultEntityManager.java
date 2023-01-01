package com.drewmalin.snickerdoodle.engine.ecs.entity;

import com.drewmalin.snickerdoodle.engine.ecs.component.Component;

import java.util.*;

public class DefaultEntityManager implements EntityManager {

    private final Set<Entity> entities;
    private final Map<Class<?>, Map<Entity, ? extends Component>> entityComponentMaps;

    private final Object entityLock = new Object();

    public DefaultEntityManager() {
        this.entities = new HashSet<>();
        entityComponentMaps = new HashMap<>();
    }

    @Override
    public Entity newEntity() {
        synchronized (this.entityLock) {
            final Entity entity = new Entity(UUID.randomUUID());
            this.entities.add(entity);
            return entity;
        }
    }

    @Override
    public Entity newEntity(String name) {
        return null;
    }

    @Override
    public <T extends Component> void putComponent(final Entity entity, final T component) {
        synchronized (this.entityLock) {
            if (!this.entities.contains(entity)) {
                return;
            }
        }

        synchronized (this.entityComponentMaps) {
            if (!this.entityComponentMaps.containsKey(component.getClass())) {
                this.entityComponentMaps.put(component.getClass(), new HashMap<Entity, T>());
            }

            final var rawEntityComponentMap = this.entityComponentMaps.get(component.getClass());
            if (rawEntityComponentMap == null) {
//                LOGGER.error("Attempted to put a component onto entity {}, which does not exist", entity);
                throw new IllegalArgumentException(String.format("Attempted to put a component onto entity %s, which does not exist", entity));
            }

            @SuppressWarnings("unchecked") HashMap<Entity, T> entityComponentMap = (HashMap<Entity, T>) rawEntityComponentMap;
            entityComponentMap.put(entity, component);
        }
    }

    @Override
    public <T extends Component> Optional<T> getComponent(final Entity entity, final Class<T> componentType) {
        synchronized (this.entityLock) {
            if (!this.entities.contains(entity)) {
                return Optional.empty();
            }
        }

        synchronized (this.entityComponentMaps) {
            var entityComponentMap = this.entityComponentMaps.get(componentType);
            if (entityComponentMap == null) {
                // a direct mapping for componentType was not found -- instead, scan through the set of all
                // mapped types and see if componentType is actually a supertype of some previously-mapped
                // type. if no mapping is found still, return an empty Optional.
                // todo: should getComponent therefore be "getComponents"?
                for (final var type : this.entityComponentMaps.keySet()) {
                    if (componentType.isAssignableFrom(type) && this.entityComponentMaps.get(type).containsKey(entity)) {
                        entityComponentMap = this.entityComponentMaps.get(type);
                        break;
                    }
                }
                if (entityComponentMap == null) {
                    return Optional.empty();
                }
            }


            final Component rawComponent = entityComponentMap.get(entity);
            if (rawComponent == null) {
//                LOGGER.error("Attempted to get a component for entity {}, which does not exist", entity);
//                LOGGER.debug("have: {}", entityComponentMap);
                throw new IllegalArgumentException(String.format("Attempted to get a component for entity %s, which does not exist", entity));
            }

            @SuppressWarnings("unchecked") T component = (T) rawComponent;
            return Optional.of(component);
        }
    }

    @Override
    public <T extends Component> Set<Entity> getEntitiesWithComponent(Class<T> componentType) {
        synchronized (this.entityComponentMaps) {
            final var set = new HashSet<Entity>();

            for (final var type : this.entityComponentMaps.keySet()) {
                if (componentType.isAssignableFrom(type)) {
                    final var entityComponentMap = this.entityComponentMaps.get(type);
                    if (entityComponentMap != null) {
                        set.addAll(entityComponentMap.keySet());
                    }
                }
            }
//            if (entityComponentMap == null) {
//                return Optional.empty();
//            }
//
//            final var entityComponentMap = this.entityComponentMaps.get(componentType);
//            if (entityComponentMap == null) {
//                return new HashSet<>();
//            }

//            return entityComponentMap.keySet();
            return set;
        }
    }
}
