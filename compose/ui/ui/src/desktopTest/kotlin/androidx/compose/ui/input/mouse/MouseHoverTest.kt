/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("DEPRECATION") // https://github.com/JetBrains/compose-jb/issues/1514

package androidx.compose.ui.input.mouse

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.use
import com.google.common.truth.Truth.assertThat
import java.util.concurrent.Executors
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.Test

@OptIn(ExperimentalComposeUiApi::class)
class MouseHoverTest {
    @Test
    fun `inside window`() = ImageComposeScene(
        width = 100,
        height = 100,
        density = Density(2f)
    ).use { scene ->
        val counts = HoverCounts()

        scene.setContent {
            Box(
                modifier = Modifier
                    .pointerMove(counts)
                    .size(10.dp, 20.dp)
            )
        }

        scene.sendPointerEvent(PointerEventType.Enter, Offset(0f, 0f))
        scene.sendPointerEvent(PointerEventType.Move, Offset(10f, 20f))
        counts.assertEquals(enter = 1, exit = 0, move = 1)

        scene.sendPointerEvent(PointerEventType.Move, Offset(10f, 15f))
        counts.assertEquals(enter = 1, exit = 0, move = 2)

        scene.sendPointerEvent(PointerEventType.Move, Offset(30f, 30f))
        counts.assertEquals(enter = 1, exit = 1, move = 2)
    }

    @Test
    fun `window enter`() = ImageComposeScene(
        width = 100,
        height = 100,
        density = Density(2f)
    ).use { scene ->
        val counts = HoverCounts()

        scene.setContent {
            Box(
                modifier = Modifier
                    .pointerMove(counts)
                    .size(10.dp, 20.dp)
            )
        }

        scene.sendPointerEvent(PointerEventType.Enter, Offset(10f, 20f))
        counts.assertEquals(enter = 1, exit = 0, move = 0)

        scene.sendPointerEvent(PointerEventType.Exit, Offset(-1f, -1f))
        counts.assertEquals(enter = 1, exit = 1, move = 0)
    }

    @Test
    fun `move between two components`() = ImageComposeScene(
        width = 100,
        height = 100
    ).use { scene ->
        val counts1 = HoverCounts()
        val counts2 = HoverCounts()

        scene.setContent {
            Column {
                Box(
                    modifier = Modifier
                        .pointerMove(counts1)
                        .size(10.dp, 20.dp)
                )
                Box(
                    modifier = Modifier
                        .pointerMove(counts2)
                        .size(10.dp, 20.dp)
                )
            }
        }

        scene.sendPointerEvent(PointerEventType.Enter, Offset(0f, 0f))
        counts1.assertEquals(enter = 1, exit = 0, move = 0)
        counts2.assertEquals(enter = 0, exit = 0, move = 0)

        scene.sendPointerEvent(PointerEventType.Move, Offset(0f, 10f))
        counts1.assertEquals(enter = 1, exit = 0, move = 1)
        counts2.assertEquals(enter = 0, exit = 0, move = 0)

        scene.sendPointerEvent(PointerEventType.Move, Offset(0f, 30f))
        counts1.assertEquals(enter = 1, exit = 1, move = 1)
        counts2.assertEquals(enter = 1, exit = 0, move = 0)
    }

    @Test
    fun `hover on scroll`() = ImageComposeScene(
        width = 100,
        height = 100
    ).use { scene ->
        val counts1 = HoverCounts()
        val counts2 = HoverCounts()

        scene.setContent {
            val state = rememberScrollState()
            Column(Modifier.size(10.dp).verticalScroll(state)) {
                Box(
                    modifier = Modifier
                        .pointerMove(counts1)
                        .size(10.dp, 10.dp)
                )
                Box(
                    modifier = Modifier
                        .pointerMove(counts2)
                        .size(10.dp, 10.dp)
                )
            }
        }

        scene.sendPointerEvent(PointerEventType.Enter, Offset(0f, 0f))
        counts1.assertEquals(enter = 1, exit = 0, move = 0)
        counts2.assertEquals(enter = 0, exit = 0, move = 0)

        scene.sendPointerEvent(PointerEventType.Scroll, Offset(0f, 0f), scrollDelta = Offset(0f, 10000f))
        scene.render() // we update hover only on relayout
        counts1.assertEquals(enter = 1, exit = 1, move = 0)
        counts2.assertEquals(enter = 1, exit = 0, move = 0)

        scene.sendPointerEvent(PointerEventType.Scroll, Offset(0f, 0f), scrollDelta = Offset(0f, -10000f))
        scene.render() // we update hover only on relayout
        counts1.assertEquals(enter = 2, exit = 1, move = 0)
        counts2.assertEquals(enter = 1, exit = 1, move = 0)
    }

    @Test
    fun `hover on scroll in lazy list`() = ImageComposeScene(
        width = 100,
        height = 100
    ).use { scene ->
        val counts1 = HoverCounts()
        val counts2 = HoverCounts()

        scene.setContent {
            LazyColumn(Modifier.size(10.dp)) {
                items(2) {
                    Box(
                        modifier = Modifier
                            .pointerMove(if (it == 0) counts1 else counts2)
                            .size(10.dp, 10.dp)
                    )
                }
            }
        }

        scene.sendPointerEvent(PointerEventType.Enter, Offset(0f, 0f))
        counts1.assertEquals(enter = 1, exit = 0, move = 0)
        counts2.assertEquals(enter = 0, exit = 0, move = 0)

        scene.sendPointerEvent(PointerEventType.Scroll, Offset(0f, 0f), scrollDelta = Offset(0f, 10000f))
        scene.render() // we update hover only on relayout
        counts1.assertEquals(enter = 1, exit = 1, move = 0)
        counts2.assertEquals(enter = 1, exit = 0, move = 0)

        scene.sendPointerEvent(PointerEventType.Scroll, Offset(0f, 0f), scrollDelta = Offset(0f, -10000f))
        scene.render() // we update hover only on relayout
        counts1.assertEquals(enter = 2, exit = 1, move = 0)
        counts2.assertEquals(enter = 1, exit = 1, move = 0)
    }

    // bug https://github.com/JetBrains/compose-jb/issues/2147
    @OptIn(ExperimentalComposeUiApi::class)
    @Test
    fun `move between two components with an intermediate render`() {
        @Composable
        fun Modifier.hoverable(hoveredState: MutableState<Boolean>) = this
            .onPointerEvent(PointerEventType.Enter) {
                hoveredState.value = true
            }
            .onPointerEvent(PointerEventType.Exit) {
                hoveredState.value = false
            }

        @Composable
        fun Item(hoveredState: MutableState<Boolean>) {
            Box(
                Modifier
                    .hoverable(hoveredState)
                    .background(if (hoveredState.value) Color.Cyan else Color.Transparent)
                    .size(10.dp, 20.dp)
            )
        }

        val context = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
        runBlocking(context) {
            ImageComposeScene(
                width = 100,
                height = 100,
                coroutineContext = context
            ).use { scene ->
                val isHovered1 = mutableStateOf(false)
                val isHovered2 = mutableStateOf(false)

                scene.setContent {
                    Column {
                        Item(isHovered1)
                        Item(isHovered2)
                    }
                }

                while (scene.hasInvalidations()) {
                    yield()
                    scene.render()
                }

                scene.sendPointerEvent(PointerEventType.Enter, Offset(0f, 0f))
                assertThat(isHovered1.value).isTrue()
                assertThat(isHovered2.value).isFalse()

                scene.sendPointerEvent(PointerEventType.Move, Offset(0f, 10f))
                assertThat(isHovered1.value).isTrue()
                assertThat(isHovered2.value).isFalse()

                scene.render()   // this causes the test to fail before the fixing of 2147
                scene.sendPointerEvent(PointerEventType.Move, Offset(0f, 30f))
                assertThat(isHovered1.value).isFalse()
                assertThat(isHovered2.value).isTrue()
            }
        }
    }
}

private fun Modifier.pointerMove(
    counts: HoverCounts,
): Modifier = pointerInput(counts) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent()
            when (event.type) {
                PointerEventType.Move -> counts.move++
                PointerEventType.Enter -> counts.enter++
                PointerEventType.Exit -> counts.exit++
            }
        }
    }
}

private class HoverCounts {
    var enter = 0
    var exit = 0
    var move = 0

    fun assertEquals(enter: Int, exit: Int, move: Int) {
        assertThat(this.enter).isEqualTo(enter)
        assertThat(this.exit).isEqualTo(exit)
        assertThat(this.move).isEqualTo(move)
    }
}