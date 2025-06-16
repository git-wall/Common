package org.app.common.utils

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.core.{JsonParser, JsonProcessingException}
import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.app.common.support.TypeRef
import org.slf4j.LoggerFactory

import java.text.SimpleDateFormat
import scala.jdk.CollectionConverters._

object SJacksonUtils {
  private val log = LoggerFactory.getLogger(this.getClass)

  private val MAPPER: ObjectMapper = new ObjectMapper()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
    .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
    .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)

  private val READER: ObjectReader = MAPPER.reader()
  private val WRITER: ObjectWriter = MAPPER.writer()
  private val WRITER_PRETTY: ObjectWriter = MAPPER.writerWithDefaultPrettyPrinter()

  def mapper(): ObjectMapper = MAPPER

  def reader(): ObjectReader = READER

  def writer(): ObjectWriter = WRITER

  def writerPretty(): ObjectWriter = WRITER_PRETTY

  def readValue[T](value: String, clazz: Class[T]): T = {
    MAPPER.readValue(value, clazz)
  }

  def readValue[T](value: String, typeRef: TypeReference[T]): T = {
    MAPPER.readValue(value, typeRef)
  }

  def readValue[T](value: String): T = {
    MAPPER.readValue(value, TypeRef.refer())
  }

  def readValue[T](value: Object): T = {
    MAPPER.readValue(value.toString, TypeRef.refer())
  }

  def newMapperWithCaseInsensitive(dateFormat: String): ObjectMapper = {
    val mapper = new ObjectMapper()
    mapper.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES)
    mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
    mapper.setDateFormat(new SimpleDateFormat(dateFormat))
    mapper
  }

  def newMapperWithCaseInsensitive(): ObjectMapper = {
    val mapper = JsonMapper.builder()
    mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
    mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
    mapper.build()
  }

  def toJson(value: Object): String = {
    if (value == null) return ""

    WRITER_PRETTY.writeValueAsString(value).replace("%", "%%")
  }

  def toString(value: Object): String = {
    try WRITER_PRETTY.writeValueAsString(value)
    catch {
      case e: InvalidDefinitionException =>
        value.toString
      case e: JsonProcessingException =>
        log.error("Error occurred while parsing json", e)
        // handler more case has special char that make json invalid and not parseable and error
        WRITER_PRETTY.writeValueAsString(e).replace('%', ' ')
    }
  }

  def readTree(value: String): JsonNode = {
    MAPPER.readTree(value)
  }

  // need convert list asScala to use fun foreach like java
  def replaceNullStrings(node: JsonNode): Unit = {
    if (node.isObject) node.fields.asScala.foreach(entry => {
      val childNode = entry.getValue
      if (childNode.isTextual && childNode.textValue.equalsIgnoreCase("null"))
        node.asInstanceOf[ObjectNode].putNull(entry.getKey)
      else
        replaceNullStrings(childNode)
    })
    else if (node.isArray) {
      for (arrayElement <- node.elements.asScala) {
        replaceNullStrings(arrayElement)
      }
    }
  }
}
