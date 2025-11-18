export interface PageInfo {
  size: number
  number: number
  totalElements: number
  totalPages: number
}

export interface Page<T> {
  content: T[]
  page: PageInfo
}
