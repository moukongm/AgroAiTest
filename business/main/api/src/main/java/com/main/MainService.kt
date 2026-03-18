package com.main

import com.alibaba.android.arouter.facade.template.IProvider

interface MainService : IProvider {
    fun getMainInfo(): String
}
