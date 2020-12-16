package com.mcmkhianjuan.chequewriterapp.ModelClasses

class Users {
    private var uid: String = ""
    private var fullNameInput: String = ""

    constructor()
    constructor(uid: String, fullNameInput: String) {
        this.uid = uid
        this.fullNameInput = fullNameInput
    }

    fun getUID(): String? {
        return uid
    }

    fun setUID(uid: String) {
        this.uid = uid
    }

    fun getfullNameInput(): String? {
        return fullNameInput
    }

    fun setfullNameInput(fullNameInput: String) {
        this.fullNameInput = fullNameInput
    }
}