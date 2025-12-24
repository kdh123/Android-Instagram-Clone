package com.dhkim.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val InstagramStyle = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Normal,
)

internal val Typography = InstagramTypography(
    // 1. Headline (주요 화면 제목 및 큰 텍스트, Bold 중심)

    headlineLarge = InstagramStyle.copy( // 예: 로그인/가입 화면 타이틀
        fontSize = 32.sp,
        lineHeight = 40.sp,
        fontWeight = FontWeight.Bold
    ),
    headlineMedium = InstagramStyle.copy( // 예: 프로필 설정 타이틀
        fontSize = 28.sp,
        lineHeight = 36.sp,
        fontWeight = FontWeight.Bold
    ),
    headlineSmall = InstagramStyle.copy( // 예: 알림 탭 섹션 타이틀
        fontSize = 24.sp,
        lineHeight = 32.sp,
        fontWeight = FontWeight.Bold
    ),

    // 2. Title (중간 제목, 사용자 이름, 버튼 텍스트 등)

    titleLarge = InstagramStyle.copy( // 예: 큰 버튼 위의 텍스트
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.SemiBold
    ),
    titleMedium = InstagramStyle.copy( // 예: 게시물의 사용자 이름
        fontSize = 18.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.15.sp,
    ),
    titleSmall = InstagramStyle.copy( // 예: 스토리 사용자 이름
        fontSize = 16.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.1.sp,
    ),

    // 3. Body (본문 텍스트, 가장 흔한 텍스트)

    bodyLarge = InstagramStyle.copy( // 16sp: 일반적인 피드 캡션 및 본문
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.5.sp,
    ),
    bodyLargeBold = InstagramStyle.copy( // 16sp Bold: 캡션 내 강조
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.5.sp,
        fontWeight = FontWeight.Bold
    ),
    bodyMedium = InstagramStyle.copy( // 14sp: 보조 텍스트, 프로필 설명 등
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.25.sp,
    ),
    bodyMediumBold = InstagramStyle.copy( // 14sp Bold: 팔로우 버튼 텍스트
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.25.sp,
        fontWeight = FontWeight.Bold
    ),
    bodySmall = InstagramStyle.copy( // 12sp: 시간 표시, 작은 정보 텍스트
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),
    bodySmallGray = InstagramStyle.copy( // 12sp Gray: 회색 텍스트
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
        color = Color.Gray // Gray 상수 사용
    ),

    // 4. Label (버튼, 탭바 라벨 등)

    labelLarge = InstagramStyle.copy( // 16sp: 큰 버튼 텍스트
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        fontWeight = FontWeight.SemiBold
    ),
    labelMedium = InstagramStyle.copy( // 14sp: 탭 바 라벨
        fontSize = 14.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        fontWeight = FontWeight.SemiBold
    ),
    labelMediumBold = InstagramStyle.copy( // 14sp: 탭 바 라벨
        fontSize = 14.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        fontWeight = FontWeight.Bold
    ),
    labelSmall = InstagramStyle.copy( // 12sp: 아주 작은 라벨
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        fontWeight = FontWeight.SemiBold
    ),
    labelSmallBold = InstagramStyle.copy(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        fontWeight = FontWeight.Bold
    )
)

val LocalTypography = staticCompositionLocalOf {
    // 모든 필드를 기본 스타일로 채웁니다.
    InstagramTypography(
        headlineLarge = InstagramStyle,
        headlineMedium = InstagramStyle,
        headlineSmall = InstagramStyle,
        titleLarge = InstagramStyle,
        titleMedium = InstagramStyle,
        titleSmall = InstagramStyle,
        bodyLarge = InstagramStyle,
        bodyLargeBold = InstagramStyle,
        bodyMedium = InstagramStyle,
        bodyMediumBold = InstagramStyle,
        bodySmall = InstagramStyle,
        bodySmallGray = InstagramStyle,
        labelLarge = InstagramStyle,
        labelMedium = InstagramStyle,
        labelMediumBold = InstagramStyle,
        labelSmall = InstagramStyle,
        labelSmallBold = InstagramStyle,
    )
}

@Immutable
data class InstagramTypography(
    // 1. Headline
    val headlineLarge: TextStyle,
    val headlineMedium: TextStyle,
    val headlineSmall: TextStyle,
    // 2. Title
    val titleLarge: TextStyle,
    val titleMedium: TextStyle,
    val titleSmall: TextStyle,
    // 3. Body
    val bodyLarge: TextStyle,
    val bodyLargeBold: TextStyle,
    val bodyMedium: TextStyle,
    val bodyMediumBold: TextStyle,
    val bodySmall: TextStyle,
    val bodySmallGray: TextStyle,
    // 4. Label
    val labelLarge: TextStyle,
    val labelMedium: TextStyle,
    val labelMediumBold: TextStyle,
    val labelSmall: TextStyle,
    val labelSmallBold: TextStyle
)