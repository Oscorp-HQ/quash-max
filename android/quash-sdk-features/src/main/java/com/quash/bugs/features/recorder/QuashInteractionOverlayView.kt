/*
 * Copyright (c) 2024 Quash.
 *
 * Licensed under the MIT License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://opensource.org/licenses/MIT
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.quash.bugs.features.recorder

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

/**
 * A custom view that overlays on top of other views to capture and visualize touch interactions.
 *
 * This view captures touch events, specifically single taps, and records their positions. It uses a
 * GestureDetector to handle single tap events and stores the tap positions as PointF objects.
 *
 * @param context The context associated with this view.
 */
class QuashInteractionOverlayView(context: Context) : View(context) {

    // Initialize the view to be clickable
    init {
        isClickable = true
    }

    // GestureDetector to handle single tap events
    private val gestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                // Add a tap indicator at the tap location
                addTapIndicator(e.x, e.y)
                return super.onSingleTapConfirmed(e)
            }
        })

    // List to store the positions of tap indicators
    private val tapIndicators = mutableListOf<PointF>()

    /**
     * Returns a list of recorded tap indicators.
     *
     * @return A list of PointF objects representing the tap positions.
     */
    fun getTapIndicators(): List<PointF> {
        return tapIndicators.toList()
    }

    /**
     * Handles touch events on the view.
     *
     * This method captures ACTION_DOWN events to record tap positions and passes all touch events
     * to the GestureDetector for further processing.
     *
     * @param event The MotionEvent representing the touch event.
     * @return False to allow the touch event to pass through to the underlying views.
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Add a tap indicator at the touch location
                    addTapIndicator(it.x, it.y)
                }
            }
            // Pass the touch event to the gesture detector
            gestureDetector.onTouchEvent(it)
        }
        // Return false to allow the touch event to pass through to the underlying views
        return false
    }

    /**
     * Adds a tap indicator at the specified coordinates.
     *
     * This method records the position of the tap and requests the view to be redrawn.
     *
     * @param x The x-coordinate of the tap.
     * @param y The y-coordinate of the tap.
     */
    private fun addTapIndicator(x: Float, y: Float) {
        tapIndicators.add(PointF(x, y))
        invalidate() // Request the view to be redrawn
    }
}
