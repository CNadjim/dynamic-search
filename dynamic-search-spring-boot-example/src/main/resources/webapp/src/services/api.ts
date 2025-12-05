import type {
  OperatingSystem,
  SearchRequest,
  SearchResult,
  FilterDescriptorResponse,
} from '../types/api';

const API_BASE_URL = '/api/operating-systems';

export type Technology = 'jpa' | 'mongo' | 'elastic';

export const operatingSystemApi = {
  /**
   * Récupère les filtres disponibles pour une technologie donnée
   */
  async getAvailableFilters(technology: Technology): Promise<FilterDescriptorResponse[]> {
    const response = await fetch(`${API_BASE_URL}/${technology}/filters`);
    if (!response.ok) {
      throw new Error('Erreur lors de la récupération des filtres');
    }
    return response.json();
  },

  /**
   * Effectue une recherche sur une technologie donnée
   */
  async search(
    request: SearchRequest,
    technology: Technology
  ): Promise<{ result: SearchResult<OperatingSystem>; responseTime: number }> {
    const startTime = performance.now();

    const response = await fetch(`${API_BASE_URL}/${technology}/search`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(request),
    });

    if (!response.ok) {
      throw new Error('Erreur lors de la recherche');
    }

    const result = await response.json();
    const endTime = performance.now();
    const responseTime = Math.round(endTime - startTime);

    return { result, responseTime };
  },
};
