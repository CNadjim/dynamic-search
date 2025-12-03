import type {
  OperatingSystem,
  SearchRequest,
  SearchResult,
  FilterDescriptorResponse,
} from '../types/api';

const API_BASE_URL = '/api/operating-systems';

export const operatingSystemApi = {
  /**
   * Récupère les filtres disponibles pour les systèmes d'exploitation
   */
  async getAvailableFilters(): Promise<FilterDescriptorResponse[]> {
    const response = await fetch(`${API_BASE_URL}/filters`);
    if (!response.ok) {
      throw new Error('Erreur lors de la récupération des filtres');
    }
    return response.json();
  },

  /**
   * Effectue une recherche de systèmes d'exploitation
   */
  async search(
    request: SearchRequest
  ): Promise<SearchResult<OperatingSystem>> {
    const response = await fetch(`${API_BASE_URL}/search`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(request),
    });

    if (!response.ok) {
      throw new Error('Erreur lors de la recherche');
    }

    return response.json();
  },
};
