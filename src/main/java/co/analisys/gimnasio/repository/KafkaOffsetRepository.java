package co.analisys.gimnasio.repository;

import co.analisys.gimnasio.model.KafkaOffset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KafkaOffsetRepository extends JpaRepository<KafkaOffset, Long> {

    Optional<KafkaOffset> findByTopicAndPartitionAndConsumerGroup(String topic, Integer partition, String consumerGroup);

    @Query("SELECT k FROM KafkaOffset k WHERE k.consumerGroup = :consumerGroup")
    List<KafkaOffset> findByConsumerGroup(@Param("consumerGroup") String consumerGroup);
}