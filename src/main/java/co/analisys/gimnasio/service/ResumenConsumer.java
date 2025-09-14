package co.analisys.gimnasio.service;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import co.analisys.gimnasio.model.KafkaOffset;
import co.analisys.gimnasio.model.ResumenEntrenamiento;
import co.analisys.gimnasio.repository.KafkaOffsetRepository;
import lombok.extern.slf4j.Slf4j;

@Service 
@Slf4j
public class ResumenConsumer { 
    @Autowired 
    private KafkaConsumer<String, ResumenEntrenamiento> consumer; 

    @Autowired
    private KafkaOffsetRepository kafkaOffsetRepository;

    private static final String CONSUMER_GROUP = "resumen-consumer-group";
    
    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        new Thread(this::iniciarProcesamiento, "resumen-consumer-thread").start();
    }
 
    public void iniciarProcesamiento() { 
        consumer.subscribe(Arrays.asList("resumen-entrenamiento"));

        while (consumer.assignment().isEmpty()) {
            consumer.poll(Duration.ofMillis(1000));
        }

        Map<TopicPartition, Long> ultimoOffsetProcesado = cargarUltimoOffset();

        if (ultimoOffsetProcesado.isEmpty()) {
            log.info("No se encontraron offsets previos, comenzando desde el inicio.");
        } else {
            log.info("Cargando offsets previos: {}", ultimoOffsetProcesado);
            for (Map.Entry<TopicPartition, Long> entry : ultimoOffsetProcesado.entrySet()) {
                consumer.seek(entry.getKey(), entry.getValue() + 1); // Seek to next offset to avoid reprocessing
            }
        }
        
        while (true) {
            ConsumerRecords<String, ResumenEntrenamiento> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, ResumenEntrenamiento> record : records) {
                log.info("Procesando resumen: {}", record.value());
                guardarOffset(record.topic(), record.partition(), record.offset());
            }
        }
    } 
        
    private void guardarOffset(String topic, int partition, long offset) { 
        Optional<KafkaOffset> existing = kafkaOffsetRepository.findByTopicAndPartitionAndConsumerGroup(topic, partition, CONSUMER_GROUP);
        KafkaOffset kafkaOffset;
        if (existing.isPresent()) {
            kafkaOffset = existing.get();
            kafkaOffset.setOffset(offset);
        } else {
            kafkaOffset = new KafkaOffset();
            kafkaOffset.setTopic(topic);
            kafkaOffset.setPartition(partition);
            kafkaOffset.setConsumerGroup(CONSUMER_GROUP);
            kafkaOffset.setOffset(offset);
        }
        kafkaOffsetRepository.save(kafkaOffset);
    } 

    private Map<TopicPartition, Long> cargarUltimoOffset() { 

        List<KafkaOffset> offsets = kafkaOffsetRepository.findByConsumerGroup(CONSUMER_GROUP);
        
        Map<TopicPartition, Long> offsetMap = new HashMap<>();

        for (KafkaOffset offset : offsets) {
            TopicPartition tp = new TopicPartition(offset.getTopic(), offset.getPartition());
            offsetMap.put(tp, offset.getOffset());
        }
        return offsetMap;
    }
} 