## Oracle SQL Function Cheat Sheet

### 1. Handling NULLs

| Function   | Syntax                               | Description                                    |
| ---------- | ------------------------------------ | ---------------------------------------------- |
| `NVL`      | `NVL(expr1, expr2)`                  | If expr1 is NULL, return expr2                 |
| `NVL2`     | `NVL2(expr1, expr2, expr3)`          | If expr1 is NOT NULL, return expr2; else expr3 |
| `COALESCE` | `COALESCE(expr1, expr2, ..., exprN)` | Returns the first non-NULL expression          |
| `NULLIF`   | `NULLIF(expr1, expr2)`               | Returns NULL if expr1 = expr2, else expr1      |

**Example:**

```sql
SELECT
  NVL(discount, 0) AS safe_discount,
  NVL2(discount, 'Có giảm giá', 'Không giảm'),
  COALESCE(discount, price, 0) AS effective_value,
  NULLIF(price, discount) AS price_vs_discount
FROM products;
```

---

### 2. Math Functions

| Function | Syntax                          | Description                |
| -------- | ------------------------------- | -------------------------- |
| `ABS`    | `ABS(number)`                   | Absolute value             |
| `CEIL`   | `CEIL(number)`                  | Round up                   |
| `FLOOR`  | `FLOOR(number)`                 | Round down                 |
| `ROUND`  | `ROUND(number, decimal_places)` | Round to decimal places    |
| `TRUNC`  | `TRUNC(number, decimal_places)` | Truncate decimal           |
| `MOD`    | `MOD(a, b)`                     | Modulus (remainder)        |
| `POWER`  | `POWER(a, b)`                   | a raised to the power of b |
| `SQRT`   | `SQRT(number)`                  | Square root                |

**Example:**

```sql
SELECT
  ABS(-10),
  CEIL(4.3),
  FLOOR(4.7),
  ROUND(3.14159, 2),
  TRUNC(3.14159, 2),
  MOD(10, 3),
  POWER(2, 3),
  SQRT(16)
FROM dual;
```

---

### 3. Date/Time Functions

| Function                  | Syntax                                    | Description                       |
| ------------------------- | ----------------------------------------- | --------------------------------- |
| `SYSDATE`                 | `SYSDATE`                                 | Current date/time (DATE)          |
| `SYSTIMESTAMP`            | `SYSTIMESTAMP`                            | Current date/time (TIMESTAMP)     |
| `CURRENT_DATE`            | `CURRENT_DATE`                            | Current date in session time zone |
| `TRUNC(date [, fmt])`     | Truncate time part                        |                                   |
| `ADD_MONTHS(date, n)`     | Add n months to date                      |                                   |
| `MONTHS_BETWEEN(d1, d2)`  | Months difference between d1 and d2       |                                   |
| `NEXT_DAY(date, 'DAY')`   | Next specific day                         |                                   |
| `LAST_DAY(date)`          | Last day of month                         |                                   |
| `EXTRACT(part FROM date)` | Extract part (YEAR, MONTH, DAY) from date |                                   |

**Example:**

```sql
SELECT
  SYSDATE AS now,
  TRUNC(SYSDATE) AS today,
  ADD_MONTHS(SYSDATE, 1) AS next_month,
  MONTHS_BETWEEN(SYSDATE, TO_DATE('2024-01-01','YYYY-MM-DD')) AS months_diff,
  NEXT_DAY(SYSDATE, 'MONDAY') AS next_monday,
  LAST_DAY(SYSDATE) AS end_of_month
FROM dual;
```

---

### 4. String Functions

| Function                        | Syntax                     | Description |                        |
| ------------------------------- | -------------------------- | ----------- | ---------------------- |
| `UPPER`, `LOWER`, `INITCAP`     | Convert case               |             |                        |
| `CONCAT(str1, str2)`            | Concatenate strings        |             |                        |
| \`                              |                            | \`          | Concatenation operator |
| `SUBSTR(str, start, length)`    | Substring                  |             |                        |
| `INSTR(str, substr)`            | Find position of substring |             |                        |
| `LENGTH(str)`                   | String length              |             |                        |
| `TRIM`, `LTRIM`, `RTRIM`        | Trim spaces                |             |                        |
| `REPLACE(str, search, replace)` | Replace text               |             |                        |
| `LPAD`, `RPAD`                  | Pad string left/right      |             |                        |

**Example:**

```sql
SELECT
  UPPER(name),
  LOWER(name),
  INITCAP(name),
  'Product: ' || name AS product_label,
  SUBSTR(name, 1, 3) AS short_name,
  LENGTH(name) AS name_len,
  REPLACE(name, 'Phone', 'Device') AS replaced_name
FROM products;
```

---

### 5. Aggregate Functions

| Function                | Syntax                  | Description |
| ----------------------- | ----------------------- | ----------- |
| `SUM(col)`              | Sum                     |             |
| `AVG(col)`              | Average                 |             |
| `MIN(col)`              | Minimum                 |             |
| `MAX(col)`              | Maximum                 |             |
| `COUNT(*) / COUNT(col)` | Count rows              |             |
| `GROUP BY` + `HAVING`   | Group and filter groups |             |

**Example:**

```sql
SELECT
  COUNT(*) AS total_products,
  SUM(price) AS total_price,
  AVG(price) AS avg_price,
  MIN(price) AS min_price,
  MAX(price) AS max_price
FROM products;
```

---

### 6. Analytic / Window Functions

| Function                        | Syntax                  | Description |
| ------------------------------- | ----------------------- | ----------- |
| `ROW_NUMBER()`                  | Row number in partition |             |
| `RANK()`                        | Ranking with gaps       |             |
| `DENSE_RANK()`                  | Ranking without gaps    |             |
| `LAG(col, n)`                   | Previous value          |             |
| `LEAD(col, n)`                  | Next value              |             |
| `SUM(col) OVER (...)`           | Cumulative sum          |             |
| `PARTITION BY ... ORDER BY ...` | Partitioning            |             |

**Example:**

```sql
SELECT
  id,
  name,
  price,
  RANK() OVER (ORDER BY price DESC) AS rank_by_price,
  SUM(price) OVER (ORDER BY price) AS running_total
FROM products;
```

---

### 7. Type Conversion Functions

| Function                       | Syntax                   | Description |
| ------------------------------ | ------------------------ | ----------- |
| `TO_NUMBER(char)`              | Convert string to number |             |
| `TO_CHAR(date/number, format)` | Convert to string        |             |
| `TO_DATE(char, format)`        | Convert to date          |             |
| `TO_TIMESTAMP(...)`            | Convert to timestamp     |             |
| `CAST(expr AS type)`           | Explicit cast            |             |

**Example:**

```sql
SELECT
  TO_CHAR(created_at, 'YYYY-MM-DD') AS created_date_str,
  TO_DATE('2024-12-18', 'YYYY-MM-DD') AS parsed_date,
  CAST(price AS VARCHAR2(10)) AS price_text
FROM products;
```

---

### Full Query Example:

```sql
SELECT
  id,
  name,
  NVL(discount, 0) AS discount_safe,
  price,
  price - NVL(discount, 0) AS final_price,
  ROUND(price, 2) AS price_rounded,
  TO_CHAR(created_at, 'YYYY-MM-DD') AS created_date,
  RANK() OVER (ORDER BY price DESC) AS price_rank,
  SUM(price) OVER (PARTITION BY TO_CHAR(created_at, 'YYYY-MM')) AS monthly_total,
  LENGTH(name) AS name_length,
  REPLACE(name, 'Phone', 'Device') AS new_name
FROM products
WHERE TRUNC(created_at) >= TO_DATE('2024-01-01', 'YYYY-MM-DD')
ORDER BY final_price DESC;
```

