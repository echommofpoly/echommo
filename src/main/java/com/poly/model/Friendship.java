package com.poly.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "friendship")
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Đã xóa ký tự ẩn (Invalid Character)
    @Column(name = "friendship_id")
    private Integer friendshipId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "friend_user_id", nullable = false)
    private User friendUser;

    @Column(length = 20)
    // Chuẩn hóa trạng thái mặc định về 'PENDING'
    private String status = "PENDING";

    private LocalDateTime createdAt = LocalDateTime.now();
}