| Concept  | Ý nghĩa             |
|----------|---------------------|
| Index    | giống database      |
| Document | giống row (JSON)    |
| Field    | giống column        |
| Mapping  | schema của document |
| Shard    | chia nhỏ dữ liệu    |
| Replica  | bản sao để tăng HA  |

| Clause   | Ý nghĩa                           |
|----------|-----------------------------------|
| must     | bắt buộc (AND + scoring)          |
| filter   | bắt buộc (NO scoring → nhanh hơn) |
| must_not | NOT                               |
| should   | OR                                |

| Case        | Query  |
|-------------|--------|
| search text | match  |
| exact       | term   |
| range       | range  |
| conditions  | bool   |
| object list | nested |
| filter      | filter |

Khi insert dữ liệu KHÔNG define mapping
Elasticsearch tạo 2 version của cùng 1 field:

| Field          | Dùng cho                           |
|----------------|------------------------------------|
| `name`         | full-text search (match)           |
| `name.keyword` | exact match (term, sort, group by) |

Best Practices search:
name → full-text -> match -> phân tích text, tìm các giá trị tương tự
name.keyword → exact -> =

search chính xác : dùng term query, không phân tích text, tìm chính xác giá trị
đã cho
search gần đúng : dùng match query, phân tích text, tìm các giá trị tương tự
search theo điều kiện : dùng bool query, kết hợp nhiều điều kiện với must,
should, must_not
search trong object list : dùng nested query, tìm kiếm trong mảng các object
filter : dùng filter context, không tính điểm, nhanh hơn must
Aggregation : dùng để tính toán, phân nhóm dữ liệu, không trả về document mà trả
về kết quả tính toán

ví dụ:
term query:

```json
{
  "query": {
    "term": {
      "status": "active"
    }
  }
}
```

match query:

```json
{
  "query": {
    "match": {
      "description": "quick brown fox"
    }
  }
}
```

bool query:

```json
{
  "query": {
    "bool": {
      "must": [
        {
          "match": {
            "title": "Elasticsearch"
          }
        },
        {
          "range": {
            "publish_date": {
              "gte": "2020-01-01"
            }
          }
        }
      ],
      "should": [
        {
          "match": {
            "author": "John Doe"
          }
        },
        {
          "match": {
            "author": "Jane Smith"
          }
        }
      ],
      "must_not": [
        {
          "term": {
            "status": "draft"
          }
        }
      ]
    }
  }
}
```

nested query:

```json
{
  "query": {
    "nested": {
      "path": "comments",
      "query": {
        "bool": {
          "must": [
            {
              "match": {
                "comments.author": "Alice"
              }
            },
            {
              "match": {
                "comments.text": "great post"
              }
            }
          ]
        }
      }
    }
  }
}
```

filter context:

```json
{
  "query": {
    "bool": {
      "filter": [
        {
          "term": {
            "status": "active"
          }
        },
        {
          "range": {
            "publish_date": {
              "gte": "2020-01-01"
            }
          }
        }
      ]
    }
  }
}
```

Aggregation search:

```json
{
  "aggs": {
    "avg_price": {
      "avg": {
        "field": "price"
      }
    },
    "terms_by_category": {
      "terms": {
        "field": "category.keyword"
      }
    }
  }
}
```
