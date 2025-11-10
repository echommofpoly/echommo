// /EchoMMO/src/main/java/com/poly/model/UserItem.java
package com.poly.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name = "user_item", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "item_id"}, name = "UQ_UserItem")
})
public class UserItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_item_id")
    private Integer userItemId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 1 CHECK (quantity >= 0)")
    private Integer quantity = 1;

    @Column(name = "acquired_at", columnDefinition = "DATETIME2 DEFAULT GETDATE()", updatable = false)
    private LocalDateTime acquiredAt;

    @Column(name = "is_equipped", columnDefinition = "BIT DEFAULT 0")
    private Boolean isEquipped = false;

    @PrePersist
    protected void onCreate() {
        acquiredAt = LocalDateTime.now();
        if (quantity == null) quantity = 1;
        if (isEquipped == null) isEquipped = false;
    }
     // Các phương thức getter/setter thủ công đã bị xóa
}