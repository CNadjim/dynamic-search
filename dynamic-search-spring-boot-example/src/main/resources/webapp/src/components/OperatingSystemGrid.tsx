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
import { operatingSystemApi, type Technology } from '../services/api';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from './ui/card';
import { Badge } from './ui/badge';
import { Tabs, TabsList, TabsTrigger } from './ui/tabs';
import { Database, Filter, SortAsc, Search, Clock } from 'lucide-react';
import { Input } from './ui/input';

// Enregistrer tous les modules AG Grid Community
ModuleRegistry.registerModules([AllCommunityModule]);

export const OperatingSystemGrid = () => {
  const [technology, setTechnology] = useState<Technology>('jpa');
  const [filterDescriptors, setFilterDescriptors] = useState<FilterDescriptorResponse[]>([]);
  const [gridApi, setGridApi] = useState<any>(null);
  const [fullTextQuery, setFullTextQuery] = useState('');
  const [responseTime, setResponseTime] = useState<number | null>(null);

  // Charger les métadonnées des filtres quand la technologie change
  useEffect(() => {
    const loadFilterDescriptors = async () => {
      try {
        const descriptors = await operatingSystemApi.getAvailableFilters(technology);
        setFilterDescriptors(descriptors);
        console.log(`Descripteurs de filtres chargés pour ${technology}:`, descriptors);
      } catch (err) {
        console.error('Erreur lors du chargement des descripteurs:', err);
      }
    };

    loadFilterDescriptors();
  }, [technology]);

  /**
   * Formate une valeur de date pour l'API (supprime l'heure)
   */
  const formatDateValue = (value: any): string => {
    if (!value) return value;

    if (typeof value === 'string') {
      const dateMatch = value.match(/^(\d{4}-\d{2}-\d{2})/);
      if (dateMatch) {
        return dateMatch[1];
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

    let filterValue = dateFrom || filter;
    let filterToValue = dateTo || filterTo;

    const isDateFilter = dateFrom !== undefined || dateTo !== undefined;
    if (isDateFilter) {
      filterValue = formatDateValue(filterValue);
      filterToValue = formatDateValue(filterToValue);
    }

    if (operator === 'between' && filterToValue !== undefined) {
      apiFilter.value = filterValue;
      apiFilter.valueTo = filterToValue;
    } else if (operator !== 'blank' && operator !== 'notBlank') {
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
          technology,
          fullTextQuery,
        });

        try {
          const pageSize = params.endRow! - params.startRow!;
          const pageNumber = Math.floor(params.startRow! / pageSize);

          const page = {
            number: pageNumber,
            size: pageSize,
          };

          const sorts: SortRequest[] = (params.sortModel || []).map((sort: SortModelItem) => ({
            key: sort.colId,
            direction: sort.sort as 'asc' | 'desc',
          }));

          const filters: FilterRequest[] = [];
          const filterModel = params.filterModel || {};

          Object.keys(filterModel).forEach((field) => {
            const apiFilter = mapAgGridFilterToApiFilter(field, (filterModel as any)[field]);
            if (apiFilter) {
              filters.push(apiFilter);
            }
          });

          const searchRequest: SearchRequest = {
            filters,
            sorts: sorts.length > 0 ? sorts : [{ key: 'name', direction: 'asc' }],
            fullText: fullTextQuery ? { query: fullTextQuery } : undefined,
            page,
          };

          console.log('API Search Request:', searchRequest);

          const { result, responseTime: time } = await operatingSystemApi.search(
            searchRequest,
            technology
          );
          setResponseTime(time);

          const lastRow = result.last ? params.startRow! + result.content.length : undefined;

          params.successCallback(result.content, lastRow);
        } catch (error) {
          console.error('Erreur lors du chargement des données:', error);
          params.failCallback();
        }
      },
    };
  };

  // Générer les colonnes dynamiquement
  const columnDefs: ColDef<OperatingSystem>[] = useMemo(() => {
    if (filterDescriptors.length === 0) {
      return [];
    }

    const columns: ColDef<OperatingSystem>[] = [];

    filterDescriptors.forEach((descriptor) => {
      const column: ColDef<OperatingSystem> = {
        field: descriptor.key as keyof OperatingSystem,
        headerName: formatHeaderName(descriptor.key),
        sortable: true,
        resizable: true,
        filter: true,
        floatingFilter: true,
      };

      switch (descriptor.fieldType) {
        case 'number':
          column.filter = 'agNumberColumnFilter';
          column.filterParams = {
            suppressAndOrCondition: true,
          };
          column.width = 150;
          if (descriptor.key === 'usages') {
            column.valueFormatter = (params) => {
              if (params.value === null || params.value === undefined) return '';
              return new Intl.NumberFormat('fr-FR').format(params.value);
            };
          }
          break;

        case 'date':
          column.filter = 'agDateColumnFilter';
          column.filterParams = {
            suppressAndOrCondition: true,
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
            suppressAndOrCondition: true,
          };
          column.width = 140;
          column.cellRenderer = (params: any) => {
            return params.value ? '✅ Oui' : '❌ Non';
          };
          break;

        case 'string':
          column.filter = 'agTextColumnFilter';
          column.filterParams = {
            suppressAndOrCondition: true,
          };
          if (descriptor.key === 'id') {
            column.width = 100;
          } else {
            column.flex = 1;
          }
          break;
      }

      columns.push(column);
    });

    return columns;
  }, [filterDescriptors]);

  const defaultColDef = useMemo<ColDef>(
    () => ({
      resizable: true,
      sortable: true,
      filter: true,
      floatingFilter: true,
      filterParams: {
        suppressAndOrCondition: true,
      },
    }),
    []
  );

  const onGridReady = (params: GridReadyEvent) => {
    setGridApi(params.api);

    if (filterDescriptors.length > 0) {
      const datasource = createInfiniteDatasource();
      params.api.setGridOption('datasource', datasource);
    }
  };

  // Reconfigurer la datasource quand les descripteurs, la technologie ou le fullText changent
  useEffect(() => {
    if (gridApi && filterDescriptors.length > 0) {
      const datasource = createInfiniteDatasource();
      gridApi.setGridOption('datasource', datasource);
    }
  }, [gridApi, filterDescriptors, technology, fullTextQuery]);

  function formatHeaderName(key: string): string {
    const mapping: Record<string, string> = {
      id: 'ID',
      name: 'Nom',
      version: 'Version',
      kernel: 'Kernel',
      releaseDate: 'Date de sortie',
      usages: 'Utilisations',
    };

    return mapping[key] || key;
  }

  const getTechLabel = (tech: Technology) => {
    const labels = {
      jpa: 'PostgreSQL (JPA)',
      mongo: 'MongoDB',
      elastic: 'Elasticsearch',
    };
    return labels[tech];
  };

  const getTechBadgeVariant = (tech: Technology) => {
    const variants = {
      jpa: 'default',
      mongo: 'secondary',
      elastic: 'outline',
    };
    return variants[tech] as 'default' | 'secondary' | 'outline';
  };

  return (
    <div className="h-screen w-full flex flex-col bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50 overflow-hidden">
      <div className="flex flex-col h-full p-4 gap-3 mx-auto w-full max-w-[1400px]">
        {/* En-tête */}
        <div className="flex-shrink-0 space-y-1">
          <div className="flex items-center gap-3">
            <div className="rounded-lg bg-primary p-2 text-primary-foreground shadow-lg">
              <Database className="h-5 w-5" />
            </div>
            <div>
              <h1 className="text-2xl font-bold tracking-tight text-slate-900">
                Dynamic Search - Multi-Technology Demo
              </h1>
              <p className="text-sm text-muted-foreground">
                Comparez les performances de PostgreSQL, MongoDB et Elasticsearch
              </p>
            </div>
          </div>
        </div>

        {/* Tabs pour choisir la technologie */}
        <Tabs value={technology} onValueChange={(value) => setTechnology(value as Technology)} className="flex-shrink-0">
          <TabsList className="grid w-full max-w-md grid-cols-3">
            <TabsTrigger value="jpa">PostgreSQL</TabsTrigger>
            <TabsTrigger value="mongo">MongoDB</TabsTrigger>
            <TabsTrigger value="elastic">Elasticsearch</TabsTrigger>
          </TabsList>
        </Tabs>

        {/* Badges d'information */}
        <div className="flex-shrink-0 flex flex-wrap gap-2 items-center">
          <Badge variant={getTechBadgeVariant(technology)} className="flex items-center gap-1.5 px-2 py-1 text-xs">
            <Database className="h-3 w-3" />
            {getTechLabel(technology)}
          </Badge>
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
          {responseTime !== null && (
            <Badge variant="outline" className="flex items-center gap-1.5 px-2 py-1 text-xs">
              <Clock className="h-3 w-3" />
              {responseTime} ms
            </Badge>
          )}
        </div>

        {/* Recherche full-text */}
        <div className="flex-shrink-0">
          <div className="relative max-w-md">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              type="text"
              placeholder="Recherche globale dans tous les champs..."
              value={fullTextQuery}
              onChange={(e: React.ChangeEvent<HTMLInputElement>) => setFullTextQuery(e.target.value)}
              className="pl-10"
            />
          </div>
        </div>

        {/* Carte principale avec la grille */}
        <Card className="flex-1 shadow-xl flex flex-col overflow-hidden">
          <CardHeader className="flex-shrink-0 pb-2">
            <CardTitle className="text-lg">Données</CardTitle>
            <CardDescription className="text-sm">
              Utilisez les filtres flottants pour filtrer et les en-têtes pour trier. Le défilement charge automatiquement les données.
            </CardDescription>
          </CardHeader>
          <CardContent className="flex-1 overflow-hidden p-4">
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
          <p className="font-mono">dynamic-search-api v2.0</p>
        </div>
      </div>
    </div>
  );
};
