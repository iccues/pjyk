import { ElMessage, ElMessageBox } from "element-plus";
import { ref } from "vue";
import {
  createMapping,
  deleteMapping,
  getUnmappedMappingList,
  updateMappingAnime,
} from "@/api/admin";
import type { AdminMapping } from "@/types/adminAnime";
import type { DraggableChangeEvent } from "@/types/draggable";

export function useMappingList() {
  const mappingList = ref<AdminMapping[]>([]);
  const mappingDialogVisible = ref(false);

  // 加载未关联映射列表
  const loadMappingList = async () => {
    try {
      const mappings = await getUnmappedMappingList();
      mappingList.value = mappings;
    } catch (e) {
      ElMessage.error("加载映射列表失败: " + (e instanceof Error ? e.message : "未知错误"));
    }
  };

  // 应用映射变化
  const applyMappingChange = async (
    mapping: AdminMapping,
    newAnimeId: number | null,
    onAnimeRemoveMapping?: (animeId: number, mappingId: number) => void,
    onAnimeAddMapping?: (animeId: number, mapping: AdminMapping) => void,
  ) => {
    const oldAnimeId = mapping.animeId;
    if (oldAnimeId === newAnimeId) return;

    try {
      await updateMappingAnime(mapping.mappingId, newAnimeId);

      // 如果之前关联了动画，从原动画中移除
      if (oldAnimeId !== null) {
        if (onAnimeRemoveMapping) {
          onAnimeRemoveMapping(oldAnimeId, mapping.mappingId);
        }
      } else {
        // 从未关联列表中移除
        const idx = mappingList.value.findIndex((m) => m.mappingId === mapping.mappingId);
        if (idx !== -1) mappingList.value.splice(idx, 1);
      }

      // 更新映射的 animeId
      mapping.animeId = newAnimeId;

      // 如果关联到新动画
      if (newAnimeId !== null) {
        if (onAnimeAddMapping) {
          onAnimeAddMapping(newAnimeId, mapping);
        }
        ElMessage.success("映射关联成功");
      } else {
        // 添加到未关联列表
        if (!mappingList.value.some((m) => m.mappingId === mapping.mappingId)) {
          mappingList.value.push(mapping);
        }
        ElMessage.success("已解除映射关联");
      }
    } catch (e) {
      ElMessage.error("更新失败: " + (e instanceof Error ? e.message : "未知错误"));
    }
  };

  // 处理拖拽到动画
  const handleMappingToAnime = (
    evt: DraggableChangeEvent<AdminMapping>,
    animeId: number,
    onAnimeRemoveMapping?: (animeId: number, mappingId: number) => void,
    onAnimeAddMapping?: (animeId: number, mapping: AdminMapping) => void,
  ) => {
    const mapping = evt.added?.element;
    if (mapping) {
      applyMappingChange(mapping, animeId, onAnimeRemoveMapping, onAnimeAddMapping);
    }
  };

  // 处理拖拽到未关联列表
  const handleMappingToUnmapped = (
    evt: DraggableChangeEvent<AdminMapping>,
    onAnimeRemoveMapping?: (animeId: number, mappingId: number) => void,
    onAnimeAddMapping?: (animeId: number, mapping: AdminMapping) => void,
  ) => {
    const mapping = evt.added?.element;
    if (mapping) {
      applyMappingChange(mapping, null, onAnimeRemoveMapping, onAnimeAddMapping);
    }
  };

  // 打开创建 Mapping 对话框
  const handleCreateMapping = () => {
    mappingDialogVisible.value = true;
  };

  // 关闭 Mapping 对话框
  const handleCloseMappingDialog = () => {
    mappingDialogVisible.value = false;
  };

  // 处理创建 Mapping
  const handleSubmitMapping = async (sourcePlatform: string, platformId: string) => {
    try {
      const newMapping = await createMapping(sourcePlatform, platformId);
      mappingList.value.unshift(newMapping);
      ElMessage.success("映射创建成功");
      handleCloseMappingDialog();
    } catch (e) {
      ElMessage.error("创建失败: " + (e instanceof Error ? e.message : "未知错误"));
    }
  };

  // 处理删除 Mapping
  const handleDeleteMapping = async (mappingId: number) => {
    try {
      const mapping = mappingList.value.find((m) => m.mappingId === mappingId);
      if (!mapping) return;

      await ElMessageBox.confirm(`确定要删除该映射吗？此操作不可恢复。`, "确认删除", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      });

      await deleteMapping(mappingId);

      // 从列表中移除
      const idx = mappingList.value.findIndex((m) => m.mappingId === mappingId);
      if (idx !== -1) {
        mappingList.value.splice(idx, 1);
      }

      ElMessage.success("删除成功");
    } catch (e) {
      if (e === "cancel") return;
      ElMessage.error("删除失败: " + (e instanceof Error ? e.message : "未知错误"));
    }
  };

  // 添加映射到未关联列表
  const addMappingToUnmapped = (mapping: AdminMapping) => {
    mapping.animeId = null;
    if (!mappingList.value.some((m) => m.mappingId === mapping.mappingId)) {
      mappingList.value.push(mapping);
    }
  };

  return {
    mappingList,
    mappingDialogVisible,
    loadMappingList,
    applyMappingChange,
    handleMappingToAnime,
    handleMappingToUnmapped,
    handleCreateMapping,
    handleCloseMappingDialog,
    handleSubmitMapping,
    handleDeleteMapping,
    addMappingToUnmapped,
  };
}
