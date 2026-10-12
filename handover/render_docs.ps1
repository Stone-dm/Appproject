<# Regenerate browser-readable companions. Requires PowerShell 7, not runtime usage. #>
$ErrorActionPreference = 'Stop'
$root = Join-Path (Split-Path $PSScriptRoot -Parent) 'docs/handover'
if (-not (Get-Command ConvertFrom-Markdown -ErrorAction SilentlyContinue)) {
    throw 'Document export requires PowerShell 7 ConvertFrom-Markdown. Read the included HTML or Markdown directly.'
}
$css = @'
:root{color-scheme:light;--ink:#192e3a;--muted:#526575;--accent:#087c86;--line:#dbe5ea}
*{box-sizing:border-box}html{scroll-behavior:smooth}body{margin:0;background:#f3f6f8;color:var(--ink);font:16px/1.85 "Microsoft YaHei","PingFang SC",system-ui,sans-serif}
header{background:#152e3a;color:white;padding:30px max(24px,calc((100vw - 1120px)/2));border-bottom:5px solid #13a8ad}header p{margin:6px 0;color:#c7dce4;font-size:14px}header a{color:#c9fcff}
.layout{max-width:1240px;margin:auto;display:grid;grid-template-columns:250px minmax(0,1fr);gap:30px;padding:30px 26px}nav{position:sticky;top:20px;max-height:94vh;overflow:auto;align-self:start;font-size:14px}nav a{display:block;padding:7px 0;border-bottom:1px solid var(--line)}main{background:white;min-width:0;padding:34px 46px;border:1px solid var(--line);border-radius:8px}
h1{font-size:30px;line-height:1.4;margin-top:0}h2{font-size:23px;margin:44px 0 20px;padding-top:18px;border-top:2px solid var(--line);scroll-margin-top:24px}h3{font-size:18px;margin-top:30px;scroll-margin-top:24px}a{color:var(--accent);text-decoration:none;overflow-wrap:anywhere}a:hover{text-decoration:underline}p,li{overflow-wrap:break-word}li{margin-bottom:7px}code{background:#edf3f6;padding:2px 5px;border-radius:4px;font:0.88em/1.6 Consolas,"Microsoft YaHei",monospace;overflow-wrap:anywhere}pre{background:#eef4f7;border-left:3px solid #2997a3;padding:18px;overflow:auto;line-height:1.65}pre code{padding:0;background:none;white-space:pre}blockquote{border-left:4px solid #19a3a8;padding:10px 20px;margin:22px 0;background:#eff9f8}blockquote p{margin:6px 0}.notice{font-size:14px;color:var(--muted)}footer{text-align:center;color:var(--muted);padding:24px;font-size:13px}
@media(max-width:900px){.layout{display:block;padding:16px}nav{position:static;max-height:none;margin:0 0 20px}main{padding:26px 22px}}@media print{body{background:white;font-size:11pt}.layout{display:block;padding:0}nav,header,footer{display:none}main{border:0;padding:0}pre{white-space:pre-wrap;overflow:visible}pre code{white-space:pre-wrap}h2,h3{break-after:avoid}a{color:inherit}}
'@
foreach ($name in @('项目交接文档','00-先读我')) {
    $body = (ConvertFrom-Markdown -Path (Join-Path $root ($name + '.md'))).Html
    $navItems = [System.Collections.Generic.List[string]]::new()
    $body = [regex]::Replace($body, '<h([1-6]) id="[^"]*">(.*?)</h\1>', {
        param($m)
        $plain = [System.Net.WebUtility]::HtmlDecode([regex]::Replace($m.Groups[2].Value, '<[^>]+>', ''))
        $slug = [regex]::Replace($plain.ToLowerInvariant(), '[^\p{L}\p{Nd}\s_-]', '')
        $slug = [regex]::Replace($slug.Trim(), '\s+', '-')
        if ($m.Groups[1].Value -eq '2' -and $plain -ne '阅读导航') {
            $navItems.Add('<a href="#' + $slug + '">' + [System.Net.WebUtility]::HtmlEncode($plain) + '</a>')
        }
        return '<h' + $m.Groups[1].Value + ' id="' + $slug + '">' + $m.Groups[2].Value + '</h' + $m.Groups[1].Value + '>'
    })
    $body = $body.Replace('href="项目交接文档.md"','href="项目交接文档.html"').Replace('href="00-先读我.md"','href="00-先读我.html"')
    $navigation = $navItems -join "`n"
    $html = @"
<!doctype html>
<html lang="zh-CN"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>$name · 机巧江湖项目交接</title><style>$css</style></head>
<body><header><strong>机巧江湖 · 项目交接</strong><p>2026-10-11 / 完整源码、运行与维护说明</p><a href="00-先读我.html">首次使用</a> · <a href="项目交接文档.html">完整文档</a> · <a href="../../handover/project.json">AI 项目地图</a></header>
<div class="layout"><nav aria-label="章节目录">$navigation</nav><main><p class="notice">此页由同名 Markdown 生成，可离线阅读。源码与附录链接须保持完整交接目录结构。</p>$body</main></div>
<footer>内容依据本次交接快照。实际验证范围见 handover/verification.json。</footer></body></html>
"@
    [System.IO.File]::WriteAllText((Join-Path $root ($name + '.html')), $html, [System.Text.UTF8Encoding]::new($false))
    Write-Output ('Generated: ' + $name + '.html')
}
