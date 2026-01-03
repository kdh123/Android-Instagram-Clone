package com.dhkim.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// =================================================================
// 1. 공통 색상 (테마와 관계없이 동일한 색상)
// =================================================================

// Primary: 핵심 액션 (팔로우 버튼, 링크)
val PrimaryBlue = Color(0xFF495EFA)

// System: 알림, 좋아요, 오류 (빨간색)
val SystemRed = Color(0xFFED4956)


// =================================================================
// 2. 라이트 테마 색상 (Light Theme)
// =================================================================

val LightBackground = Color(0xFFFFFFFF)       // 순수한 흰색 배경
val LightTextPrimary = Color(0xFF262626)      // 인스타그램 표준 검은색 텍스트
val LightTextSecondary = Color(0xFF8E8E8E)    // 보조/회색 텍스트, 아이콘
val LightDivider = Color(0xFFEFEFEF)          // 아주 옅은 구분선


// =================================================================
// 3. 다크 테마 색상 (Dark Theme)
// =================================================================

val DarkBackground = Color(0xFF000000)        // 순수한 검은색 배경 (OLED 최적화)
val DarkTextPrimary = Color(0xFFFFFFFF)       // 흰색 텍스트
val DarkTextSecondary = Color(0xFFA8A8A8)     // 보조/옅은 회색 텍스트
val DarkDivider = Color(0xFF262626)           // 어두운 구분선 (Light TextPrimary와 동일톤)

@Immutable
data class InstagramColors(
    val primary: Color,         // 핵심 액션 (파란색)
    val background: Color,      // 화면 배경
    val onBackground: Color,    // 배경 위에 올라오는 기본 텍스트/아이콘 색상
    val secondary: Color,
    val onSecondary: Color,
    val surface: Color,         // 카드, 모달 등 표면 색상
    val onSurface: Color,       // surface 위에 올라오는 색상
    val divider: Color,         // 구분선
    val textPrimary: Color,     // 기본 텍스트 색상
    val textSecondary: Color,   // 보조 텍스트 색상
    val error: Color,           // 오류/시스템 상태 (빨간색)
    val isLight: Boolean        // 현재 테마가 Light인지 Dark인지 구분
)

val LightInstagramColors = InstagramColors(
    primary = PrimaryBlue,
    background = LightBackground,
    onBackground = LightTextPrimary,
    secondary = LightDivider,
    onSecondary = LightTextSecondary,
    surface = LightBackground,
    onSurface = LightTextPrimary,
    divider = LightDivider,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    error = SystemRed,
    isLight = true
)

val DarkInstagramColors = InstagramColors(
    primary = PrimaryBlue,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    secondary = DarkDivider,
    onSecondary = DarkTextSecondary,
    surface = DarkBackground,
    onSurface = DarkTextPrimary,
    divider = DarkDivider,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    error = SystemRed,
    isLight = false
)

val LocalInstagramColors = staticCompositionLocalOf { LightInstagramColors }