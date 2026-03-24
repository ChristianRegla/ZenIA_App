package com.zenia.app.ui.screens.relax

import androidx.lifecycle.ViewModel
import com.zenia.app.data.session.UserSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RelaxViewModel @Inject constructor(
    sessionManager: UserSessionManager
) : ViewModel() {

    val isPremium = sessionManager.isPremium
}