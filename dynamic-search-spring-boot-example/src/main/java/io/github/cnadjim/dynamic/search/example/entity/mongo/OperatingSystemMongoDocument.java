package io.github.cnadjim.dynamic.search.example.entity.mongo;

import io.github.cnadjim.dynamic.search.annotation.EnableSearchable;
import io.github.cnadjim.dynamic.search.annotation.SearchableExclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Document MongoDB - Représentation technique pour la persistance NoSQL
 *
 * @EnableSearchable active automatiquement la recherche dynamique pour cette entité
 *
 * Par défaut, TOUS les champs sont searchable (auto-détection du type)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "operating_systems")
@EnableSearchable(beanName = "operatingSystemMongoSearchUseCase")
public class OperatingSystemMongoDocument implements Serializable {

    @SearchableExclude
    private static final long serialVersionUID = -1730538653948604612L;

    @Id
    private String id;

    @Field("name")
    private String name;

    @Field("version")
    private String version;

    @Field("kernel")
    private String kernel;

    @Field("release_date")
    private LocalDate releaseDate;

    @Field("usages")
    private Integer usages;

}
