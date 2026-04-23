package org.app.common.elastic.spring;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Mapping;
import org.springframework.data.elasticsearch.annotations.Setting;

@Document(indexName = "products")
@Setting(settingPath = "/es/settings.json")
@Mapping(mappingPath = "/es/mapping.json")
public class ElasticModel {
}
