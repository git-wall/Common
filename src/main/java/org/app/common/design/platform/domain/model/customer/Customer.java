package org.app.common.design.platform.domain.model.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    private String id;

    private String name;
    private String email;
    private String phone;

    @Enumerated(EnumType.STRING)
    private CustomerTier tier;

    @CreatedDate
    private LocalDateTime createdAt;
}
