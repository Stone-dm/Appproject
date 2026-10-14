package com.jueqiao.jianghu.ui.screens.houshan9

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jueqiao.jianghu.R
import com.jueqiao.jianghu.ui.components.AnimatedCloudImage
import com.jueqiao.jianghu.ui.components.CloudMotion
import com.jueqiao.jianghu.ui.components.CloudTintCool
import com.jueqiao.jianghu.ui.components.FocusCloudBand
import com.jueqiao.jianghu.ui.components.HoushanMistLayer
import com.jueqiao.jianghu.ui.components.HoushanMistVariant
import com.jueqiao.jianghu.ui.components.rememberCloudProgress
import com.jueqiao.jianghu.ui.theme.YaHei
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ── 后山9 → 后山10 沉浸式纵深推进(dolly-in)参数 (2026-09-19 §2 **已启用**)──

// 参数沿用 2026-09-16 §36 后山2 首创、经 §24 后山3 / §33 后山4 / 2026-09-18 §4 后山5 / §9 后山6 / §10 后山7 / §11 后山8 演进的同款。
// 参数沿用后山2 §36 / 后山3 §24 / 后山4 §33 / 后山5 2026-09-18 §4 / 后山6 §9 / 后山7 §10 的同款。
private const val DOLLY_DURATION_MS = 1050
// 半程交给导航:此时山体已推进 3/4,由后山10 交叉淡入接棒,取代硬切
private const val DOLLY_HANDOFF_MS = 560L
// 三个景深平面各自的推进幅度 —— 近景推得多、远景推得少,差值即"纵深"
private const val DOLLY_BG_SCALE = 0.34f      // 主山峰/近景山体 1.00 → 1.34
private const val DOLLY_CLOUD_SCALE = 0.09f   // 云雾 1.00 → 1.09(相对山体后移)
private const val DOLLY_LABEL_SCALE = 0.34f   // 标签与山体同速,避免相对滑动
// 灭点(视觉焦点):略高于画面中心
private const val FOCAL_X = 0.5f
private const val FOCAL_Y = 0.48f

/**
 * 后山9 页所有可调用 action —— 用 data class 一次传入,避免 §3 的 slot 0 null bug
 * 详见 §5(SESSION-LOG-2026-09-18)真机验证根因:多 lambda 签名 → 1 个 data class,bug 触发条件消失。
 */
data class Houshan9Actions(
    val onBack: () -> Unit = {},
    // ── 2026-09-19 §2:后山9 从"终点页"**升级为过场页** —— 标签触发 dolly 推进 ──
    //   用户指令:"那种山峰拉近的动画也要在跳转到后山10页面时出现" + 触发方式"点击赏罚驭灵诀"
    val onOpenHoushan10: () -> Unit = {},   // dolly 半程后调用:赏罚驭灵诀 / 听言解意篇
    // ── 2026-09-19 §3:后山11 创建 —— 「正心守道录」改推后山11 ──
    //   用户指令:"点击任意页面的'正心守道录'标签" → 每页的该标签都触发 dolly 推进到后山11
    val onOpenHoushan11: () -> Unit = {},   // dolly 半程后调用:正心守道录
    // ── 卷跳转(2026-09-19 §2 临时改 · 2026-10-13 §1 恢复 §15)──
    //   2026-10-13 §1:用户决定恢复 §15 规则 —— 赏罚驭灵诀(本页 Y 最大,Y=521)点 → 直接跳第八卷-1,无 dolly。
    //   dolly 推后山10/11 的入口由「听言解意篇 / 正心守道录」承担(它们在各自页面是 Y 最大,
    //   跳过去对应后山正是 §15 的"非 Y 最大 → 该文本 Y 最大页")。
    val onOpenVolume8Part1: () -> Unit = {},
    val onOpenVolume9Part1: () -> Unit = {},
    val onOpenVolume10Part1: () -> Unit = {},
)

/**
 * 后山9 页 — 后山8 页 dolly 推进而来;点击"返回"按钮回到后山8;
 * **3 个标签都触发 dolly 推进**(赏罚驭灵诀 / 听言解意篇 → 后山10;正心守道录 → 后山11,2026-09-19 §3)。
 *
 * 2026-09-18 §11 新建:用户指令"创建后山9页面...后山9复用后山7页面的素材和动画"。
 * 2026-09-19 §2 升级:从"终点页"变成"过场页" —— 补上跳转入口,
 *                    触发方式是**点击 3 个标签中的任意一个**(用户指定"点击赏罚驭灵诀"),
 *                    整屏点击**保持 noop**(与后山2~8 一致,用户 §13 已取消)。
 *
 * ══════════════════════════════════════════════════════════════════════════
 * 【2026-09-19 §2 交互变更】从"终点页"变成"过场页"
 *
 *   2026-09-18 §11~§16 期间:后山9 是**终点页** —— 整屏 clickable = noop,
 *                dolly 代码保留为模板但未启用;标签或跳卷、或死区。
 *   2026-09-19 §2 起:用户要求"那种山峰拉近的动画也要在跳转到后山10页面时出现",
 *                触发方式指定为**点击「赏罚驭灵诀」** → 3 个标签统一触发 `startDollyIn()`,
 *                dolly 半程后调 `actions.onOpenHoushan10()` 推进到后山10。
 *                ✅ 同 2026-09-18 §39→§40 后山7 的升级路径(那次的 5 件事已全部落地)。
 *   2026-09-19 §3 起:后山11 创建 → `startDollyIn` 改为**接收跳转目标**的参数化形式,
 *                「正心守道录」改推后山11(用户指令:"点击任意页面的'正心守道录'标签")。
 *
 *   📜 **2026-10-13 §1 恢复 §15**:本页赏罚驭灵诀(Y=521,Y 最大)→ 卷 8-1 直接(无 dolly);dolly
 *      推后山10/11 由听言解意篇/正心守道录(非 Y 最大,对应 §15 的"非 Y 最大 → 该文本 Y 最大页")承担。
 *      2026-09-19 §2 曾让赏罚驭灵诀改走 dolly,以至后山9 失去 → 卷 8 入口;本次回退 §2 决策。
 * ══════════════════════════════════════════════════════════════════════════
 * 【点击行为 · 当前】
 *
 *   | 点击位置                  | 结果                          |
 *   |--------------------------|-------------------------------|
 *   | 标签2 赏罚驭灵诀(Y=521)   | → **第八卷-1**(本页 Y 最大,§15 规则;2026-10-13 §1 恢复)|
 *   | 标签3 听言解意篇(Y=390)   | → **dolly 推进 → 后山10**(它在后山10 是 Y 最大页)|
 *   | 标签4 正心守道录(Y=295)   | → **dolly 推进 → 后山11**(2026-09-19 §3 改动)|
 *   | 其余任意位置(空白/云/熊猫)| → ❌ noop(整屏点击仍取消)|
 *   | 左上角返回按钮 / 系统返回键 | → 后山8                       |
 *   | (dolly 进行中)点任何位置   | ❌ 无效(标签带 `enabled = !isTransitioning` 门检)|
 * ══════════════════════════════════════════════════════════════════════════
 *
 * 复用(与后山 7 同款素材):
 *   - 全屏背景图 img_shilian2_bg.png
 *   - 云雾层 HoushanMistLayer(variant = HoushanMistVariant.Houshan3)
 *   - 5 朵 ACI 动画云(58 / FCB左下 / 60 / FCB中下 / 62)+ 5 朵老云(old / 56 / 58 / 57 / 5)
 *   - 熊猫 img_shilian2_recovered_8,X=118 Y=405 W=181 H=96
 *   - 3 个标签    赏罚驭灵诀 / 听言解意篇 / 正心守道录(位置与后山7 完全一致,2026-09-18 §11 文案重命名)
 *
 * 布局(与后山7 一致):
 *   - 全屏背景图(img_shilian2_bg.png)
 *   - 5 朵 ACI 动画云 + 5 朵老云
 *   - 熊猫图像(X=118, Y=405, W=181, H=96)
 *   - 标签2 赏罚驭灵诀(X=124, Y=521, W=96, H=170)—— 字号 14sp,行间距 14sp,5×14=70dp < 容器 80dp ✓
 *   - 标签3 听言解意篇(X=43, Y=390, W=51, H=91)—— 字号 10sp,行间距 9sp,5×9=45dp < 容器 60dp ✓
 *   - 标签4 正心守道录(X=105, Y=295, W=30, H=53.5)—— 字号 4sp,行间距 6sp,5×6=30dp < 容器 60dp ✓
 *   - 返回按钮(X=30, Y=60, W=18, H=18)
 */
@Composable
fun Houshan9Screen(
    actions: Houshan9Actions = Houshan9Actions(),
) {
    // §3 + 2026-09-18 §4:用 data class 包成 1 个参数,bug 触发条件(slot 0 = lambda)消失,无需 safeXxx 兜底
    // (旧版的 if (xxx == null) ({}) else xxx 5 行兜底已删)

    val scope = rememberCoroutineScope()
    var isTransitioning by remember { mutableStateOf(false) }
    // 0 → 1 的推进进度;三个景深平面共用同一个进度值,保证同步
    val dolly = remember { Animatable(0f) }

    // 2026-09-18 §13:导航已断开 → BackHandler 保留拦截但动作置空(按返回键不跳转)
    BackHandler(enabled = !isTransitioning) { actions.onBack() }   // 2026-09-19 §2 升级为过场页:过渡期间禁用返回手势

    // 熊猫上下浮 + 呼吸缩放(与后山1/2/3 同款参数)
    val pandaTransition = rememberInfiniteTransition(label = "pandaFloat")
    val pandaScale by pandaTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pandaScale",
    )
    val pandaDy by pandaTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pandaDy",
    )

    // §21 5 朵 ACI 动画云进度(与直接复用源后山7 同一套周期)
    val c58Progress = rememberCloudProgress(21_400, "cloud58")
    val c60Progress = rememberCloudProgress(8_000, "cloud60")
    val c62Progress = rememberCloudProgress(9_000, "cloud62")

    // §21f 5 朵老云动画进度(与直接复用源后山7 同一套)
    val oOldProgress = rememberCloudProgress(4_300, "oldOld")
    val o56Progress = rememberCloudProgress(3_700, "old56")
    val o58Progress = rememberCloudProgress(9_500, "old58")
    val o57Progress = rememberCloudProgress(8_100, "old57")
    val o5Progress = rememberCloudProgress(7_900, "old5")

    // ── 由 dolly 进度派生三个景深平面 + UI chrome 的当前值 (2026-09-19 §2:已启用) ──────
    val p = dolly.value
    val bgScale = 1f + DOLLY_BG_SCALE * p
    val cloudScale = 1f + DOLLY_CLOUD_SCALE * p
    val labelScale = 1f + DOLLY_LABEL_SCALE * p
    val cloudFade = (1f - p).coerceIn(0f, 1f)
    val labelFade = (1f - p * 1.4f).coerceIn(0f, 1f)   // 文字比云雾先淡出,视线留给山体
    val chromeFade = (1f - p * 1.8f).coerceIn(0f, 1f)
    val focal = TransformOrigin(FOCAL_X, FOCAL_Y)

    // 2026-09-19 §2:后山9 升级为过场页 —— 标签触发 dolly 推进;整屏 clickable 保持 noop。
    // 2026-09-19 §3:改为**接收跳转目标**的参数化形式 —— 赏罚驭灵诀/听言解意篇 → 后山10,
    //   正心守道录 → 后山11(用户指令:"点击任意页面的'正心守道录'标签")。
    val startDollyIn: (() -> Unit) -> Unit = { onComplete ->
        if (!isTransitioning) {
            isTransitioning = true
            scope.launch {
                dolly.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = DOLLY_DURATION_MS, easing = FastOutSlowInEasing),
                )
            }
            scope.launch {
                delay(DOLLY_HANDOFF_MS)
                onComplete()   // 2026-09-19 §3:dolly 半程后推进到调用者指定的页面
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            // 2026-09-19 §2:整屏 clickable 保持 **noop** —— 本页的跳转入口是 3 个标签,不是整屏。
            //   2026-09-19 §2:本页已升级为过场页,但**触发点是 3 个标签**(用户指定"点击赏罚驭灵诀"),整屏保持 noop。
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
            ),
    ) {
        // ── 景深平面 1:背景山体(推进最多 → "向用户靠近")────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = bgScale
                    scaleY = bgScale
                    transformOrigin = focal
                },
        ) {
            // 全屏背景图(试炼转换.png —— 与直接复用源后山7 同一张;沿革:后山7←后山5←后山3)
            Image(
                painter = painterResource(R.drawable.img_shilian2_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )

            // 云雾层(程序化水墨云海,持续循环)— 用 HoushanMistVariant.Houshan3 变体(与后山7 同)
            //   注意:"Houshan3" 是变体枚举名(背景是那张水墨画作),与"复用源是后山几"无关
            HoushanMistLayer(variant = HoushanMistVariant.Houshan3)
        }

        // 内容层(避开系统导航条)— 标签 Box 自带 clickable 消费事件,点击标签不会冒泡触发 dolly
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            // ── 景深平面 2:云雾(推进最少 + 淡出 → 相对山体后移)─────────────
            // 把 5 朵 ACI + 5 朵老云全部包进一个 graphicsLayer(scale + alpha),
            // dolly 时一起 ×1.09 放大 + 一起淡出,层次一致,不会"留在屏上不动"。
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = cloudScale
                        scaleY = cloudScale
                        transformOrigin = focal
                        alpha = cloudFade
                    },
            ) {
                // ══ §21 动画云元素竖排(5 个,与后山7 完全同一套)════════════════
                // y 序列(顶边):ACI58 8 · FCB左下 178.5 · ACI60 330.5 · FCB中下 477.5 · ACI62 620
                AnimatedCloudImage(
                    painter = painterResource(R.drawable.img_houshan1_cloud_58),
                    contentDescription = "云朵58",
                    xOffset = -60f,
                    yOffset = 8f,
                    widthDp = 180f,
                    heightDp = 101.5f,
                    progress = c58Progress,
                    phase = 0.00f,
                    motion = CloudMotion.Oscillate,
                    amplitudeX = 200f,
                    amplitudeY = 10f,
                    baseAlpha = 0.50f,
                    alphaAmp = 0.25f,
                    tint = CloudTintCool,
                )
                FocusCloudBand(
                    xOffset = 0f,
                    yOffset = 178.5f,
                    widthDp = 180f,
                    heightDp = 90f,
                    motion = CloudMotion.DriftWrap,
                    amplitudeX = 0f,
                    amplitudeY = 10f,
                    baseAlpha = 0.50f,
                    alphaAmp = 0.30f,
                    periodMs = 11_000,
                )
                AnimatedCloudImage(
                    painter = painterResource(R.drawable.img_houshan1_cloud_60),
                    contentDescription = "云朵60",
                    xOffset = 0f,
                    yOffset = 330.5f,
                    widthDp = 210f,
                    heightDp = 81f,
                    progress = c60Progress,
                    phase = 0.35f,
                    motion = CloudMotion.DriftWrap,
                    amplitudeX = 0f,
                    amplitudeY = 8f,
                    baseAlpha = 0.50f,
                    alphaAmp = 0.25f,
                )
                FocusCloudBand(
                    xOffset = 0f,
                    yOffset = 477.5f,
                    widthDp = 240f,
                    heightDp = 82.5f,
                    motion = CloudMotion.DriftWrapLeft,
                    amplitudeX = 0f,
                    amplitudeY = 10f,
                    baseAlpha = 0.50f,
                    alphaAmp = 0.30f,
                    periodMs = 10_000,
                )
                AnimatedCloudImage(
                    painter = painterResource(R.drawable.img_houshan3_cloud_62),
                    contentDescription = "云朵62",
                    xOffset = 50f,
                    yOffset = 620f,
                    widthDp = 180f,
                    heightDp = 73.5f,
                    progress = c62Progress,
                    phase = 0.70f,
                    motion = CloudMotion.Oscillate,
                    amplitudeX = 200f,
                    amplitudeY = 8f,
                    baseAlpha = 0.50f,
                    alphaAmp = 0.25f,
                )

                // ══ §21f 5 朵老云(与后山7 完全同一套)════════════════════════════
                AnimatedCloudImage(
                    painter = painterResource(R.drawable.img_shilian3_cloud_old),
                    contentDescription = "旧云朵",
                    xOffset = -46f, yOffset = 476f,
                    widthDp = 331f, heightDp = 92f,
                    progress = oOldProgress, phase = 0.00f,
                    motion = CloudMotion.Oscillate,
                    amplitudeX = 80f, amplitudeY = 10f,
                    baseAlpha = 0.75f, alphaAmp = 0.25f,
                    pulseBase = 0f, pulseAmp = 0f,
                )

                AnimatedCloudImage(
                    painter = painterResource(R.drawable.img_shilian3_cloud_56),
                    contentDescription = "云朵56",
                    xOffset = 263f, yOffset = 755f,
                    widthDp = 335f, heightDp = 297f,
                    progress = o56Progress, phase = 0.42f,
                    motion = CloudMotion.Oscillate,
                    amplitudeX = 70f, amplitudeY = 16f,
                    baseAlpha = 0.75f, alphaAmp = 0.25f,
                    pulseBase = 0f, pulseAmp = 0f,
                )

                AnimatedCloudImage(
                    painter = painterResource(R.drawable.img_shilian3_cloud_58),
                    contentDescription = "云朵58",
                    xOffset = 0f, yOffset = 170f,
                    widthDp = 331f, heightDp = 92f,
                    progress = o58Progress, phase = 0.18f,
                    motion = CloudMotion.DriftWrap,
                    amplitudeX = 0f, amplitudeY = 10f,
                    baseAlpha = 0.75f, alphaAmp = 0.25f,
                    pulseBase = 0f, pulseAmp = 0f,
                )

                AnimatedCloudImage(
                    painter = painterResource(R.drawable.img_shilian3_cloud_57),
                    contentDescription = "云朵57",
                    xOffset = 0f, yOffset = 500f,
                    widthDp = 225f, heightDp = 191f,
                    progress = o57Progress, phase = 0.63f,
                    motion = CloudMotion.DriftWrap,
                    amplitudeX = 0f, amplitudeY = 12f,
                    baseAlpha = 0.75f, alphaAmp = 0.25f,
                    pulseBase = 0f, pulseAmp = 0f,
                )

                AnimatedCloudImage(
                    painter = painterResource(R.drawable.img_shilian3_cloud_5),
                    contentDescription = "云朵5",
                    xOffset = 0f, yOffset = 308f,
                    widthDp = 225f, heightDp = 191f,
                    progress = o5Progress, phase = 0.81f,
                    motion = CloudMotion.DriftWrapLeft,
                    amplitudeX = 0f, amplitudeY = 12f,
                    baseAlpha = 0.75f, alphaAmp = 0.25f,
                    pulseBase = 0f, pulseAmp = 0f,
                )
            }

            // ── 景深平面 3:标签 + 熊猫(与山体同速推进 + 淡出 → 不相对滑动)──
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = labelScale
                        scaleY = labelScale
                        transformOrigin = focal
                        alpha = labelFade
                    },
            ) {
                // 熊猫图像(未标题-1-恢复的 8.png,X=118, Y=405, W=181, H=96)— 与直接复用源后山7 同
                Image(
                    painter = painterResource(R.drawable.img_shilian2_recovered_8),
                    contentDescription = "熊猫",
                    modifier = Modifier
                        .offset(x = 118.dp, y = (405f + pandaDy).dp)
                        .size(width = 181.dp, height = 96.dp)
                        .graphicsLayer(
                            scaleX = pandaScale,
                            scaleY = pandaScale,
                        ),
                    contentScale = ContentScale.FillBounds,
                )

                // "标签3" 图像(听言解意篇,X=43, Y=390, W=51, H=91)— 2026-09-19 §3:dolly 推进 → 后山10(该文本在后山10 是 Y 最大)
                Box(
                    modifier = Modifier
                        .offset(x = 43.dp, y = 390.dp)
                        .size(width = 51.dp, height = 91.dp)
                        .clickable(enabled = !isTransitioning, onClick = { startDollyIn(actions.onOpenHoushan10) })   /* 2026-09-19 §3:dolly → 后山10 */,
                ) {
                    Image(
                        painter = painterResource(R.drawable.img_shilian_recovered_4),
                        contentDescription = "标签3",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds,
                    )
                    Text(
                        text = "听\n言\n解\n意\n篇",
                        color = Color.Black,
                        // §34 用户指令:行间距 9sp(略小于字号 10sp,字符紧凑);5×9=45dp,容器 60dp 留 15dp 余量
                        style = TextStyle(fontFamily = YaHei, fontSize = 10.sp, lineHeight = 9.sp),
                        modifier = Modifier
                            .offset(x = 20.5.dp, y = 25.dp)
                            .size(width = 12.dp, height = 60.dp),
                    )
                    Text(
                        text = "炼",
                        color = Color(0xFF385816),
                        style = TextStyle(fontFamily = YaHei, fontSize = 6.sp),
                        modifier = Modifier
                            .offset(x = 22.dp, y = 12.dp)
                            .size(width = 10.dp, height = 14.dp),
                    )
                }

                // "标签4" 图像(正心守道录,X=105, Y=295, W=30, H=53.5)— 2026-09-19 §3:触发 dolly → **后山11**
                Box(
                    modifier = Modifier
                        .offset(x = 105.dp, y = 295.dp)
                        .size(width = 30.dp, height = 53.5.dp)
                        .clickable(enabled = !isTransitioning, onClick = { startDollyIn(actions.onOpenHoushan11) })   /* 2026-09-19 §3:dolly → 后山11 */,
                ) {
                    Image(
                        painter = painterResource(R.drawable.img_shilian_recovered_4),
                        contentDescription = "标签4",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds,
                    )
                    Text(
                        text = "正\n心\n守\n道\n录",
                        color = Color.Black,
                        // §34 用户指令:行间距 6sp(略大于字号 4sp,适度撑开);5×6=30dp,容器 60dp 留 30dp 余量
                        style = TextStyle(fontFamily = YaHei, fontSize = 4.sp, lineHeight = 6.sp),
                        modifier = Modifier
                            .offset(x = 13.5.dp, y = 14.dp)
                            .size(width = 12.dp, height = 60.dp),
                    )
                    Text(
                        text = "炼",
                        color = Color(0xFF385816),
                        style = TextStyle(fontFamily = YaHei, fontSize = 4.sp),
                        modifier = Modifier
                            .offset(x = 13.5.dp, y = 7.dp)
                            .size(width = 10.dp, height = 14.dp),
                    )
                }

                // "标签2" 图像(赏罚驭灵诀,X=124, Y=521, W=96, H=170)— 2026-10-13 §1:点击 → **第八卷-1**(本页 Y 最大,§15 规则)
                //   (2026-09-19 §2/§3 一度改为 dolly 推进 → 后山10;2026-10-13 用户决定恢复,见顶部 doc §1)
                Box(
                    modifier = Modifier
                        .offset(x = 124.dp, y = 521.dp)
                        .size(width = 96.dp, height = 170.dp)
                        .clickable(enabled = !isTransitioning, onClick = actions.onOpenVolume8Part1)   /* 2026-10-13 §1:Y 最大 → 卷8(直接,无 dolly) */,
                ) {
                    Image(
                        painter = painterResource(R.drawable.img_shilian_recovered_4),
                        contentDescription = "标签2",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds,
                    )
                    Text(
                        text = "赏\n罚\n驭\n灵\n诀",
                        color = Color.Black,
                        // §34 用户指令:行间距 14sp(等于字号,字符紧凑);5×14=70dp,容器 80dp 有 10dp 余量
                        style = TextStyle(fontFamily = YaHei, fontSize = 14.sp, lineHeight = 14.sp),
                        modifier = Modifier
                            .offset(x = 43.dp, y = 48.dp)
                            .size(width = 14.dp, height = 80.dp),
                    )
                    Text(
                        text = "炼",
                        color = Color(0xFF385816),
                        style = TextStyle(fontFamily = YaHei, fontSize = 12.sp),
                        modifier = Modifier
                            .offset(x = 43.dp, y = 25.dp)
                            .size(width = 12.dp, height = 16.dp),
                    )
                }
            }

            // ── UI chrome:左上角返回按钮 ─────────────────────────────────────
            // 2026-09-19 §2:过场页 → 返回按钮加 chromeFade 淡出(与后山2~8 一致)
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 30.dp, y = 60.dp)
                    .size(width = 18.dp, height = 18.dp)
                    .graphicsLayer { alpha = chromeFade }
                    .clickable(enabled = !isTransitioning, onClick = actions.onBack)   /* 2026-09-19 §2:过场页 → 淡出 + 门检 */,
            ) {
                Image(
                    painter = painterResource(R.drawable.img_shilian_return),
                    contentDescription = "返回",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}
