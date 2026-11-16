package com.rubiksolver.presentation.tutor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rubiksolver.application.learning.ManageTutorSessionUseCase
import com.rubiksolver.domain.learning.Curriculum
import com.rubiksolver.domain.profile.AppLanguage
import com.rubiksolver.domain.profile.ProfileKind
import com.rubiksolver.domain.solving.Method

@Composable
fun TutorAndProfilesScreen(modifier: Modifier = Modifier) {
    val tutorUseCase = remember { ManageTutorSessionUseCase() }
    var activeProfileKind by remember { mutableStateOf(ProfileKind.KID) }
    var activeLanguage by remember { mutableStateOf(AppLanguage.SPANISH) }
    var masteredIds by remember { mutableStateOf(setOf(Curriculum.NOTATION_TECHNIQUE_ID)) }
    val selectedLesson =
        remember(masteredIds) {
            Curriculum.recommendNextLesson(masteredIds) ?: Curriculum.allLessons.first()
        }
    var session by remember(selectedLesson, activeProfileKind) {
        mutableStateOf(tutorUseCase.startLesson(selectedLesson, activeProfileKind))
    }

    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ProfileAndLanguageBar(
                activeProfileKind = activeProfileKind,
                activeLanguage = activeLanguage,
                onSelectProfileKind = { activeProfileKind = it },
                onSelectLanguage = { activeLanguage = it },
            )
            ActiveLessonCard(
                sessionTitle = session.lesson.title,
                phaseName = session.currentPhase.name,
                hintLevelName = session.hintProgress.currentLevel.name,
                showTimer = session.showTimer,
                encouragement = session.encouragementMessage,
                onNextPhase = { session = tutorUseCase.advancePhase(session) },
                onPracticeSuccess = {
                    val updated = tutorUseCase.recordPracticeAttempt(session, succeeded = true)
                    session = updated
                    if (updated.masteryState.isMastered) {
                        masteredIds = masteredIds + updated.lesson.technique.id
                    }
                },
            )
            CurriculumListSection(masteredIds = masteredIds, onMasterToggle = { id ->
                masteredIds = if (id in masteredIds) masteredIds - id else masteredIds + id
            })
        }
    }
}

@Composable
private fun ProfileAndLanguageBar(
    activeProfileKind: ProfileKind,
    activeLanguage: AppLanguage,
    onSelectProfileKind: (ProfileKind) -> Unit,
    onSelectLanguage: (AppLanguage) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ProfileKind.entries.forEach { kind ->
            FilterChip(
                selected = activeProfileKind == kind,
                onClick = { onSelectProfileKind(kind) },
                label = { Text("Profile: ${kind.name}") },
            )
        }
        AppLanguage.entries.forEach { lang ->
            FilterChip(
                selected = activeLanguage == lang,
                onClick = { onSelectLanguage(lang) },
                label = { Text(lang.code.uppercase()) },
            )
        }
    }
}

@Composable
private fun ActiveLessonCard(
    sessionTitle: String,
    phaseName: String,
    hintLevelName: String,
    showTimer: Boolean,
    encouragement: String?,
    onNextPhase: () -> Unit,
    onPracticeSuccess: () -> Unit,
) {
    Text(text = "Active Lesson: $sessionTitle", style = MaterialTheme.typography.titleMedium)
    Text(text = "Phase: $phaseName | Hint Level: $hintLevelName | Timer: ${if (showTimer) "ON" else "OFF"}")
    encouragement?.let { msg ->
        Text(text = msg, style = MaterialTheme.typography.bodyMedium)
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = onNextPhase) {
            Text("Next Phase")
        }
        Button(onClick = onPracticeSuccess) {
            Text("Complete Practice")
        }
    }
}

@Composable
private fun CurriculumListSection(
    masteredIds: Set<String>,
    onMasterToggle: (String) -> Unit,
) {
    Text(text = "Curriculum & Algorithm Reference", style = MaterialTheme.typography.titleSmall)
    Curriculum.lessonStatuses(masteredIds).forEach { status ->
        val stateLabel =
            when {
                status.isMastered -> "Mastered"
                status.isLocked -> "Locked"
                else -> "Unlocked"
            }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = "${status.lesson.title} ($stateLabel) — ${status.lesson.technique.targetAlgorithm.notation}")
            FilterChip(
                selected = status.isMastered,
                onClick = { onMasterToggle(status.lesson.technique.id) },
                label = { Text(if (status.isMastered) "Done" else "Mark") },
            )
        }
    }
    val cfopRefs = Curriculum.algorithmReference(Method.CFOP, masteredIds)
    Text(text = "CFOP Reference Count: ${cfopRefs.size}")
}
