package com.example.phototranslate.ui.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.phototranslate.R
import com.example.phototranslate.databinding.ItemModelBinding
import com.example.phototranslate.domain.LanguageOption
import com.example.phototranslate.domain.ModelDownloadStatus
import com.example.phototranslate.domain.ModelStatus

data class ModelRow(
    val option: LanguageOption,
    val status: ModelStatus
)

class ModelAdapter(
    private val onAction: (LanguageOption, ModelDownloadStatus) -> Unit
) : ListAdapter<ModelRow, ModelAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemModelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemModelBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.btnAction.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val row = getItem(pos)
                    onAction(row.option, row.status.downloadStatus)
                }
            }
        }

        fun bind(row: ModelRow) {
            val (option, status) = row
            binding.itemFlag.text = option.flagEmoji
            binding.itemName.text = option.displayName

            val (statusText, statusColor, actionText) = when (status.downloadStatus) {
                ModelDownloadStatus.INSTALLED ->
                    Triple(binding.root.context.getString(R.string.model_installed), R.color.primary, binding.root.context.getString(R.string.delete_model))
                ModelDownloadStatus.DOWNLOADING ->
                    Triple(binding.root.context.getString(R.string.model_downloading), R.color.secondary, binding.root.context.getString(R.string.model_downloading))
                ModelDownloadStatus.FAILED ->
                    Triple(binding.root.context.getString(R.string.translation_failed), R.color.error, binding.root.context.getString(R.string.download_model))
                else ->
                    Triple(binding.root.context.getString(R.string.model_not_installed), R.color.on_surface_variant, binding.root.context.getString(R.string.download_model))
            }
            binding.itemStatus.text = if (status.downloadStatus == ModelDownloadStatus.DOWNLOADING && status.progress > 0f)
                "$statusText ${((status.progress * 100).toInt())}%" else statusText
            binding.itemStatus.setTextColor(
                androidx.appcompat.content.res.AppCompatResources.getColorStateList(binding.root.context, statusColor)
            )
            binding.btnAction.text = actionText
            binding.btnAction.isEnabled = status.downloadStatus != ModelDownloadStatus.DOWNLOADING
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ModelRow>() {
            override fun areItemsTheSame(oldItem: ModelRow, newItem: ModelRow): Boolean =
                oldItem.option.code == newItem.option.code

            override fun areContentsTheSame(oldItem: ModelRow, newItem: ModelRow): Boolean =
                oldItem == newItem
        }
    }
}
