# SESSION-LOG — 2026-10-13

> 承接 [SESSION-LOG-2026-10-12.md](./SESSION-LOG-2026-10-12.md) §41(推送 a2 HHH 完成)。
> 本日主要工作:**扩展 .gitignore 政策——多类一次性文档不入库**。

---

## §42 10-13 — 扩展 .gitignore 排除一次性文档

**用户要求**:"将一些文档不要推送到GitHub"

### 范围

| 类别 | 决定 |
|---|---|
| `docs/SESSION-LOG-*.md` + `docs/_archive/SESSION-LOG-*.md` | ✓ 6f581c2 已处理,沿用 |
| `docs/SUMMARY-*.md` | ✓ 本次新增 |
| `docs/REGRESSION-*.md` | ✓ 本次新增(只匹配 REGRESSION-chuangdang-5stages,不误伤 CHUANGDANG-REGRESSION-PLAYBOOK) |
| `docs/acceptance-*.md` | ✓ 本次新增 |
| `docs/creation-*.md` | ✓ 本次新增(creation-archive/conversation/demo-acceptance/growth-rules) |
| `docs/conference-page-plan-*.md` | ✓ 本次新增 |
| `docs/backend-delivery-checklist.md` | ✓ 本次新增 |

**保留**(用户没要求改):CODE-AUDIT-*、DESIGN-SOURCES、IMAGE-COORDINATE-VERIFICATION、MERGE-PLAN-*、MERGE-WORKFLOW、ONBOARDING、README、TROUBLESHOOTING、CHUANGDANG-REGRESSION-PLAYBOOK、PROJECT-CLEANUP。

### 仓库范围

**只 main(origin/Appproject)**,不动 a2/HHH(20 SESSION-LOG + 20 handover 仍保留)。

### 处理方式

`untrack(本地留,远程不推)` —— 加 .gitignore + `git rm --cached`,文件保留在本地磁盘。

### 待 untrack 的 9 个文件

```
docs/SUMMARY-2026-09-09-to-2026-09-10.md
docs/REGRESSION-chuangdang-5stages-20260919.md
docs/acceptance-20260909.md
docs/creation-archive-categories-20260912.md
docs/creation-conversation-simplification-20260910.md
docs/creation-demo-acceptance.md
docs/creation-growth-rules-20260912.md
docs/conference-page-plan-and-implementation-prompt-20260911.md
docs/backend-delivery-checklist.md
```

### 操作

```bash
# 1. 编辑 .gitignore,在 line 45-46 之后加 6 行新 ignore 模式
#    注释清楚:per 2026-10-13 决定

# 2. untrack 9 个文件
git rm --cached docs/SUMMARY-2026-09-09-to-2026-09-10.md \
              docs/REGRESSION-chuangdang-5stages-20260919.md \
              docs/acceptance-20260909.md \
              docs/creation-archive-categories-20260912.md \
              docs/creation-conversation-simplification-20260910.md \
              docs/creation-demo-acceptance.md \
              docs/creation-growth-rules-20260912.md \
              docs/conference-page-plan-and-implementation-prompt-20260911.md \
              docs/backend-delivery-checklist.md

# 3. commit
git add .gitignore
git commit -m "chore(gitignore): 一次性 docs/ 整个类(SUMMARY/REGRESSION/acceptance/creation/conference/backend-delivery)不入库"

# 4. 等用户决定是否推 main
```

### 验证(commit 后)

- `git ls-files docs/` 应该看不到这 9 个
- `git ls-tree -r origin/main docs/` 远端历史还看得到(commit 链不断)
- `git check-ignore -v docs/SUMMARY-*.md` 应该显示被新规则拦下
- `ls docs/SUMMARY-*.md` 本地文件仍在

### 取回示例(以后想从远端历史捞这 9 个)

```bash
git log --oneline -- docs/creation-demo-acceptance.md
# → 输出最后一次 commit
git show <commit>:docs/creation-demo-acceptance.md > /tmp/recovered.md
```

### 待办

- [ ] 实际执行(等系统执行 .gitignore 改动 + untrack + commit)
- [ ] 决定是否推 main(等用户确认)
- [ ] 是否要同步反转"docs/ 公共文档保留"列表(让 README.md 索引更准确)
