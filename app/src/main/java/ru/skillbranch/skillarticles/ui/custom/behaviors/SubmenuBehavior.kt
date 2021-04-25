package ru.skillbranch.skillarticles.ui.custom.behaviors

import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.marginRight
import ru.skillbranch.skillarticles.ui.custom.ArticleSubmenu
import ru.skillbranch.skillarticles.ui.custom.Bottombar

class SubmenuBehavior : CoordinatorLayout.Behavior<ArticleSubmenu>() {
    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: ArticleSubmenu,
        dependency: View
    ): Boolean = dependency is Bottombar

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: ArticleSubmenu,
        dependency: View
    ): Boolean {
        return if (dependency is Bottombar && dependency.translationY >= 0) {
            animate(child, dependency)
        } else {
            false
        }
    }

    private fun animate(child: View, dependency: View): Boolean {
        val fraction = dependency.translationY / dependency.minimumHeight
        child.translationX = (child.width + child.marginRight) * fraction
        return true
    }
}