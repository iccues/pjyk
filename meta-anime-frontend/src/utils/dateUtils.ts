/**
 * 生成年份选项（从当前年份往前推指定年数）
 * @param yearsBack 往前推的年数，默认为 10 年
 * @returns 年份选项数组，包含"全部"选项和年份选项
 */
export function generateYearOptions(
  yearsBack: number = 10,
): { label: string; value: number | undefined }[] {
  const currentYear = new Date().getFullYear();
  const years: { label: string; value: number | undefined }[] = [
    { label: "全部", value: undefined },
  ];

  for (let i = 0; i <= yearsBack; i++) {
    const year = currentYear - i;
    years.push({ label: `${year}年`, value: year });
  }

  return years;
}

/**
 * 格式化日期为 YYYY-MM-DD 格式
 * @param date 日期对象或日期字符串
 * @returns 格式化后的日期字符串
 */
export function formatDate(date: Date | string): string {
  const d = typeof date === "string" ? new Date(date) : date;
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

/**
 * 验证日期格式是否为 YYYY-MM-DD
 * @param dateStr 日期字符串
 * @returns 是否为有效的日期格式
 */
export function isValidDateFormat(dateStr: string): boolean {
  return /^\d{4}-\d{2}-\d{2}$/.test(dateStr);
}
