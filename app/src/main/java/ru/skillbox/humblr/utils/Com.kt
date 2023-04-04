package ru.skillbox.humblr.utils

import ru.skillbox.humblr.data.entities.Account
import ru.skillbox.humblr.data.entities.Comment
import ru.skillbox.humblr.data.interfaces.Created

sealed class Com {
    data class CommentUser(val comment: Comment, val account: Account?) : Com() {
    }

    companion object {
        object NullComment : Com(), Created {
            override val created: Long
                get() = 0
            override val createdUTC: Long
                get() = 0
            private var currentPage = 1
            var previousPage = 0
            private var pagesCount = 0
            var pages: List<String> = mutableListOf()
            fun getPageCount(): Int {
                return pagesCount
            }

            fun setPagesCount(count: Int) {
                pagesCount = count
            }

            fun getCurrent(): Int {
                return currentPage
            }

            fun setCurrentPage(int: Int) {
                this.currentPage = int
            }

            override fun getParent(): String? {
                return null
            }

            override fun getDepth2(): Int? {
                return null
            }

            override fun getIds(): String? {
                return null
            }
        }
    }

    interface OnSet {
        fun onSetPage(index: Int)
    }

}
