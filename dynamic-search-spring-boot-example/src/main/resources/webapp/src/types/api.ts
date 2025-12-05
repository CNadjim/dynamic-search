// Types pour l'API de recherche dynamique
// Ces types correspondent exactement aux contrats de l'API REST

export interface OperatingSystem {
  id?: number | string;
  name: string;
  version: string;
  kernel: string;
  releaseDate: string;
  usages: number;
}

export interface FilterRequest {
  key: string;
  operator: FilterOperator;
  value?: any;
  valueTo?: any;
  values?: any[];
}

// Opérateurs en camelCase comme dans l'API
export type FilterOperator =
  | 'equals'
  | 'notEquals'
  | 'lessThan'
  | 'greaterThan'
  | 'contains'
  | 'notContains'
  | 'startsWith'
  | 'endsWith'
  | 'in'
  | 'notIn'
  | 'between'
  | 'blank'
  | 'notBlank';

export interface SortRequest {
  key: string;
  direction: 'asc' | 'desc';
}

export interface PageRequest {
  number: number;
  size: number;
}

export interface FullTextRequest {
  query: string;
}

export interface SearchRequest {
  filters: FilterRequest[];
  sorts?: SortRequest[];
  fullText?: FullTextRequest;
  page?: PageRequest;
}

export interface SearchResult<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  pageNumber: number;
  pageSize: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

// Types de champs en lowercase comme dans l'API
export type FieldType = 'string' | 'number' | 'date' | 'boolean';

export interface FilterDescriptorResponse {
  key: string;
  fieldType: FieldType;
  nullable: boolean;
  availableOperators: FilterOperator[];
}

// Mapping des types vers les labels français
export const FIELD_TYPE_LABELS: Record<FieldType, string> = {
  string: 'Texte',
  number: 'Nombre',
  date: 'Date',
  boolean: 'Booléen',
};

// Mapping des opérateurs vers les labels français
export const OPERATOR_LABELS: Record<FilterOperator, string> = {
  equals: 'Égal à',
  notEquals: 'Différent de',
  lessThan: 'Inférieur à',
  greaterThan: 'Supérieur à',
  contains: 'Contient',
  notContains: 'Ne contient pas',
  startsWith: 'Commence par',
  endsWith: 'Se termine par',
  in: 'Dans la liste',
  notIn: 'Pas dans la liste',
  between: 'Entre',
  blank: 'Vide',
  notBlank: 'Non vide',
};
