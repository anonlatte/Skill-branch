package ru.skillbranch.skillarticles.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewAnimationUtils
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.animation.doOnEnd
import androidx.core.view.isVisible
import com.google.android.material.shape.MaterialShapeDrawable
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.databinding.LayoutBottombarBinding
import ru.skillbranch.skillarticles.ui.custom.behaviors.BottombarBehavior
import kotlin.math.hypot

class Bottombar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), CoordinatorLayout.AttachedBehavior {
    val binding = LayoutBottombarBinding.inflate(LayoutInflater.from(context), this)
    var isSearchMode = false

    init {
        val materialBg = MaterialShapeDrawable.createWithElevationOverlay(context)
        materialBg.elevation = elevation
        background = materialBg
    }

    override fun getBehavior(): CoordinatorLayout.Behavior<*> = BottombarBehavior()

    fun setSearchState(isSearch: Boolean) {
        if (isSearch == isSearchMode || !isAttachedToWindow) return
        isSearchMode = isSearch
        if (isSearchMode) animateShowSearch() else animateHideSearch()
    }

    fun setSearchInfo(searchAmount: Int = 0, position: Int = 0) {
        with(binding) {
            btnResultUp.isEnabled = searchAmount > 0
            btnResultDown.isEnabled = searchAmount > 0
            tvSearchResult.text = if (searchAmount == 0) {
                context.getString(R.string.message_not_found)
            } else {
                "${position.inc()} of $searchAmount"
            }
            when (position) {
                0 -> btnResultUp.isEnabled = false
                searchAmount.dec() -> btnResultDown.isEnabled = false
            }
        }
    }

    private fun animateShowSearch() {
        binding.reveal.isVisible = true
        val endRadius = hypot(width.toDouble(), height / 2.toDouble())
        val animator = ViewAnimationUtils.createCircularReveal(
            binding.reveal,
            width,
            height / 2,
            0f,
            endRadius.toFloat()
        )
        animator.doOnEnd { binding.bottomGroup.isVisible = false }
        animator.start()
    }

    private fun animateHideSearch() {
        binding.bottomGroup.isVisible = true

        val endRadius = hypot(width.toDouble(), height / 2.toDouble())

        val animator = ViewAnimationUtils.createCircularReveal(
            binding.reveal,
            width,
            height / 2,
            endRadius.toFloat(),
            0f
        )
        animator.doOnEnd { binding.reveal.isVisible = false }
        animator.start()
    }
}