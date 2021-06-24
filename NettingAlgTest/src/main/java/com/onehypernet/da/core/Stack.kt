package com.onehypernet.da.core

class Stack<T> : Iterable<T> {
    private var mTail: Node? = null

    val size: Int
        get() {
            if (mTail == null) return 0
            return mTail!!.index + 1
        }

    fun isNotEmpty(): Boolean = mTail != null

    fun isEmpty(): Boolean = !isNotEmpty()

    fun pop(): T {
        val tail = mTail ?: error("Stack empty")
        val value = tail.value
        val previous = tail.unlink()
        mTail = previous
        return value
    }

    fun lastElement(): T {
        return mTail?.value ?: error("Stack empty")
    }

    fun add(destination: T) {
        val newNode = Node(destination)
        mTail?.link(newNode)
        mTail = newNode
    }

    override fun iterator(): Iterator<T> = StackIterator()

    private inner class Node(val value: T) {
        private var mNext: Node? = null
        private var mPrevious: Node? = null

        val next get() = mNext
        val previous get() = mPrevious
        val head: Node get() = if (mPrevious == null) this else mPrevious!!.head

        val index: Int get() = if (mPrevious == null) 0 else mPrevious!!.index + 1

        fun link(newNode: Node) {
            mNext = newNode
            newNode.mPrevious = this
        }

        fun unlink(): Node? {
            val previous = mPrevious
            previous?.mNext = null
            mPrevious = null
            return previous
        }
    }

    private inner class StackIterator : Iterator<T> {
        private var mCurrent = mTail?.head

        override fun hasNext(): Boolean = mCurrent != null

        override fun next(): T {
            val value = mCurrent!!.value
            val next = mCurrent?.next
            mCurrent = next
            return value
        }
    }
}