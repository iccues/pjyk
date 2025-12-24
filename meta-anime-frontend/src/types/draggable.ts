/**
 * Vuedraggable 相关类型定义
 *
 * 由于 vuedraggable 库本身的类型定义不够完善，
 * 这里提供更精确的类型定义以提升类型安全性。
 */

/**
 * 拖拽事件的通用接口
 */
export interface DraggableEvent<T = unknown> {
  /** 添加元素时触发 */
  added?: {
    element: T
    newIndex: number
  }
  /** 移除元素时触发 */
  removed?: {
    element: T
    oldIndex: number
  }
  /** 元素移动时触发（同一列表内） */
  moved?: {
    element: T
    oldIndex: number
    newIndex: number
  }
  /** 原始 Sortable.js 事件 */
  originalEvent?: Event
}

/**
 * 拖拽更新事件（用于 @change 事件）
 */
export interface DraggableChangeEvent<T = unknown> {
  added?: DraggableEvent<T>['added']
  removed?: DraggableEvent<T>['removed']
  moved?: DraggableEvent<T>['moved']
}
