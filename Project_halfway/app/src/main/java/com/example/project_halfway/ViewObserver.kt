package com.example.project_halfway
/*
MVC Structure adapted from MVC and Android lecture slides
 */
interface ViewObserver<M> {
    fun update(model : M)
}