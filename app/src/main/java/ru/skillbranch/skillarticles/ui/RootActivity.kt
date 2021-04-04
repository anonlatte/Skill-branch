package ru.skillbranch.skillarticles.ui

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat.getDrawable
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_root.*
import kotlinx.android.synthetic.main.layout_bottombar.*
import kotlinx.android.synthetic.main.layout_submenu.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.viewmodels.ArticleState
import ru.skillbranch.skillarticles.viewmodels.ArticleViewModel
import ru.skillbranch.skillarticles.viewmodels.Notify
import ru.skillbranch.skillarticles.viewmodels.ViewModelFactory

class RootActivity : AppCompatActivity() {

    private lateinit var viewModel: ArticleViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_root)
        setupToolbar()
        setupBottombar()
        setupSubmenu()

        val vmFactory = ViewModelFactory("0")
        viewModel = ViewModelProviders.of(this, vmFactory).get(ArticleViewModel::class.java)
        viewModel.observeState(this) {
            renderUi(it)
            setupToolbar()
        }

        viewModel.observeNotifications(this) {
            renderNotification(it)
        }
    }

    private fun setupSubmenu() {
        btn_text_up.setOnClickListener { viewModel.handleUpText() }
        btn_text_down.setOnClickListener { viewModel.handleDownText() }
        switch_mode.setOnClickListener { viewModel.handleNightMode() }
    }

    private fun setupBottombar() {
        btn_like.setOnClickListener { viewModel.handleLike() }
        btn_bookmark.setOnClickListener { viewModel.handleBookmark() }
        btn_share.setOnClickListener { viewModel.handleShare() }
        btn_settings.setOnClickListener { viewModel.handleToggleMenu() }
    }

    private fun setupToolbar() {
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

    private fun renderUi(data: ArticleState) {
        btn_settings.isChecked = data.isShowMenu
        if (data.isShowMenu) submenu.open() else submenu.close()

        btn_like.isChecked = data.isLike
        btn_bookmark.isChecked = data.isBookmark

        switch_mode.isChecked = data.isDarkMode
        delegate.localNightMode = if (data.isDarkMode) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        if (data.isBigText) {
            tv_text_content.textSize = 18f
            btn_text_up.isChecked = true
            btn_text_down.isChecked = false
        } else {
            tv_text_content.textSize = 14f
            btn_text_up.isChecked = false
            btn_text_down.isChecked = true
        }

        tv_text_content.text = if (data.isLoadingContent) {
            "loading"
        } else {
            data.content.first() as String
        }

        toolbar.apply {
            title = data.title ?: "Skill Articles"
            subtitle = data.category ?: "loading..."
            if (data.categoryIcon != null) {
                logo = getDrawable(context, data.categoryIcon as Int)
            }
        }
    }

    private fun renderNotification(notify: Notify) {
        val snackbar = Snackbar.make(
            coordinator_container,
            notify.message,
            Snackbar.LENGTH_LONG
        ).setAnchorView(bottombar)
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