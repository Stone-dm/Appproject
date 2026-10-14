package com.jueqiao.jianghu.nav

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jueqiao.jianghu.JianghuApp
import com.jueqiao.jianghu.auth.AuthOperation
import com.jueqiao.jianghu.auth.AuthViewModel
import com.jueqiao.jianghu.auth.AuthViewModelFactory
import com.jueqiao.jianghu.auth.VerificationPurpose
import com.jueqiao.jianghu.creation.CreationViewModel
import com.jueqiao.jianghu.creation.CreationViewModelFactory
import com.jueqiao.jianghu.conference.ConferenceViewModel
import com.jueqiao.jianghu.conference.ConferenceViewModelFactory
import com.jueqiao.jianghu.luggage.LuggageViewModel
import com.jueqiao.jianghu.luggage.LuggageViewModelFactory
import com.jueqiao.jianghu.luggage.LuggageUiState
import com.jueqiao.jianghu.luggage.PrivacySettingsPatchDto
import com.jueqiao.jianghu.luggage.RetrySessionDto
import com.jueqiao.jianghu.ui.screens.agreement.AgreementScreen
import com.jueqiao.jianghu.ui.screens.dahui.DahuiScreen
import com.jueqiao.jianghu.ui.screens.dahui.ConferenceCollectionsScreen
import com.jueqiao.jianghu.ui.screens.dahui.ConferenceHubScreen
import com.jueqiao.jianghu.ui.screens.dahui.ConferenceLettersScreen
import com.jueqiao.jianghu.ui.screens.dahui.ConferenceMatchScreen
import com.jueqiao.jianghu.ui.screens.dahui.ConferenceMatchRecordsScreen
import com.jueqiao.jianghu.ui.screens.dahui.ConferenceRequestsScreen
import com.jueqiao.jianghu.ui.screens.dahui.ConferenceWorkScreen
import com.jueqiao.jianghu.ui.screens.forgot.ForgotScreen
import com.jueqiao.jianghu.ui.screens.home.Home1Screen
import com.jueqiao.jianghu.ui.screens.home.HomeScreen
import com.jueqiao.jianghu.ui.screens.home.LuggageScreen
import com.jueqiao.jianghu.ui.screens.home.SettingsScreen
import com.jueqiao.jianghu.ui.screens.luggage.BadgesScreen
import com.jueqiao.jianghu.ui.screens.luggage.CreationDetailScreen
import com.jueqiao.jianghu.ui.screens.luggage.ConferencePublishScreen
import com.jueqiao.jianghu.ui.screens.luggage.CreationsScreen
import com.jueqiao.jianghu.ui.screens.luggage.EvidenceScreen
import com.jueqiao.jianghu.ui.screens.luggage.ManualDetailScreen
import com.jueqiao.jianghu.ui.screens.luggage.ManualsScreen
import com.jueqiao.jianghu.ui.screens.luggage.LearningTrialScreen
import com.jueqiao.jianghu.ui.screens.luggage.MistakeDetailScreen
import com.jueqiao.jianghu.ui.screens.luggage.MistakesScreen
import com.jueqiao.jianghu.ui.screens.luggage.PrivacySafetyScreen
import com.jueqiao.jianghu.ui.screens.luggage.RetryTrialScreen
import com.jueqiao.jianghu.ui.screens.settings.SettingsDetailScreen
import com.jueqiao.jianghu.ui.screens.settings.SettingsPage
import com.jueqiao.jianghu.ui.screens.login.LoginComponentsFadeMillis
import com.jueqiao.jianghu.ui.screens.login.LoginMistTransition
import com.jueqiao.jianghu.ui.screens.login.LoginScreen
import com.jueqiao.jianghu.ui.screens.login.MistCoveredHoldMillis
import com.jueqiao.jianghu.ui.screens.login.MistDisperseMillis
import com.jueqiao.jianghu.ui.screens.login.MistGatherMillis
import com.jueqiao.jianghu.ui.screens.privacy.PrivacyScreen
import com.jueqiao.jianghu.ui.screens.register.RegisterScreen
import com.jueqiao.jianghu.ui.screens.splash.SplashScreen
import com.jueqiao.jianghu.ui.screens.xiulian.XiulianScreen
import com.jueqiao.jianghu.ui.screens.gunlun1.Gunlun1Screen
import com.jueqiao.jianghu.ui.screens.gunlun3.Gunlun3Screen
import com.jueqiao.jianghu.ui.screens.gunlun4.Gunlun4Screen
import com.jueqiao.jianghu.ui.screens.gunlun5.Gunlun5Screen
import com.jueqiao.jianghu.ui.screens.gunlun6.Gunlun6Screen
import com.jueqiao.jianghu.ui.screens.gunlun7.Gunlun7Screen
import com.jueqiao.jianghu.ui.screens.houshan1.Houshan1Actions           // §7 全量迁移,data class 防 §3 slot bug
import com.jueqiao.jianghu.ui.screens.houshan1.Houshan1Screen
import com.jueqiao.jianghu.ui.screens.houshan2.Houshan2Actions           // §7 全量迁移
import com.jueqiao.jianghu.ui.screens.houshan2.Houshan2Screen
import com.jueqiao.jianghu.ui.screens.houshan3.Houshan3Actions           // §7 全量迁移
import com.jueqiao.jianghu.ui.screens.houshan3.Houshan3Screen
import com.jueqiao.jianghu.ui.screens.houshan4.Houshan4Actions           // §7 全量迁移
import com.jueqiao.jianghu.ui.screens.houshan4.Houshan4Screen            // §24 新增
import com.jueqiao.jianghu.ui.screens.houshan5.Houshan5Actions           // §5/§6 先行重构,data class 防 §3 slot bug
import com.jueqiao.jianghu.ui.screens.houshan5.Houshan5Screen            // §33 新增
import com.jueqiao.jianghu.ui.screens.houshan6.Houshan6Actions           // §5/§6 先行重构
import com.jueqiao.jianghu.ui.screens.houshan6.Houshan6Screen            // 2026-09-18 §4 新增
import com.jueqiao.jianghu.ui.screens.houshan7.Houshan7Actions           // 2026-09-18 §9 新增
import com.jueqiao.jianghu.ui.screens.houshan7.Houshan7Screen            // 2026-09-18 §9 新增
import com.jueqiao.jianghu.ui.screens.houshan8.Houshan8Actions           // 2026-09-18 §10 新增
import com.jueqiao.jianghu.ui.screens.houshan8.Houshan8Screen            // 2026-09-18 §10 新增
import com.jueqiao.jianghu.ui.screens.houshan9.Houshan9Actions           // 2026-09-18 §11 新增
import com.jueqiao.jianghu.ui.screens.houshan9.Houshan9Screen            // 2026-09-18 §11 新增
import com.jueqiao.jianghu.ui.screens.houshan10.Houshan10Actions          // 2026-09-19 §2 新增
import com.jueqiao.jianghu.ui.screens.houshan10.Houshan10Screen           // 2026-09-19 §2 新增
import com.jueqiao.jianghu.ui.screens.houshan11.Houshan11Actions          // 2026-09-19 §3 新增
import com.jueqiao.jianghu.ui.screens.houshan11.Houshan11Screen           // 2026-09-19 §3 新增
import com.jueqiao.jianghu.ui.screens.gunlun8.Gunlun8Screen
import com.jueqiao.jianghu.ui.screens.gunlun9.Gunlun9Screen
import com.jueqiao.jianghu.ui.screens.gunlun10.Gunlun10Screen
import com.jueqiao.jianghu.ui.screens.gunlun11.Gunlun11Screen
import com.jueqiao.jianghu.ui.screens.gunlun12.Gunlun12Screen
import com.jueqiao.jianghu.ui.screens.gunlun13.Gunlun13Screen
import com.jueqiao.jianghu.ui.screens.gunlun14.Gunlun14Screen
import com.jueqiao.jianghu.ui.screens.gunlun15.Gunlun15Screen
import com.jueqiao.jianghu.ui.screens.gunlun16.Gunlun16Screen
import com.jueqiao.jianghu.ui.screens.manualreader.ManualReaderScreen
import com.jueqiao.jianghu.ui.screens.wushuhuan.WushuhuanScreen
import com.jueqiao.jianghu.ui.screens.zaowu.ZaowuScreen
import com.jueqiao.jianghu.ui.screens.gongfang.GongfangScreen
import com.jueqiao.jianghu.ui.screens.gongfang.toCreationResumeItem
import com.jueqiao.jianghu.ui.screens.shengtu.ShengtuScreen
import com.jueqiao.jianghu.ui.screens.unfinished.UnfinishedScreen
import com.jueqiao.jianghu.ui.screens.volume1.Volume1Screen
import com.jueqiao.jianghu.ui.screens.volume1part2.Volume1Part2Screen
import com.jueqiao.jianghu.ui.screens.volume1part3.Volume1Part3Screen
import com.jueqiao.jianghu.ui.screens.volume1part4.Volume1Part4Screen
import com.jueqiao.jianghu.ui.screens.volume1part5.Volume1Part5Screen
import com.jueqiao.jianghu.ui.screens.volume1part6.Volume1Part6Screen
import com.jueqiao.jianghu.ui.screens.volume1part7.Volume1Part7Screen
import com.jueqiao.jianghu.ui.screens.volume1part8.Volume1Part8Screen
import com.jueqiao.jianghu.ui.screens.volume1part9.Volume1Part9Screen
import com.jueqiao.jianghu.ui.screens.volume1part10.Volume1Part10Screen
import com.jueqiao.jianghu.ui.screens.volume1part11.Volume1Part11Screen
import com.jueqiao.jianghu.ui.screens.volume1part12.Volume1Part12Screen
import com.jueqiao.jianghu.ui.screens.volume1part13.Volume1Part13Screen
import com.jueqiao.jianghu.ui.screens.volume1part14.Volume1Part14Screen
import com.jueqiao.jianghu.ui.screens.volume2part1.Volume2Part1Screen
import com.jueqiao.jianghu.ui.screens.volume2part2.Volume2Part2Screen
import com.jueqiao.jianghu.ui.screens.volume2part3.Volume2Part3Screen
import com.jueqiao.jianghu.ui.screens.volume2part4.Volume2Part4Screen
import com.jueqiao.jianghu.ui.screens.volume2part5.Volume2Part5Screen
import com.jueqiao.jianghu.ui.screens.volume2part6.Volume2Part6Screen
import com.jueqiao.jianghu.ui.screens.volume2part7.Volume2Part7Screen
import com.jueqiao.jianghu.ui.screens.volume2part8.Volume2Part8Screen
import com.jueqiao.jianghu.ui.screens.volume2part9.Volume2Part9Screen
import com.jueqiao.jianghu.ui.screens.volume2part10.Volume2Part10Screen
import com.jueqiao.jianghu.ui.screens.volume2part11.Volume2Part11Screen
import com.jueqiao.jianghu.ui.screens.volume2part12.Volume2Part12Screen
import com.jueqiao.jianghu.ui.screens.volume2part13.Volume2Part13Screen
import com.jueqiao.jianghu.ui.screens.volume2part14.Volume2Part14Screen
import com.jueqiao.jianghu.ui.screens.volume2part15.Volume2Part15Screen
import com.jueqiao.jianghu.ui.screens.volume3part10.Volume3Part10Screen
import com.jueqiao.jianghu.ui.screens.volume3part11.Volume3Part11Screen
import com.jueqiao.jianghu.ui.screens.volume3part12.Volume3Part12Screen
import com.jueqiao.jianghu.ui.screens.volume3part13.Volume3Part13Screen
import com.jueqiao.jianghu.ui.screens.volume3part14.Volume3Part14Screen
import com.jueqiao.jianghu.ui.screens.volume4part1.Volume4Part1Screen
import com.jueqiao.jianghu.ui.screens.volume5part1.Volume5Part1Screen
import com.jueqiao.jianghu.ui.screens.volume5part2.Volume5Part2Screen
import com.jueqiao.jianghu.ui.screens.volume5part3.Volume5Part3Screen
import com.jueqiao.jianghu.ui.screens.volume5part4.Volume5Part4Screen
import com.jueqiao.jianghu.ui.screens.volume5part5.Volume5Part5Screen
import com.jueqiao.jianghu.ui.screens.volume5part6.Volume5Part6Screen
import com.jueqiao.jianghu.ui.screens.volume5part7.Volume5Part7Screen
import com.jueqiao.jianghu.ui.screens.volume5part8.Volume5Part8Screen
import com.jueqiao.jianghu.ui.screens.volume5part9.Volume5Part9Screen
import com.jueqiao.jianghu.ui.screens.volume5part10.Volume5Part10Screen
import com.jueqiao.jianghu.ui.screens.volume5part11.Volume5Part11Screen
import com.jueqiao.jianghu.ui.screens.volume5part12.Volume5Part12Screen
import com.jueqiao.jianghu.ui.screens.volume5part13.Volume5Part13Screen
import com.jueqiao.jianghu.ui.screens.volume5part14.Volume5Part14Screen
import com.jueqiao.jianghu.ui.screens.volume5part15.Volume5Part15Screen
import com.jueqiao.jianghu.ui.screens.volume6part1.Volume6Part1Screen
import com.jueqiao.jianghu.ui.screens.volume6part2.Volume6Part2Screen
import com.jueqiao.jianghu.ui.screens.volume6part3.Volume6Part3Screen
import com.jueqiao.jianghu.ui.screens.volume6part4.Volume6Part4Screen
import com.jueqiao.jianghu.ui.screens.volume6part5.Volume6Part5Screen
import com.jueqiao.jianghu.ui.screens.volume6part6.Volume6Part6Screen
import com.jueqiao.jianghu.ui.screens.volume6part7.Volume6Part7Screen
import com.jueqiao.jianghu.ui.screens.volume6part8.Volume6Part8Screen
import com.jueqiao.jianghu.ui.screens.volume6part9.Volume6Part9Screen
import com.jueqiao.jianghu.ui.screens.volume6part10.Volume6Part10Screen
import com.jueqiao.jianghu.ui.screens.volume6part11.Volume6Part11Screen
import com.jueqiao.jianghu.ui.screens.volume6part12.Volume6Part12Screen
import com.jueqiao.jianghu.ui.screens.volume6part13.Volume6Part13Screen
import com.jueqiao.jianghu.ui.screens.volume6part14.Volume6Part14Screen
import com.jueqiao.jianghu.ui.screens.volume6part15.Volume6Part15Screen
import com.jueqiao.jianghu.ui.screens.volume7part1.Volume7Part1Screen
import com.jueqiao.jianghu.ui.screens.volume7part2.Volume7Part2Screen
import com.jueqiao.jianghu.ui.screens.volume7part3.Volume7Part3Screen
import com.jueqiao.jianghu.ui.screens.volume7part4.Volume7Part4Screen
import com.jueqiao.jianghu.ui.screens.volume7part5.Volume7Part5Screen
import com.jueqiao.jianghu.ui.screens.volume7part6.Volume7Part6Screen
import com.jueqiao.jianghu.ui.screens.volume7part7.Volume7Part7Screen
import com.jueqiao.jianghu.ui.screens.volume7part8.Volume7Part8Screen
import com.jueqiao.jianghu.ui.screens.volume7part9.Volume7Part9Screen
import com.jueqiao.jianghu.ui.screens.volume7part10.Volume7Part10Screen
import com.jueqiao.jianghu.ui.screens.volume7part11.Volume7Part11Screen
import com.jueqiao.jianghu.ui.screens.volume7part12.Volume7Part12Screen
import com.jueqiao.jianghu.ui.screens.volume8part1.Volume8Part1Screen
import com.jueqiao.jianghu.ui.screens.volume8part2.Volume8Part2Screen
import com.jueqiao.jianghu.ui.screens.volume8part3.Volume8Part3Screen
import com.jueqiao.jianghu.ui.screens.volume8part4.Volume8Part4Screen
import com.jueqiao.jianghu.ui.screens.volume8part5.Volume8Part5Screen
import com.jueqiao.jianghu.ui.screens.volume8part6.Volume8Part6Screen
import com.jueqiao.jianghu.ui.screens.volume8part7.Volume8Part7Screen
import com.jueqiao.jianghu.ui.screens.volume8part8.Volume8Part8Screen
import com.jueqiao.jianghu.ui.screens.volume8part9.Volume8Part9Screen
import com.jueqiao.jianghu.ui.screens.volume8part10.Volume8Part10Screen
import com.jueqiao.jianghu.ui.screens.volume8part11.Volume8Part11Screen
import com.jueqiao.jianghu.ui.screens.volume8part12.Volume8Part12Screen
import com.jueqiao.jianghu.ui.screens.volume8part13.Volume8Part13Screen
import com.jueqiao.jianghu.ui.screens.volume8part14.Volume8Part14Screen
import com.jueqiao.jianghu.ui.screens.volume9part1.Volume9Part1Screen
import com.jueqiao.jianghu.ui.screens.volume9part2.Volume9Part2Screen
import com.jueqiao.jianghu.ui.screens.volume9part3.Volume9Part3Screen
import com.jueqiao.jianghu.ui.screens.volume9part4.Volume9Part4Screen
import com.jueqiao.jianghu.ui.screens.volume9part5.Volume9Part5Screen
import com.jueqiao.jianghu.ui.screens.volume9part6.Volume9Part6Screen
import com.jueqiao.jianghu.ui.screens.volume9part7.Volume9Part7Screen
import com.jueqiao.jianghu.ui.screens.volume9part8.Volume9Part8Screen
import com.jueqiao.jianghu.ui.screens.volume9part9.Volume9Part9Screen
import com.jueqiao.jianghu.ui.screens.volume9part10.Volume9Part10Screen
import com.jueqiao.jianghu.ui.screens.volume9part11.Volume9Part11Screen
import com.jueqiao.jianghu.ui.screens.volume9part12.Volume9Part12Screen
import com.jueqiao.jianghu.ui.screens.volume9part13.Volume9Part13Screen
import com.jueqiao.jianghu.ui.screens.volume9part14.Volume9Part14Screen
import com.jueqiao.jianghu.ui.screens.volume9part15.Volume9Part15Screen
import com.jueqiao.jianghu.ui.screens.volume10part1.Volume10Part1Screen
import com.jueqiao.jianghu.ui.screens.volume10part2.Volume10Part2Screen
import com.jueqiao.jianghu.ui.screens.volume10part3.Volume10Part3Screen
import com.jueqiao.jianghu.ui.screens.volume10part4.Volume10Part4Screen
import com.jueqiao.jianghu.ui.screens.volume10part5.Volume10Part5Screen
import com.jueqiao.jianghu.ui.screens.volume10part6.Volume10Part6Screen
import com.jueqiao.jianghu.ui.screens.volume10part7.Volume10Part7Screen
import com.jueqiao.jianghu.ui.screens.volume10part8.Volume10Part8Screen
import com.jueqiao.jianghu.ui.screens.volume10part9.Volume10Part9Screen
import com.jueqiao.jianghu.ui.screens.volume10part10.Volume10Part10Screen
import com.jueqiao.jianghu.ui.screens.volume10part11.Volume10Part11Screen
import com.jueqiao.jianghu.ui.screens.volume10part12.Volume10Part12Screen
import com.jueqiao.jianghu.ui.screens.volume10part13.Volume10Part13Screen
import com.jueqiao.jianghu.ui.screens.volume10part14.Volume10Part14Screen
import com.jueqiao.jianghu.ui.screens.volume4part2.Volume4Part2Screen
import com.jueqiao.jianghu.ui.screens.volume4part3.Volume4Part3Screen
import com.jueqiao.jianghu.ui.screens.volume4part4.Volume4Part4Screen
import com.jueqiao.jianghu.ui.screens.volume4part5.Volume4Part5Screen
import com.jueqiao.jianghu.ui.screens.volume4part6.Volume4Part6Screen
import com.jueqiao.jianghu.ui.screens.volume4part7.Volume4Part7Screen
import com.jueqiao.jianghu.ui.screens.volume4part8.Volume4Part8Screen
import com.jueqiao.jianghu.ui.screens.volume4part9.Volume4Part9Screen
import com.jueqiao.jianghu.ui.screens.volume4part10.Volume4Part10Screen
import com.jueqiao.jianghu.ui.screens.volume4part11.Volume4Part11Screen
import com.jueqiao.jianghu.ui.screens.volume4part12.Volume4Part12Screen
import com.jueqiao.jianghu.ui.screens.volume4part13.Volume4Part13Screen
import com.jueqiao.jianghu.ui.screens.volume4part14.Volume4Part14Screen
import com.jueqiao.jianghu.ui.screens.volume3part1.Volume3Part1Screen
import com.jueqiao.jianghu.ui.screens.volume3part2.Volume3Part2Screen
import com.jueqiao.jianghu.ui.screens.volume3part3.Volume3Part3Screen
import com.jueqiao.jianghu.ui.screens.volume3part4.Volume3Part4Screen
import com.jueqiao.jianghu.ui.screens.volume3part5.Volume3Part5Screen
import com.jueqiao.jianghu.ui.screens.volume3part6.Volume3Part6Screen
import com.jueqiao.jianghu.ui.screens.volume3part7.Volume3Part7Screen
import com.jueqiao.jianghu.ui.screens.volume3part8.Volume3Part8Screen
import com.jueqiao.jianghu.ui.screens.volume3part9.Volume3Part9Screen
import com.jueqiao.jianghu.ui.screens.picture.CreationEditorScreen
import com.jueqiao.jianghu.ui.screens.chuangdang.CD_BOSS_INDEX
import com.jueqiao.jianghu.ui.screens.chuangdang.ChuangdangBattleActions
import com.jueqiao.jianghu.ui.screens.chuangdang.ChuangdangBattleScreen
import com.jueqiao.jianghu.ui.screens.chuangdang.ChuangdangBossActions
import com.jueqiao.jianghu.ui.screens.chuangdang.ChuangdangBossScreen
import com.jueqiao.jianghu.ui.screens.chuangdang.ChuangdangMapActions
import com.jueqiao.jianghu.ui.screens.chuangdang.ChuangdangMapScreen
import com.jueqiao.jianghu.ui.screens.chuangzuodangan.ChuangzuodanganScreen
import com.jueqiao.jianghu.ui.screens.yanwuchang.YanwuchangScreen
import com.jueqiao.jianghu.ui.screens.yanwuchangvideo.YanwuchangVideoScreen
import com.jueqiao.jianghu.ui.screens.yanwuchangvideobrowserecord.YanwuchangVideoBrowseRecordScreen
import com.jueqiao.jianghu.ui.screens.yanwuchangvideocomment.YanwuchangVideoComment1Screen
import com.jueqiao.jianghu.ui.screens.yanwuchangvideocomment.YanwuchangVideoComment2Screen
import com.jueqiao.jianghu.ui.screens.yanwuchangvideomy.YanwuchangVideoMyScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private fun NavHostController.navigateSingleTop(route: String) {
    navigate(route) { launchSingleTop = true }
}

private fun NavHostController.openBackMountainWendao(targetLessonId: String? = null) {
    navigateSingleTop(Routes.shilian(targetLessonId))
}

@Composable
fun JianghuNavHost(
    navController: NavHostController = rememberNavController(),
) {
    val application = LocalContext.current.applicationContext as JianghuApp
    val authFactory = remember(application) {
        AuthViewModelFactory(application.authRepository)
    }
    val authViewModel: AuthViewModel = viewModel(factory = authFactory)
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val luggageFactory = remember(application) {
        LuggageViewModelFactory(application.luggageRepository)
    }
    val luggageViewModel: LuggageViewModel = viewModel(factory = luggageFactory)
    val luggageState by luggageViewModel.uiState.collectAsStateWithLifecycle()
    val luggageDetailState by luggageViewModel.detailState.collectAsStateWithLifecycle()
    val progressSnapshot = (luggageState as? LuggageUiState.Content)?.snapshot
    val progressLoading = luggageState is LuggageUiState.Loading ||
        (luggageState as? LuggageUiState.Content)?.refreshing == true ||
        luggageDetailState.loading
    val progressMessage = when (val state = luggageState) {
        is LuggageUiState.Content -> state.notice
        is LuggageUiState.Error -> state.message
        LuggageUiState.Loading -> null
    } ?: luggageDetailState.learningOverviewMessage
        ?: authState.errorMessage?.takeIf {
            authState.errorCode == "NETWORK_UNAVAILABLE" ||
                authState.errorCode == "LOCAL_AUTH_FAILURE"
        }
    val creationFactory = remember(application) {
        CreationViewModelFactory(application, application.luggageRepository)
    }
    val creationViewModel: CreationViewModel = viewModel(factory = creationFactory)
    val creationState by creationViewModel.state.collectAsStateWithLifecycle()
    val conferenceFactory = remember(application) {
        ConferenceViewModelFactory(application.luggageRepository)
    }
    val conferenceViewModel: ConferenceViewModel = viewModel(factory = conferenceFactory)
    val conferenceState by conferenceViewModel.state.collectAsStateWithLifecycle()
    val transitionScope = rememberCoroutineScope()
    val loginContentAlpha = remember { Animatable(1f) }
    val mistPhase = remember { Animatable(0f) }
    var loginTransitionRunning by remember { mutableStateOf(false) }
    var animateHomeQuickActionsOnNextEntry by remember { mutableStateOf(false) }
    LaunchedEffect(currentUser?.id) {
        conferenceViewModel.reset()
        creationViewModel.clearDerivative()
        conferenceViewModel.loadCapabilities()
        if (currentUser != null) conferenceViewModel.syncLetters()
    }

    val startLoginTransition: (String) -> Unit = { destination ->
        if (!loginTransitionRunning) {
            loginTransitionRunning = true
            transitionScope.launch {
                loginContentAlpha.snapTo(1f)
                mistPhase.snapTo(0f)
                loginContentAlpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = LoginComponentsFadeMillis,
                        easing = FastOutSlowInEasing,
                    ),
                )
                delay(80)
                mistPhase.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = MistGatherMillis,
                        easing = FastOutSlowInEasing,
                    ),
                )
                delay(MistCoveredHoldMillis)

                navController.navigate(destination) {
                    popUpTo(Routes.Login) { inclusive = true }
                    launchSingleTop = true
                }
                delay(50)
                mistPhase.animateTo(
                    targetValue = 2f,
                    animationSpec = tween(
                        durationMillis = MistDisperseMillis,
                        easing = LinearOutSlowInEasing,
                    ),
                )
                mistPhase.snapTo(0f)
                loginContentAlpha.snapTo(1f)
                loginTransitionRunning = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    NavHost(
        navController    = navController,
        startDestination = Routes.Splash,
    ) {
        composable(Routes.Splash) {
            SplashScreen(
                onTap = {
                    authViewModel.bootstrap { authenticated ->
                        navController.navigate(
                            if (authenticated) Routes.Home1 else Routes.Login
                        ) {
                            popUpTo(Routes.Splash) { inclusive = true }
                        }
                    }
                },
            )
        }

        composable(Routes.Login) {
            LoginScreen(
                onLogin         = { phone, password ->
                    authViewModel.login(phone, password) {
                        animateHomeQuickActionsOnNextEntry = true
                        startLoginTransition(Routes.Home)
                    }
                },
                onOpenForgot    = {
                    authViewModel.clearFeedback()
                    navController.navigate(Routes.Forgot)
                },
                onOpenRegister  = {
                    authViewModel.clearFeedback()
                    navController.navigate(Routes.Register)
                },
                onOpenAgreement = { navController.navigate(Routes.Agreement) },
                onOpenPrivacy   = { navController.navigate(Routes.Privacy) },
                onBack          = {
                    authViewModel.clearFeedback()
                    navController.popBackStack()
                },
                isSubmitting    = authState.operation == AuthOperation.Login,
                isTransitioning = loginTransitionRunning,
                contentAlpha    = { loginContentAlpha.value },
                errorMessage    = authState.errorMessage,
                onClearError    = authViewModel::clearFeedback,
            )
        }

        composable(Routes.Register) {
            RegisterScreen(
                onRequestCode   = { phone, purpose, onCooldown ->
                    authViewModel.requestCode(phone, purpose, onCooldown)
                },
                onRegister      = { phone, code, password, ageBand ->
                    authViewModel.register(
                        phone,
                        code,
                        password,
                        ageBand,
                    ) {
                        animateHomeQuickActionsOnNextEntry = true
                        navController.navigate(Routes.Home) {
                            popUpTo(Routes.Login) { inclusive = true }
                        }
                    }
                },
                onOpenAgreement = { navController.navigate(Routes.Agreement) },
                onOpenPrivacy   = { navController.navigate(Routes.Privacy) },
                onBack          = { navController.popBackStack() },
                operation       = authState.operation,
                errorMessage    = authState.errorMessage,
                onClearError    = authViewModel::clearFeedback,
            )
        }

        composable(Routes.Forgot) {
            ForgotScreen(
                onSubmitted     = { navController.popBackStack(Routes.Login, inclusive = false) },
                onRequestCode   = { phone, onCooldown ->
                    authViewModel.requestCode(
                        phone,
                        VerificationPurpose.RESET_PASSWORD,
                        onCooldown,
                    )
                },
                onResetPassword = { phone, code, newPassword, onSuccess ->
                    authViewModel.resetPassword(phone, code, newPassword, onSuccess)
                },
                onBack          = {
                    authViewModel.clearFeedback()
                    if (!navController.popBackStack()) {
                        navController.navigate(Routes.Login) {
                            popUpTo(Routes.Login) { inclusive = true }
                        }
                    }
                },
                operation       = authState.operation,
                errorMessage    = authState.errorMessage,
                onClearError    = authViewModel::clearFeedback,
            )
        }

        composable(Routes.Agreement) {
            AgreementScreen(
                onBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Routes.Login) {
                            popUpTo(Routes.Login) { inclusive = true }
                        }
                    }
                },
            )
        }

        composable(Routes.Privacy) {
            PrivacyScreen(
                onBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Routes.Login) {
                            popUpTo(Routes.Login) { inclusive = true }
                        }
                    }
                },
            )
        }

        composable(Routes.Home) {
            HomeScreen(
                onOpenHome1 = {
                    // 引导结束进入首页时，重新请求一次八个入口的进场动效。
                    // 从各功能页返回 Home1 不会走此分支，因此仍保持直接显示。
                    animateHomeQuickActionsOnNextEntry = true
                    navController.navigate(Routes.Home1)
                },
            )
        }

        composable(Routes.Home1) {
            LaunchedEffect(Unit) { conferenceViewModel.syncLetters() }
            Home1Screen(
                animateQuickActionsEntrance = animateHomeQuickActionsOnNextEntry,
                quickActionsEntranceReady = !loginTransitionRunning,
                onQuickActionsEntranceConsumed = {
                    animateHomeQuickActionsOnNextEntry = false
                },
                onOpenXiulian   = { navController.navigateSingleTop(Routes.Gunlun1) },
                onOpenWendao    = { navController.openBackMountainWendao() },
                onOpenLuggage   = { navController.navigateSingleTop(Routes.Luggage) },
                // 每次从首页进入工坊都创建新的引导页实例，确保气泡与入口状态正确。
                onOpenZaowu     = { navController.navigateSingleTop(Routes.Zaowu) },
                onOpenSettings  = { navController.navigateSingleTop(Routes.Settings) },
                onOpenLetters = {
                    navController.navigateSingleTop(Routes.DahuiLetters)
                },
onOpenDahui     = { navController.navigate(Routes.Dahui) },
                // 2026-09-19 §6:第五个主入口「闯荡江湖」→ 雾隐机关镇地图页
                onOpenChuangdang = { navController.navigate(Routes.Chuangdang) },
                dahuiEnabled = conferenceState.conferenceEnabled != false,
                hasUnreadLetters = conferenceState.unreadLetterCount > 0,
                progressSnapshot = progressSnapshot,
                learningOverview = luggageDetailState.learningOverview,
                progressLoading = progressLoading,
                progressMessage = progressMessage,
                onRefreshProgress = {
                    authViewModel.clearFeedback()
                    luggageViewModel.loadProgressOverview()
                },
                onOpenRecommendedManual = { id ->
                    navController.navigateSingleTop(Routes.luggageManualDetail(id))
                },
            )
        }

        // 2026-09-19 §6:闯荡江湖 —— 雾隐机关镇地图(五节点 + 闯荡令 + 出发)
        composable(Routes.Chuangdang) {
            ChuangdangMapScreen(
                actions = ChuangdangMapActions(
                    onBack = { navController.popBackStack() },
                    // 第 5 关走 Boss 页(制作 + 评审),其余走三心攻防战斗
                    onEnterStage = { stage ->
                        if (stage >= CD_BOSS_INDEX) {
                            navController.navigate(Routes.ChuangdangBoss)
                        } else {
                            navController.navigate(Routes.chuangdangBattle(stage))
                        }
                    },
                    // 免费练习(文档 §2:不消耗闯荡令、可看提示、不解锁正式节点)
                    onPracticeStage = { stage ->
                        navController.navigate(Routes.chuangdangBattle(stage, practice = true))
                    },
                ),
            )
        }
        // 2026-09-19 §6:闯荡江湖 —— 第 1~4 关的三心攻防(关卡号 + 是否练习模式走路由参数)
        composable(
            route = Routes.ChuangdangBattlePattern,
            arguments = listOf(
                navArgument("stage") { type = NavType.IntType },
                // 免费练习(文档 §2:不消耗闯荡令、可看提示、不解锁正式节点)
                navArgument("practice") {
                    type = NavType.BoolType
                    defaultValue = false
                },
            ),
        ) { backStackEntry ->
            val stage = backStackEntry.arguments?.getInt("stage") ?: 1
            val practice = backStackEntry.arguments?.getBoolean("practice") ?: false
            ChuangdangBattleScreen(
                stageIndex = stage,
                practiceMode = practice,
                actions = ChuangdangBattleActions(
                    onExit = { navController.popBackStack() },
                    // 2026-09-20:撤退结果面板三选项(策划 §9「普通关失败」)
                    //   · 继续练习:留在战斗页路由,只把 practice 切到 true
                    onPracticeSame = {
                        navController.navigate(Routes.chuangdangBattle(stage, practice = true)) {
                            popUpTo(Routes.ChuangdangBattlePattern) { inclusive = true }
                        }
                    },
                    //   · 重新出发:从当前关正式重打
                    onRestartSame = {
                        navController.navigate(Routes.chuangdangBattle(stage, practice = false)) {
                            popUpTo(Routes.ChuangdangBattlePattern) { inclusive = true }
                        }
                    },
                    //   · 前往补修:目前修学/补修模块在"等后端的一批"里(SESSION-LOG-2026-09-20 待办 #9);
                    //     先跳到修学主页,模块接上后改成真正的补修页跳转。
                    onGoReview = {
                        navController.navigate(Routes.Xiulian) {
                            popUpTo(Routes.ChuangdangBattlePattern) { inclusive = true }
                        }
                    },
                    // 2026-09-21:胜利后「继续挑战 下一关」直跳
                    //   · 普通关 1→2→3→4 → chuangdangBattle(n+1) [策划 §6.1:runActive 期间不扣令]
                    //   · 第 4 关 → Boss 路由(切到制作+评审页,不是战斗页)
                    //   · Boss 屏战斗不挂这个回调(BossScreen 自己有 onBack)
                    onGoNext = if (stage < CD_BOSS_INDEX - 1) {
                        // stage = 1..3: 跳 stage+1
                        {
                            navController.navigate(Routes.chuangdangBattle(stage + 1, practice = false)) {
                                popUpTo(Routes.ChuangdangBattlePattern) { inclusive = true }
                            }
                        }
                    } else if (stage == CD_BOSS_INDEX - 1) {
                        // stage = 4: 跳 Boss 路由
                        {
                            navController.navigate(Routes.ChuangdangBoss) {
                                popUpTo(Routes.ChuangdangBattlePattern) { inclusive = true }
                            }
                        }
                    } else {
                        null  // 不该到这里(stage 5 走 Boss 路由,不会到战斗路由)
                    },
                ),
            )
        }
        // 2026-09-19 §6:闯荡江湖 —— 第五关 Boss(制作任务 + 本地规则评审)
        composable(Routes.ChuangdangBoss) {
            ChuangdangBossScreen(
                actions = ChuangdangBossActions(
                    onBack = { navController.popBackStack() },
                ),
            )
        }

        composable(Routes.Xiulian)  {
            LaunchedEffect(Unit) { luggageViewModel.loadLearningOverview() }
            XiulianScreen(
                onBack         = { navController.popBackStack() },
                onOpenGunlun1 = {
                    navController.navigate(Routes.Gunlun1) {
                        popUpTo(Routes.Xiulian) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                learningOverview = luggageDetailState.learningOverview,
            )
        }
        composable(
            route = Routes.ShilianPattern,
            arguments = listOf(
                navArgument("lessonId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) { backStackEntry ->
            LaunchedEffect(Unit) { luggageViewModel.loadLearningOverview() }
            val targetLessonId = backStackEntry.arguments?.getString("lessonId")
            Houshan1Screen(
actions = Houshan1Actions(
                    onBack = { navController.popBackStack() },
                    onOpenHoushan2 = { navController.navigate(Routes.Shilian2) },
                    // §25:用户指令"点击标签识机真决改成无法跳转" → 不再传 onOpenVolume1
                    //   (反转 §22 的 `onOpenVolume1 = { navigate(Routes.Volume1) }`)。
                    //   后山2 的同名标签仍保留跳转,未受影响。
                ),
            )
        }
        // 后山2 → 后山3:沉浸式纵深推进(§36)。缩放由 Houshan2Screen 内部按景深分层完成,
        // 导航层只负责交叉淡入淡出,因此这里不做位移,避免与内部推进叠加成"跳页"。
        composable(
            route = Routes.Shilian2,
            exitTransition = {
                if (targetState.destination.route == Routes.Shilian3) {
                    // 与 Houshan2 内部推进在 DOLLY_HANDOFF_MS 处交接,淡出稍长以覆盖后半程
                    fadeOut(animationSpec = tween(durationMillis = 520, easing = LinearEasing))
                } else {
                    // 其他去向(返回后山1、识机真决→第一卷-1)保持轻淡出,不引入硬切
                    fadeOut(animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing))
                }
            },
        ) {
            Houshan2Screen(
                actions = Houshan2Actions(
                    onBack = { navController.popBackStack() },   // 2026-09-18 §15 恢复:返回上一页
                    onOpenHoushan3 = { navController.navigate(Routes.Shilian3) },   // §15 拆招心法   → 后山3
                    onOpenHoushan4 = { navController.navigate(Routes.Shilian4) },   // §15 万象谱     → 后山4
                    onOpenHoushan5 = { navController.navigate(Routes.Shilian5) },   // §15 寻径迷踪步 → 后山5
                    onOpenVolume1 = { navController.navigate(Routes.Volume1) },   // 2026-09-18 §14 恢复(Y 最大标签:识机真决 → 卷1)
                    // §28:拆招心法标签 → 第二卷-1
                    onOpenVolume2Part1 = {},   // 2026-09-18 §12 取消跳转(待重设)
                    // §29:万象谱标签 → 第三卷-1
                    onOpenVolume3Part1 = {},   // 2026-09-18 §12 取消跳转(待重设)
                    // §30:寻径迷踪步标签 → 第四卷-1
                    onOpenVolume4Part1 = {},   // 2026-09-18 §12 取消跳转(待重设)
                ),
            )
        }
        composable(
            route = Routes.Shilian3,
            // 镜头减速停稳:由轻微放大回落到 1.00,灭点与后山2 的推进焦点一致
            enterTransition = {
                scaleIn(
                    animationSpec = tween(durationMillis = 760, easing = FastOutSlowInEasing),
                    initialScale = 1.10f,
                    transformOrigin = TransformOrigin(0.5f, 0.48f),
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 640, delayMillis = 120, easing = LinearEasing),
                )
            },
            // §24:反转 §21o —— 后山 3 现在 dolly 到后山 4,而非未完待续;
            // 淡出与后山 2 → 后山 3 同款:稍长以覆盖 dolly 后半程,与 §24 Houshan3 内部推进在 DOLLY_HANDOFF_MS 处交接
            exitTransition = {
                if (targetState.destination.route == Routes.Shilian4) {
                    fadeOut(animationSpec = tween(durationMillis = 520, easing = LinearEasing))
                } else {
                    // 其他去向(返回后山2 等)保持轻淡出,不引入硬切
                    fadeOut(animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing))
                }
            },
            // 显式覆盖:不设则 popEnterTransition 会默认继承上面的 enterTransition,
            // 导致从"未完待续"返回时也播一次推进动画
            popEnterTransition = {
                fadeIn(animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing))
            },
        ) {
            Houshan3Screen(
                actions = Houshan3Actions(
                    onBack = { navController.popBackStack() },   // 2026-09-18 §15 恢复:返回上一页
                    // §24:反转 §21o —— 不再跳转未完待续页,改为跳转新建的后山 4 页
                    onOpenHoushan4 = { navController.navigate(Routes.Shilian4) },   // §15 万象谱     → 后山4
                    onOpenHoushan5 = { navController.navigate(Routes.Shilian5) },   // §15 寻径迷踪步 → 后山5
                    // §31:3 个标签 → 按"文字 → 卷"映射(与后山2 §28~§30 同一张表)
                    onOpenVolume2Part1 = { navController.navigate(Routes.Volume2Part1) },   // 2026-09-18 §14 恢复(Y 最大标签:拆招心法 → 卷2)
                    onOpenVolume3Part1 = {},   // 2026-09-18 §12 取消跳转(原:万象谱)
                    onOpenVolume4Part1 = {},   // 2026-09-18 §12 取消跳转(原:寻径迷踪步)
                ),
            )
        }
        // §24:后山 4 页 —— 复用后山 2 素材;§33 起从"终点页"变成"过场页"(dolly 到后山5)
        composable(
            route = Routes.Shilian4,
            // §24 镜头减速停稳(同 Shilian3 enterTransition,与后山 3 dolly 末态衔接)
            enterTransition = {
                scaleIn(
                    animationSpec = tween(durationMillis = 760, easing = FastOutSlowInEasing),
                    initialScale = 1.10f,
                    transformOrigin = TransformOrigin(0.5f, 0.48f),
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 640, delayMillis = 120, easing = LinearEasing),
                )
            },
            // §33:后山 4 现在 dolly 到后山 5,淡出与后山 2→3 / 后山 3→4 同款
            exitTransition = {
                if (targetState.destination.route == Routes.Shilian5) {
                    fadeOut(animationSpec = tween(durationMillis = 520, easing = LinearEasing))
                } else {
                    fadeOut(animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing))
                }
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing))
            },
        ) {
            Houshan4Screen(
                actions = Houshan4Actions(
                    onBack = { navController.popBackStack() },
                    onOpenHoushan5 = { navController.navigate(Routes.Shilian5) },
                    onOpenHoushan6 = { navController.navigate(Routes.Shilian6) },
                    onOpenHoushan7 = { navController.navigate(Routes.Shilian7) },
                    onOpenVolume3Part1 = { navController.navigate(Routes.Volume3Part1) },
                    onOpenVolume4Part1 = {},
                    onOpenVolume5Part1 = {},
                    onOpenVolume6Part1 = {},
                ),
            )
        }
        // §33:后山 5 页 —— 复用后山 3 的素材/动画,2026-09-18 §4 升级为过场页
        composable(
            route = Routes.Shilian5,
            enterTransition = {
                scaleIn(
                    animationSpec = tween(durationMillis = 760, easing = FastOutSlowInEasing),
                    initialScale = 1.10f,
                    transformOrigin = TransformOrigin(0.5f, 0.48f),
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 640, delayMillis = 120, easing = LinearEasing),
                )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing))
            },
        ) {
            Houshan5Screen(
                actions = Houshan5Actions(
                    onBack = { navController.popBackStack() },
                    onOpenHoushan6 = { navController.navigate(Routes.Shilian6) },
                    onOpenHoushan7 = { navController.navigate(Routes.Shilian7) },
                    onOpenVolume4Part1 = { navController.navigate(Routes.Volume4Part1) },
                    onOpenVolume5Part1 = {},
                    onOpenVolume6Part1 = {},
                ),
            )
        }
        // 2026-09-18 §4:后山 6 页 —— 复用后山 4 的素材/动画,§9 升级为过场页
        composable(
            route = Routes.Shilian6,
            enterTransition = {
                scaleIn(
                    animationSpec = tween(durationMillis = 760, easing = FastOutSlowInEasing),
                    initialScale = 1.10f,
                    transformOrigin = TransformOrigin(0.5f, 0.48f),
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 640, delayMillis = 120, easing = LinearEasing),
                )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing))
            },
        ) {
            Houshan6Screen(
                actions = Houshan6Actions(
                    onBack = { navController.popBackStack() },
                    onOpenHoushan7 = { navController.navigate(Routes.Shilian7) },
                    onOpenHoushan8 = { navController.navigate(Routes.Shilian8) },
                    onOpenHoushan9 = { navController.navigate(Routes.Shilian9) },
                    onOpenVolume5Part1 = { navController.navigate(Routes.Volume5Part1) },
                    onOpenVolume6Part1 = {},
                    onOpenVolume7Part1 = {},
                    onOpenVolume8Part1 = {},
                ),
            )
        }
        // 2026-09-18 §9:后山 7 页 —— 复用后山 5 的素材/动画,§10 升级为过场页
        composable(
            route = Routes.Shilian7,
            enterTransition = {
                scaleIn(
                    animationSpec = tween(durationMillis = 760, easing = FastOutSlowInEasing),
                    initialScale = 1.10f,
                    transformOrigin = TransformOrigin(0.5f, 0.48f),
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 640, delayMillis = 120, easing = LinearEasing),
                )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing))
            },
        ) {
            Houshan7Screen(
                actions = Houshan7Actions(
                    onBack = { navController.popBackStack() },
                    onOpenHoushan8 = { navController.navigate(Routes.Shilian8) },
                    onOpenHoushan9 = { navController.navigate(Routes.Shilian9) },
                    onOpenVolume6Part1 = { navController.navigate(Routes.Volume6Part1) },
                    onOpenVolume7Part1 = {},
                    onOpenVolume8Part1 = {},
                ),
            )
        }
        // 2026-09-18 §10:后山 8 页 —— 复用后山 6 的素材/动画,§11 升级为过场页
        composable(
            route = Routes.Shilian8,
            enterTransition = {
                scaleIn(
                    animationSpec = tween(durationMillis = 760, easing = FastOutSlowInEasing),
                    initialScale = 1.10f,
                    transformOrigin = TransformOrigin(0.5f, 0.48f),
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 640, delayMillis = 120, easing = LinearEasing),
                )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing))
            },
        ) {
            Houshan8Screen(
                actions = Houshan8Actions(
                    onBack = { navController.popBackStack() },
                    onOpenHoushan9 = { navController.navigate(Routes.Shilian9) },
                    onOpenHoushan10 = { navController.navigate(Routes.Shilian10) },
                    onOpenHoushan11 = { navController.navigate(Routes.Shilian11) },
                    onOpenVolume7Part1 = { navController.navigate(Routes.Volume7Part1) },
                    onOpenVolume8Part1 = {},
                    onOpenVolume9Part1 = {},
                    onOpenVolume10Part1 = {},
                ),
            )
        }
        // 2026-09-18 §11:后山 9 页 —— 复用后山 7 的素材/动画;2026-09-19 §2 升级为过场页
        composable(
            route = Routes.Shilian9,
            enterTransition = {
                scaleIn(
                    animationSpec = tween(durationMillis = 760, easing = FastOutSlowInEasing),
                    initialScale = 1.10f,
                    transformOrigin = TransformOrigin(0.5f, 0.48f),
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 640, delayMillis = 120, easing = LinearEasing),
                )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing))
            },
        ) {
            Houshan9Screen(
                actions = Houshan9Actions(
                    onBack = { navController.popBackStack() },
                    onOpenHoushan10 = { navController.navigate(Routes.Shilian10) },
                    onOpenHoushan11 = { navController.navigate(Routes.Shilian11) },
                    onOpenVolume8Part1 = { navController.navigate(Routes.Volume8Part1) },   // 2026-10-13 §1:赏罚驭灵诀(本页 Y 最大)→ 卷8-1(恢复 §15)
                    onOpenVolume9Part1 = {},
                    onOpenVolume10Part1 = {},
                ),
            )
        }
        // 2026-09-19 §2:后山 10 页 —— 复用后山 8 的素材/动画,当前是终点页
        composable(
            route = Routes.Shilian10,
            enterTransition = {
                scaleIn(
                    animationSpec = tween(durationMillis = 760, easing = FastOutSlowInEasing),
                    initialScale = 1.10f,
                    transformOrigin = TransformOrigin(0.5f, 0.48f),
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 640, delayMillis = 120, easing = LinearEasing),
                )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing))
            },
        ) {
            Houshan10Screen(
                actions = Houshan10Actions(
                    onBack = { navController.popBackStack() },
                    onOpenHoushan11 = { navController.navigate(Routes.Shilian11) },
                    onOpenVolume9Part1 = { navController.navigate(Routes.Volume9Part1) },
                ),
            )
        }
        // 2026-09-19 §3:后山 11 页 —— 复用后山 9 的素材/动画,当前是终点页
        composable(
            route = Routes.Shilian11,
            enterTransition = {
                scaleIn(
                    animationSpec = tween(durationMillis = 760, easing = FastOutSlowInEasing),
                    initialScale = 1.10f,
                    transformOrigin = TransformOrigin(0.5f, 0.48f),
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 640, delayMillis = 120, easing = LinearEasing),
                )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing))
            },
        ) {
            Houshan11Screen(
                actions = Houshan11Actions(
                    onBack = { navController.popBackStack() },
                    onOpenVolume10Part1 = { navController.navigate(Routes.Volume10Part1) },
                ),
            )
        }
        composable(Routes.Unfinished) {
            UnfinishedScreen(
                onBack = { navController.popBackStack() },
                onOpenGunlun1 = {
                    navController.navigate(Routes.Wushuhuan) {
                        popUpTo(Routes.Gunlun1) { inclusive = false }
                        launchSingleTop = true
                    }
                },
            )
        }
        // 旧 Learning/PendingUnlock 静态分支已从导航图移除。
        // 预测、作答、服务端判分统一复用 LearningTrialScreen，避免自动判对与伪奖励。
        composable(Routes.Gunlun1) {
            // 首页会直接进入本页；概览尚未载入时在此补充请求。
            LaunchedEffect(Unit) {
                if (luggageDetailState.learningOverview == null && !luggageDetailState.loading) {
                    luggageViewModel.loadLearningOverview()
                }
            }
            Gunlun1Screen(
                onBack = { navController.popBackStack() },
                onOpenWushuhuan = {
                    navController.navigateSingleTop(Routes.Wushuhuan)
                },
                onOpenHoushan1 = {
                    navController.openBackMountainWendao()
                },
                learningOverview = luggageDetailState.learningOverview,
            )
        }
        // Gunlun2—15 仅保留为旧深链兼容别名；正常书环交互使用独立 Wushuhuan 路由。
        listOf(
            Routes.Wushuhuan,
            Routes.Gunlun2,
            Routes.Gunlun3,
            Routes.Gunlun4,
            Routes.Gunlun5,
            Routes.Gunlun6,
            Routes.Gunlun7,
            Routes.Gunlun8,
            Routes.Gunlun9,
            Routes.Gunlun10,
            Routes.Gunlun11,
            Routes.Gunlun12,
            Routes.Gunlun13,
            Routes.Gunlun14,
            Routes.Gunlun15,
        ).forEach { route ->
            composable(route) {
                LaunchedEffect(Unit) { luggageViewModel.loadLearningOverview() }
                WushuhuanScreen(
                    onBack = { navController.popBackStack() },
                    learningOverview = luggageDetailState.learningOverview,
                    manualDetail = luggageDetailState.manualDetail,
                    isLoading = luggageDetailState.loading,
                    loadMessage = luggageDetailState.message,
                    onOpenHoushan = { lessonId ->
                        navController.openBackMountainWendao(lessonId)
                    },
                    onLoadManualDetail = luggageViewModel::loadManualDetail,
                    onOpenReader = { volumeNo, manualId, continueToTrial ->
                        navController.navigateSingleTop(
                            Routes.manualReader(volumeNo, manualId, continueToTrial)
                        )
                    },
                    onOpenTrial = { trialId ->
                        navController.navigateSingleTop(
                            Routes.learningTrial(trialId, returnToWushuhuan = true)
                        )
                    },
                    onUseInCreation = { manualId ->
                        navController.navigateSingleTop(Routes.gongfang(manualId))
                    },
                )
            }
        }
        composable(
            route = Routes.ManualReaderPattern,
            arguments = listOf(
                navArgument("volumeNo") { type = NavType.IntType },
                navArgument("manualId") { type = NavType.StringType },
                navArgument("continueToTrial") {
                    type = NavType.BoolType
                    defaultValue = false
                },
            ),
        ) { entry ->
            val volumeNo = entry.arguments?.getInt("volumeNo") ?: 1
            val manualId = entry.arguments?.getString("manualId").orEmpty()
            val continueToTrial = entry.arguments?.getBoolean("continueToTrial") ?: false
            LaunchedEffect(manualId) {
                if (manualId.isNotBlank()) luggageViewModel.loadManualDetail(manualId)
            }
            val detail = luggageDetailState.manualDetail
                ?.takeIf { it.manual.id == manualId }
            ManualReaderScreen(
                volumeNo = volumeNo,
                lessonPageNo = detail?.manual?.pageNo,
                isCompleting = luggageDetailState.loading,
                message = luggageDetailState.message,
                onBack = { navController.popBackStack() },
                onComplete = {
                    if (manualId.isBlank()) return@ManualReaderScreen
                    luggageViewModel.completeManualReading(manualId) {
                        val trialId = detail?.manual?.trialId
                        if (continueToTrial && trialId != null) {
                            navController.navigate(
                                Routes.learningTrial(trialId, returnToWushuhuan = true)
                            ) {
                                popUpTo(entry.destination.id) { inclusive = true }
                                launchSingleTop = true
                            }
                        } else {
                            navController.popBackStack()
                        }
                    }
                },
            )
        }
        composable(Routes.Gunlun3) {
            Gunlun3Screen(
                onBack = { navController.popBackStack() },
                onOpenGunlun4 = { navController.navigate(Routes.Gunlun4) },
            )
        }
        composable(Routes.Gunlun4) {
            Gunlun4Screen(
                onBack = { navController.popBackStack() },
                onOpenGunlun5 = { navController.navigate(Routes.Gunlun5) },
            )
        }
        composable(Routes.Gunlun5) {
            Gunlun5Screen(
                onBack = { navController.popBackStack() },
                onOpenGunlun6 = { navController.navigate(Routes.Gunlun6) },
            )
        }
        composable(Routes.Gunlun6) {
            Gunlun6Screen(
                onBack = { navController.popBackStack() },
                onOpenGunlun7 = { navController.navigate(Routes.Gunlun7) },
                onOpenVolume1 = { navController.navigate(Routes.Volume1) },
            )
        }
        composable(Routes.Volume1) {
            Volume1Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume1Part2 = { navController.navigate(Routes.Volume1Part2) },
            )
        }
        composable(Routes.Volume1Part2) {
            Volume1Part2Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume1Part3 = { navController.navigate(Routes.Volume1Part3) },
            )
        }
        composable(Routes.Volume1Part3) {
            Volume1Part3Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume1Part4 = { navController.navigate(Routes.Volume1Part4) },
            )
        }
        composable(Routes.Volume1Part4) {
            Volume1Part4Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume1Part5 = { navController.navigate(Routes.Volume1Part5) },
            )
        }
        composable(Routes.Volume1Part5) {
            Volume1Part5Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume1Part6 = { navController.navigate(Routes.Volume1Part6) },
            )
        }
        composable(Routes.Volume1Part6) {
            Volume1Part6Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume1Part7 = { navController.navigate(Routes.Volume1Part7) },
            )
        }
        composable(Routes.Volume1Part7) {
            Volume1Part7Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume1Part8 = { navController.navigate(Routes.Volume1Part8) },
            )
        }
        composable(Routes.Volume1Part8) {
            Volume1Part8Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume1Part9 = { navController.navigate(Routes.Volume1Part9) },
            )
        }
        composable(Routes.Volume1Part9) {
            Volume1Part9Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume1Part10 = { navController.navigate(Routes.Volume1Part10) },
            )
        }
        composable(Routes.Volume1Part10) {
            Volume1Part10Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume1Part11 = { navController.navigate(Routes.Volume1Part11) },
            )
        }
        composable(Routes.Volume1Part11) {
            Volume1Part11Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume1Part12 = { navController.navigate(Routes.Volume1Part12) },
            )
        }
        composable(Routes.Volume1Part12) {
            Volume1Part12Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume1Part13 = { navController.navigate(Routes.Volume1Part13) },
            )
        }
        composable(Routes.Volume1Part13) {
            Volume1Part13Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume1Part14 = { navController.navigate(Routes.Volume1Part14) },
            )
        }
        composable(Routes.Volume1Part14) {
            Volume1Part14Screen(
                onBack = { navController.popBackStack() },
                onOpenGunlun6 = { navController.navigateSingleTop(Routes.Wushuhuan) },
            )
        }
        composable(Routes.Volume2Part1) {
            Volume2Part1Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume2Part2 = { navController.navigate(Routes.Volume2Part2) },
            )
        }
        composable(Routes.Volume2Part2) {
            Volume2Part2Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume2Part3 = { navController.navigate(Routes.Volume2Part3) },
            )
        }
        composable(Routes.Volume2Part3) {
            Volume2Part3Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume2Part4 = { navController.navigate(Routes.Volume2Part4) },
            )
        }
        composable(Routes.Volume2Part4) {
            Volume2Part4Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume2Part5 = { navController.navigate(Routes.Volume2Part5) },
            )
        }
        composable(Routes.Volume2Part5) {
            Volume2Part5Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume2Part6 = { navController.navigate(Routes.Volume2Part6) },
            )
        }
        composable(Routes.Volume2Part6) {
            Volume2Part6Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume2Part7 = { navController.navigate(Routes.Volume2Part7) },
            )
        }
        composable(Routes.Volume2Part7) {
            Volume2Part7Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume2Part8 = { navController.navigate(Routes.Volume2Part8) },
            )
        }
        composable(Routes.Volume2Part8) {
            Volume2Part8Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume2Part9 = { navController.navigate(Routes.Volume2Part9) },
            )
        }
        composable(Routes.Volume2Part9) {
            Volume2Part9Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume2Part10 = { navController.navigate(Routes.Volume2Part10) },
            )
        }
        composable(Routes.Volume2Part10) {
            Volume2Part10Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume2Part11 = { navController.navigate(Routes.Volume2Part11) },
            )
        }
        composable(Routes.Volume2Part11) {
            Volume2Part11Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume2Part12 = { navController.navigate(Routes.Volume2Part12) },
            )
        }
        composable(Routes.Volume2Part12) {
            Volume2Part12Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume2Part13 = { navController.navigate(Routes.Volume2Part13) },
            )
        }
        composable(Routes.Volume2Part13) {
            Volume2Part13Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume2Part14 = { navController.navigate(Routes.Volume2Part14) },
            )
        }
        composable(Routes.Volume2Part14) {
            Volume2Part14Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume2Part15 = { navController.navigate(Routes.Volume2Part15) },
            )
        }
        composable(Routes.Volume2Part15) {
            Volume2Part15Screen(
                onBack = { navController.popBackStack() },
                onOpenGunlun10 = { navController.navigateSingleTop(Routes.Wushuhuan) },
            )
        }
        composable(Routes.Volume3Part1) {
            Volume3Part1Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume3Part2 = { navController.navigate(Routes.Volume3Part2) },
            )
        }
        composable(Routes.Volume3Part2) {
            Volume3Part2Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume3Part3 = { navController.navigate(Routes.Volume3Part3) },
            )
        }
        composable(Routes.Volume3Part3) {
            Volume3Part3Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume3Part4 = { navController.navigate(Routes.Volume3Part4) },
            )
        }
        composable(Routes.Volume3Part4) {
            Volume3Part4Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume3Part5 = { navController.navigate(Routes.Volume3Part5) },
            )
        }
        composable(Routes.Volume3Part5) {
            Volume3Part5Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume3Part6 = { navController.navigate(Routes.Volume3Part6) },
            )
        }
        composable(Routes.Volume3Part6) {
            Volume3Part6Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume3Part7 = { navController.navigate(Routes.Volume3Part7) },
            )
        }
        composable(Routes.Volume3Part7) {
            Volume3Part7Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume3Part8 = { navController.navigate(Routes.Volume3Part8) },
            )
        }
        composable(Routes.Volume3Part8) {
            Volume3Part8Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume3Part9 = { navController.navigate(Routes.Volume3Part9) },
            )
        }
        composable(Routes.Volume3Part9) {
            Volume3Part9Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume3Part10 = { navController.navigate(Routes.Volume3Part10) },
            )
        }
        composable(Routes.Volume3Part10) {
            Volume3Part10Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume3Part11 = { navController.navigate(Routes.Volume3Part11) },
            )
        }
        composable(Routes.Volume3Part11) {
            Volume3Part11Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume3Part12 = { navController.navigate(Routes.Volume3Part12) },
            )
        }
        composable(Routes.Volume3Part12) {
            Volume3Part12Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume3Part13 = { navController.navigate(Routes.Volume3Part13) },
            )
        }
        composable(Routes.Volume3Part13) {
            Volume3Part13Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume3Part14 = { navController.navigate(Routes.Volume3Part14) },
            )
        }
        composable(Routes.Volume3Part14) {
            Volume3Part14Screen(
                onBack = { navController.popBackStack() },
                onOpenWushuhuan = { navController.navigateSingleTop(Routes.Wushuhuan) },
            )
        }
        composable(Routes.Gunlun7) {
            Gunlun7Screen(
                onBack = { navController.popBackStack() },
                onOpenGunlun8 = { navController.navigate(Routes.Gunlun8) },
                onOpenVolume3Part1 = { navController.navigate(Routes.Volume3Part1) },
            )
        }
        composable(Routes.Gunlun8) {
            Gunlun8Screen(
                onBack = { navController.popBackStack() },
                onOpenGunlun9 = { navController.navigate(Routes.Gunlun9) },
                onOpenVolume4Part1 = { navController.navigate(Routes.Volume4Part1) },
            )
        }
        composable(Routes.Volume4Part1) {
            Volume4Part1Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume4Part2 = { navController.navigate(Routes.Volume4Part2) },
            )
        }
        composable(Routes.Volume4Part2) {
            Volume4Part2Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume4Part3 = { navController.navigate(Routes.Volume4Part3) },
            )
        }
        composable(Routes.Volume4Part3) {
            Volume4Part3Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume4Part4 = { navController.navigate(Routes.Volume4Part4) },
            )
        }
        composable(Routes.Volume4Part4) {
            Volume4Part4Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume4Part5 = { navController.navigate(Routes.Volume4Part5) },
            )
        }
        composable(Routes.Volume4Part5) {
            Volume4Part5Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume4Part6 = { navController.navigate(Routes.Volume4Part6) },
            )
        }
        composable(Routes.Volume4Part6) {
            Volume4Part6Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume4Part7 = { navController.navigate(Routes.Volume4Part7) },
            )
        }
        composable(Routes.Volume4Part7) {
            Volume4Part7Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume4Part8 = { navController.navigate(Routes.Volume4Part8) },
            )
        }
        composable(Routes.Volume4Part8) {
            Volume4Part8Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume4Part9 = { navController.navigate(Routes.Volume4Part9) },
            )
        }
        composable(Routes.Volume4Part9) {
            Volume4Part9Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume4Part10 = { navController.navigate(Routes.Volume4Part10) },
            )
        }
        composable(Routes.Volume4Part10) {
            Volume4Part10Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume4Part11 = { navController.navigate(Routes.Volume4Part11) },
            )
        }
        composable(Routes.Volume4Part11) {
            Volume4Part11Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume4Part12 = { navController.navigate(Routes.Volume4Part12) },
            )
        }
        composable(Routes.Volume4Part12) {
            Volume4Part12Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume4Part13 = { navController.navigate(Routes.Volume4Part13) },
            )
        }
        composable(Routes.Volume4Part13) {
            Volume4Part13Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume4Part14 = { navController.navigate(Routes.Volume4Part14) },
            )
        }
        composable(Routes.Volume4Part14) {
            Volume4Part14Screen(
                onBack = { navController.popBackStack() },
                onOpenGunlun8 = { navController.navigateSingleTop(Routes.Wushuhuan) },
            )
        }
        composable(Routes.Gunlun9) {
            Gunlun9Screen(
                onBack = { navController.popBackStack() },
                onOpenGunlun10 = { navController.navigate(Routes.Gunlun10) },
                onOpenVolume9Part1 = { navController.navigate(Routes.Volume9Part1) },
            )
        }
        composable(Routes.Volume9Part1) {
            Volume9Part1Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume9Part2 = { navController.navigate(Routes.Volume9Part2) },
            )
        }
        composable(Routes.Volume9Part2) {
            Volume9Part2Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume9Part3 = { navController.navigate(Routes.Volume9Part3) },
            )
        }
        composable(Routes.Volume9Part3) {
            Volume9Part3Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume9Part4 = { navController.navigate(Routes.Volume9Part4) },
            )
        }
        composable(Routes.Volume9Part4) {
            Volume9Part4Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume9Part5 = { navController.navigate(Routes.Volume9Part5) },
            )
        }
        composable(Routes.Volume9Part5) {
            Volume9Part5Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume9Part6 = { navController.navigate(Routes.Volume9Part6) },
            )
        }
        composable(Routes.Volume9Part6) {
            Volume9Part6Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume9Part7 = { navController.navigate(Routes.Volume9Part7) },
            )
        }
        composable(Routes.Volume9Part7) {
            Volume9Part7Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume9Part8 = { navController.navigate(Routes.Volume9Part8) },
            )
        }
        composable(Routes.Volume9Part8) {
            Volume9Part8Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume9Part9 = { navController.navigate(Routes.Volume9Part9) },
            )
        }
        composable(Routes.Volume9Part9) {
            Volume9Part9Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume9Part10 = { navController.navigate(Routes.Volume9Part10) },
            )
        }
        composable(Routes.Volume9Part10) {
            Volume9Part10Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume9Part11 = { navController.navigate(Routes.Volume9Part11) },
            )
        }
        composable(Routes.Volume9Part11) {
            Volume9Part11Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume9Part12 = { navController.navigate(Routes.Volume9Part12) },
            )
        }
        composable(Routes.Volume9Part12) {
            Volume9Part12Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume9Part13 = { navController.navigate(Routes.Volume9Part13) },
            )
        }
        composable(Routes.Volume9Part13) {
            Volume9Part13Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume9Part14 = { navController.navigate(Routes.Volume9Part14) },
            )
        }
        composable(Routes.Volume9Part14) {
            Volume9Part14Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume9Part15 = { navController.navigate(Routes.Volume9Part15) },
            )
        }
        composable(Routes.Volume9Part15) {
            Volume9Part15Screen(
                onBack = { navController.popBackStack() },
                onOpenGunlun9 = { navController.navigateSingleTop(Routes.Wushuhuan) },
            )
        }
        composable(Routes.Gunlun10) {
            Gunlun10Screen(
                onBack = { navController.popBackStack() },
                onOpenGunlun11 = { navController.navigate(Routes.Gunlun11) },
                onOpenVolume2Part1 = { navController.navigate(Routes.Volume2Part1) },
            )
        }
        composable(Routes.Gunlun11) {
            Gunlun11Screen(
                onBack = { navController.popBackStack() },
                onOpenGunlun12 = { navController.navigate(Routes.Gunlun12) },
                onOpenVolume8Part1 = { navController.navigate(Routes.Volume8Part1) },
            )
        }
        composable(Routes.Volume8Part1) {
            Volume8Part1Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume8Part2 = { navController.navigate(Routes.Volume8Part2) },
            )
        }
        composable(Routes.Volume8Part2) {
            Volume8Part2Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume8Part3 = { navController.navigate(Routes.Volume8Part3) },
            )
        }
        composable(Routes.Volume8Part3) {
            Volume8Part3Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume8Part4 = { navController.navigate(Routes.Volume8Part4) },
            )
        }
        composable(Routes.Volume8Part4) {
            Volume8Part4Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume8Part5 = { navController.navigate(Routes.Volume8Part5) },
            )
        }
        composable(Routes.Volume8Part5) {
            Volume8Part5Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume8Part6 = { navController.navigate(Routes.Volume8Part6) },
            )
        }
        composable(Routes.Volume8Part6) {
            Volume8Part6Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume8Part7 = { navController.navigate(Routes.Volume8Part7) },
            )
        }
        composable(Routes.Volume8Part7) {
            Volume8Part7Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume8Part8 = { navController.navigate(Routes.Volume8Part8) },
            )
        }
        composable(Routes.Volume8Part8) {
            Volume8Part8Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume8Part9 = { navController.navigate(Routes.Volume8Part9) },
            )
        }
        composable(Routes.Volume8Part9) {
            Volume8Part9Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume8Part10 = { navController.navigate(Routes.Volume8Part10) },
            )
        }
        composable(Routes.Volume8Part10) {
            Volume8Part10Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume8Part11 = { navController.navigate(Routes.Volume8Part11) },
            )
        }
        composable(Routes.Volume8Part11) {
            Volume8Part11Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume8Part12 = { navController.navigate(Routes.Volume8Part12) },
            )
        }
        composable(Routes.Volume8Part12) {
            Volume8Part12Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume8Part13 = { navController.navigate(Routes.Volume8Part13) },
            )
        }
        composable(Routes.Volume8Part13) {
            Volume8Part13Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume8Part14 = { navController.navigate(Routes.Volume8Part14) },
            )
        }
        composable(Routes.Volume8Part14) {
            Volume8Part14Screen(
                onBack = { navController.popBackStack() },
                onOpenGunlun11 = { navController.navigateSingleTop(Routes.Wushuhuan) },
            )
        }
        composable(Routes.Gunlun12) {
            Gunlun12Screen(
                onBack = { navController.popBackStack() },
                onOpenGunlun13 = { navController.navigate(Routes.Gunlun13) },
                onOpenVolume5Part1 = { navController.navigate(Routes.Volume5Part1) },
            )
        }
        composable(Routes.Volume5Part1) {
            Volume5Part1Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume5Part2 = { navController.navigate(Routes.Volume5Part2) },
            )
        }
        composable(Routes.Volume5Part2) {
            Volume5Part2Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume5Part3 = { navController.navigate(Routes.Volume5Part3) },
            )
        }
        composable(Routes.Volume5Part3) {
            Volume5Part3Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume5Part4 = { navController.navigate(Routes.Volume5Part4) },
            )
        }
        composable(Routes.Volume5Part4) {
            Volume5Part4Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume5Part5 = { navController.navigate(Routes.Volume5Part5) },
            )
        }
        composable(Routes.Volume5Part5) {
            Volume5Part5Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume5Part6 = { navController.navigate(Routes.Volume5Part6) },
            )
        }
        composable(Routes.Volume5Part6) {
            Volume5Part6Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume5Part7 = { navController.navigate(Routes.Volume5Part7) },
            )
        }
        composable(Routes.Volume5Part7) {
            Volume5Part7Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume5Part8 = { navController.navigate(Routes.Volume5Part8) },
            )
        }
        composable(Routes.Volume5Part8) {
            Volume5Part8Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume5Part9 = { navController.navigate(Routes.Volume5Part9) },
            )
        }
        composable(Routes.Volume5Part9) {
            Volume5Part9Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume5Part10 = { navController.navigate(Routes.Volume5Part10) },
            )
        }
        composable(Routes.Volume5Part10) {
            Volume5Part10Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume5Part11 = { navController.navigate(Routes.Volume5Part11) },
            )
        }
        composable(Routes.Volume5Part11) {
            Volume5Part11Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume5Part12 = { navController.navigate(Routes.Volume5Part12) },
            )
        }
        composable(Routes.Volume5Part12) {
            Volume5Part12Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume5Part13 = { navController.navigate(Routes.Volume5Part13) },
            )
        }
        composable(Routes.Volume5Part13) {
            Volume5Part13Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume5Part14 = { navController.navigate(Routes.Volume5Part14) },
            )
        }
        composable(Routes.Volume5Part14) {
            Volume5Part14Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume5Part15 = { navController.navigate(Routes.Volume5Part15) },
            )
        }
        composable(Routes.Volume5Part15) {
            Volume5Part15Screen(
                onBack = { navController.popBackStack() },
                onOpenGunlun12 = { navController.navigateSingleTop(Routes.Wushuhuan) },
            )
        }
        composable(Routes.Gunlun13) {
            Gunlun13Screen(
                onBack = { navController.popBackStack() },
                onPandaClick = { navController.navigate(Routes.Gunlun14) },
                onOpenVolume10Part1 = { navController.navigate(Routes.Volume10Part1) },
            )
        }
        composable(Routes.Volume10Part1) {
            Volume10Part1Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume10Part2 = { navController.navigate(Routes.Volume10Part2) },
            )
        }
        composable(Routes.Volume10Part2) {
            Volume10Part2Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume10Part3 = { navController.navigate(Routes.Volume10Part3) },
            )
        }
        composable(Routes.Volume10Part3) {
            Volume10Part3Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume10Part4 = { navController.navigate(Routes.Volume10Part4) },
            )
        }
        composable(Routes.Volume10Part4) {
            Volume10Part4Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume10Part5 = { navController.navigate(Routes.Volume10Part5) },
            )
        }
        composable(Routes.Volume10Part5) {
            Volume10Part5Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume10Part6 = { navController.navigate(Routes.Volume10Part6) },
            )
        }
        composable(Routes.Volume10Part6) {
            Volume10Part6Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume10Part7 = { navController.navigate(Routes.Volume10Part7) },
            )
        }
        composable(Routes.Volume10Part7) {
            Volume10Part7Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume10Part8 = { navController.navigate(Routes.Volume10Part8) },
            )
        }
        composable(Routes.Volume10Part8) {
            Volume10Part8Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume10Part9 = { navController.navigate(Routes.Volume10Part9) },
            )
        }
        composable(Routes.Volume10Part9) {
            Volume10Part9Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume10Part10 = { navController.navigate(Routes.Volume10Part10) },
            )
        }
        composable(Routes.Volume10Part10) {
            Volume10Part10Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume10Part11 = { navController.navigate(Routes.Volume10Part11) },
            )
        }
        composable(Routes.Volume10Part11) {
            Volume10Part11Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume10Part12 = { navController.navigate(Routes.Volume10Part12) },
            )
        }
        composable(Routes.Volume10Part12) {
            Volume10Part12Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume10Part13 = { navController.navigate(Routes.Volume10Part13) },
            )
        }
        composable(Routes.Volume10Part13) {
            Volume10Part13Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume10Part14 = { navController.navigate(Routes.Volume10Part14) },
            )
        }
        composable(Routes.Volume10Part14) {
            Volume10Part14Screen(
                onBack = { navController.popBackStack() },
                onOpenGunlun13 = { navController.navigateSingleTop(Routes.Wushuhuan) },
            )
        }
        composable(Routes.Gunlun14) {
            Gunlun14Screen(
                onBack = { navController.popBackStack() },
                onPandaClick = { navController.navigate(Routes.Gunlun15) },
                onOpenVolume6Part1 = { navController.navigate(Routes.Volume6Part1) },
            )
        }
        composable(Routes.Volume6Part1) {
            Volume6Part1Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume6Part2 = { navController.navigate(Routes.Volume6Part2) },
            )
        }
        composable(Routes.Volume6Part2) {
            Volume6Part2Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume6Part3 = { navController.navigate(Routes.Volume6Part3) },
            )
        }
        composable(Routes.Volume6Part3) {
            Volume6Part3Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume6Part4 = { navController.navigate(Routes.Volume6Part4) },
            )
        }
        composable(Routes.Volume6Part4) {
            Volume6Part4Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume6Part5 = { navController.navigate(Routes.Volume6Part5) },
            )
        }
        composable(Routes.Volume6Part5) {
            Volume6Part5Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume6Part6 = { navController.navigate(Routes.Volume6Part6) },
            )
        }
        composable(Routes.Volume6Part6) {
            Volume6Part6Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume6Part7 = { navController.navigate(Routes.Volume6Part7) },
            )
        }
        composable(Routes.Volume6Part7) {
            Volume6Part7Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume6Part8 = { navController.navigate(Routes.Volume6Part8) },
            )
        }
        composable(Routes.Volume6Part8) {
            Volume6Part8Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume6Part9 = { navController.navigate(Routes.Volume6Part9) },
            )
        }
        composable(Routes.Volume6Part9) {
            Volume6Part9Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume6Part10 = { navController.navigate(Routes.Volume6Part10) },
            )
        }
        composable(Routes.Volume6Part10) {
            Volume6Part10Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume6Part11 = { navController.navigate(Routes.Volume6Part11) },
            )
        }
        composable(Routes.Volume6Part11) {
            Volume6Part11Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume6Part12 = { navController.navigate(Routes.Volume6Part12) },
            )
        }
        composable(Routes.Volume6Part12) {
            Volume6Part12Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume6Part13 = { navController.navigate(Routes.Volume6Part13) },
            )
        }
        composable(Routes.Volume6Part13) {
            Volume6Part13Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume6Part14 = { navController.navigate(Routes.Volume6Part14) },
            )
        }
        composable(Routes.Volume6Part14) {
            Volume6Part14Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume6Part15 = { navController.navigate(Routes.Volume6Part15) },
            )
        }
        composable(Routes.Volume6Part15) {
            Volume6Part15Screen(
                onBack = { navController.popBackStack() },
                onOpenGunlun14 = { navController.navigateSingleTop(Routes.Wushuhuan) },
            )
        }
        composable(Routes.Gunlun15) {
            Gunlun15Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume7Part1 = { navController.navigate(Routes.Volume7Part1) },
            )
        }
        composable(Routes.Gunlun16) {
            Gunlun16Screen(
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.Volume7Part1) {
            Volume7Part1Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume7Part2 = { navController.navigate(Routes.Volume7Part2) },
            )
        }
        composable(Routes.Volume7Part2) {
            Volume7Part2Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume7Part3 = { navController.navigate(Routes.Volume7Part3) },
            )
        }
        composable(Routes.Volume7Part3) {
            Volume7Part3Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume7Part4 = { navController.navigate(Routes.Volume7Part4) },
            )
        }
        composable(Routes.Volume7Part4) {
            Volume7Part4Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume7Part5 = { navController.navigate(Routes.Volume7Part5) },
            )
        }
        composable(Routes.Volume7Part5) {
            Volume7Part5Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume7Part6 = { navController.navigate(Routes.Volume7Part6) },
            )
        }
        composable(Routes.Volume7Part6) {
            Volume7Part6Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume7Part7 = { navController.navigate(Routes.Volume7Part7) },
            )
        }
        composable(Routes.Volume7Part7) {
            Volume7Part7Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume7Part8 = { navController.navigate(Routes.Volume7Part8) },
            )
        }
        composable(Routes.Volume7Part8) {
            Volume7Part8Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume7Part9 = { navController.navigate(Routes.Volume7Part9) },
            )
        }
        composable(Routes.Volume7Part9) {
            Volume7Part9Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume7Part10 = { navController.navigate(Routes.Volume7Part10) },
            )
        }
        composable(Routes.Volume7Part10) {
            Volume7Part10Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume7Part11 = { navController.navigate(Routes.Volume7Part11) },
            )
        }
        composable(Routes.Volume7Part11) {
            Volume7Part11Screen(
                onBack = { navController.popBackStack() },
                onOpenVolume7Part12 = { navController.navigate(Routes.Volume7Part12) },
            )
        }
        composable(Routes.Volume7Part12) {
            Volume7Part12Screen(
                onBack = { navController.popBackStack() },
                onOpenGunlun16 = { navController.navigateSingleTop(Routes.Wushuhuan) },
            )
        }
        composable(Routes.Zaowu)    {
            ZaowuScreen(
                guideSessionKey = currentUser?.id ?: "guest",
                onBack         = {
                    // 引导页的页面返回与系统返回都直接回首页，并移除本次引导实例。
                    navController.navigate(Routes.Home1) {
                        popUpTo(Routes.Home1) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onOpenCreationDesk = {
                    navController.navigate(Routes.Gongfang) {
                        launchSingleTop = true
                    }
                },
                onOpenCreationArchive = {
                    navController.navigate(Routes.Chuangzuodangan) {
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(Routes.Dahui) {
            DahuiScreen(
                onBack = { navController.popBackStack() },
                // 大会入口先展示引导页；演武场组件进入图1的演武场页面。
                onOpenArena = { navController.navigate(Routes.Yanwuchang) },
            )
        }
        composable(Routes.Yanwuchang) {
            YanwuchangScreen(
                onBack = { navController.popBackStack() },
                onOpenDahui = { navController.navigate(Routes.DahuiArena) },
                onOpenYanwuchangVideo = { navController.navigate(Routes.YanwuchangVideo) },
            )
        }
        composable(Routes.YanwuchangVideo) {
            YanwuchangVideoScreen(
                state = conferenceState,
                onBack = { navController.popBackStack() },
                onLoadFeed = { category -> conferenceViewModel.loadFeed(category = category) },
                onLoadWork = conferenceViewModel::loadWork,
                onAddLike = conferenceViewModel::addLike,
                onRemoveLike = conferenceViewModel::removeLike,
                onCreateReview = conferenceViewModel::createReview,
                onAddCollection = conferenceViewModel::addCollection,
                onRemoveCollection = conferenceViewModel::removeCollection,
                onCreateCoCreateRequest = conferenceViewModel::createDerivativeRequest,
                onOpenOwnerRequests = { navController.navigate(Routes.DahuiRequests) },
                onOpenMy = { navController.navigate(Routes.YanwuchangVideoMy) },
            )
        }
        composable(Routes.YanwuchangVideoMy) {
            YanwuchangVideoMyScreen(
                state = conferenceState,
                nickname = currentUser?.nickname.orEmpty(),
                userId = currentUser?.id.orEmpty(),
                onBack = { navController.popBackStack() },
                onLoad = conferenceViewModel::loadMyShelf,
                onOpenWorks = {
                    navController.popBackStack(Routes.YanwuchangVideo, inclusive = false)
                },
                onOpenWork = { navController.navigate(Routes.dahuiWork(it)) },
                onOpenReviews = { navController.navigate(Routes.dahuiWork(it)) },
                onOpenBrowseRecord = {
                    navController.navigate(Routes.YanwuchangVideoBrowseRecord)
                },
            )
        }
        composable(Routes.YanwuchangVideoBrowseRecord) {
            YanwuchangVideoBrowseRecordScreen(
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.YanwuchangVideoComment) {
            YanwuchangVideoComment1Screen(
                onBack = { navController.popBackStack() },
                onOpenExpanded = {
                    navController.navigate(Routes.YanwuchangVideoCommentExpanded)
                },
            )
        }
        composable(Routes.YanwuchangVideoCommentExpanded) {
            YanwuchangVideoComment2Screen(
                onBackToHome = {
                    navController.popBackStack(Routes.YanwuchangVideo, inclusive = false)
                },
                onBackToComment = { navController.popBackStack() },
            )
        }
        composable(Routes.DahuiArena) {
            ConferenceHubScreen(
                state = conferenceState,
                onBack = { navController.popBackStack() },
                onLoad = { conferenceViewModel.loadMatch() },
                onOpenLetters = { navController.navigateConferenceRoot(Routes.DahuiLetters) },
                onOpenRecords = { navController.navigateConferenceRoot(Routes.DahuiRecords) },
                onOpenMatch = { navController.navigate(Routes.DahuiMatch) },
                onOpenCreation = { navController.navigate(Routes.Gongfang) },
            )
        }
        composable(
            Routes.DahuiWorkPattern,
            arguments = listOf(navArgument("publicationId") { type = NavType.StringType }),
        ) { entry ->
            val publicationId = entry.arguments?.getString("publicationId").orEmpty()
            ConferenceWorkScreen(
                publicationId = publicationId,
                state = conferenceState,
                onBack = { navController.popBackStack() },
                onLoad = conferenceViewModel::loadWork,
                onAddCollection = conferenceViewModel::addCollection,
                onCreateReview = conferenceViewModel::createReview,
                onDecideReview = conferenceViewModel::decideReview,
                onAdoptReview = conferenceViewModel::adoptReview,
                onReportReview = conferenceViewModel::reportReview,
                onCreateDerivativeRequest = conferenceViewModel::createDerivativeRequest,
                onOpenRevision = { projectId ->
                    navController.navigate(Routes.creationEditorProject(projectId))
                },
                onMessageShown = conferenceViewModel::clearMessage,
            )
        }
        composable(Routes.DahuiCollections) {
            ConferenceCollectionsScreen(
                state = conferenceState,
                onBack = { navController.popBackStack() },
                onLoad = conferenceViewModel::loadCollections,
                onRemove = conferenceViewModel::removeCollection,
                onOpenWork = { navController.navigate(Routes.dahuiWork(it)) },
            )
        }
        composable(Routes.DahuiRequests) {
            ConferenceRequestsScreen(
                state = conferenceState,
                onBack = { navController.popBackStack() },
                onLoad = conferenceViewModel::loadDerivativeRequests,
                onDecision = conferenceViewModel::decideDerivativeRequest,
                onRevoke = conferenceViewModel::revokeDerivativeAuthorization,
                onOpenWork = { navController.navigate(Routes.dahuiWork(it)) },
                onCreateDerivative = { authorizationId, sourceTitle ->
                    creationViewModel.beginDerivative(authorizationId, sourceTitle)
                    navController.navigate(Routes.Gongfang)
                },
            )
        }
        composable(Routes.DahuiMatch) {
            ConferenceMatchScreen(
                state = conferenceState,
                onBack = { navController.popBackStack() },
                onLoad = conferenceViewModel::loadMatch,
                onJoin = conferenceViewModel::joinMatch,
                onExit = conferenceViewModel::exitMatch,
                onReport = { matchId, reason -> conferenceViewModel.reportMatch(matchId, reason) },
                onSubmitAnswer = conferenceViewModel::submitMatchAnswer,
                onCreateEvaluation = conferenceViewModel::createMatchEvaluation,
                onCreateReflection = conferenceViewModel::createMatchReflection,
                onStartNewMatch = conferenceViewModel::clearCompletedMatch,
                onOpenCreation = { navController.navigate(Routes.Gongfang) },
                onOpenArena = { navController.navigateConferenceRoot(Routes.DahuiArena) },
                onOpenRecords = { navController.navigateConferenceRoot(Routes.DahuiRecords) },
                onOpenLetters = { navController.navigateConferenceRoot(Routes.DahuiLetters) },
                onMessageShown = conferenceViewModel::clearMessage,
            )
        }
        composable(
            Routes.DahuiMatchPattern,
            arguments = listOf(navArgument("matchId") { type = NavType.StringType }),
        ) { entry ->
            ConferenceMatchScreen(
                state = conferenceState,
                directMatchId = entry.arguments?.getString("matchId"),
                onBack = { navController.popBackStack() },
                onLoad = conferenceViewModel::loadMatch,
                onJoin = conferenceViewModel::joinMatch,
                onExit = conferenceViewModel::exitMatch,
                onReport = { matchId, reason -> conferenceViewModel.reportMatch(matchId, reason) },
                onSubmitAnswer = conferenceViewModel::submitMatchAnswer,
                onCreateEvaluation = conferenceViewModel::createMatchEvaluation,
                onCreateReflection = conferenceViewModel::createMatchReflection,
                onStartNewMatch = {
                    conferenceViewModel.clearCompletedMatch()
                    navController.navigate(Routes.DahuiMatch) { launchSingleTop = true }
                },
                onOpenCreation = { navController.navigate(Routes.Gongfang) },
                onOpenArena = { navController.navigateConferenceRoot(Routes.DahuiArena) },
                onOpenRecords = { navController.navigateConferenceRoot(Routes.DahuiRecords) },
                onOpenLetters = { navController.navigateConferenceRoot(Routes.DahuiLetters) },
                onMessageShown = conferenceViewModel::clearMessage,
            )
        }
        composable(Routes.DahuiRecords) {
            ConferenceMatchRecordsScreen(
                state = conferenceState,
                onBack = { navController.popBackStack() },
                onLoad = conferenceViewModel::loadMatchRecords,
                onOpenMatch = { navController.navigate(Routes.dahuiMatch(it)) },
                onOpenArena = { navController.navigateConferenceRoot(Routes.DahuiArena) },
                onOpenLetters = { navController.navigateConferenceRoot(Routes.DahuiLetters) },
                onMessageShown = conferenceViewModel::clearMessage,
            )
        }
        composable(Routes.DahuiLetters) {
            ConferenceLettersScreen(
                state = conferenceState,
                onBack = { navController.popBackStack() },
                onLoad = conferenceViewModel::loadLetters,
                onMarkRead = conferenceViewModel::readLetter,
                onOpenTarget = { letter ->
                    Routes.conferenceLetterDestination(
                        navigationTarget = letter.navigationTarget,
                        navigationId = letter.navigationId,
                        actionType = letter.actionType,
                        actionId = letter.actionId,
                    )?.let { destination ->
                        navController.navigate(destination) { launchSingleTop = true }
                        true
                    } ?: false
                },
                onOpenArena = { navController.navigateConferenceRoot(Routes.DahuiArena) },
                onOpenRecords = { navController.navigateConferenceRoot(Routes.DahuiRecords) },
                onMessageShown = conferenceViewModel::clearMessage,
            )
        }
        composable(
            route = Routes.GongfangPattern,
            arguments = listOf(
                navArgument("sourceManualId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) { entry ->
            val sourceManualId = entry.arguments?.getString("sourceManualId")
            val recentWorks = creationState.recentProjects
                .asSequence()
                .mapNotNull { it.toCreationResumeItem() }
                .distinctBy { it.projectId }
                .take(2)
                .toList()
            val openCreationArchive = {
                navController.navigate(Routes.Chuangzuodangan) {
                    launchSingleTop = true
                }
            }
            GongfangScreen(
                onBack   = {
                    creationViewModel.clearDerivative()
                    if (!sourceManualId.isNullOrBlank()) {
                        navController.popBackStack()
                    } else if (
                        navController.previousBackStackEntry?.destination?.route == Routes.Zaowu
                    ) {
                        navController.popBackStack()
                    } else {
                        // 非工坊入口继续沿用原逻辑，避免改变其他创作来源的返回路径。
                        navController.navigate(Routes.Home1) {
                            popUpTo(Routes.Home1) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                },
                initialManualId = sourceManualId,
                onStartConversation = { idea, assets, manuals ->
                    creationViewModel.startConversation(idea, assets, manuals) { conversation ->
                        navController.navigate(Routes.shengtuProject(conversation.project.id))
                    }
                },
                onContinueWork = { workId ->
                    navController.navigate(Routes.shengtuProject(workId))
                },
                onOpenAllWorks = openCreationArchive,
                recentWorks = recentWorks,
                recentWorksLoading = creationState.loadingRecent,
                recentWorksMessage = creationState.recentError,
                derivativeSourceTitle = creationState.derivativeSourceTitle,
                onClearDerivative = creationViewModel::clearDerivative,
                onLoadRecentWorks = creationViewModel::loadRecentProjects,
                manualSources = creationState.manualSources,
                manualSourcesLoading = creationState.loadingManualSources,
                manualSourcesMessage = creationState.manualSourceError,
                onLoadCreationSources = creationViewModel::loadCreationSources,
                uploadingSketch = creationState.uploadingSketch,
                sketchSourceAssetId = creationState.sketchSource?.assetId,
                sketchSourceMessage = creationState.sketchSourceMessage,
                onUploadSketch = creationViewModel::uploadSketch,
                startingConversation = creationState.startingConversation,
                analysisMessage = creationState.analysisError,
            )
        }

        composable(
            Routes.ShengtuProjectPattern,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType }),
        ) { entry ->
            val projectId = entry.arguments?.getString("projectId").orEmpty()
            LaunchedEffect(projectId) {
                luggageViewModel.loadCreationDetail(projectId)
                creationViewModel.loadCreationSources()
            }
            val bundle = luggageDetailState.creationDetail
                ?.takeIf { it.project.id == projectId }
            val bundleGeneration = bundle?.imageGenerations?.items?.firstOrNull()
            val visibleGeneration = creationState.generationJob
                ?.takeIf { it.projectId == projectId }
                ?: bundleGeneration
            LaunchedEffect(projectId, bundleGeneration?.id, bundleGeneration?.status) {
                bundleGeneration?.let { job ->
                    creationViewModel.resumeImageGeneration(job) {
                        luggageViewModel.loadCreationDetail(projectId)
                        luggageViewModel.refresh(force = true)
                    }
                }
            }
            ShengtuScreen(
                projectId = projectId,
                projectTitle = bundle?.project?.title,
                conversation = bundle?.conversation,
                activeGeneration = visibleGeneration,
                loading = luggageDetailState.loading && bundle == null,
                busy = creationState.conversationBusy &&
                    creationState.conversationProjectId == projectId,
                statusMessage = creationState.conversationMessage
                    ?.takeIf { creationState.conversationProjectId == projectId },
                errorMessage = creationState.conversationError
                    ?.takeIf { creationState.conversationProjectId == projectId },
                generationBusy = creationState.generationBusy &&
                    creationState.generationProjectId == projectId,
                generationMessage = creationState.generationMessage
                    ?.takeIf { creationState.generationProjectId == projectId },
                onSendMessage = { text ->
                    creationViewModel.sendConversationMessage(projectId, text) {
                        luggageViewModel.loadCreationDetail(projectId)
                    }
                },
                onAcceptSuggestion = { message ->
                    bundle?.conversation?.let { current ->
                        creationViewModel.acceptConversationSuggestion(
                            projectId = projectId,
                            message = message,
                            expectedRevision = current.rowVersion,
                        ) {
                            luggageViewModel.loadCreationDetail(projectId)
                        }
                    }
                },
                onSaveDraftAndGenerate = {
                    bundle?.conversation?.let { current ->
                        creationViewModel.saveConversationDraftAndGenerate(
                            projectId = projectId,
                            expectedRevision = current.rowVersion,
                        ) {
                            luggageViewModel.loadCreationDetail(projectId)
                            luggageViewModel.refresh(force = true)
                        }
                    }
                },
                onSaveWork = {
                    bundle?.conversation?.let { current ->
                        creationViewModel.saveConversationResult(
                            projectId = projectId,
                            expectedRevision = current.rowVersion,
                        ) {
                            luggageViewModel.loadCreationDetail(projectId)
                            creationViewModel.loadRecentProjects()
                        }
                    }
                },
                onRetryGeneration = { job ->
                    creationViewModel.retryImageGeneration(job) {
                        luggageViewModel.loadCreationDetail(projectId)
                        luggageViewModel.refresh(force = true)
                    }
                },
                onBack = { navController.popBackStack() },
                onOpenChuangzuodangan = { navController.navigate(Routes.Chuangzuodangan) },
                onPublishToConference = {
                    navController.navigateSingleTop(Routes.conferencePublish(projectId))
                },
            )
        }

        composable(Routes.Chuangzuodangan) {
            LaunchedEffect(Unit) {
                creationViewModel.loadRecentProjects()
            }
            ChuangzuodanganScreen(
                onBack = { navController.popBackStack() },
                guideSessionKey = currentUser?.id.orEmpty().ifBlank { "guest" },
                onContinueWork = { projectId ->
                    creationViewModel.continueProject(projectId) {
                        luggageViewModel.loadCreationDetail(projectId)
                        navController.navigate(Routes.shengtuProject(projectId))
                    }
                },
                onPublishWork = { projectId ->
                    navController.navigateSingleTop(Routes.conferencePublish(projectId))
                },
                onDeleteWork = { projectId ->
                    luggageViewModel.deleteCreationProject(projectId) {
                        creationViewModel.removeDeletedProject(projectId)
                    }
                },
                recentWorks = creationState.recentProjects,
                recentWorksLoading = creationState.loadingRecent,
                recentWorksMessage = creationState.recentError,
                continuingProjectId = creationState.continuingProjectId,
                continueMessage = creationState.continueMessage,
                onRetryRecentWorks = creationViewModel::loadRecentProjects,
                archiveDetail = luggageDetailState.creationDetail,
                archiveDetailProjectId = luggageDetailState.creationDetailProjectId,
                archiveDetailLoading = luggageDetailState.loading,
                archiveDetailMessage = luggageDetailState.message,
                onArchiveWorkSelected = luggageViewModel::loadCreationDetail,
            )
        }


        // 行囊页(Figma 设计) — 点击首页1的"行囊"按钮跳转
        composable(Routes.Luggage) {
            LaunchedEffect(Unit) { luggageViewModel.refresh() }
            LuggageScreen(
                uiState = luggageState,
                onBack = { navController.popBackStack() },
                onOpenZaowu = { navController.navigate(Routes.Zaowu) },
                onRefresh = { luggageViewModel.refresh(force = true) },
                onOpenBadges = { navController.navigate(Routes.LuggageBadges) },
                onOpenGrowth = { navController.navigate(Routes.LuggageGrowth) },
                onOpenManuals = { state -> navController.navigate(Routes.luggageManuals(state)) },
                onOpenManual = { id -> navController.navigate(Routes.luggageManualDetail(id)) },
                onOpenMistakes = { navController.navigate(Routes.LuggageMistakes) },
                onRetryMistake = { id ->
                    luggageViewModel.retryMistake(id) { session ->
                        navController.navigate(
                            Routes.retryTrial(
                                session.mistakeId,
                                session.trialId,
                                session.trialVersionId,
                                session.id,
                            )
                        )
                    }
                },
                onOpenCreations = { navController.navigate(Routes.LuggageCreations) },
                onOpenCreation = { id -> navController.navigate(Routes.luggageCreationDetail(id)) },
                onContinueCreation = { id -> navController.navigate(Routes.luggageCreationDetail(id)) },
                onOpenEvidence = { navController.navigate(Routes.LuggageEvidence) },
                onOpenPrivacy = { navController.navigate(Routes.LuggagePrivacySafety) },
            )
        }

        composable(
            Routes.CreationEditorProjectPattern,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType }),
        ) { entry ->
            val projectId = entry.arguments?.getString("projectId").orEmpty()
            LaunchedEffect(projectId) {
                creationViewModel.loadEditor(projectId)
            }
            val belongsToProject = creationState.editorProjectId == projectId
            val currentVersion = creationState.editorVersions
                .takeIf { belongsToProject }
                ?.maxByOrNull { it.versionNumber }
            CreationEditorScreen(
                projectTitle = creationState.editorProject
                    ?.takeIf { belongsToProject }
                    ?.title,
                version = currentVersion,
                assets = creationState.editorAssets.takeIf { belongsToProject }.orEmpty(),
                loading = creationState.editorLoading && belongsToProject,
                saving = creationState.editorSaving && belongsToProject,
                message = creationState.editorMessage.takeIf { belongsToProject },
                diff = creationState.editorDiff.takeIf { belongsToProject },
                exportBusy = creationState.editorExportBusy && belongsToProject,
                exportJob = creationState.editorExportJob.takeIf { belongsToProject },
                exportMessage = creationState.editorExportMessage.takeIf { belongsToProject },
                onBack = { navController.popBackStack() },
                onReload = { creationViewModel.loadEditor(projectId) },
                onSave = { parent, layers, reason ->
                    creationViewModel.saveEditorVersion(
                        projectId = projectId,
                        parent = parent,
                        layers = layers,
                        modificationReason = reason,
                    ) {
                        luggageViewModel.loadCreationDetail(projectId)
                        luggageViewModel.refresh(force = true)
                        conferenceState.work
                            ?.takeIf { it.projectId == projectId }
                            ?.let { conferenceViewModel.loadWork(it.publicationId) }
                    }
                },
                onCompare = creationViewModel::compareEditorWithPrevious,
                onExport = creationViewModel::exportEditorVersion,
                onDismissDiff = creationViewModel::clearEditorDiff,
            )
        }

        composable(Routes.LuggageBadges) {
            BadgesScreen(
                state = luggageDetailState,
                onBack = { navController.popBackStack() },
                onLoad = luggageViewModel::loadBadges,
            )
        }

        composable(Routes.LuggageGrowth) {
            EvidenceScreen(
                title = "本周成长记录",
                weekOnly = true,
                state = luggageDetailState,
                onBack = { navController.popBackStack() },
                onLoad = { category -> luggageViewModel.loadEvidence(category, weekOnly = true) },
                onLoadMore = { category -> luggageViewModel.loadMoreEvidence(category, weekOnly = true) },
            )
        }

        composable(Routes.LuggageEvidence) {
            EvidenceScreen(
                title = "学习证据",
                weekOnly = false,
                state = luggageDetailState,
                onBack = { navController.popBackStack() },
                onLoad = { category -> luggageViewModel.loadEvidence(category) },
                onLoadMore = { category -> luggageViewModel.loadMoreEvidence(category) },
            )
        }

        composable(
            Routes.LuggageManualsPattern,
            arguments = listOf(navArgument("state") { type = NavType.StringType }),
        ) { entry ->
            val initialState = entry.arguments?.getString("state")?.takeUnless { it == "ALL" }
            ManualsScreen(
                state = luggageDetailState,
                initialState = initialState,
                onBack = { navController.popBackStack() },
                onLoad = { volume, query, state, favorites ->
                    luggageViewModel.loadManuals(volume, query, state, favorites)
                },
                onLoadMore = { volume, query, state, favorites ->
                    luggageViewModel.loadMoreManuals(volume, query, state, favorites)
                },
                onOpenManual = { id -> navController.navigate(Routes.luggageManualDetail(id)) },
                onToggleFavorite = luggageViewModel::toggleManualFavorite,
            )
        }

        composable(
            Routes.LuggageManualDetailPattern,
            arguments = listOf(navArgument("manualId") { type = NavType.StringType }),
        ) { entry ->
            val manualId = entry.arguments?.getString("manualId").orEmpty()
            ManualDetailScreen(
                manualId = manualId,
                state = luggageDetailState,
                onBack = { navController.popBackStack() },
                onLoad = luggageViewModel::loadManualDetail,
                onOpenTrial = { trialId -> navController.navigate(Routes.learningTrial(trialId)) },
            )
        }

        composable(
            Routes.LearningTrialPattern,
            arguments = listOf(
                navArgument("trialId") { type = NavType.StringType },
                navArgument("returnToWushuhuan") {
                    type = NavType.BoolType
                    defaultValue = false
                },
            ),
        ) { entry ->
            val trialId = entry.arguments?.getString("trialId").orEmpty()
            val returnToWushuhuan =
                entry.arguments?.getBoolean("returnToWushuhuan") ?: false
            val completeTrial: () -> Unit = {
                luggageViewModel.loadLearningOverview()
                if (returnToWushuhuan) {
                    if (!navController.popBackStack(Routes.Wushuhuan, inclusive = false)) {
                        navController.navigate(Routes.Wushuhuan) {
                            popUpTo(entry.destination.id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                } else {
                    navController.popBackStack()
                }
            }
            LearningTrialScreen(
                trialId = trialId,
                state = luggageDetailState,
                onBack = {
                    if (returnToWushuhuan && luggageDetailState.trialResult?.passed == true) {
                        completeTrial()
                    } else {
                        navController.popBackStack()
                    }
                },
                onLoad = luggageViewModel::loadLearningTrial,
                onSubmit = { prediction, answer, explanation ->
                    luggageViewModel.submitLearningTrial(trialId, prediction, answer, explanation)
                },
                onRetry = luggageViewModel::retryLearningTrial,
                onComplete = completeTrial,
                completeLabel = if (returnToWushuhuan) "返回悟书环查看状态" else "返回秘籍",
            )
        }

        composable(Routes.LuggageMistakes) {
            MistakesScreen(
                state = luggageDetailState,
                onBack = { navController.popBackStack() },
                onLoad = { luggageViewModel.loadMistakes() },
                onLoadMore = { luggageViewModel.loadMoreMistakes() },
                onOpen = { id -> navController.navigate(Routes.luggageMistakeDetail(id)) },
                onRetry = { id ->
                    luggageViewModel.retryMistake(id) { session ->
                        navController.navigate(
                            Routes.retryTrial(
                                session.mistakeId,
                                session.trialId,
                                session.trialVersionId,
                                session.id,
                            )
                        )
                    }
                },
            )
        }

        composable(
            Routes.LuggageMistakeDetailPattern,
            arguments = listOf(navArgument("mistakeId") { type = NavType.StringType }),
        ) { entry ->
            val mistakeId = entry.arguments?.getString("mistakeId").orEmpty()
            MistakeDetailScreen(
                mistakeId = mistakeId,
                state = luggageDetailState,
                onBack = { navController.popBackStack() },
                onLoad = luggageViewModel::loadMistakeDetail,
                onRetry = { id ->
                    luggageViewModel.retryMistake(id) { session ->
                        navController.navigate(
                            Routes.retryTrial(
                                session.mistakeId,
                                session.trialId,
                                session.trialVersionId,
                                session.id,
                            )
                        )
                    }
                },
            )
        }

        composable(Routes.LuggageCreations) {
            CreationsScreen(
                state = luggageDetailState,
                onBack = { navController.popBackStack() },
                onLoad = { luggageViewModel.loadCreations() },
                onLoadMore = { luggageViewModel.loadMoreCreations() },
                onOpen = { id -> navController.navigate(Routes.luggageCreationDetail(id)) },
                onContinue = { id -> navController.navigate(Routes.luggageCreationDetail(id)) },
            )
        }

        composable(
            Routes.LuggageCreationDetailPattern,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType }),
        ) { entry ->
            val projectId = entry.arguments?.getString("projectId").orEmpty()
            CreationDetailScreen(
                projectId = projectId,
                state = luggageDetailState,
                onBack = { navController.popBackStack() },
                onLoad = luggageViewModel::loadCreationDetail,
                onContinue = {
                    navController.navigate(Routes.shengtuProject(it))
                },
                onPublish = { id ->
                    navController.navigateSingleTop(Routes.conferencePublish(id))
                },
                onWithdraw = luggageViewModel::withdrawPublication,
                onAppeal = luggageViewModel::createAppeal,
                onDelete = { id ->
                    luggageViewModel.deleteCreationProject(id) {
                        navController.popBackStack()
                    }
                },
            )
        }

        composable(
            Routes.ConferencePublishPattern,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType }),
        ) { entry ->
            val projectId = entry.arguments?.getString("projectId").orEmpty()
            ConferencePublishScreen(
                projectId = projectId,
                state = luggageDetailState,
                publishBusy = creationState.workflowBusy &&
                    creationState.workflowMessageProjectId == projectId,
                publishMessage = creationState.workflowMessage
                    ?.takeIf { creationState.workflowMessageProjectId == projectId },
                publishFailed = creationState.workflowFailed &&
                    creationState.workflowMessageProjectId == projectId,
                onBack = { navController.popBackStack() },
                onLoad = luggageViewModel::loadCreationDetail,
                onSubmit = { bundle, draft ->
                    creationViewModel.publishToConference(bundle, draft) {
                        luggageViewModel.loadCreationDetail(projectId)
                        luggageViewModel.refresh(force = true)
                        creationViewModel.loadRecentProjects()
                        conferenceViewModel.invalidateFeed()
                    }
                },
            )
        }

        composable(Routes.LuggagePrivacySafety) {
            PrivacySafetyScreen(
                state = luggageDetailState,
                onBack = { navController.popBackStack() },
                onLoad = luggageViewModel::loadPrivacy,
                onToggle = { key, value ->
                    luggageViewModel.updatePrivacy { current ->
                        when (key) {
                            "learning_card_public" -> PrivacySettingsPatchDto(
                                learningCardPublic = value,
                                rowVersion = current.rowVersion,
                            )
                            "aigc_export_mark_enabled" -> PrivacySettingsPatchDto(
                                aigcExportMarkEnabled = value,
                                rowVersion = current.rowVersion,
                            )
                            else -> PrivacySettingsPatchDto(
                                profileDiscoveryEnabled = value,
                                rowVersion = current.rowVersion,
                            )
                        }
                    }
                },
                onVisibility = { visibility ->
                    luggageViewModel.updatePrivacy { current ->
                        PrivacySettingsPatchDto(
                            defaultWorkVisibility = visibility,
                            rowVersion = current.rowVersion,
                        )
                    }
                },
                onExport = luggageViewModel::loadAccountExport,
                onRequestDeletion = luggageViewModel::requestAccountDeletion,
            )
        }

        composable(
            Routes.RetryTrialPattern,
            arguments = listOf(
                navArgument("mistakeId") { type = NavType.StringType },
                navArgument("trialId") { type = NavType.StringType },
                navArgument("versionId") { type = NavType.StringType },
                navArgument("sessionId") { type = NavType.StringType },
            ),
        ) { entry ->
            val session = RetrySessionDto(
                id = entry.arguments?.getString("sessionId").orEmpty(),
                mistakeId = entry.arguments?.getString("mistakeId").orEmpty(),
                trialId = entry.arguments?.getString("trialId").orEmpty(),
                trialVersionId = entry.arguments?.getString("versionId").orEmpty(),
                expiresAt = "",
                submitUrl = "",
            )
            RetryTrialScreen(
                session = session,
                state = luggageDetailState,
                onBack = { navController.popBackStack() },
                onLoad = luggageViewModel::loadRetryTrial,
                onSubmit = { prediction, answer, explanation ->
                    luggageViewModel.submitRetry(session, prediction, answer, explanation)
                },
                onComplete = {
                    luggageViewModel.refreshMistakeAfterRetry(session.mistakeId)
                    navController.popBackStack()
                },
            )
        }

        // 设置页(Figma 设计) — 点击首页1的"设置"图标跳转
        composable(Routes.Settings) {
            SettingsScreen(
                onBack             = { navController.popBackStack() },
                onOpenAccount      = { navController.navigate(Routes.SettingsAccount) },
                onOpenMessage      = { navController.navigate(Routes.SettingsMessage) },
                onOpenGeneral      = { navController.navigate(Routes.SettingsGeneral) },
                onOpenSound        = { navController.navigate(Routes.SettingsSound) },
                onOpenBlacklist    = { navController.navigate(Routes.SettingsBlacklist) },
                onOpenPrivacy      = { navController.navigate(Routes.Privacy) },
                onOpenAgreement    = { navController.navigate(Routes.Agreement) },
                onOpenCollection   = { navController.navigate(Routes.SettingsCollection) },
                onOpenSharing      = { navController.navigate(Routes.SettingsSharing) },
                onOpenHelp         = { navController.navigate(Routes.SettingsHelp) },
                onOpenAbout        = { navController.navigate(Routes.SettingsAbout) },
                onOpenDataRecovery = { navController.navigate(Routes.SettingsDataRecovery) },
                onSwitchAccount    = {
                    authViewModel.logout {
                        navController.navigate(Routes.Login) {
                            popUpTo(navController.graph.id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                onLogout           = {
                    authViewModel.logout {
                        navController.navigate(Routes.Login) {
                            popUpTo(navController.graph.id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
            )
        }

        composable(Routes.SettingsAccount) {
            SettingsDetailScreen(
                page = SettingsPage.Account,
                onBack = { navController.popBackStack() },
                onChangePassword = { navController.navigate(Routes.Forgot) },
            )
        }
        composable(Routes.SettingsMessage) {
            SettingsDetailScreen(SettingsPage.Message, onBack = { navController.popBackStack() })
        }
        composable(Routes.SettingsGeneral) {
            SettingsDetailScreen(SettingsPage.General, onBack = { navController.popBackStack() })
        }
        composable(Routes.SettingsSound) {
            SettingsDetailScreen(SettingsPage.Sound, onBack = { navController.popBackStack() })
        }
        composable(Routes.SettingsBlacklist) {
            SettingsDetailScreen(SettingsPage.Blacklist, onBack = { navController.popBackStack() })
        }
        composable(Routes.SettingsCollection) {
            SettingsDetailScreen(SettingsPage.Collection, onBack = { navController.popBackStack() })
        }
        composable(Routes.SettingsSharing) {
            SettingsDetailScreen(SettingsPage.Sharing, onBack = { navController.popBackStack() })
        }
        composable(Routes.SettingsHelp) {
            SettingsDetailScreen(SettingsPage.Help, onBack = { navController.popBackStack() })
        }
        composable(Routes.SettingsAbout) {
            SettingsDetailScreen(
                page = SettingsPage.About,
                onBack = { navController.popBackStack() },
                onOpenPrivacy = { navController.navigate(Routes.Privacy) },
                onOpenAgreement = { navController.navigate(Routes.Agreement) },
            )
        }
        composable(Routes.SettingsDataRecovery) {
            SettingsDetailScreen(SettingsPage.DataRecovery, onBack = { navController.popBackStack() })
        }

    }
        if (loginTransitionRunning) {
            LoginMistTransition(
                phase = { mistPhase.value },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

private fun NavHostController.navigateConferenceRoot(route: String) {
    navigate(route) {
        popUpTo(Routes.DahuiArena) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
