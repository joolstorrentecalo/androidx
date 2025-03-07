// ktlint-disable filename

/*
 * Copyright 2021 The Android Open Source Project
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

package androidx.compose.ui.test

// TODO: use constants instead of literals

// TODO replace it by common PointerButton, when we upstream it
@ExperimentalTestApi
@JvmInline
actual value class MouseButton(val buttonId: Int) {
    @ExperimentalTestApi
    actual companion object {
        /**
         * The left mouse button
         */
        actual val Primary = MouseButton(0)

        /**
         * The right mouse button
         */
        actual val Secondary = MouseButton(1)

        /**
         * The middle mouse button
         */
        actual val Tertiary = MouseButton(2)
    }
}
