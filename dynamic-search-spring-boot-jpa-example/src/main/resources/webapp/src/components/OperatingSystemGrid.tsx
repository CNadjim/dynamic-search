import { useEffect, useState, useMemo } from 'react';
import { AgGridReact } from 'ag-grid-react';
import { ModuleRegistry, AllCommunityModule } from 'ag-grid-community';
import type {
  ColDef,
  GridReadyEvent,
  IDatasource,
  IGetRowsParams,
  SortModelItem,
} from 'ag-grid-community';
import 'ag-grid-community/styles/ag-grid.css';
import 'ag-grid-community/styles/ag-theme-alpine.css';
import type {
  OperatingSystem,
  SearchRequest,
  FilterRequest,
  FilterDescriptorResponse,
  SortRequest,
  FilterOperator,
} from '../types/api';
import { operatingSystemApi } from '../services/api';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from './ui/card';
import { Badge } from './ui/badge';
import { Database, Filter, SortAsc } from 'lucide-react';

// Enregistrer tous les modules AG Grid Community
ModuleRegistry.registerModules([AllCommunityModule]);

export const OperatingSystemGrid = () => {
  const [filterDescriptors, setFilterDescriptors] = useState<FilterDescriptorResponse[]>([]);
  const [gridApi, setGridApi] = useState<any>(null);

  // Charger les métadonnées des filtres au montage
  useEffect(() => {
    const loadFilterDescriptors = async () => {
      try {
        const descriptors = await operatingSystemApi.getAvailableFilters();
        setFilterDescriptors(descriptors);
        console.log('Descripteurs de filtres chargés:', descriptors);
      } catch (err) {
        console.error('Erreur lors du chargement des descripteurs:', err);
      }
    };

    loadFilterDescriptors();
  }, []);

  /**
   * Formater une valeur de date pour l'API (supprime l'heure)
   */
  const formatDateValue = (value: any): string => {
    if (!value) return value;

    // Si c'est une string avec un format date-time, extraire juste la date
    if (typeof value === 'string') {
      // Format: "2023-09-30 00:00:00" ou "2023-09-30T00:00:00"
      const dateMatch = value.match(/^(\d{4}-\d{2}-\d{2})/);
      if (dateMatch) {
        return dateMatch[1]; // Retourne "2023-09-30"
      }
    }

    return value;
  };

  /**
   * Mapper les filtres AG Grid vers FilterRequest de l'API
   */
  const mapAgGridFilterToApiFilter = (
    field: string,
    filterModel: any
  ): FilterRequest | null => {
    if (!filterModel) return null;

    console.log('Filter model for field', field, ':', filterModel);

    const { type, filter, filterTo, dateFrom, dateTo } = filterModel;

    // Mapping des types de filtres AG Grid vers nos opérateurs
    const operatorMap: Record<string, FilterOperator> = {
      equals: 'equals',
      notEqual: 'notEquals',
      lessThan: 'lessThan',
      greaterThan: 'greaterThan',
      contains: 'contains',
      notContains: 'notContains',
      startsWith: 'startsWith',
      endsWith: 'endsWith',
      blank: 'blank',
      notBlank: 'notBlank',
      inRange: 'between',
    };

    const operator = operatorMap[type];
    if (!operator) {
      console.warn('Unknown filter type:', type);
      return null;
    }

    const apiFilter: FilterRequest = {
      key: field,
      operator,
    };

    // Pour les filtres de dates, AG Grid utilise dateFrom/dateTo
    // Pour les autres filtres, il utilise filter/filterTo
    let filterValue = dateFrom || filter;
    let filterToValue = dateTo || filterTo;

    // Formatter les dates (enlever l'heure si présente)
    const isDateFilter = dateFrom !== undefined || dateTo !== undefined;
    if (isDateFilter) {
      filterValue = formatDateValue(filterValue);
      filterToValue = formatDateValue(filterToValue);
    }

    // Ajouter les valeurs selon l'opérateur
    if (operator === 'between' && filterToValue !== undefined) {
      apiFilter.value = filterValue;
      apiFilter.valueTo = filterToValue;
    } else if (operator !== 'blank' && operator !== 'notBlank') {
      // Vérifier qu'on a bien une valeur
      if (filterValue === undefined || filterValue === null) {
        console.warn('No value provided for filter:', field, operator);
        return null;
      }
      apiFilter.value = filterValue;
    }

    console.log('Mapped filter:', apiFilter);
    return apiFilter;
  };

  /**
   * Créer la datasource pour le modèle Infinite
   */
  const createInfiniteDatasource = (): IDatasource => {
    return {
      getRows: async (params: IGetRowsParams) => {
        console.log('Infinite model request:', {
          startRow: params.startRow,
          endRow: params.endRow,
          sortModel: params.sortModel,
          filterModel: params.filterModel,
        });

        try {
          // Calculer la pagination
          const pageSize = params.endRow! - params.startRow!;
          const pageNumber = Math.floor(params.startRow! / pageSize);

          const page = {
            number: pageNumber,
            size: pageSize,
          };

          // Extraire le tri
          const sorts: SortRequest[] = (params.sortModel || []).map((sort: SortModelItem) => ({
            key: sort.colId,
            direction: sort.sort as 'asc' | 'desc',
          }));

          // Extraire les filtres
          const filters: FilterRequest[] = [];
          const filterModel = params.filterModel || {};

          Object.keys(filterModel).forEach((field) => {
            const apiFilter = mapAgGridFilterToApiFilter(field, (filterModel as any)[field]);
            if (apiFilter) {
              filters.push(apiFilter);
            }
          });

          // Construire la requête API
          const searchRequest: SearchRequest = {
            filters,
            sorts: sorts.length > 0 ? sorts : [{ key: 'name', direction: 'asc' }],
            page,
          };

          console.log('API Search Request:', searchRequest);

          // Appel API
          const result = await operatingSystemApi.search(searchRequest);

          // Retourner les résultats à AG Grid
          const lastRow = result.last ? params.startRow! + result.content.length : undefined;

          params.successCallback(result.content, lastRow);
        } catch (error) {
          console.error('Erreur lors du chargement des données:', error);
          params.failCallback();
        }
      },
    };
  };

  // Générer les colonnes dynamiquement à partir des descripteurs de filtres
  const columnDefs: ColDef<OperatingSystem>[] = useMemo(() => {
    if (filterDescriptors.length === 0) {
      return [];
    }

    // Générer les colonnes à partir des descripteurs
    const columns: ColDef<OperatingSystem>[] = [];

    filterDescriptors.forEach((descriptor) => {
      const column: ColDef<OperatingSystem> = {
        field: descriptor.key as keyof OperatingSystem,
        headerName: formatHeaderName(descriptor.key),
        sortable: true,
        resizable: true,
        filter: true, // Activer les filtres
        floatingFilter: true, // Activer les filtres flottants
      };

      // Configuration spécifique selon le type de champ
      switch (descriptor.fieldType) {
        case 'number':
          column.filter = 'agNumberColumnFilter';
          column.filterParams = {
            suppressAndOrCondition: true, // Désactiver les conditions AND/OR
          };
          column.width = 150;
          if (descriptor.key === 'marketShare') {
            column.valueFormatter = (params) => {
              if (params.value === null || params.value === undefined) return '';
              return `${params.value}%`;
            };
          }
          break;

        case 'date':
          column.filter = 'agDateColumnFilter';
          column.filterParams = {
            suppressAndOrCondition: true, // Désactiver les conditions AND/OR
          };
          column.width = 180;
          column.valueFormatter = (params) => {
            if (!params.value) return '';
            return new Date(params.value).toLocaleDateString('fr-FR');
          };
          break;

        case 'boolean':
          column.filter = 'agSetColumnFilter';
          column.filterParams = {
            suppressAndOrCondition: true, // Désactiver les conditions AND/OR
          };
          column.width = 140;
          column.cellRenderer = (params: any) => {
            return params.value ? '✅ Oui' : '❌ Non';
          };
          break;

        case 'string':
          column.filter = 'agTextColumnFilter';
          column.filterParams = {
            suppressAndOrCondition: true, // Désactiver les conditions AND/OR
          };
          if (descriptor.key === 'id') {
            column.width = 80;
          } else {
            column.flex = 1;
          }
          break;
      }

      columns.push(column);
    });

    return columns;
  }, [filterDescriptors]);

  // Configuration par défaut de la grille
  const defaultColDef = useMemo<ColDef>(
    () => ({
      resizable: true,
      sortable: true,
      filter: true,
      floatingFilter: true, // Activer les filtres flottants par défaut
      filterParams: {
        suppressAndOrCondition: true, // Désactiver les conditions AND/OR pour toutes les colonnes
      },
    }),
    []
  );

  /**
   * Callback quand la grille est prête
   */
  const onGridReady = (params: GridReadyEvent) => {
    setGridApi(params.api);

    // Configurer la datasource infinite
    if (filterDescriptors.length > 0) {
      const datasource = createInfiniteDatasource();
      params.api.setGridOption('datasource', datasource);
    }
  };

  /**
   * Reconfigurer la datasource quand les descripteurs changent
   */
  useEffect(() => {
    if (gridApi && filterDescriptors.length > 0) {
      const datasource = createInfiniteDatasource();
      gridApi.setGridOption('datasource', datasource);
    }
  }, [gridApi, filterDescriptors]);

  /**
   * Formate le nom du champ en un en-tête lisible
   */
  function formatHeaderName(key: string): string {
    // Mapping manuel pour les champs connus
    const mapping: Record<string, string> = {
      id: 'ID',
      name: 'Nom',
      version: 'Version',
      releaseDate: 'Date de sortie',
      marketShare: 'Part de marché (%)',
      isOpenSource: 'Open Source',
    };

    return mapping[key] || key;
  }

  return (
    <div className="h-screen w-full flex flex-col bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50 overflow-hidden">
      <div className="flex flex-col h-full p-4 gap-3 mx-auto w-full max-w-[1200px]">
        {/* En-tête */}
        <div className="flex-shrink-0 space-y-1">
          <div className="flex items-center gap-3">
            <div className="rounded-lg bg-primary p-2 text-primary-foreground shadow-lg">
              <Database className="h-5 w-5" />
            </div>
            <div>
              <h1 className="text-2xl font-bold tracking-tight text-slate-900">
                Systèmes d'Exploitation
              </h1>
              <p className="text-sm text-muted-foreground">
                Recherche dynamique avec filtrage, tri et pagination côté serveur
              </p>
            </div>
          </div>
        </div>

        {/* Badges d'information */}
        <div className="flex-shrink-0 flex flex-wrap gap-2">
          {filterDescriptors.length > 0 && (
            <Badge variant="secondary" className="flex items-center gap-1.5 px-2 py-1 text-xs">
              <Filter className="h-3 w-3" />
              {filterDescriptors.length} champs filtrables
            </Badge>
          )}
          <Badge variant="secondary" className="flex items-center gap-1.5 px-2 py-1 text-xs">
            <SortAsc className="h-3 w-3" />
            Tri multi-colonnes
          </Badge>
          <Badge variant="outline" className="px-2 py-1 text-xs">
            Infinite Scroll Model
          </Badge>
          <Badge variant="outline" className="px-2 py-1 text-xs">
            AG Grid Community
          </Badge>
        </div>

        {/* Carte principale avec la grille - prend tout l'espace restant */}
        <Card className="flex-1 shadow-xl flex flex-col overflow-hidden">
          <CardHeader className="flex-shrink-0 pb-2">
            <CardTitle className="text-lg">Données</CardTitle>
            <CardDescription className="text-sm">
              Utilisez les filtres flottants pour filtrer et les en-têtes pour trier. Le défilement charge automatiquement les données.
            </CardDescription>
          </CardHeader>
          <CardContent className="flex-1 overflow-hidden p-4">
            {/* Grille AG Grid avec Infinite Row Model - prend toute la hauteur disponible */}
            <div className="ag-theme-alpine rounded-lg border shadow-sm h-full w-full">
              <AgGridReact<OperatingSystem>
                columnDefs={columnDefs}
                defaultColDef={defaultColDef}
                onGridReady={onGridReady}
                rowModelType="infinite"
                cacheBlockSize={20}
                cacheOverflowSize={2}
                maxConcurrentDatasourceRequests={1}
                infiniteInitialRowCount={1}
                maxBlocksInCache={10}
                animateRows={true}
                rowSelection="multiple"
                theme="legacy"
              />
            </div>
          </CardContent>
        </Card>

        {/* Footer */}
        <div className="flex-shrink-0 flex items-center justify-between text-xs text-muted-foreground">
          <p>Propulsé par AG Grid Community & Spring Boot</p>
          <p className="font-mono">dynamic-search-api v1.0</p>
        </div>
      </div>
    </div>
  );
};
