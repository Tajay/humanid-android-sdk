package com.nbs.humanidui.presentation.welcome


import android.app.Dialog
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.humanid.auth.HumanIDAuth
import com.nbs.humanidui.R
import com.nbs.humanidui.presentation.main.MainDialogFragment
import com.nbs.nucleo.utils.extensions.onClick
import io.reactivex.annotations.NonNull
import kotlinx.android.synthetic.main.fragment_dialog_welcome.*


class WelcomeDialogFragment : BottomSheetDialogFragment() {

    companion object{
        var listener: OnWelcomeDialogListener? = null

        fun newInstance(): WelcomeDialogFragment {
            val fragment = WelcomeDialogFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(R.style.AppBottomSheetDialogTheme, R.style.AppTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_dialog_welcome, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(false)

        btnContinue.onClick {
            dismiss()
            listener?.onButtonContinueClicked()
            showMainDialog()
        }
    }

    private fun showMainDialog(){
        val mainDialogFragment = MainDialogFragment.newInstance()
        mainDialogFragment.show(
                childFragmentManager, mainDialogFragment.tag
        )
    }

    interface OnWelcomeDialogListener {
        fun onButtonContinueClicked()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setWhiteNavigationBar(dialog)
        }

        return dialog
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun setWhiteNavigationBar(@NonNull dialog: Dialog) {
        val window = dialog.window
        if (window != null) {
            val metrics = DisplayMetrics()
            window.windowManager.defaultDisplay.getMetrics(metrics)

            val dimDrawable = GradientDrawable()
            // ...customize your dim effect here

            val navigationBarDrawable = GradientDrawable()
            navigationBarDrawable.shape = GradientDrawable.RECTANGLE
            navigationBarDrawable.setColor(resources.getColor(R.color.colorTwilightBlue))

            val layers = arrayOf<Drawable>(dimDrawable, navigationBarDrawable)

            val windowBackground = LayerDrawable(layers)
            windowBackground.setLayerInsetTop(1, metrics.heightPixels)

            window.setBackgroundDrawable(windowBackground)
        }
    }
}
