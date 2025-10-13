package com.shubham.hard75kmm

import cocoapods.FirebaseCore.FIRApp
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
fun onDidFinishLaunchingWithOptions() {
    println("KMP Initializer: Starting setup...")
    FIRApp.configure() // Call Firebase configure
    println("KMP Initializer: Firebase project ID: ${FIRApp.defaultApp()?.options!!.projectID()}")
}