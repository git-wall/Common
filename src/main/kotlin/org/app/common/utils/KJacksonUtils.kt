package org.app.common.utils

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException
import com.fasterxml.jackson.databind.json.JsonMapper
import org.app.common.support.TypeRefer
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat

object KJacksonUtils {
    private val log = LoggerFactory.getLogger(this::class.java)

    private var MAPPER: ObjectMapper = ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
        .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)

    private var READER: ObjectReader = MAPPER.reader()
    private var WRITER: ObjectWriter = MAPPER.writer()
    private var WRITER_PRETTY: ObjectWriter = MAPPER.writerWithDefaultPrettyPrinter()

    fun mapper(): ObjectMapper = MAPPER
    fun reader(): ObjectReader = READER
    fun writer(): ObjectWriter = WRITER
    fun writerPretty(): ObjectWriter = WRITER_PRETTY

    fun <T> readValue(value: String, valueType: Class<T>): T {
        return MAPPER.readValue(value, valueType)
    }

    fun <T> readValue(value: String, type: TypeReference<T>): T {
        return MAPPER.readValue(value, type)
    }

    fun <T> readValue(value: String): T {
        return MAPPER.readValue(value, TypeRefer.refer())
    }

    fun <T> readValue(value: Any): T {
        return MAPPER.readValue(value.toString(), TypeRefer.refer())
    }

    fun newMapperWithCaseInsensitive(dataFormat: String): ObjectMapper {
        return ObjectMapper()
            .enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
            .setDateFormat(SimpleDateFormat(dataFormat))
    }

    fun newMapperWithCaseInsensitive(): ObjectMapper {
        return JsonMapper
            .builder()
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
            .build()
    }

    fun toJson(value: Any?): String {
        if (value == null) return ""

        return WRITER_PRETTY.writeValueAsString(value).replace("%", "%%")
    }

    fun toJson(value: Any?, pretty: Boolean): String {
        if (value == null) return ""

        return if (pretty) {
            WRITER_PRETTY.writeValueAsString(value).replace("%", "%%")
        } else {
            WRITER.writeValueAsString(value).replace("%", "%%")
        }
    }

    fun asString(value: Any?): String {
        try {
            return WRITER_PRETTY.writeValueAsString(value)
        } catch (e: InvalidDefinitionException) {
            return value.toString()
        } catch (e: JsonProcessingException) {
            log.error("Error occurred while parsing json", e)
            // handler more case have special char that make json invalid and not parseable and error
            return WRITER_PRETTY.writeValueAsString(e).replace('%', ' ')
        }
    }
}