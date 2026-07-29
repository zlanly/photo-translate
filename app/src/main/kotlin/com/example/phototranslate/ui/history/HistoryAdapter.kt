package com.example.phototranslate.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.phototranslate.data.history.TranslationHistoryEntity
import com.example.phototranslate.databinding.ItemHistoryBinding
import com.example.phototranslate.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(
    private val onItemClick: (TranslationHistoryEntity) -> Unit,
    private val onFavorite: (TranslationHistoryEntity) -> Unit,
    private val onDelete: (TranslationHistoryEntity) -> Unit
) : ListAdapter<TranslationHistoryEntity, HistoryAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) onItemClick(getItem(pos))
            }
            binding.btnFavorite.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) onFavorite(getItem(pos))
            }
            binding.btnDelete.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) onDelete(getItem(pos))
            }
        }

        fun bind(item: TranslationHistoryEntity) {
            binding.itemOriginal.text = if (item.originalText.isBlank()) "—" else item.originalText
            binding.itemTranslated.text = if (item.translatedText.isBlank()) "—" else item.translatedText
            binding.itemTime.text = DATE_FORMAT.format(Date(item.timestamp))

            val favTint = if (item.isFavorite) R.color.secondary else R.color.outline_variant
            binding.btnFavorite.imageTintList =
                androidx.appcompat.content.res.AppCompatResources.getColorStateList(
                    binding.root.context, favTint
                )
        }
    }

    companion object {
        private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
        private val DIFF = object : DiffUtil.ItemCallback<TranslationHistoryEntity>() {
            override fun areItemsTheSame(
                oldItem: TranslationHistoryEntity,
                newItem: TranslationHistoryEntity
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: TranslationHistoryEntity,
                newItem: TranslationHistoryEntity
            ): Boolean = oldItem == newItem
        }
    }
}
