# 开发文档索引

日常开发从 [主工程说明](../README.md) 开始；本文集中列出仍有维护用途的文档。历史审计中的问题和测试数字须重新验证，不能直接当作当前状态。

## 环境与协作

- [Git 仓库交接入口](../handover/README.md)、[完整交接文档](handover/项目交接文档.md)、[AI 项目地图](../handover/project.json)。
- [新人入门](ONBOARDING.md)：最短阅读路径和开发边界。
- [环境要求](../ENVIRONMENT.md)、[本地启动](../DEV-SETUP.md)。
- [Android 构建](../android/README.md)、[API 启动与测试](../services/api/README.md)。
- [协作约定](../CONTRIBUTING.md)、[屏幕适配](../android/docs/screen-adaptation.md)。
- [分支合并流程](MERGE-WORKFLOW.md)：需要合并分支时使用。

## 业务实现与验收

- [创作流程接入](../services/api/docs/creation-workflow-integration.md)。
- [创作对话精简方案](creation-conversation-simplification-20260910.md)。
- [创作档案分类](creation-archive-categories-20260912.md)、[成长规则](creation-growth-rules-20260912.md)。
- [大会页面方案](conference-page-plan-and-implementation-prompt-20260911.md)。
- [创作流程验收指南](creation-demo-acceptance.md)：运行工具、前置条件和证据位置。
- [后端交付检查清单](backend-delivery-checklist.md)：待验证门槛，不预填通过状态。
- 其他领域接口说明从 [API 文档入口](../services/api/README.md) 查找。

## 整理记录与历史参考

- [2026-10-11 项目整理记录](PROJECT-CLEANUP-2026-10-11.md)：历次删除范围及核验结果。
- [9 月 9 日审计](CODE-AUDIT-2026-09-09.md)、[9 月 10 日审计](CODE-AUDIT-2026-09-10.md)、[9 月 11 日审计](CODE-AUDIT-2026-09-11.md)：仍有源码注释或审计交叉引用，因此保留原路径，仅作为历史背景。

旧会话日志、重复跨日摘要和证据已失效的 9 月 9 日验收报告已移出当前工作树；原文可从 Git 提交 `b5d2c8c13df325acedeea3e16549f1b0f1449633` 查看。不要求每次会话新增日志或重复审计文档。优先维护已有说明，临时输出写入 `artifacts/` 或 `tmp/`。
