package com.example.project_halfway
/*
MVC Structure adapted from MVC and Android lecture slides
 */
open class ObservableModel<M> {
    private val views = mutableListOf<ViewObserver<M>>()

    fun addView(view : ViewObserver<M>) {
        if (view !in views) {
            views.add(view)
        }
    }

    fun removeView(view : ViewObserver<M>) {
        if (view in views) {
            views.remove(view)
        }
    }

    protected fun notifyViews(model : M) {
        for (view in views) {
            view.update(model)
        }
    }
}