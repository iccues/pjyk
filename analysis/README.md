# pjyk-analysis

`pjyk-analysis` 是有希计划的数据分析模块，直接连接 PostgreSQL，读取动漫、映射和平台数据，输出评分与热度统计结果。

## 技术栈

- Python 3.14+
- pandas
- numpy
- SQLAlchemy

## 入口

- `scripts/score.py`：评分统计入口，依赖 `pjyk_analysis`（`src/pjyk_analysis`）
- `scripts/popularity.py`：热度统计入口，依赖 `pjyk_analysis`（`src/pjyk_analysis`）

## 快速开始

如需自定义数据库连接，可复制 `.env.example` 为 `.env` 并配置 `DB_URL`

安装依赖

```bash
cd analysis
uv sync
```

运行脚本

```bash
# 评分统计
uv run python scripts/score.py

# 热度统计
uv run python scripts/popularity.py
```

## 说明

- 脚本会直接查询数据库中的 `anime` 和 `mapping` 表
- 只统计 `review_status = 'APPROVED'` 的数据
- 连接配置在 `src/pjyk_analysis/engine.py`
