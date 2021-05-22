package com.zayphyo.passwordview

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.util.Log
import android.util.SparseArray
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.animation.BounceInterpolator
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import java.lang.IndexOutOfBoundsException
import java.lang.StringBuilder


class PasswordPicker(context: Context, attributeSet: AttributeSet) :
    LinearLayout(context, attributeSet), AnimateEditText.AnimateTextChange {

    fun interface Listener {
        fun onComplete(pw: String)
    }

    private lateinit var edtPw1: AnimateEditText
    private lateinit var edtPw2: AnimateEditText
    private lateinit var edtPw3: AnimateEditText
    private lateinit var edtPw4: AnimateEditText
    private lateinit var edtPw5: AnimateEditText
    private lateinit var edtPw6: AnimateEditText
    var isEnableErrorAnimation = true
    var isPasswordType = false
    private var textColor = Color.BLACK
    private var passwordMask = ""


    init {
        val typeArray =
            resources.obtainAttributes(
                attributeSet,
                intArrayOf(R.attr.enableErrorAnimation, R.attr.isPasswordType, R.attr.viewtextColor)
            )
        isEnableErrorAnimation =
            typeArray.getBoolean(R.styleable.PasswordPicker_enableErrorAnimation, true)
        isPasswordType = typeArray.getBoolean(R.styleable.PasswordPicker_isPasswordType, false)
        textColor = typeArray.getInt(R.styleable.PasswordPicker_viewtextColor, Color.BLACK)




        typeArray.recycle()
    }


    private val listOfPwEditText = ArrayList<AnimateEditText>()
    private val listOfPwAnimation = ArrayList<ObjectAnimator>()

    private fun loadAnimation() {
        val sp = SparseArray<String>()

        Log.d("animation size", "${listOfPwEditText.size}")
        listOfPwEditText.forEachIndexed { i, e ->
            Log.d("add $i", "animation")
            listOfPwAnimation.add(ObjectAnimator.ofFloat(e, "translationX", 0f, 40f, 0f).apply {
                interpolator = BounceInterpolator()
                duration = 500
            })
        }
    }

    val OTP_MAX = 6
    private var currentIndex = 0;
    lateinit var pwView: View


    override fun onFinishInflate() {
        super.onFinishInflate()
        LayoutInflater.from(context).inflate(R.layout.password_picker_view, this, true)
        createPasswordSlots()

        loadAnimation()
        edtPw1.requestFocus()
    }

    private fun createPasswordSlots() {
        edtPw1 = findViewById(R.id.pwField1)
        edtPw2 = findViewById(R.id.pwField2)
        edtPw3 = findViewById(R.id.pwField3)
        edtPw4 = findViewById(R.id.pwField4)
        edtPw5 = findViewById(R.id.pwField5)
        edtPw6 = findViewById(R.id.pwField6)

        listOfPwEditText.apply {
            add(edtPw1)
            add(edtPw2)
            add(edtPw3)
            add(edtPw4)
            add(edtPw5)
            add(edtPw6)
        }

        listOfPwEditText.forEach {
            it.setTextColor(textColor)
            if (isPasswordType) {

                passwordMask = if (passwordMask.isEmpty()) "*" else passwordMask
                it.passwordMask = passwordMask
            }
        }
        listenPasswordFill()
    }

    private fun unFoucsableAfterFirst(i: Int, e: AnimateEditText) {
        if (i != 0) {
            e.isFocusableInTouchMode = false
        }
    }

    private fun listenPasswordFill() {
        listOfPwEditText.forEachIndexed { i, e ->
            e.addTextChange(this)

            e.setOnKeyListener { v, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    onTapBackKey()

                }
                false
            }
            unFoucsableAfterFirst(i, e)

        }

    }


    private fun enableFocusSlot(i: Int) {
        listOfPwEditText[i].isFocusableInTouchMode = true
    }

    private fun disableFocusSlot(i: Int) {
        listOfPwEditText[i].isFocusableInTouchMode = false
    }

    private fun goToNext() {
        disableFocusSlot(currentIndex)
        currentIndex++
        if (currentIndex < listOfPwEditText.size) {
            enableFocusSlot(currentIndex)
            listOfPwEditText[currentIndex].requestFocus()

        } else {
            currentIndex = listOfPwEditText.size
        }
    }


    var listener: Listener? = null

    fun setOnPasswordSubmitListener(listener: Listener) {
        this.listener = listener
    }


    private fun deleteTextIfExist() {
        listOfPwEditText[currentIndex].deleteTextIfNotEmpty()
    }

    private fun onTapBackKey() {
        if (currentIndex == listOfPwEditText.size) {
            disableFocusSlot(currentIndex - 1)
        } else {
            disableFocusSlot(currentIndex)
        }
        currentIndex--
        if (currentIndex >= 0) {
            enableFocusSlot(currentIndex)
            listOfPwEditText[currentIndex].requestFocus()
            deleteTextIfExist()
        } else {
            currentIndex = 0
        }
    }

    private fun startAnimation(i: Int) {
        listOfPwAnimation[i].start()

    }

    fun verify() {
        val password = StringBuilder()
        var didShowAnim = false
        listOfPwEditText.forEachIndexed { i, e ->

            if(!isPasswordType) {
                appendOriginText(e, password, didShowAnim, i)
            }else {
                appendTextWhenMaskIsOn(e, password, didShowAnim, i)
            }
        }
        invokeCompleteFill(password)

    }

    private fun appendOriginText(
        e: AnimateEditText,
        password: StringBuilder,
        didShowAnim: Boolean,
        i: Int
    ) {
        var didShowAnim1 = didShowAnim
        if (e.text.toString().isNotEmpty()) {
            password.append(e.text)
        } else {
            if (isEnableErrorAnimation) {
                if (!didShowAnim1) {
                    didShowAnim1 = true
                    startAnimation(i)
                }
            }
        }
    }

    private fun appendTextWhenMaskIsOn(
        e: AnimateEditText,
        password: StringBuilder,
        didShowAnim: Boolean,
        i: Int
    ) {
        var didShowAnim1 = didShowAnim
        if (e.orginTextWhenMaskHide.isNotEmpty()) {
            password.append(e.orginTextWhenMaskHide)
        } else {
            if (isEnableErrorAnimation) {
                if (!didShowAnim1) {
                    didShowAnim1 = true
                    startAnimation(i)
                }
            }
        }
    }

    private fun invokeCompleteFill(password: StringBuilder) {
        if (password.length == OTP_MAX) {
            listener?.onComplete(password.toString())
        }
    }


    override fun onChange(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (s.toString().isOneChar() || s.toString().isNotEmpty()) {
            listOfPwEditText[currentIndex].background =
                ContextCompat.getDrawable(context, R.drawable.bg_pw)
            goToNext()
        }
    }

}