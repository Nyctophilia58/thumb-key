package com.dessalines.thumbkey.textprocessors

import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.ExtractedTextRequest
import com.dessalines.thumbkey.IMEService
import com.dessalines.thumbkey.utils.TAG

class AvroTextProcessor : TextProcessor {
    private var buffer = ""
    private var selectionStart = 0
    private var selectionEnd = 0

    override fun handleCommitText(
        ime: IMEService,
        input: CharSequence,
    ) {
        val ic = ime.currentInputConnection
        val ch = input.firstOrNull() ?: return

        if (ch == ' ') {
            val bangla = AvroEngine.convert(buffer)+ " "   // convert + space as one string
            ic.commitText(bangla, 1)
            selectionStart += bangla.length
            selectionEnd = selectionStart
            buffer = ""
        } else {
            buffer += input
            Log.d(TAG, "AvroProcessor: buffer='$buffer'")
            ic.setComposingText(AvroEngine.convert(buffer), 1) // live-render, replaces previous
            selectionStart += 1
            selectionEnd += 1
        }
    }

    override fun handleKeyEvent(
        ime: IMEService,
        ev: KeyEvent,
    ) {
        val ic = ime.currentInputConnection
        if (ev.keyCode == KeyEvent.KEYCODE_DEL && buffer.isNotEmpty()) {
            buffer = buffer.dropLast(1)
            ic.setComposingText(AvroEngine.convert(buffer), 1) // rewind buffer, re-render — app never involved
            selectionStart -= 1
            selectionEnd -= 1
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
        if (oldSelStart == newSelStart || selectionStart == newSelStart) return
        selectionStart = newSelStart
        selectionEnd = newSelEnd
        if (buffer.isNotEmpty()) {
            // user moved the cursor mid-word: seal what's on screen, start fresh
            ime.currentInputConnection.finishComposingText()
            buffer = ""
        }
    }

    override fun updateCursorPosition(ime: IMEService) {
        val extracted = ime.currentInputConnection.getExtractedText(ExtractedTextRequest(), 0)
        selectionStart = extracted.selectionStart
        selectionEnd = extracted.selectionEnd
    }
}
