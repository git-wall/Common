package org.app.common.eav;

import java.util.Collection;

/**
 * If you are use EAV pattern in db design you can use this class
 * <p> Remember implement EAV interface in your entity </p>
 * <p> Then you can use this class and function for simple way to add dynamic field to class </>
 * @see org.app.common.eav.EAV
 */
public class EAVUtils {
    private static <T> T addFields(T t, Collection<EAV> attributes) {
        return new DynamicClass<>(t)
                .fields(attributes)
                .build();
    }
}
