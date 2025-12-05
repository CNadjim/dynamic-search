package io.github.cnadjim.dynamic.search.example.config;

import io.github.cnadjim.dynamic.search.example.entity.elastic.OperatingSystemElasticDocument;
import io.github.cnadjim.dynamic.search.example.entity.jpa.OperatingSystemJpaEntity;
import io.github.cnadjim.dynamic.search.example.entity.mongo.OperatingSystemMongoDocument;
import io.github.cnadjim.dynamic.search.example.mapper.OperatingSystemElasticMapper;
import io.github.cnadjim.dynamic.search.example.mapper.OperatingSystemJpaMapper;
import io.github.cnadjim.dynamic.search.example.mapper.OperatingSystemMongoMapper;
import io.github.cnadjim.dynamic.search.example.model.OperatingSystemModel;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Initialisation des données de test pour les 3 technologies
 * Génère des enregistrements dans PostgreSQL, MongoDB et Elasticsearch
 *
 * Configuration dans application.yml :
 * app.data.init.enabled=true
 * app.data.init.size=1000
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.data.init.enabled", havingValue = "true")
public class DataInitializer {

    @PersistenceContext
    private EntityManager entityManager;

    private final MongoTemplate mongoTemplate;
    private final ElasticsearchOperations elasticsearchOperations;

    @Value("${app.data.init.size:1000}")
    private int totalRecords;

    private static final Random RANDOM = new Random(42); // Seed fixe pour reproductibilité
    private static final int BATCH_SIZE = 100; // Insertion par batch

    private static final String[] OS_NAMES = {
            "Windows", "Ubuntu", "Debian", "Fedora", "Red Hat Enterprise Linux",
            "CentOS", "Arch Linux", "openSUSE", "Linux Mint", "macOS",
            "Android", "iOS", "FreeBSD", "OpenBSD", "Rocky Linux",
            "AlmaLinux", "Manjaro", "Pop!_OS", "elementary OS", "Zorin OS"
    };

    private static final String[] KERNELS = {
            "NT 10.0", "NT 6.3", "NT 6.1",
            "Linux 6.11", "Linux 6.10", "Linux 6.8", "Linux 6.6", "Linux 6.1",
            "Linux 5.15", "Linux 5.14", "Linux 5.10", "Linux 5.4",
            "Linux 4.19", "Linux 4.18", "Linux 3.10",
            "Darwin 24.1", "Darwin 24.0", "Darwin 23.6", "Darwin 23.0", "Darwin 22.0",
            "FreeBSD 14.1", "OpenBSD 7.6"
    };

    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void initData() {
        long startTime = System.currentTimeMillis();
        log.info("🚀 Début de l'initialisation de {} enregistrements dans les 3 bases...", totalRecords);

        // Nettoyage des données existantes
        cleanupExistingData();

        // Génération et insertion
        insertDataInAllDatabases();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        double rate = (double) totalRecords / duration * 1000;

        log.info("✅ Initialisation terminée: {} enregistrements x 3 bases en {} ms ({} inserts/sec)",
                totalRecords,
                duration,
                String.format("%.0f", rate * 3));
    }

    private void cleanupExistingData() {
        log.info("🗑️ Nettoyage des données existantes...");

        // PostgreSQL
        long jpaCount = entityManager
                .createQuery("SELECT COUNT(o) FROM OperatingSystemJpaEntity o", Long.class)
                .getSingleResult();
        if (jpaCount > 0) {
            entityManager.createQuery("DELETE FROM OperatingSystemJpaEntity").executeUpdate();
            log.info("   ✓ PostgreSQL: {} enregistrements supprimés", jpaCount);
        }

        // MongoDB
        long mongoCount = mongoTemplate.count(new org.springframework.data.mongodb.core.query.Query(),
                OperatingSystemMongoDocument.class);
        if (mongoCount > 0) {
            mongoTemplate.remove(new org.springframework.data.mongodb.core.query.Query(),
                    OperatingSystemMongoDocument.class);
            log.info("   ✓ MongoDB: {} enregistrements supprimés", mongoCount);
        }

        // Elasticsearch
        try {
            elasticsearchOperations.indexOps(OperatingSystemElasticDocument.class).delete();
            elasticsearchOperations.indexOps(OperatingSystemElasticDocument.class).create();
            log.info("   ✓ Elasticsearch: Index recréé");
        } catch (Exception e) {
            log.warn("   ⚠ Elasticsearch: {}", e.getMessage());
        }
    }

    @Transactional
    public void insertDataInAllDatabases() {
        List<OperatingSystemJpaEntity> jpaBatch = new ArrayList<>(BATCH_SIZE);
        List<OperatingSystemMongoDocument> mongoBatch = new ArrayList<>(BATCH_SIZE);
        List<OperatingSystemElasticDocument> elasticBatch = new ArrayList<>(BATCH_SIZE);

        for (int i = 1; i <= totalRecords; i++) {
            // Générer le modèle domaine
            OperatingSystemModel model = generateOperatingSystemModel(i);

            // Mapper vers les 3 technologies
            jpaBatch.add(OperatingSystemJpaMapper.toEntity(model));
            mongoBatch.add(OperatingSystemMongoMapper.toDocument(model));
            elasticBatch.add(OperatingSystemElasticMapper.toDocument(model));

            // Flush par batch
            if (i % BATCH_SIZE == 0) {
                insertBatches(jpaBatch, mongoBatch, elasticBatch);
                jpaBatch.clear();
                mongoBatch.clear();
                elasticBatch.clear();

                // Log progression
                if (i % 500 == 0) {
                    long elapsed = System.currentTimeMillis();
                    log.info("📊 Progression: {}/{} ({}%)",
                            i, totalRecords, (i * 100) / totalRecords);
                }
            }
        }

        // Insérer le reste
        if (!jpaBatch.isEmpty()) {
            insertBatches(jpaBatch, mongoBatch, elasticBatch);
        }
    }

    private void insertBatches(
            List<OperatingSystemJpaEntity> jpaBatch,
            List<OperatingSystemMongoDocument> mongoBatch,
            List<OperatingSystemElasticDocument> elasticBatch) {

        // PostgreSQL (JPA)
        for (OperatingSystemJpaEntity entity : jpaBatch) {
            entityManager.persist(entity);
        }
        entityManager.flush();
        entityManager.clear();

        // MongoDB
        mongoTemplate.insertAll(mongoBatch);

        // Elasticsearch
        elasticsearchOperations.save(elasticBatch);
    }

    private OperatingSystemModel generateOperatingSystemModel(int id) {
        String osName = OS_NAMES[id % OS_NAMES.length];
        String version = generateVersion(id, osName);
        String kernel = selectKernel(osName, id);
        LocalDate releaseDate = generateReleaseDate(id);
        Integer usages = generateUsages(osName, id);

        return OperatingSystemModel.builder()
                .name(osName)
                .version(version)
                .kernel(kernel)
                .releaseDate(releaseDate)
                .usages(usages)
                .build();
    }

    private String generateVersion(int id, String osName) {
        return switch (osName) {
            case "Windows" -> "Build-" + String.format("%06d", id);
            case "Ubuntu", "Debian", "Linux Mint" -> {
                int major = 20 + (id % 5);
                int minor = id % 12;
                yield major + "." + String.format("%02d", minor) + " Build-" + id;
            }
            case "macOS" -> {
                int major = 10 + (id % 6);
                int minor = id % 16;
                yield major + "." + minor + " Build-" + id;
            }
            case "Android", "iOS" -> {
                int major = 10 + (id % 6);
                int minor = id % 8;
                yield major + "." + minor + " Build-" + id;
            }
            case "Fedora" -> (35 + (id % 10)) + "." + id;
            case "Red Hat Enterprise Linux" -> {
                int major = 7 + (id % 4);
                int minor = id % 10;
                yield major + "." + minor + "." + id;
            }
            default -> "Version-" + id;
        };
    }

    private String selectKernel(String osName, int id) {
        if (osName.contains("Windows")) {
            String[] windowsKernels = {"NT 10.0", "NT 6.3", "NT 6.1"};
            return windowsKernels[id % windowsKernels.length];
        } else if (osName.equals("macOS") || osName.equals("iOS")) {
            String[] darwinKernels = {"Darwin 24.1", "Darwin 24.0", "Darwin 23.6", "Darwin 23.0", "Darwin 22.0"};
            return darwinKernels[id % darwinKernels.length];
        } else if (osName.equals("FreeBSD")) {
            return "FreeBSD 14.1";
        } else if (osName.equals("OpenBSD")) {
            return "OpenBSD 7.6";
        } else {
            String[] linuxKernels = {
                    "Linux 6.11", "Linux 6.10", "Linux 6.8", "Linux 6.6", "Linux 6.1",
                    "Linux 5.15", "Linux 5.14", "Linux 5.10", "Linux 5.4"
            };
            return linuxKernels[id % linuxKernels.length];
        }
    }

    private LocalDate generateReleaseDate(int id) {
        // Dates étalées sur les 10 dernières années
        int daysAgo = id % 3650; // 10 ans
        return LocalDate.now().minusDays(daysAgo);
    }

    private Integer generateUsages(String osName, int id) {
        // Usages plus élevés pour les OS populaires
        int baseUsage = switch (osName) {
            case "Android" -> 5_000_000;
            case "iOS" -> 4_000_000;
            case "Windows" -> 3_000_000;
            case "macOS" -> 1_000_000;
            case "Ubuntu", "Debian" -> 800_000;
            default -> 100_000;
        };

        // Ajouter variation aléatoire déterministe
        int variation = (id * 17) % (baseUsage / 2);
        return baseUsage + variation;
    }
}
