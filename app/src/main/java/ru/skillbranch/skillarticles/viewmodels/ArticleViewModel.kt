package ru.skillbranch.skillarticles.viewmodels

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import ru.skillbranch.skillarticles.data.ArticleData
import ru.skillbranch.skillarticles.data.ArticlePersonalInfo
import ru.skillbranch.skillarticles.data.repositories.ArticleRepository
import ru.skillbranch.skillarticles.extensions.asMap
import ru.skillbranch.skillarticles.extensions.data.toAppSettings
import ru.skillbranch.skillarticles.extensions.data.toArticlePersonalInfo
import ru.skillbranch.skillarticles.extensions.format
import ru.skillbranch.skillarticles.extensions.indexesOf

class ArticleViewModel(
    private val articleId: String,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<ArticleState>(ArticleState(), savedStateHandle), IArticleViewModel {
    private val repository = ArticleRepository()
    private var menuIsShown = false

    init {
        savedStateHandle.setSavedStateProvider("state", currentState::toBundle)
        subscribeOnDataSource(getArticleData()) { newValue, currentState ->
            newValue ?: return@subscribeOnDataSource null
            currentState.copy(
                author = newValue.author,
                category = newValue.category,
                categoryIcon = newValue.categoryIcon,
                date = newValue.date.format(),
                shareLink = newValue.shareLink,
                title = newValue.title
            )
        }
        subscribeOnDataSource(getArticleContent()) { newValue, currentState ->
            newValue ?: return@subscribeOnDataSource null
            currentState.copy(
                isLoadingContent = false,
                content = newValue
            )
        }
        subscribeOnDataSource(getArticlePersonalInfo()) { newValue, currentState ->
            newValue ?: return@subscribeOnDataSource null
            currentState.copy(
                isBookmark = newValue.isBookmark,
                isLike = newValue.isLike
            )
        }
        subscribeOnDataSource(repository.getAppSettings()) { newValue, currentState ->
            currentState.copy(
                isDarkMode = newValue.isDarkMode,
                isBigText = newValue.isBigText
            )
        }
    }

    override fun getArticleContent(): LiveData<List<String>?> {
        return repository.loadArticleContent(articleId)
    }

    override fun getArticleData(): LiveData<ArticleData?> {
        return repository.getArticle(articleId)
    }

    override fun getArticlePersonalInfo(): LiveData<ArticlePersonalInfo?> {
        return repository.loadArticlePersonalInfo(articleId)
    }

    override fun handleUpText() {
        val settings = currentState.toAppSettings()
        repository.updateSettings(
            settings.copy(isBigText = true)
        )
    }

    override fun handleDownText() {
        val settings = currentState.toAppSettings()
        repository.updateSettings(
            settings.copy(isBigText = false)
        )
    }

    override fun handleNightMode() {
        val settings = currentState.toAppSettings()
        repository.updateSettings(
            settings.copy(isDarkMode = !settings.isDarkMode)
        )
    }

    override fun handleLike() {
        val toggleLike = {
            val info = currentState.toArticlePersonalInfo()
            repository.updateArticlePersonalInfo(info.copy(isLike = !info.isLike))
        }
        toggleLike()
        val msg = if (currentState.isLike) {
            Notify.TextMessage("Mark is liked")
        } else {
            Notify.ActionMessage(
                msg = "Don`t like it anymore",
                actionLabel = "No, still like it",
                actionHandler = toggleLike
            )
        }
        notify(msg)
    }

    override fun handleBookmark() {
        val info = currentState.toArticlePersonalInfo()
        repository.updateArticlePersonalInfo(info.copy(isBookmark = !info.isBookmark))
        val msg = if (currentState.isBookmark) "Add to bookmarks" else "Remove from bookmarks"
        notify(Notify.TextMessage(msg))
    }

    override fun handleShare() {
        notify(Notify.ErrorMessage("Share is not implemented", "OK", null))
    }

    override fun handleToggleMenu() {
        updateState { state ->
            state.copy(isShowMenu = !state.isShowMenu).also {
                menuIsShown = !it.isShowMenu
            }
        }
    }

    fun hideMenu() {
        updateState { it.copy(isShowMenu = false) }
    }

    fun showMenu() {
        updateState { it.copy(isShowMenu = menuIsShown) }
    }

    fun handleSearchQuery(query: String?) {
        updateState { it.copy(searchQuery = query) }
    }

    fun handleIsSearch(isSearch: Boolean) {
        updateState { it.copy(isSearch = isSearch) }
    }

    override fun handleSearchMode(isSearch: Boolean) {
        updateState { it.copy(isSearch = isSearch, isShowMenu = false, searchPosition = 0) }
    }

    override fun handleSearch(query: String?) {
        query ?: return
        val result = currentState.content.firstOrNull().indexesOf(query).map {
            it to it + query.length
        }
        updateState { it.copy(searchQuery = query, searchResults = result) }
    }

    override fun handleUpResult() {
        updateState { it.copy(searchPosition = it.searchPosition.dec()) }
    }

    override fun handleDownResult() {
        updateState { it.copy(searchPosition = it.searchPosition.inc()) }
    }
}

data class ArticleState(
    val isAuth: Boolean = false,
    val isLoadingContent: Boolean = true,
    val isLoadingReviews: Boolean = true,
    val isLike: Boolean = false,
    val isBookmark: Boolean = false,
    val isShowMenu: Boolean = false,
    val isDarkMode: Boolean = false,
    val isBigText: Boolean = false,
    val isSearch: Boolean = false,
    val searchQuery: String? = null,
    val searchResults: List<Pair<Int, Int>> = emptyList(),
    val searchPosition: Int = 0,
    val shareLink: String? = null,
    val title: String? = null,
    val category: String? = null,
    val categoryIcon: Any? = null,
    val date: String? = null,
    val author: Any? = null,
    val poster: String? = null,
    val content: List<String> = emptyList(),
    val reviews: List<Any> = emptyList()
) : VMState {
    override fun toBundle(): Bundle {
        val map = copy(content = emptyList(), isLoadingContent = true)
            .asMap()
            .toList()
            .toTypedArray()
        return bundleOf(*map)
    }

    override fun fromBundle(bundle: Bundle): VMState {
        val map = bundle.keySet().associateWith { bundle[it] }
        return copy(
            isAuth = map["isAuth"] as Boolean,
            isLoadingContent = map["isLoadingContent"] as Boolean,
            isLoadingReviews = map["isLoadingReviews"] as Boolean,
            isLike = map["isLike"] as Boolean,
            isBookmark = map["isBookmark"] as Boolean,
            isShowMenu = map["isShowMenu"] as Boolean,
            isDarkMode = map["isDarkMode"] as Boolean,
            isBigText = map["isBigText"] as Boolean,
            isSearch = map["isSearch"] as Boolean,
            searchQuery = map["searchQuery"] as String?,
            searchResults = map["searchResults"] as List<Pair<Int, Int>>,
            searchPosition = map["searchPosition"] as Int,
            shareLink = map["shareLink"] as String?,
            title = map["title"] as String?,
            category = map["category"] as String?,
            categoryIcon = map["categoryIcon"] as Any?,
            date = map["date"] as String?,
            author = map["author"] as Any?,
            poster = map["poster"] as String?,
            content = map["content"] as List<String>,
            reviews = map["reviews"] as List<Any>,
        )
    }
}

data class BottombarData(
    val isLike: Boolean = false,
    val isBookmark: Boolean = false,
    val isShowMenu: Boolean = false,
    val isSearch: Boolean = false,
    val resultsCount: Int = 0,
    val searchPosition: Int = 0
)

data class SubmenuData(
    val isShowMenu: Boolean = false,
    val isBigText: Boolean = false,
    val isDarkMode: Boolean = false
)

fun ArticleState.toBottombarData() = BottombarData(
    isLike,
    isBookmark,
    isShowMenu,
    isSearch,
    searchResults.size,
    searchPosition
)

fun ArticleState.toSubmenuData() = SubmenuData(isShowMenu, isBigText, isDarkMode)