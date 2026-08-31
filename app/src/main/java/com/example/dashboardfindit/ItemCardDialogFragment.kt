package com.example.dashboardfindit

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.File

enum class CardMode { ADD, UPDATE, DETAILS }

class ItemCardDialogFragment : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_MODE = "arg_mode"
        private const val ARG_PHOTO_PATH = "arg_photo_path"
        private const val ARG_NAME = "arg_name"
        private const val ARG_LOCATION = "arg_location"

        // Used right after a photo is captured in TakePictureActivity
        fun forAdd(photoPath: String): ItemCardDialogFragment {
            return newInstance(CardMode.ADD, photoPath, "", "")
        }

        // Used when editing an existing item (e.g. tapped from a grid card later)
        fun forUpdate(photoPath: String?, name: String, location: String): ItemCardDialogFragment {
            return newInstance(CardMode.UPDATE, photoPath, name, location)
        }

        // Used for "See Details" — same card, read-only
        fun forDetails(photoPath: String?, name: String, location: String): ItemCardDialogFragment {
            return newInstance(CardMode.DETAILS, photoPath, name, location)
        }

        private fun newInstance(
            mode: CardMode,
            photoPath: String?,
            name: String,
            location: String
        ): ItemCardDialogFragment {
            val fragment = ItemCardDialogFragment()
            fragment.arguments = Bundle().apply {
                putString(ARG_MODE, mode.name)
                putString(ARG_PHOTO_PATH, photoPath)
                putString(ARG_NAME, name)
                putString(ARG_LOCATION, location)
            }
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_item_card, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mode = CardMode.valueOf(arguments?.getString(ARG_MODE) ?: CardMode.ADD.name)
        val photoPath = arguments?.getString(ARG_PHOTO_PATH)
        val name = arguments?.getString(ARG_NAME).orEmpty()
        val location = arguments?.getString(ARG_LOCATION).orEmpty()

        val tvTitle: TextView = view.findViewById(R.id.tvCardTitle)
        val btnClose: ImageButton = view.findViewById(R.id.btnCardClose)
        val imgPhoto: ImageView = view.findViewById(R.id.imgCardPhoto)
        val etItemName: EditText = view.findViewById(R.id.etItemName)
        val etLocation: EditText = view.findViewById(R.id.etLocation)
        val btnSave: Button = view.findViewById(R.id.btnCardSave)

        btnClose.setOnClickListener { dismiss() }

        // Load photo if we have one
        if (!photoPath.isNullOrEmpty()) {
            val file = File(photoPath)
            if (file.exists()) {
                imgPhoto.setImageURI(Uri.fromFile(file))
            }
        }

        etItemName.setText(name)
        etLocation.setText(location)

        when (mode) {
            CardMode.ADD -> {
                tvTitle.text = "Add Item"
                btnSave.text = "Save"
            }
            CardMode.UPDATE -> {
                tvTitle.text = "Update Item"
                btnSave.text = "Update"
            }
            CardMode.DETAILS -> {
                tvTitle.text = "Item Details"
                btnSave.text = "Close"
                etItemName.isEnabled = false
                etLocation.isEnabled = false
            }
        }

        btnSave.setOnClickListener {
            if (mode == CardMode.DETAILS) {
                dismiss()
                return@setOnClickListener
            }

            val enteredName = etItemName.text.toString().trim()
            val enteredLocation = etLocation.text.toString().trim()

            if (enteredName.isEmpty() || enteredLocation.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in name and location", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // TODO: persist the item (Room DB / repository) once your data layer exists.
            // For now this just confirms the flow works end-to-end.
            Toast.makeText(requireContext(), "$enteredName saved to $enteredLocation", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }
}