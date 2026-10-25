package com.dessalines.thumbkey.textprocessors

import android.util.Log
import android.view.KeyEvent
import com.dessalines.thumbkey.IMEService
import com.dessalines.thumbkey.utils.TAG

class AvroTextProcessor : TextProcessor {
    private var buffer = ""

    override fun handleCommitText(
        ime: IMEService,
        input: CharSequence,
    ) {
        val ic = ime.currentInputConnection
        val ch = input.firstOrNull() ?: return

        if (ch == ' ') {
            ic.commitText("$buffer ", 1)   // seal word + space
            buffer = ""
        } else {
            buffer += input
            Log.d(TAG, "AvroProcessor: buffer='$buffer'")
            ic.setComposingText(buffer, 1) // live-render, replaces previous
        }
    }

    override fun handleKeyEvent(
        ime: IMEService,
        ev: KeyEvent,
    ) {
        val ic = ime.currentInputConnection
        if (ev.keyCode == KeyEvent.KEYCODE_DEL && buffer.isNotEmpty()) {
            buffer = buffer.dropLast(1)
            ic.setComposingText(buffer, 1) // rewind buffer, re-render — app never involved
        } else {
            ic.sendKeyEvent(ev)
        }
    }

    override fun handleFinishInput(ime: IMEService) {
        if (buffer.isNotEmpty()) {
            ime.currentInputConnection.finishComposingText()
            buffer = ""
        }
    }

    override fun handleCursorUpdate(
        ime: IMEService,
        oldSelStart: Int,
        oldSelEnd: Int,
        newSelStart: Int,
        newSelEnd: Int,
    ) {
        // User tapped elsewhere mid-word — seal and clear (Korean's pattern)
    }

    override fun updateCursorPosition(ime: IMEService) {
        // Copy Korean's extracted-text pattern later
    }
}
