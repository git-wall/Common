package org.app.common.utils;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.app.common.entities.csv.BaseCsv;
import org.app.common.entities.csv.CsvColumn;
import org.app.common.entities.csv.CsvName;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class CSVUtils {
    @SneakyThrows
    public <T> List<T> readCsv(String filePath, Class<T> clazz) {
        List<T> dataList = new ArrayList<>();

        try (Reader fileReader = new FileReader(filePath);
             CSVReader csvReader = new CSVReaderBuilder(fileReader)
                     .withSkipLines(1)
                     .build()) { // Skip header if needed

            String[] lineArray;
            while ((lineArray = csvReader.readNext()) != null) {
                for (String csvLine : lineArray) {
                    T data = JacksonUtils.mapper().readValue(csvLine, clazz);
                    if (data != null) {
                        dataList.add(data);
                    }
                }
            }
        }
        return dataList;
    }

    public static <T> List<T> readObjectList(Class<T> type, InputStream is) throws Exception {
        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = CsvSchema.emptySchema().withHeader();

        MappingIterator<T> readValues = mapper.readerFor(type)
                .with(schema)
                .readValues(is);

        return readValues.readAll();
    }

    public Map<Integer, List<String>> readExcel(String fileLocation) throws IOException {
        Map<Integer, List<String>> data = new HashMap<>();
        FileInputStream file = new FileInputStream(fileLocation);
        Workbook workbook = new XSSFWorkbook(file);

        Sheet sheet = workbook.getSheetAt(0);
        int i = 0;
        for (Row row : sheet) {
            data.put(i, new ArrayList<>());
            for (Cell cell : row) {
                switch (cell.getCellType()) {
                    case STRING:
                        data.get(i)
                                .add(cell.getRichStringCellValue()
                                        .getString());
                        break;
                    case NUMERIC:
                        if (DateUtil.isCellDateFormatted(cell)) {
                            data.get(i)
                                    .add(cell.getDateCellValue().toString());
                        } else {
                            data.get(i)
                                    .add(String.valueOf(cell.getNumericCellValue()));
                        }
                        break;
                    case BOOLEAN:
                        data.get(i)
                                .add(String.valueOf(cell.getBooleanCellValue()));
                        break;
                    case FORMULA:
                        data.get(i)
                                .add(String.valueOf(cell.getCellFormula()));
                        break;
                    default:
                        data.get(i)
                                .add(" ");
                }
            }
            i++;
        }
        workbook.close();
        return data;
    }

    private static final String GET_PREFIX = "get";

    public static <T> byte[] exportToCsv(List<BaseCsv> dataList, Class<T> clazz) throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             OutputStreamWriter outputStreamWriter = new OutputStreamWriter(byteArrayOutputStream,
                     StandardCharsets.UTF_8);
             CSVWriter csvWriter = new CSVWriter(outputStreamWriter, ICSVWriter.DEFAULT_SEPARATOR,
                     ICSVWriter.NO_QUOTE_CHARACTER,
                     ICSVWriter.DEFAULT_ESCAPE_CHARACTER, ICSVWriter.DEFAULT_LINE_END)) {

            // Write CSV header
            writeCsvHeader(csvWriter, clazz);

            // Write CSV data
            writeCsvData(csvWriter, dataList, clazz);

            csvWriter.flush();
            return byteArrayOutputStream.toByteArray();
        }
    }

    private static <T> void writeCsvHeader(CSVWriter csvWriter, Class<T> clazz) {
        Field[] baseFields = BaseCsv.class.getDeclaredFields();
        Field[] fields = clazz.getDeclaredFields();

        String[] header = Stream.concat(Stream.of(baseFields), Stream.of(fields))
                .filter(field -> field.getAnnotation(CsvColumn.class) != null)
                .map(field -> field.getAnnotation(CsvColumn.class).columnName())
                .toArray(String[]::new);

        csvWriter.writeNext(header);
    }

    private static <T> void writeCsvData(CSVWriter csvWriter, List<BaseCsv> dataList,
                                         Class<T> clazz) {
        Field[] baseFields = BaseCsv.class.getDeclaredFields();
        Field[] fields = clazz.getDeclaredFields();

        Field[] allFields = Stream.concat(Stream.of(baseFields), Stream.of(fields))
                .filter(field -> field.getAnnotation(CsvColumn.class) != null)
                .toArray(Field[]::new);

        for (BaseCsv data : dataList) {
            String[] row = getFieldValues(allFields, data);
            csvWriter.writeNext(row);
        }
    }

    private static String[] getFieldValues(Field[] fields, Object data) {
        return Stream.of(fields)
                .filter(field -> field.getAnnotation(CsvColumn.class) != null)
                .map(field -> getFieldValueAsString(field, data))
                .toArray(String[]::new);
    }

    private static String getFieldValueAsString(Field field, Object data) {
        try {
            String getterName = GET_PREFIX + StringUtils.capitalize(field.getName());
            Method getter = data.getClass().getMethod(getterName);
            Object value = getter.invoke(data);

            if (!Objects.isNull(value) && value instanceof List<?>) {
                var list = (List<?>) value;
                return "[" + list.stream()
                        .map(v -> Objects.toString(v, ""))
                        .collect(Collectors.joining("|")) + "]";
            }

            return value != null ? value.toString() : StringUtils.EMPTY;
        } catch (IllegalAccessException e) {
            log.warn("Get value field err {}", e.getMessage());
            return StringUtils.EMPTY;
        } catch (InvocationTargetException | NoSuchMethodException e) {
            log.warn("Get value err {}", e.getMessage());
            return StringUtils.EMPTY;
        }
    }

    public static <T> String createFileName(Class<T> clazz) {
        String fromDate = DateTimeUtils.format(LocalDateTime.now());
        CsvName csvName = clazz.getAnnotation(CsvName.class);
        return String.format("%s_%s.csv", csvName.fileName(), fromDate);
    }
}
