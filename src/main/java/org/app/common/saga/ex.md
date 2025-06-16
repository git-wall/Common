```java
package com.example.service;

import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BusinessService {

    @Autowired
    private OrderService orderService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private AccountService accountService;

    /**
     * Global transaction example using Seata
     * This demonstrates how Seata coordinates transactions across multiple microservices
     */
    @GlobalTransactional(name = "business-service-tx", rollbackFor = Exception.class)
    public void createOrder(String userId, String productId, int count, double amount) {
        // Create an order in Order Service
        orderService.createOrder(userId, productId, count, amount);
        
        // Reduce product inventory in Inventory Service
        inventoryService.deduct(productId, count);
        
        // Deduct user account balance in Account Service
        accountService.deduct(userId, amount);
        
        // If any of the above operations fails, Seata will rollback all transactions
    }
}
```

```sql
-- This table should be created in each microservice's database
-- that participates in global transactions

CREATE TABLE `undo_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branch_id` bigint(20) NOT NULL,
  `xid` varchar(100) NOT NULL,
  `context` varchar(128) NOT NULL,
  `rollback_info` longblob NOT NULL,
  `log_status` int(11) NOT NULL,
  `log_created` datetime NOT NULL,
  `log_modified` datetime NOT NULL,
  `ext` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_undo_log` (`xid`,`branch_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
```