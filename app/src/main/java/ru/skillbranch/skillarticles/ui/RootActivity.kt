package ru.skillbranch.skillarticles.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Selection
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.text.getSpans
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.databinding.ActivityRootBinding
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.extensions.setMarginOptionally
import ru.skillbranch.skillarticles.ui.custom.SearchFocusSpan
import ru.skillbranch.skillarticles.ui.custom.SearchSpan
import ru.skillbranch.skillarticles.ui.custom.markdown.MarkdownBuilder
import ru.skillbranch.skillarticles.ui.delegates.viewBinding
import ru.skillbranch.skillarticles.viewmodels.*

class RootActivity : AppCompatActivity(), IArticleView {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var viewModelFactory: ViewModelProvider.Factory = ViewModelFactory(this, "0")
    private val viewModel: ArticleViewModel by viewModels { viewModelFactory }
    private val binding: ActivityRootBinding by viewBinding(ActivityRootBinding::inflate)
    private val bindingBottomBar get() = binding.bottombar.binding
    private val bindingSubMenu get() = binding.submenu.binding

    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupToolbar()
        setupBottombar()
        setupSubmenu()

        viewModel.observeState(this, ::renderUi)
        viewModel.observeSubState(this, ArticleState::toBottombarData, ::renderBotombar)
        viewModel.observeSubState(this, ArticleState::toSubmenuData, ::renderSubmenu)
        viewModel.observeNotifications(this, ::renderNotification)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        val menuItem = menu.findItem(R.id.action_search)
        searchView = (menuItem.actionView as SearchView)
        searchView.queryHint = getString(R.string.article_search_placeholder)

        //restore SearchView
        if (viewModel.currentState.isSearch) {
            menuItem.expandActionView()
            searchView.setQuery(viewModel.currentState.searchQuery, false)
            searchView.requestFocus()
        } else {
            searchView.clearFocus()
        }

        menuItem?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                viewModel.handleSearchMode(true)
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                viewModel.handleSearchMode(false)
                return true
            }
        })
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.handleSearch(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.handleSearch(newText)
                return true
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        viewModel.saveState()
        super.onSaveInstanceState(outState)
    }

    override fun renderSearchResult(searchResult: List<Pair<Int, Int>>) {
        val content = binding.tvTextContent.text as Spannable
        clearSearchResult()
        searchResult.forEach { (start, end) ->
            content.setSpan(
                SearchSpan(),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    override fun renderSearchPosition(searchPosition: Int) {
        val content = binding.tvTextContent.text as Spannable
        val spans = content.getSpans<SearchSpan>()
        content.getSpans<SearchFocusSpan>().forEach { content.removeSpan(it) }
        if (spans.isNotEmpty()) {
            val result = spans[searchPosition]
            Selection.setSelection(content, content.getSpanStart(result))
            content.setSpan(
                SearchFocusSpan(),
                content.getSpanStart(result),
                content.getSpanEnd(result),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    override fun clearSearchResult() {
        val content = binding.tvTextContent.text as Spannable
        content.getSpans<SearchSpan>().forEach { content.removeSpan(it) }
    }

    override fun showSearchBar(resultsCount: Int, searchPosition: Int) {
        with(binding.bottombar) {
            setSearchState(true)
            setSearchInfo(resultsCount, searchPosition)
        }
        binding.scroll.setMarginOptionally(bottom = dpToIntPx(56))
    }

    override fun hideSearchBar() {
        binding.bottombar.setSearchState(false)
        binding.scroll.setMarginOptionally(bottom = 0)
    }

    override fun setupSubmenu() {
        with(bindingSubMenu) {
            btnTextUp.setOnClickListener { viewModel.handleUpText() }
            btnTextDown.setOnClickListener { viewModel.handleDownText() }
            switchMode.setOnClickListener { viewModel.handleNightMode() }
        }
    }

    override fun setupBottombar() {
        with(bindingBottomBar) {
            btnLike.setOnClickListener { viewModel.handleLike() }
            btnBookmark.setOnClickListener { viewModel.handleBookmark() }
            btnShare.setOnClickListener { viewModel.handleShare() }
            btnSettings.setOnClickListener { viewModel.handleToggleMenu() }
            btnResultUp.setOnClickListener {
                searchView.clearFocus()
                binding.tvTextContent.requestFocus()
                viewModel.handleUpResult()
            }
            btnResultDown.setOnClickListener {
                searchView.clearFocus()
                binding.tvTextContent.requestFocus()
                viewModel.handleDownResult()
            }
            btnSearchClose.setOnClickListener {
                viewModel.handleSearchMode(false)
                invalidateOptionsMenu()
            }
        }
    }

    override fun setupToolbar() {
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

    override fun renderBotombar(data: BottombarData) {
        Log.d(javaClass.canonicalName, "renderBotombar $data")
        with(bindingBottomBar) {
            btnSettings.isChecked = data.isShowMenu
            btnLike.isChecked = data.isLike
            btnBookmark.isChecked = data.isBookmark
        }
        if (data.isSearch) {
            showSearchBar(data.resultsCount, data.searchPosition)
        } else {
            hideSearchBar()
        }
    }

    override fun renderSubmenu(data: SubmenuData) {
        Log.d(javaClass.canonicalName, "renderSubmenu $data")
        with(bindingSubMenu) {
            switchMode.isChecked = data.isDarkMode
            btnTextDown.isChecked = !data.isBigText
            btnTextUp.isChecked = data.isBigText
        }
        if (data.isShowMenu) binding.submenu.open() else binding.submenu.close()
    }

    override fun renderUi(data: ArticleState) {
        Log.d(javaClass.canonicalName, "renderUi $data")
        bindingBottomBar.btnSettings.isChecked = data.isShowMenu
        if (data.isShowMenu) binding.submenu.open() else binding.submenu.close()

        with(binding.tvTextContent) {
            textSize = if (data.isBigText) 18f else 14f
            movementMethod = LinkMovementMethod()
            MarkdownBuilder(context)
                .markdownToSpan(data.content)
                .run { setText(this, TextView.BufferType.SPANNABLE) }
        }

        bindingBottomBar.btnLike.isChecked = data.isLike
        bindingBottomBar.btnBookmark.isChecked = data.isBookmark

        bindingSubMenu.switchMode.isChecked = data.isDarkMode
        delegate.localNightMode = if (data.isDarkMode) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }

        with(binding.toolbar) {
            title = data.title ?: "Skill Articles"
            subtitle = data.category ?: "loading..."
            if (data.categoryIcon != null) logo = getDrawable(context, data.categoryIcon as Int)
        }
        if (data.isLoadingContent) return
        if (data.isSearch) {
            renderSearchResult(data.searchResults)
            renderSearchPosition(data.searchPosition)
        } else {
            clearSearchResult()
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