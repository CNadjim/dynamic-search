package io.github.cnadjim.dynamic.search.spring.starter.gateway;

import io.github.cnadjim.dynamic.search.spring.starter.request.SearchRequest;
import io.github.cnadjim.dynamic.search.spring.starter.response.FilterDescriptorResponse;
import io.github.cnadjim.dynamic.search.model.SearchResult;

import java.util.List;

/**
 * Gateway unifié pour les opérations de recherche dynamique
 * Simplifie l'utilisation en encapsulant les use cases du domaine
 *
 * Ce composant fait office de façade pour :
 * - SearchUseCase : recherche avec critères
 * - GetAvailableFiltersUseCase : découverte des filtres disponibles
 *
 * Utilisation typique dans un contrôleur REST :
 * <pre>
 * {@code
 * @RestController
 * @RequiredArgsConstructor
 * public class ProductController {
 *     private final SearchGateway searchGateway;
 *
 *     @PostMapping("/search")
 *     public SearchResult<Product> search(@RequestBody SearchRequest request) {
 *         return searchGateway.search(request, Product.class);
 *     }
 *
 *     @GetMapping("/filters")
 *     public List<FilterDescriptorResponse> getFilters() {
 *         return searchGateway.getAvailableFilters(Product.class);
 *     }
 * }
 * }
 * </pre>
 */
public interface SearchGateway {

    /**
     * Effectue une recherche dynamique sur une entité
     *
     * @param request Requête de recherche contenant filtres, tris et pagination
     * @param entityClass Classe de l'entité à rechercher
     * @param <T> Type de l'entité
     * @return Résultat de recherche paginé avec les entités trouvées
     * @throws IllegalArgumentException si la requête ou la classe est null
     * @throws IllegalStateException si aucun SearchUseCase n'est configuré pour cette entité
     */
    <T> SearchResult<T> search(SearchRequest request, Class<T> entityClass);

    /**
     * Récupère les filtres disponibles pour une entité
     * Permet la découverte dynamique des champs filtrables et leurs opérateurs
     *
     * @param entityClass Classe de l'entité
     * @param <T> Type de l'entité
     * @return Liste des descripteurs de filtres disponibles
     * @throws IllegalArgumentException si la classe est null
     * @throws IllegalStateException si aucun GetAvailableFiltersUseCase n'est configuré pour cette entité
     */
    <T> List<FilterDescriptorResponse> getAvailableFilters(Class<T> entityClass);
}
