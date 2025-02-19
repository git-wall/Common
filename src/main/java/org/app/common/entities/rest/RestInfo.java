package org.app.common.entities.rest;

import lombok.Getter;
import lombok.Setter;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.NameValuePair;

@Setter
@Getter
public class RestInfo {
    private String uri;
    private Header[] headers;
    private NameValuePair[] formParams;

    public void setHeaders(Header... headers) {
        this.headers = headers;
    }

    public void bodyForm(NameValuePair... formParams) {
        this.formParams = formParams;
    }
}