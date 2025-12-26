package com.example.keyboardlauncher

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment

class NotesFragment : Fragment() {

    private lateinit var notesEditText: EditText

    private val PREFS = "notes_prefs"
    private val NOTES_KEY = "notes_text"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_notes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        notesEditText = view.findViewById(R.id.notesEditText)

        val prefs = requireContext()
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        // Load saved notes
        notesEditText.setText(prefs.getString(NOTES_KEY, ""))

        // Auto-save on change
        notesEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                prefs.edit().putString(NOTES_KEY, s.toString()).apply()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Tap anywhere → focus + keyboard
        notesEditText.setOnClickListener {
            showKeyboard()
        }
    }

    private fun showKeyboard() {
        val imm = requireContext()
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(notesEditText, InputMethodManager.SHOW_IMPLICIT)
    }
}
