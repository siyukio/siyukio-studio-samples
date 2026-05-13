/**
 * Common types used across all API modules
 */

/**
 * Generic pagination request interface
 * @template T - The query parameter type
 */
export interface PageRequest<T> {
  page?: number;
  size?: number;
  filter?: T;
}

/**
 * Generic pagination response interface
 * @template T - The item type
 */
export interface PageResponse<T> {
  items: T[];
  total: number;
}
