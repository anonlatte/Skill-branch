package ru.skillbranch.skillarticles.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toolbar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat.getDrawable
import com.google.android.material.snackbar.Snackbar
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.databinding.ActivityRootBinding
import ru.skillbranch.skillarticles.databinding.LayoutBottombarBinding
import ru.skillbranch.skillarticles.databinding.LayoutSubmenuBinding
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.viewmodels.ArticleState
import ru.skillbranch.skillarticles.viewmodels.ArticleViewModel
import ru.skillbranch.skillarticles.viewmodels.Notify
import ru.skillbranch.skillarticles.viewmodels.ViewModelFactory

class RootActivity : AppCompatActivity() {

    private val viewModel: ArticleViewModel by viewModels { ViewModelFactory("0") }
    private lateinit var binding: ActivityRootBinding
    private val bindingBottomBar: LayoutBottombarBinding
        get() = binding.bottombar.binding
    private val bindingSubMenu: LayoutSubmenuBinding
        get() = binding.submenu.binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRootBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setupToolbar()
        setupBottombar()
        setupSubmenu()

        viewModel.observeState(this) {
            renderUi(it)
            setupToolbar()
        }

        viewModel.observeNotifications(this) {
            renderNotification(it)
        }
    }

    private fun setupSubmenu() {
        with(bindingSubMenu) {
            btnTextUp.setOnClickListener { viewModel.handleUpText() }
            btnTextDown.setOnClickListener { viewModel.handleDownText() }
            switchMode.setOnClickListener { viewModel.handleNightMode() }
        }
    }

    private fun setupBottombar() {
        with(bindingBottomBar) {
            btnLike.setOnClickListener { viewModel.handleLike() }
            btnBookmark.setOnClickListener { viewModel.handleBookmark() }
            btnShare.setOnClickListener { viewModel.handleShare() }
            btnSettings.setOnClickListener { viewModel.handleToggleMenu() }
        }
    }

    private fun setupToolbar() {
        with(binding) {
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            (toolbar.getChildAt(2) as? ImageView)?.let { logo ->
                logo.scaleType = ImageView.ScaleType.CENTER_CROP
                (logo.layoutParams as? Toolbar.LayoutParams)?.let {
                    it.width = dpToIntPx(40)
                    it.height = dpToIntPx(40)
                    it.marginEnd = dpToIntPx(16)
                    logo.layoutParams = it
                }
            }
        }
    }

    private fun renderUi(data: ArticleState) {
        bindingBottomBar.btnSettings.isChecked = data.isShowMenu
        if (data.isShowMenu) binding.submenu.open() else binding.submenu.close()

        bindingBottomBar.btnLike.isChecked = data.isLike
        bindingBottomBar.btnBookmark.isChecked = data.isBookmark

        bindingSubMenu.switchMode.isChecked = data.isDarkMode
        delegate.localNightMode = if (data.isDarkMode) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        if (data.isBigText) {
            binding.tvTextContent.textSize = 18f
            bindingSubMenu.btnTextUp.isChecked = true
            bindingSubMenu.btnTextDown.isChecked = false
        } else {
            binding.tvTextContent.textSize = 14f
            bindingSubMenu.btnTextUp.isChecked = false
            bindingSubMenu.btnTextDown.isChecked = true
        }

        binding.tvTextContent.text = if (data.isLoadingContent) {
            "loading"
        } else {
            data.content.first() as String
        }

        binding.toolbar.apply {
            title = data.title ?: "Skill Articles"
            subtitle = data.category ?: "loading..."
            if (data.categoryIcon != null) {
                logo = getDrawable(context, data.categoryIcon as Int)
            }
        }
    }

    private fun renderNotification(notify: Notify) {
        @SuppressLint("ShowToast")
        val snackbar = Snackbar.make(
            binding.coordinatorContainer,
            notify.message,
            Snackbar.LENGTH_LONG
        ).setAnchorView(binding.bottombar)
        when (notify) {
            is Notify.ActionMessage -> {
                snackbar.setTextColor(getColor(R.color.color_accent_dark))
                snackbar.setAction(notify.actionLabel) {
                    notify.actionHandler?.invoke()
                }
            }
            is Notify.ErrorMessage -> {

                with(snackbar) {
                    setBackgroundTint(getColor(R.color.design_default_color_error))
                    setTextColor(getColor(android.R.color.white))
                    setActionTextColor(getColor(android.R.color.white))
                    setAction(notify.errLabel) {
                        notify.errHandler?.invoke()
                    }
                }
            }
            is Notify.TextMessage -> {

            }
        }
        snackbar.show()
    }
}