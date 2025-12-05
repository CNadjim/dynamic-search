package io.github.cnadjim.dynamic.search.example.entity.elastic;

import io.github.cnadjim.dynamic.search.annotation.EnableSearchable;
import io.github.cnadjim.dynamic.search.annotation.SearchableExclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Document Elasticsearch - Représentation technique pour le moteur de recherche
 *
 * @EnableSearchable active automatiquement la recherche dynamique pour cette entité
 *
 * Par défaut, TOUS les champs sont searchable (auto-détection du type)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "operating_systems")
@EnableSearchable(beanName = "operatingSystemElasticSearchUseCase")
public class OperatingSystemElasticDocument implements Serializable {

    @SearchableExclude
    private static final long serialVersionUID = -1730538653948604613L;

    @Id
    @SearchableExclude // elastic ne gère pas les recherches sur l'id automatiquement
    private String id;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Text)
    private String version;

    @Field(type = FieldType.Text)
    private String kernel;

    @Field(type = FieldType.Date)
    private LocalDate releaseDate;

    @Field(type = FieldType.Integer)
    private Integer usages;

}
