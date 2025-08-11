package org.app.common.eav.v1;

/**
 * <h4>If you are use EAV pattern in db design you can use this class</h4>
 * 
 * <p><strong>{@code Remember implement EAV interface in your entity when use this}</strong></p>
 */
public class EAVUtils {
    
    
    /**
     * Adds dynamic fields to an object based on EAV (Entity-Attribute-Value) attributes.
     * 
     * <p>This method creates a new instance of DynamicClass with the given object,
     * adds fields based on the provided EAV attributes, and builds the resulting object.</p>
     *
     * @param <T> the type of the object to which fields will be added
     * @param t the object to which fields will be added
     * @param attributes an Iterable of EAV objects representing the attributes to be added as fields
     * @return a new instance of T with the added fields
     */
    private static <T> T addFields(T t, Iterable<EAV> attributes) {
        return new DynamicClass<>(t)
                .fields(attributes)
                .build();
    }
}
