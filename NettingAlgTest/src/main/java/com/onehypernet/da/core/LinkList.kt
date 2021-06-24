package com.onehypernet.da.core

class LinkList<T> : Iterable<T> {
    val size: Int get() = mRoot?.size ?: 0

    private var mRoot: Node? = null

    fun add(item: T) {
        val node = Node(item)
        if (mRoot == null) {
            mRoot = node
        } else {
            mRoot!!.tail.link(node)
        }
    }

    fun remove(item: T) {
        val node = findNode { it.value == item } ?: return
        if (node == mRoot) {
            mRoot = node.next()
        }
        node.unlink()
    }

    private fun findNode(accept: (Node) -> Boolean): Node? {
        var current = mRoot
        while (current != null) {
            if (accept(current)) return current
            current = current.next()
        }
        return null
    }

    fun contains(observer: T): Boolean {
        return findNode { it.value == observer } != null
    }

    override fun iterator(): Iterator<T> = object : Iterator<T> {
        private var mCurrent = mRoot

        override fun hasNext(): Boolean = mCurrent != null

        override fun next(): T {
            val value = mCurrent!!.value
            mCurrent = mCurrent!!.next()
            return value
        }
    }

    private inner class Node(val value: T) {
        val size: Int get() = 1 + (mNext?.size ?: 0)

        private var mNext: Node? = null
        private var mPrevious: Node? = null

        val tail: Node get() = if (mNext == null) this else mNext!!.tail

        fun next(): Node? = mNext

        fun unlink() {
            mNext?.mPrevious = mPrevious
            mPrevious?.mNext = mNext

            mNext = null
            mPrevious = null
        }

        fun link(node: Node) {
            mNext = node
            node.mPrevious = this
        }
    }
}