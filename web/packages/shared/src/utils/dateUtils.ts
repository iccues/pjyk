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
 * 生成年份选项（从指定年份到当前年份）
 * @param startYear 开始的年份
 * @returns 年份选项数组，包含"全部"选项和年份选项
 */
export function generateYearOptionsFrom(
  startYear: number,
): { label: string; value: number | undefined }[] {
  const currentYear = new Date().getFullYear();
  const years: { label: string; value: number | undefined }[] = [
    { label: "全部", value: undefined },
  ];

  for (let year = currentYear; year >= startYear; year--) {
    years.push({ label: `${year}年`, value: year });
  }

  return years;
}
