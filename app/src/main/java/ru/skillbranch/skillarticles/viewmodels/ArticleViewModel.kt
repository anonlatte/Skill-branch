package ru.skillbranch.skillarticles.viewmodels

import androidx.lifecycle.LiveData
import ru.skillbranch.skillarticles.data.ArticleData
import ru.skillbranch.skillarticles.data.ArticlePersonalInfo
import ru.skillbranch.skillarticles.data.repositories.ArticleRepository
import ru.skillbranch.skillarticles.extensions.data.toAppSettings
import ru.skillbranch.skillarticles.extensions.data.toArticlePersonalInfo
import ru.skillbranch.skillarticles.extensions.format

class ArticleViewModel(
    private val articleId: String
) : BaseViewModel<ArticleState>(ArticleState()) {
    private val repository = ArticleRepository
    private var menuIsShown = false

    init {
        subscribeOnDataSource(getArticleData()) { newValue, currentState ->
            newValue ?: return@subscribeOnDataSource null
            currentState.copy(
                shareLink = newValue.shareLink,
                title = newValue.title,
                category = newValue.category,
                categoryIcon = newValue.categoryIcon,
                date = newValue.date.format()
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

    private fun getArticleContent(): LiveData<List<Any>?> {
        return repository.loadArticleContent(articleId)
    }

    private fun getArticleData(): LiveData<ArticleData?> {
        return repository.getArticle(articleId)
    }

    private fun getArticlePersonalInfo(): LiveData<ArticlePersonalInfo?> {
        return repository.loadArticlePersonalInfo(articleId)
    }

    fun handleUpText() {
        val settings = currentState.toAppSettings()
        repository.updateSettings(
            settings.copy(isBigText = true)
        )
    }

    fun handleDownText() {
        val settings = currentState.toAppSettings()
        repository.updateSettings(
            settings.copy(isBigText = false)
        )
    }

    fun handleNightMode() {
        val settings = currentState.toAppSettings()
        repository.updateSettings(
            settings.copy(isDarkMode = !settings.isDarkMode)
        )
    }

    fun handleLike() {
        val toggleLike = {
            val info = currentState.toArticlePersonalInfo()
            repository.updateArticlePersonalInfo(info.copy(isLike = !info.isLike))
        }
        toggleLike()
        val msg = if (currentState.isLike) {
            Notify.TextMessage("Mark is liked")
        } else {
            Notify.ActionMessage(
                "Don't like it anymore",
                "No, still like it",
                toggleLike
            )
        }
        notify(msg)
    }

    fun handleBookmark() {
        val info = currentState.toArticlePersonalInfo()
        repository.updateArticlePersonalInfo(info.copy(isBookmark = !info.isBookmark))
        val msg = if (currentState.isBookmark) "Add to bookmarks" else "Remove from bookmarks"
        notify(Notify.TextMessage(msg))
    }

    fun handleShare() {
        notify(Notify.ErrorMessage("Share is not implemented", "OK", null))
    }

    fun handleToggleMenu() {
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
    val content: List<Any> = emptyList(),
    val reviews: List<Any> = emptyList()
)