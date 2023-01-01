package com.drewmalin.snickerdoodle.engine.ecs.entity;

import java.util.UUID;

public class Entity {

    private final UUID id;

    Entity(final UUID id) {
        this.id = id;
    }

    UUID getId() {
        return this.id;
    }
}
