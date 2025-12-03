package io.github.cnadjim.dynamic.search.example.config;

import io.github.cnadjim.dynamic.search.example.entity.OperatingSystemEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Initialisation des données de test pour mesurer les performances
 * Génère 1 000 000 d'enregistrements au démarrage de l'application
 *
 * Pour activer : ajouter dans application.properties
 * app.data.init.enabled=true
 *
 * Pour désactiver (par défaut) : supprimer ou mettre à false
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.data.init.enabled", havingValue = "true")
public class DataInitializer {

    @PersistenceContext
    private EntityManager entityManager;

    private static final Random RANDOM = new Random(42); // Seed fixe pour reproductibilité
    private static final int TOTAL_RECORDS = 1_000_000;
    private static final int BATCH_SIZE = 1000; // Insertion par batch de 1000

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

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initData() {
        long startTime = System.currentTimeMillis();
        log.info("🚀 Début de l'initialisation de {} enregistrements...", TOTAL_RECORDS);

        // Vérifier s'il y a déjà des données et les supprimer
        Long count = entityManager
                .createQuery("SELECT COUNT(o) FROM OperatingSystemEntity o", Long.class)
                .getSingleResult();

        if (count < TOTAL_RECORDS) {
            log.info("🗑️ Suppression de {} enregistrements existants...", count);
            int deleted = entityManager
                    .createQuery("DELETE FROM OperatingSystemEntity")
                    .executeUpdate();
            log.info("✅ {} enregistrements supprimés", deleted);
        } else {
            log.info("ℹ️ {} enregistrements déjà présents, pas d'initialisation nécessaire", count);
            return;
        }

        int totalInserted = 0;
        List<OperatingSystemEntity> batch = new ArrayList<>(BATCH_SIZE);

        for (int i = 1; i <= TOTAL_RECORDS; i++) {
            OperatingSystemEntity os = generateOperatingSystem(i);
            batch.add(os);

            // Flush par batch
            if (i % BATCH_SIZE == 0) {
                insertBatch(batch);
                totalInserted += batch.size();
                batch.clear();

                // Log progression tous les 50 000
                if (i % 50_000 == 0) {
                    long elapsed = System.currentTimeMillis() - startTime;
                    double rate = (double) totalInserted / elapsed * 1000;
                    log.info("📊 Progression: {}/{} ({} %) - {} inserts/sec",
                            totalInserted, TOTAL_RECORDS,
                            (totalInserted * 100) / TOTAL_RECORDS,
                            String.format("%.0f", rate));
                }
            }
        }

        // Insérer le reste
        if (!batch.isEmpty()) {
            insertBatch(batch);
            totalInserted += batch.size();
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        double rate = (double) TOTAL_RECORDS / duration * 1000;

        log.info("✅ Initialisation terminée: {} enregistrements en {} ms ({} inserts/sec)",
                TOTAL_RECORDS,
                duration,
                String.format("%.0f", rate));
    }

    private void insertBatch(List<OperatingSystemEntity> batch) {
        for (OperatingSystemEntity entity : batch) {
            entityManager.persist(entity);
        }
        entityManager.flush();
        entityManager.clear(); // Libérer la mémoire
    }

    private OperatingSystemEntity generateOperatingSystem(int id) {
        String osName = OS_NAMES[id % OS_NAMES.length];
        String version = generateVersion(id, osName);
        String kernel = selectKernel(osName, id);
        LocalDateTime releaseDate = generateReleaseDate(id);
        Integer usages = generateUsages(osName, id);

        return OperatingSystemEntity.builder()
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

    private LocalDateTime generateReleaseDate(int id) {
        // Dates étalées sur les 10 dernières années
        int daysAgo = id % 3650; // 10 ans
        return LocalDateTime.now().minusDays(daysAgo);
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
