package co.analisys.gimnasio.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "kafka_offsets")
@Data
@NoArgsConstructor
public class KafkaOffset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String topic;

    @Column(nullable = false)
    private Integer partition;

    @Column(name = "consumer_group", nullable = false)
    private String consumerGroup;

    @Column(name = "\"offset\"", nullable = false)
    private Long offset;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}