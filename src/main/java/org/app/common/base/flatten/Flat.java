package org.app.common.base.flatten;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.Map;
/**
 * EAV flat model
 * <pre> {@code
 * make {
 *   "name": "Mid-Autumn Campaign",
 *   "id": 123,
 *   "attributes": {
 *     "tierName": "20.000đ",
 *     "redeemType": "auto"
 *   }
 * }
 * to
 * {
 *   "name": "Mid-Autumn Campaign",
 *   "id": 123,
 *   "tierName": "20.000đ",
 *   "redeemType": "auto"
 * }
 * }
 * </>

 * */
public class Flat {
    private Map<String, Object> attributes;

    @JsonAnyGetter
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @JsonAnySetter
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
}
