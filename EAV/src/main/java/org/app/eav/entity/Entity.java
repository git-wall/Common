package org.app.eav.entity;

import lombok.Data;

/**
 * Represents the "Entity" part of the EAV (Entity-Attribute-Value) model.
 * This class is used to define entities that can have dynamic attributes.
 * External classes (like Campaign) can be linked to this entity to extend their properties.
 */
@Data
public class Entity {
    private Integer id;
    private String name;
    private String description;
}