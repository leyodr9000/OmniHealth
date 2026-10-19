package com.example.omnihealth.ui.di

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner

/**
 * 让 ViewModel 存活在 Activity 作用域，而不是单个 NavBackStackEntry。
 *
 * 底部 Tab 切换会 `saveState` 掉旧目的地，其 ViewModelStore 随之销毁；
 * 若 ViewModel 跟着目的地走，每次切回来都要重建并重新查 5～6 张表，
 * 首帧与随后的滚动就会掉帧。
 */
@Composable
fun activityScopedViewModelOwner(): ViewModelStoreOwner {
    val activity = LocalActivity.current
    return if (activity is ComponentActivity) activity else LocalViewModelStoreOwner.current!!
}
